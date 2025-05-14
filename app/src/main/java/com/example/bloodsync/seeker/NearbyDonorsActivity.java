package com.example.bloodsync.seeker;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodsync.AwareActivity;
import com.example.bloodsync.R;
import com.example.bloodsync.adapter.DonorAdapter;
import com.example.bloodsync.model.User;
import com.example.bloodsync.util.DistanceCalculator;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NearbyDonorsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NearbyDonorsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1001;
    private static final float MAX_DISTANCE_KM = 50; // Maximum distance in kilometers

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private double currentLatitude = 28.2466243;  // Default: Delhi
    private double currentLongitude = 76.8143231;

    private RecyclerView donorRecyclerView;
    private TextView noDonorsText;
    public DonorAdapter donorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_donors);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        donorRecyclerView = findViewById(R.id.recycler_donors);
        noDonorsText = findViewById(R.id.no_donors_text);
        donorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorAdapter = new DonorAdapter();
        donorRecyclerView.setAdapter(donorAdapter);

        // Set up back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        updateMapLocation(currentLatitude, currentLongitude);
                        fetchNearbyDonors(currentLatitude, currentLongitude);

                        // Update user's location in Firebase
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            mDatabase.child("users").child(currentUser.getUid())
                                    .child("latitude").setValue(currentLatitude);
                            mDatabase.child("users").child(currentUser.getUid())
                                    .child("longitude").setValue(currentLongitude);
                        }

                        // Remove location updates after getting accurate position
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                        break;
                    }
                }
            }
        };

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Check location permission
        checkLocationPermission();

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Set up Aware tab
        LinearLayout awareTab = findViewById(R.id.aware_tab);
        awareTab.setOnClickListener(v -> {
            Intent intent = new Intent(NearbyDonorsActivity.this, AwareActivity.class);
            startActivity(intent);
        });

        // Set up other tabs if needed
        // LinearLayout homeTab = findViewById(R.id.home_tab);
        // LinearLayout profileTab = findViewById(R.id.profile_tab);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Request location updates with high accuracy
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // Location settings are satisfied, get location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());

            // Also try getting last location as a fallback
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            updateMapLocation(currentLatitude, currentLongitude);
                            fetchNearbyDonors(currentLatitude, currentLongitude);

                            // Update user's location in Firebase
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                mDatabase.child("users").child(currentUser.getUid())
                                        .child("latitude").setValue(currentLatitude);
                                mDatabase.child("users").child(currentUser.getUid())
                                        .child("longitude").setValue(currentLongitude);
                            }
                        }
                    });
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                try {
                    // Show the dialog by calling startResolutionForResult()
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(NearbyDonorsActivity.this, LOCATION_SETTINGS_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error
                    fetchNearbyDonors(currentLatitude, currentLongitude);
                }
            } else {
                // Use default coordinates
                fetchNearbyDonors(currentLatitude, currentLongitude);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation(currentLatitude, currentLongitude);
    }

    private void updateMapLocation(double latitude, double longitude) {
        if (mMap != null) {
            LatLng location = new LatLng(latitude, longitude);
            mMap.clear(); // Clear previous markers

            // Add current location marker
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        }
    }

    private void fetchNearbyDonors(double latitude, double longitude) {
        mDatabase.child("users").orderByChild("userType").equalTo("donor")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<User> donors = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);

                            if (user != null && user.getLatitude() != 0 && user.getLongitude() != 0) {
                                // Calculate distance
                                double distance = DistanceCalculator.calculateDistance(
                                        latitude, longitude,
                                        user.getLatitude(), user.getLongitude());

                                // Only include donors within MAX_DISTANCE_KM
                                if (distance <= MAX_DISTANCE_KM) {
                                    user.setUid(snapshot.getKey());
                                    donors.add(user);

                                    // Add marker for this donor
                                    if (mMap != null) {
                                        LatLng donorLocation = new LatLng(user.getLatitude(), user.getLongitude());
                                        mMap.addMarker(new MarkerOptions()
                                                .position(donorLocation)
                                                .title(user.getName())
                                                .snippet("Blood Group: " + user.getBloodGroup())
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    }
                                }
                            }
                        }

                        // Update UI with fetched donors
                        if (donors.isEmpty()) {
                            donorRecyclerView.setVisibility(View.GONE);
                            noDonorsText.setVisibility(View.VISIBLE);
                        } else {
                            donorAdapter.setDonors(donors, latitude, longitude);
                            donorRecyclerView.setVisibility(View.VISIBLE);
                            noDonorsText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(NearbyDonorsActivity.this,
                                "Error fetching donors: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Results may not be accurate.",
                        Toast.LENGTH_SHORT).show();
                fetchNearbyDonors(currentLatitude, currentLongitude);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // User enabled location services, try again
                getLastLocation();
            } else {
                // User declined, use default coordinates
                Toast.makeText(this, "Location services are required for better results",
                        Toast.LENGTH_SHORT).show();
                fetchNearbyDonors(currentLatitude, currentLongitude);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}