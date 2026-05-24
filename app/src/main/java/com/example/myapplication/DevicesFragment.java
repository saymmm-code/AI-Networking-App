package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private DatabaseReference db;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_devices, container, false);

        db = FirebaseDatabase.getInstance().getReference();
        recyclerView = view.findViewById(R.id.devicesRecycler);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        adapter = new DeviceAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Device> devices = new ArrayList<>();

                for (DataSnapshot d : snapshot.getChildren()) {
                    String name =
                            d.child("name").getValue(String.class);
                    String type =
                            d.child("type").getValue(String.class);
                    String status =
                            d.child("status").getValue(String.class);
                    String ip =
                            d.child("ipAddress").getValue(String.class);
                    Double bw =
                            d.child("bandwidth").getValue(Double.class);
                    Long lastSeen =
                            d.child("lastSeen").getValue(Long.class);

                    Device dev = new Device(
                            d.getKey(),
                            name    != null ? name    : "Unknown",
                            type    != null ? type    : "Unknown",
                            status  != null ? status  : "Inactive",
                            ip      != null ? ip      : "0.0.0.0",
                            bw      != null ? bw      : 0.0,
                            lastSeen!= null ? lastSeen: 0L
                    );
                    devices.add(dev);
                }

                // Sort: Active → Suspicious → Inactive
                devices.sort((a, b) -> {
                    int rA = "Active".equals(a.getStatus())     ? 0 :
                            "Suspicious".equals(a.getStatus()) ? 1 : 2;
                    int rB = "Active".equals(b.getStatus())     ? 0 :
                            "Suspicious".equals(b.getStatus()) ? 1 : 2;
                    return Integer.compare(rA, rB);
                });

                adapter.updateDevices(devices);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        db.child("devices").addValueEventListener(listener);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && listener != null)
            db.child("devices").removeEventListener(listener);
    }
}