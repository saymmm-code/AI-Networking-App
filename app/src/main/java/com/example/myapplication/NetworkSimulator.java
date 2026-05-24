package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import com.google.firebase.database.*;
import java.util.*;

public class NetworkSimulator {

    private static NetworkSimulator instance;
    private final DatabaseReference db;
    private final Handler handler;
    private final Random random;
    private boolean isRunning = false;

    private static final String[] DEVICE_NAMES = {
            "MacBook-Pro-Alex", "iPhone-Sarah", "Android-TV-LG",
            "SmartHub-Home", "Laptop-John", "iPad-Office",
            "RaspberryPi-Server", "Unknown-Device-7F2A"
    };
    private static final String[] DEVICE_TYPES = {
            "Laptop", "Mobile", "SmartTV", "IoT",
            "Laptop", "Tablet", "Server", "Unknown"
    };
    private static final String[] ALERT_TYPES = {
            "Unknown Device", "High Traffic", "Port Scan",
            "Brute Force", "DDoS Attempt", "Malware Detected",
            "Unauthorized Access"
    };
    private static final String[] SEVERITIES = {
            "Low", "Medium", "High", "Critical"
    };

    private NetworkSimulator() {
        db      = FirebaseDatabase.getInstance().getReference();
        handler = new Handler(Looper.getMainLooper());
        random  = new Random();
    }

    public static synchronized NetworkSimulator getInstance() {
        if (instance == null) instance = new NetworkSimulator();
        return instance;
    }

    public void startSimulation() {
        if (isRunning) return;
        isRunning = true;

        // Always seed devices on start
        seedDevices();

        // Start all simulations
        scheduleDeviceUpdates();
        scheduleTrafficUpdates();
        scheduleAlertGeneration();
    }

    public void stopSimulation() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    // ── Always seed 8 devices ─────────────────────────────────────────
    private void seedDevices() {
        for (int i = 0; i < 8; i++) {
            Map<String, Object> device = new HashMap<>();
            device.put("name",      DEVICE_NAMES[i]);
            device.put("type",      DEVICE_TYPES[i]);
            device.put("status",    i == 7 ? "Suspicious" :
                    i == 6 ? "Inactive" : "Active");
            device.put("ipAddress", "192.168.1." + (10 + i));
            device.put("bandwidth",
                    Math.round(random.nextDouble() * 50 * 10.0) / 10.0);
            device.put("lastSeen",  System.currentTimeMillis());
            db.child("devices").child("device_" + i).setValue(device);
        }

        // Also generate first alert immediately
        generateOneAlert();

        // And first traffic point
        generateOneTrafficPoint();
    }

    // ── Update device bandwidth every 5 seconds ───────────────────────
    private void scheduleDeviceUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;

                String[] statuses = {
                        "Active", "Active", "Active",
                        "Inactive", "Suspicious"
                };

                for (int i = 0; i < 8; i++) {
                    Map<String, Object> updates = new HashMap<>();

                    // Random bandwidth between 1 and 95 Mbps
                    double bw = 1 + Math.round(
                            random.nextDouble() * 94 * 10.0) / 10.0;
                    updates.put("bandwidth", bw);
                    updates.put("lastSeen", System.currentTimeMillis());

                    // 30% chance to change status
                    if (random.nextInt(10) < 3) {
                        updates.put("status",
                                statuses[random.nextInt(statuses.length)]);
                    }

                    db.child("devices").child("device_" + i)
                            .updateChildren(updates);
                }

                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    // ── Add traffic data point every 3 seconds ────────────────────────
    private void scheduleTrafficUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                generateOneTrafficPoint();
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void generateOneTrafficPoint() {
        String key = "t_" + System.currentTimeMillis();
        Map<String, Object> traffic = new HashMap<>();

        // Simulate realistic traffic spikes
        double baseDownload = 20 + random.nextDouble() * 80;
        double baseUpload   = 5  + random.nextDouble() * 40;

        traffic.put("download",  Math.round(baseDownload * 10.0) / 10.0);
        traffic.put("upload",    Math.round(baseUpload   * 10.0) / 10.0);
        traffic.put("timestamp", System.currentTimeMillis());

        db.child("traffic").child(key).setValue(traffic);

        // Keep only last 20 entries
        db.child("traffic").orderByChild("timestamp")
                .get().addOnSuccessListener(snap -> {
                    if (snap.getChildrenCount() > 20) {
                        long toDelete = snap.getChildrenCount() - 20;
                        long count = 0;
                        for (DataSnapshot child : snap.getChildren()) {
                            if (count < toDelete) {
                                child.getRef().removeValue();
                                count++;
                            }
                        }
                    }
                });
    }

    // ── Generate alerts every 8-15 seconds ────────────────────────────
    private void scheduleAlertGeneration() {
        // Generate 3 alerts immediately on start
        handler.postDelayed(() -> generateOneAlert(), 1000);
        handler.postDelayed(() -> generateOneAlert(), 2000);
        handler.postDelayed(() -> generateOneAlert(), 3000);

        // Then keep generating periodically
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                generateOneAlert();
                long nextDelay = (8 + random.nextInt(8)) * 1000L;
                handler.postDelayed(this, nextDelay);
            }
        }, 8000);
    }

    private void generateOneAlert() {
        String type     = ALERT_TYPES[random.nextInt(ALERT_TYPES.length)];
        String severity = SEVERITIES[random.nextInt(SEVERITIES.length)];
        String device   = DEVICE_NAMES[random.nextInt(DEVICE_NAMES.length)];
        String key      = "alert_" + System.currentTimeMillis();

        Map<String, Object> alert = new HashMap<>();
        alert.put("type",       type);
        alert.put("severity",   severity);
        alert.put("deviceName", device);
        alert.put("message",    buildMessage(type, device));
        alert.put("timestamp",  System.currentTimeMillis());

        db.child("alerts").child(key).setValue(alert);

        // Keep only last 20 alerts
        db.child("alerts").get().addOnSuccessListener(snap -> {
            if (snap.getChildrenCount() > 20) {
                long toDelete = snap.getChildrenCount() - 20;
                long count = 0;
                for (DataSnapshot child : snap.getChildren()) {
                    if (count < toDelete) {
                        child.getRef().removeValue();
                        count++;
                    }
                }
            }
        });
    }

    private String buildMessage(String type, String device) {
        switch (type) {
            case "Unknown Device":
                return "Unrecognized device '"
                        + device + "' attempting to join network.";
            case "High Traffic":
                return device
                        + " is generating unusually high bandwidth.";
            case "Port Scan":
                return "Port scanning activity detected from "
                        + device + ".";
            case "Brute Force":
                return "Multiple failed login attempts from "
                        + device + ".";
            case "DDoS Attempt":
                return "Possible DDoS activity originating from "
                        + device + ".";
            case "Unauthorized Access":
                return "Unauthorized access attempt detected from "
                        + device + ".";
            default:
                return "Malware signature detected on " + device + ".";
        }
    }
}