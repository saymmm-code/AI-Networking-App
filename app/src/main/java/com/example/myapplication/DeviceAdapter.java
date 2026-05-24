package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class DeviceAdapter extends
        RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<Device> devices;
    private final Context context;

    public DeviceAdapter(Context ctx, List<Device> devices) {
        this.context = ctx;
        this.devices = devices;
    }

    public void updateDevices(List<Device> newDevices) {
        this.devices = newDevices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Device d = devices.get(pos);

        h.name.setText(d.getName());
        h.type.setText(d.getType());
        h.ip.setText(d.getIpAddress());
        h.bandwidth.setText(
                String.format(Locale.US, "%.1f Mbps", d.getBandwidth()));
        h.status.setText(d.getStatus());

        switch (d.getStatus()) {
            case "Active":
                h.statusDot.setBackgroundResource(
                        R.drawable.circle_green);
                h.status.setTextColor(0xFF2ECC71);
                break;
            case "Suspicious":
                h.statusDot.setBackgroundResource(
                        R.drawable.circle_yellow);
                h.status.setTextColor(0xFFFFA500);
                break;
            default:
                h.statusDot.setBackgroundResource(
                        R.drawable.circle_red);
                h.status.setTextColor(0xFFFF4757);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return devices == null ? 0 : devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, ip, bandwidth, status;
        View statusDot;

        ViewHolder(View v) {
            super(v);
            name      = v.findViewById(R.id.deviceName);
            type      = v.findViewById(R.id.deviceType);
            ip        = v.findViewById(R.id.deviceIp);
            bandwidth = v.findViewById(R.id.deviceBandwidth);
            status    = v.findViewById(R.id.deviceStatus);
            statusDot = v.findViewById(R.id.statusDot);
        }
    }
}