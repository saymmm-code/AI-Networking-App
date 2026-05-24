package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Collections;
import java.util.List;

public class AlertsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlertAdapter adapter;
    private DatabaseReference db;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_alerts, container, false);

        db = FirebaseDatabase.getInstance().getReference();
        recyclerView = view.findViewById(R.id.alertsRecycler);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        adapter = new AlertAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Alert> alerts = new ArrayList<>();

                for (DataSnapshot a : snapshot.getChildren()) {
                    Alert alert = new Alert(
                            a.getKey(),
                            a.child("type").getValue(String.class),
                            a.child("severity").getValue(String.class),
                            a.child("deviceName").getValue(String.class),
                            a.child("message").getValue(String.class),
                            a.child("timestamp").getValue(Long.class) != null
                                    ? a.child("timestamp").getValue(Long.class)
                                    : 0L
                    );
                    alerts.add(alert);
                }

                Collections.reverse(alerts);
                adapter.updateAlerts(alerts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        db.child("alerts")
                .orderByChild("timestamp")
                .addValueEventListener(listener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && listener != null)
            db.child("alerts").removeEventListener(listener);
    }
}