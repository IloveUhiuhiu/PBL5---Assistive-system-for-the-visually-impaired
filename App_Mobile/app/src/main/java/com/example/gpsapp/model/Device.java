package com.example.gpsapp.model;

public class Device {
    private String deviceId;
    private String name;
    private double latitude;
    private double longitude;
    private long timestamp;

    public Device() { }

    public Device(String name, double latitude, double longitude, long timestamp) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
