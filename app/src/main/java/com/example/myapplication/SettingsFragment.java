package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inf.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext());

        Switch notifications = view.findViewById(R.id.notificationsSwitch);
        Switch darkMode      = view.findViewById(R.id.darkModeSwitch);
        Switch autoRefresh   = view.findViewById(R.id.autoRefreshSwitch);

        notifications.setChecked(prefs.getBoolean("notifications", true));
        darkMode.setChecked(prefs.getBoolean("darkMode", true));
        autoRefresh.setChecked(prefs.getBoolean("autoRefresh", true));

        notifications.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("notifications", checked).apply());

        darkMode.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("darkMode", checked).apply());

        autoRefresh.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("autoRefresh", checked).apply();
            if (checked) NetworkSimulator.getInstance().startSimulation();
            else NetworkSimulator.getInstance().stopSimulation();
        });

        return view;
    }
}