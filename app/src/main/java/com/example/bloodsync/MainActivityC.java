package com.example.bloodsync;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodsync.adapter.BloodBankAdapter;
import com.example.bloodsync.data.FetchBloodBanksTask;
import com.example.bloodsync.model.BloodBank;
import com.example.bloodsync.model.BloodBankData;
import com.example.bloodsync.profile.ProfileActivity;
import com.example.bloodsync.seeker.NearbyDonorsActivity;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MainActivityC extends AppCompatActivity implements OnMapReadyCallback, FetchBloodBanksTask.BloodBankListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1001;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private double currentLatitude = 28.2466243;  // Default: Delhi
    private double currentLongitude = 76.8143231;

    private RecyclerView bloodBankRecyclerView;
    private BloodBankAdapter bloodBankAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MapView
        mapView = findViewById(R.id.map_view);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

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
                        fetchBloodBanks(currentLatitude, currentLongitude);

                        // Remove location updates after getting accurate position
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                        break;
                    }
                }
            }
        };

        // Initialize RecyclerView
        bloodBankRecyclerView = findViewById(R.id.recycler_blood_banks);
        bloodBankRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bloodBankAdapter = new BloodBankAdapter();
        bloodBankRecyclerView.setAdapter(bloodBankAdapter);

        // Click listener for 'See All'
        TextView seeAllText = findViewById(R.id.see_all_text);
        if (seeAllText != null) {
            seeAllText.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivityC.this, SeekerHomeActivity.class);
                startActivity(intent);
            });
        }

        // Set up bottom navigation bar click listeners
        setupBottomNavigation();

        // Check permission and get location
        checkLocationPermission();
    }

    private void setupBottomNavigation() {
        // Find the navigation items
        LinearLayout awareTab = findViewById(R.id.awareTab);
        LinearLayout searchTab = findViewById(R.id.searchTab);
        LinearLayout homeTab = findViewById(R.id.homeTab);
        LinearLayout profileTab = findViewById(R.id.profileTab);

        // Set click listeners for navigation
        if (awareTab != null) {
            awareTab.setOnClickListener(v -> {
                Toast.makeText(MainActivityC.this, "Opening Aware Tab", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivityC.this, AwareActivity.class);
                startActivity(intent);
            });
        }

        // Search tab click listener - connects to NearbyDonorsActivity
        if (searchTab != null) {
            searchTab.setOnClickListener(v -> {
                Toast.makeText(MainActivityC.this, "Opening Nearby Donors", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivityC.this, NearbyDonorsActivity.class);
                startActivity(intent);
            });
        }

        // Home tab is current screen - no need for action
        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                // Already on home screen
                Toast.makeText(MainActivityC.this, "Already on Home", Toast.LENGTH_SHORT).show();
            });
        }

        // Profile tab click listener
        if (profileTab != null) {
            profileTab.setOnClickListener(v -> {
                Toast.makeText(MainActivityC.this, "Opening Profile", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent(MainActivityC.this, ProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivityC.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                fetchBloodBanks(currentLatitude, currentLongitude);
            }
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
                            fetchBloodBanks(currentLatitude, currentLongitude);
                        }
                    });
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                try {
                    // Show the dialog by calling startResolutionForResult()
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivityC.this, LOCATION_SETTINGS_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error
                    fetchBloodBanks(currentLatitude, currentLongitude);
                }
            } else {
                // Use default coordinates
                fetchBloodBanks(currentLatitude, currentLongitude);
            }
        });
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
                Toast.makeText(this, "Location services are required for better results", Toast.LENGTH_SHORT).show();
                fetchBloodBanks(currentLatitude, currentLongitude);
            }
        }
    }

    private void fetchBloodBanks(double latitude, double longitude) {
        new FetchBloodBanksTask(this).execute(latitude, longitude);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Set default location
        updateMapLocation(currentLatitude, currentLongitude);

        // Load initial hardcoded markers
        addHardcodedBloodBankMarkers();
    }

    private void updateMapLocation(double latitude, double longitude) {
        if (googleMap != null) {
            LatLng location = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
            googleMap.clear(); // Clear previous markers
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    private void addHardcodedBloodBankMarkers() {
        List<BloodBank> bloodBanks = BloodBankData.getHardcodedBloodBanks();
        addBloodBankMarkers(bloodBanks);
        if (bloodBankAdapter != null) {
            bloodBankAdapter.setBloodBanks(bloodBanks);
        }
    }

    private void addBloodBankMarkers(List<BloodBank> bloodBanks) {
        if (googleMap != null) {
            googleMap.clear();
            LatLng userLocation = new LatLng(currentLatitude, currentLongitude);
            googleMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            for (BloodBank bloodBank : bloodBanks) {
                LatLng position = new LatLng(bloodBank.getLatitude(), bloodBank.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(bloodBank.getName())
                        .snippet("Distance: " + bloodBank.getDistance() + " km")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }

    @Override
    public void onBloodBanksFetched(List<BloodBank> bloodBanks) {
        if (bloodBankAdapter != null) {
            bloodBankAdapter.setBloodBanks(bloodBanks);
        }
        addBloodBankMarkers(bloodBanks);
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
        onBloodBanksFetched(BloodBankData.getHardcodedBloodBanks());
    }

    // Stop location updates when activity is no longer visible
    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        mapView.onPause();
    }

    // MapView lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}