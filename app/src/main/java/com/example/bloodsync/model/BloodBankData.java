package com.example.bloodsync.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BloodBankData {
    private static final int PROXIMITY_RADIUS = 50000; // 50 km
    private static final String TAG = "BloodBankData";

    // Method to get the API URL for nearby blood banks
    public static String getBloodBanksUrl(double latitude, double longitude) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlacesUrl.append("&keyword=blood+bank");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyBNZcjdsdd420mLrnn1_sWpYUJotnVNM_0"); // Replace with actual API key
        Log.d(TAG, "URL: " + googlePlacesUrl.toString());
        return googlePlacesUrl.toString();
    }

    // Download JSON data from URL
    public static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        HttpURLConnection urlConnection = null;
        InputStream iStream = null;

        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d(TAG, "Exception downloading URL: " + e.toString());
        } finally {
            if (iStream != null) iStream.close();
            if (urlConnection != null) urlConnection.disconnect();
        }
        return data;
    }

    // Parse JSON response and return list of blood banks
    public static List<BloodBank> parseBloodBanks(String jsonData, double userLat, double userLng) {
        List<BloodBank> bloodBanks = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject place = jsonArray.getJSONObject(i);

                BloodBank bloodBank = new BloodBank();
                bloodBank.setId("BB" + (1000 + i));
                bloodBank.setName(place.getString("name"));

                String vicinity = place.has("vicinity") ? place.getString("vicinity") : "";
                bloodBank.setAddress(vicinity);

                JSONObject geometry = place.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                bloodBank.setLatitude(lat);
                bloodBank.setLongitude(lng);

                // Calculate actual distance from user
                double distance = calculateDistance(userLat, userLng, lat, lng);
                bloodBank.setDistance(distance);

                bloodBank.setContactNumber("N/A"); // Most places API responses don’t include phone numbers

                bloodBanks.add(bloodBank);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            return getHardcodedBloodBanks();
        }

        return bloodBanks.isEmpty() ? getHardcodedBloodBanks() : bloodBanks;
    }

    // Haversine formula to calculate distance between two lat/lng points in km
    private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 10.0) / 10.0; // Rounded to 1 decimal place
    }

    // Hardcoded fallback blood banks (optional backup)
    public static List<BloodBank> getHardcodedBloodBanks() {
        List<BloodBank> bloodBanks = new ArrayList<>();

        BloodBank bb1 = new BloodBank();
        bb1.setId("BB001");
        bb1.setName("Ginor Devi Trust");
        bb1.setAddress("123 Main Street, Noida");
        bb1.setLatitude(28.5355);
        bb1.setLongitude(77.3910);
        bb1.setContactNumber("+91-9876543210");
        bb1.setDistance(5.0);
        bloodBanks.add(bb1);

        BloodBank bb2 = new BloodBank();
        bb2.setId("BB002");
        bb2.setName("Medical College Blood Center");
        bb2.setAddress("45 Hospital Road, Delhi");
        bb2.setLatitude(28.6356);
        bb2.setLongitude(77.2248);
        bb2.setContactNumber("+91-9876543211");
        bb2.setDistance(2.5);
        bloodBanks.add(bb2);

        BloodBank bb3 = new BloodBank();
        bb3.setId("BB003");
        bb3.setName("Red Cross Blood Bank");
        bb3.setAddress("78 Civil Lines, Delhi");
        bb3.setLatitude(28.6508);
        bb3.setLongitude(77.2343);
        bb3.setContactNumber("+91-9876543212");
        bb3.setDistance(3.1);
        bloodBanks.add(bb3);

        BloodBank bb4 = new BloodBank();
        bb4.setId("BB004");
        bb4.setName("Central Blood Bank");
        bb4.setAddress("15 Ring Road, Delhi");
        bb4.setLatitude(28.6129);
        bb4.setLongitude(77.2295);
        bb4.setContactNumber("+91-9876543213");
        bb4.setDistance(1.8);
        bloodBanks.add(bb4);

        BloodBank bb5 = new BloodBank();
        bb5.setId("BB005");
        bb5.setName("Life Saver Blood Bank");
        bb5.setAddress("56 Sector 18, Noida");
        bb5.setLatitude(28.5709);
        bb5.setLongitude(77.3251);
        bb5.setContactNumber("+91-9876543214");
        bb5.setDistance(4.2);
        bloodBanks.add(bb5);

        return bloodBanks;
    }
}
// bloodbankdata