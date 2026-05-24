package com.example.myapplication;

public class Device {
    private String id, name, type, status, ipAddress;
    private double bandwidth;
    private long lastSeen;

    public Device() {}

    public Device(String id, String name, String type,
                  String status, String ipAddress,
                  double bandwidth, long lastSeen) {
        this.id        = id;
        this.name      = name;
        this.type      = type;
        this.status    = status;
        this.ipAddress = ipAddress;
        this.bandwidth = bandwidth;
        this.lastSeen  = lastSeen;
    }

    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getType()      { return type; }
    public String getStatus()    { return status; }
    public String getIpAddress() { return ipAddress; }
    public double getBandwidth() { return bandwidth; }
    public long   getLastSeen()  { return lastSeen; }

    public void setId(String id)               { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setType(String type)           { this.type = type; }
    public void setStatus(String status)       { this.status = status; }
    public void setIpAddress(String ip)        { this.ipAddress = ip; }
    public void setBandwidth(double bandwidth) { this.bandwidth = bandwidth; }
    public void setLastSeen(long lastSeen)     { this.lastSeen = lastSeen; }
}