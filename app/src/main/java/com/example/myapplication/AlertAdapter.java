package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertAdapter extends
        RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<Alert> alerts;
    private final Context ctx;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US);

    public AlertAdapter(Context ctx, List<Alert> alerts) {
        this.ctx    = ctx;
        this.alerts = alerts;
    }

    public void updateAlerts(List<Alert> newAlerts) {
        this.alerts = newAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Alert a = alerts.get(pos);

        h.type.setText(a.getType()       != null ? a.getType()       : "Unknown");
        h.message.setText(a.getMessage() != null ? a.getMessage()    : "");
        h.severity.setText(a.getSeverity()!= null ? a.getSeverity()  : "Low");
        h.time.setText(sdf.format(new Date(a.getTimestamp())));

        int color;
        String sev = a.getSeverity() != null ? a.getSeverity() : "";
        switch (sev) {
            case "Critical": color = Color.parseColor("#FF4757"); break;
            case "High":     color = Color.parseColor("#FF6348"); break;
            case "Medium":   color = Color.parseColor("#FFA500"); break;
            default:         color = Color.parseColor("#2ECC71"); break;
        }

        h.severityBar.setBackgroundColor(color);
        h.severity.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return alerts == null ? 0 : alerts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView type, message, severity, time;
        View severityBar;

        ViewHolder(View v) {
            super(v);
            type        = v.findViewById(R.id.alertType);
            message     = v.findViewById(R.id.alertMessage);
            severity    = v.findViewById(R.id.alertSeverity);
            time        = v.findViewById(R.id.alertTime);
            severityBar = v.findViewById(R.id.severityBar);
        }
    }
}