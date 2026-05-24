package com.example.myapplication;

public class TrafficData {
    private String id;
    private double download, upload;
    private long timestamp;

    public TrafficData() {}

    public TrafficData(String id, double download, double upload, long timestamp) {
        this.id = id;
        this.download = download;
        this.upload = upload;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getDownload() { return download; }
    public void setDownload(double d) { this.download = d; }
    public double getUpload() { return upload; }
    public void setUpload(double u) { this.upload = u; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long t) { this.timestamp = t; }
}