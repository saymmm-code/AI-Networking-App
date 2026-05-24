package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.myapplication.R;
import com.example.myapplication.Alert;
import com.example.myapplication.Device;

import java.util.*;

public class RecommendationsFragment extends Fragment {

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        View view = inf.inflate(R.layout.fragment_recommendations, parent, false);
        container = view.findViewById(R.id.recommendationsContainer);
        analyze();
        return view;
    }

    private void analyze() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("devices").get().addOnSuccessListener(devSnap -> {
            List<Device> devices = devSnap.toObjects(Device.class);

            db.collection("alerts").get().addOnSuccessListener(alertSnap -> {
                List<Alert> alerts = alertSnap.toObjects(Alert.class);

                container.removeAllViews();
                List<Recommendation> recs = buildRecommendations(devices, alerts);
                for (Recommendation r : recs) addCard(r);
            });
        });
    }

    private List<Recommendation> buildRecommendations(List<Device> devices, List<Alert> alerts) {
        List<Recommendation> recs = new ArrayList<>();
        int suspicious = 0, inactive = 0;
        double maxBw = 0;
        String highBwDevice = "";

        for (Device d : devices) {
            if ("Suspicious".equals(d.getStatus())) suspicious++;
            if ("Inactive".equals(d.getStatus())) inactive++;
            if (d.getBandwidth() > maxBw) {
                maxBw = d.getBandwidth();
                highBwDevice = d.getName();
            }
        }

        long criticalAlerts = alerts.stream()
                .filter(a -> "Critical".equals(a.getSeverity())).count();

        // Rule 1
        if (suspicious > 0) {
            recs.add(new Recommendation("🚨 Suspicious Devices Detected",
                    suspicious + " device(s) flagged as suspicious. Investigate immediately and block unknown MACs.",
                    "Critical", "#FF4757"));
        }

        // Rule 2
        if (criticalAlerts >= 3) {
            recs.add(new Recommendation("⚠ Multiple Critical Threats",
                    criticalAlerts + " critical alerts detected. Enable network-level firewall rules and log analysis.",
                    "High", "#FF6348"));
        }

        // Rule 3
        if (maxBw > 60) {
            recs.add(new Recommendation("📊 High Bandwidth Usage",
                    highBwDevice + " is consuming " + String.format("%.1f", maxBw)
                            + " Mbps. Consider QoS throttling.",
                    "Medium", "#FFA500"));
        }

        // Rule 4
        if (inactive > 2) {
            recs.add(new Recommendation("💤 Inactive Devices",
                    inactive + " devices are offline. Remove from network to reduce attack surface.",
                    "Low", "#7A8BA0"));
        }

        // Rule 5 — always good practice
        recs.add(new Recommendation("🔒 Security Best Practices",
                "Ensure all devices use WPA3 encryption. Rotate network passwords every 90 days.",
                "Info", "#00D4FF"));

        return recs;
    }

    private void addCard(Recommendation r) {
        CardView card = new CardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(Color.parseColor("#111827"));
        card.setRadius(24f);

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(40, 32, 40, 32);

        // Severity badge row
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView title = new TextView(requireContext());
        title.setText(r.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(title);

        TextView badge = new TextView(requireContext());
        badge.setText(r.severity);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(10);
        badge.setBackgroundColor(Color.parseColor(r.color));
        badge.setPadding(16, 6, 16, 6);
        row.addView(badge);

        inner.addView(row);

        TextView msg = new TextView(requireContext());
        msg.setText(r.message);
        msg.setTextColor(Color.parseColor("#A0AEC0"));
        msg.setTextSize(13);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgParams.setMargins(0, 12, 0, 0);
        msg.setLayoutParams(msgParams);
        inner.addView(msg);

        card.addView(inner);
        container.addView(card);
    }

    private static class Recommendation {
        String title, message, severity, color;
        Recommendation(String t, String m, String s, String c) {
            title = t; message = m; severity = s; color = c;
        }
    }
}