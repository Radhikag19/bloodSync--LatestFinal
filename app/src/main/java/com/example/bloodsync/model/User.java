package com.example.bloodsync.model;

import com.google.firebase.database.PropertyName;

public class User {
    private String uid;
    private String name;
    private String email;
    private String bloodGroup;
    private String userType;
    private double latitude;
    private double longitude;
    private String location;
    private Object age; // Changed from int to Object to handle both String and Integer
    private String profileImageUrl;

    // Required for Firebase
    public User() {
    }

    public User(String uid, String name, String email, String bloodGroup, String userType) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.userType = userType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Modified getter to handle different types
    public int getAge() {
        if (age instanceof String) {
            try {
                return Integer.parseInt((String) age);
            } catch (NumberFormatException e) {
                return 0; // Default value if parsing fails
            }
        } else if (age instanceof Integer) {
            return (Integer) age;
        } else if (age instanceof Long) {
            return ((Long) age).intValue();
        }
        return 0; // Default value for other cases
    }

    public void setAge(Object age) {
        this.age = age;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}