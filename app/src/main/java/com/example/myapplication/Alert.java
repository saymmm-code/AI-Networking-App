package com.example.myapplication;

public class Alert {
    private String id, type, severity, deviceName, message;
    private long timestamp;

    public Alert() {}

    public Alert(String id, String type, String severity,
                 String deviceName, String message, long timestamp) {
        this.id         = id;
        this.type       = type;
        this.severity   = severity;
        this.deviceName = deviceName;
        this.message    = message;
        this.timestamp  = timestamp;
    }

    public String getId()         { return id; }
    public String getType()       { return type; }
    public String getSeverity()   { return severity; }
    public String getDeviceName() { return deviceName; }
    public String getMessage()    { return message; }
    public long   getTimestamp()  { return timestamp; }

    public void setId(String id)                 { this.id = id; }
    public void setType(String type)             { this.type = type; }
    public void setSeverity(String severity)     { this.severity = severity; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public void setMessage(String message)       { this.message = message; }
    public void setTimestamp(long timestamp)     { this.timestamp = timestamp; }
}