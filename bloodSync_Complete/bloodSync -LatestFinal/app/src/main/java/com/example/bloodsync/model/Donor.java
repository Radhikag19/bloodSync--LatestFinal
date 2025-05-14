package com.example.bloodsync.model;

public class Donor {
    private String id;
    private String name;
    private String location;
    private double distance; // in km
    private int age;
    private String bloodGroup;
    private String profileImageUrl;
    private double latitude;
    private double longitude;

    public Donor() {
        // Empty constructor needed for Firebase
    }

    public Donor(String id, String name, String location, double distance, int age, String bloodGroup) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.distance = distance;
        this.age = age;
        this.bloodGroup = bloodGroup;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
}