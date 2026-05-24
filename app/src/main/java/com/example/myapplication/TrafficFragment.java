package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.*;
import com.example.myapplication.R;
import java.util.*;

public class TrafficFragment extends Fragment {

    private LineChart lineChart;
    private TextView downloadSpeed, uploadSpeed;
    private DatabaseReference db;

    private final List<Entry> downloadEntries = new ArrayList<>();
    private final List<Entry> uploadEntries   = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inf.inflate(R.layout.fragment_traffic, container, false);

        lineChart     = view.findViewById(R.id.lineChart);
        downloadSpeed = view.findViewById(R.id.downloadSpeed);
        uploadSpeed   = view.findViewById(R.id.uploadSpeed);
        db            = FirebaseDatabase.getInstance().getReference();

        setupChart();
        listenToTraffic();
        return view;
    }

    private void setupChart() {
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#7A8BA0"));
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return ""; }
        });

        YAxis left = lineChart.getAxisLeft();
        left.setTextColor(Color.parseColor("#7A8BA0"));
        left.setGridColor(Color.parseColor("#1E293B"));
        left.setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);
    }

    private void listenToTraffic() {
        db.child("traffic").orderByChild("timestamp")
                .limitToLast(20)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        downloadEntries.clear();
                        uploadEntries.clear();

                        int i = 0;
                        double lastDl = 0, lastUl = 0;

                        for (DataSnapshot t : snapshot.getChildren()) {
                            Double dl = t.child("download").getValue(Double.class);
                            Double ul = t.child("upload").getValue(Double.class);
                            if (dl == null) dl = 0.0;
                            if (ul == null) ul = 0.0;
                            downloadEntries.add(new Entry(i, dl.floatValue()));
                            uploadEntries.add(new Entry(i, ul.floatValue()));
                            lastDl = dl;
                            lastUl = ul;
                            i++;
                        }

                        downloadSpeed.setText(
                                String.format(Locale.US, "%.1f Mbps", lastDl));
                        uploadSpeed.setText(
                                String.format(Locale.US, "%.1f Mbps", lastUl));
                        updateChart();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void updateChart() {
        LineDataSet dlSet = new LineDataSet(downloadEntries, "Download");
        dlSet.setColor(Color.parseColor("#2ECC71"));
        dlSet.setCircleColor(Color.parseColor("#2ECC71"));
        dlSet.setLineWidth(2f);
        dlSet.setCircleRadius(3f);
        dlSet.setDrawValues(false);
        dlSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dlSet.setDrawFilled(true);
        dlSet.setFillColor(Color.parseColor("#2ECC71"));
        dlSet.setFillAlpha(30);

        LineDataSet ulSet = new LineDataSet(uploadEntries, "Upload");
        ulSet.setColor(Color.parseColor("#00D4FF"));
        ulSet.setCircleColor(Color.parseColor("#00D4FF"));
        ulSet.setLineWidth(2f);
        ulSet.setCircleRadius(3f);
        ulSet.setDrawValues(false);
        ulSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ulSet.setDrawFilled(true);
        ulSet.setFillColor(Color.parseColor("#00D4FF"));
        ulSet.setFillAlpha(30);

        lineChart.setData(new LineData(dlSet, ulSet));
        lineChart.invalidate();
    }
}