package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView activeDevicesCount, totalBandwidth,
            alertsCount, suspiciousDevicesTv,
            userEmailTv, logoutBtn;
    private ListView recentAlertsList;
    private DatabaseReference db;
    private ValueEventListener devicesListener, alertsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_dashboard, container, false);

        db = FirebaseDatabase.getInstance().getReference();

        activeDevicesCount  = view.findViewById(R.id.activeDevicesCount);
        totalBandwidth      = view.findViewById(R.id.totalBandwidth);
        alertsCount         = view.findViewById(R.id.alertsCount);
        suspiciousDevicesTv = view.findViewById(R.id.suspiciousDevicesTv);
        userEmailTv         = view.findViewById(R.id.userEmailTv);
        recentAlertsList    = view.findViewById(R.id.recentAlertsList);
        logoutBtn           = view.findViewById(R.id.logoutBtn);

        // Safety check — make sure views exist
        if (activeDevicesCount == null) {
            Toast.makeText(getContext(),
                    "Layout error: check fragment_dashboard.xml",
                    Toast.LENGTH_LONG).show();
            return view;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null)
            userEmailTv.setText(auth.getCurrentUser().getEmail());

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        listenToDevices();
        listenToAlerts();
        return view;
    }

    private void listenToDevices() {
        devicesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int active = 0;
                double totalBw = 0;
                StringBuilder suspicious = new StringBuilder();

                for (DataSnapshot d : snapshot.getChildren()) {
                    String status = d.child("status").getValue(String.class);
                    Double bw = d.child("bandwidth").getValue(Double.class);
                    String name = d.child("name").getValue(String.class);
                    String ip = d.child("ipAddress").getValue(String.class);

                    // FIX: .equalsIgnoreCase makes sure it works whether you typed "Active" or "active"
                    if (status != null && "Active".equalsIgnoreCase(status.trim())) {
                        active++;
                    }

                    if (bw != null) {
                        totalBw += bw;
                    }

                    // FIX: Handles "Suspicious" safely regardless of text casing
                    if (status != null && "Suspicious".equalsIgnoreCase(status.trim())) {
                        suspicious.append("⚠ ")
                                .append(name != null ? name : "Unknown Device")
                                .append(" — ")
                                .append(ip != null ? ip : "No IP")
                                .append("\n");
                    }
                }

                activeDevicesCount.setText(String.valueOf(active));
                totalBandwidth.setText(String.format(Locale.US, "%.1f", totalBw));
                suspiciousDevicesTv.setText(
                        suspicious.length() > 0
                                ? suspicious.toString()
                                : "No suspicious devices detected.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "DB Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        db.child("devices").addValueEventListener(devicesListener);
    }

    private void listenToAlerts() {
        alertsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alertsCount.setText(String.valueOf(snapshot.getChildrenCount()));

                List<String> items = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);

                for (DataSnapshot a : snapshot.getChildren()) {
                    String severity = a.child("severity").getValue(String.class);
                    String type = a.child("type").getValue(String.class);
                    String device = a.child("deviceName").getValue(String.class);
                    Long ts = a.child("timestamp").getValue(Long.class);

                    // FIX: Safe handling of severity labels
                    String prefix = "🟢";
                    if (severity != null) {
                        if ("Critical".equalsIgnoreCase(severity.trim())) prefix = "🔴";
                        else if ("High".equalsIgnoreCase(severity.trim())) prefix = "🟠";
                        else if ("Medium".equalsIgnoreCase(severity.trim())) prefix = "🟡";
                    }

                    items.add(prefix + " ["
                            + sdf.format(new Date(ts != null ? ts : System.currentTimeMillis()))
                            + "] " + (type != null ? type : "Unknown Event")
                            + " — " + (device != null ? device : "Unknown Device"));
                }

                Collections.reverse(items);

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(
                                requireContext(),
                                android.R.layout.simple_list_item_1,
                                items) {
                            @Override
                            public View getView(int pos, View cv, ViewGroup p) {
                                TextView tv = (TextView) super.getView(pos, cv, p);
                                tv.setTextColor(0xFFFFFFFF); // Clean white text
                                tv.setBackgroundColor(0x00000000); // Transparent background
                                tv.setTextSize(12);
                                return tv;
                            }
                        };

                recentAlertsList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        // Reads the latest 5 alerts
        db.child("alerts")
                .orderByChild("timestamp")
                .limitToLast(5)
                .addValueEventListener(alertsListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            if (devicesListener != null) db.child("devices").removeEventListener(devicesListener);
            if (alertsListener != null) db.child("alerts").removeEventListener(alertsListener);
        }
    }
}