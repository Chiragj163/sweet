package com.example.sweet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = new DatabaseHelper(requireContext());

        TextView tvSales = view.findViewById(R.id.tvSales);
        TextView tvTotalBills = view.findViewById(R.id.tvTotalBills);
        TextView tvAvgBill = view.findViewById(R.id.tvAvgBill);
        TextView tvTopItem = view.findViewById(R.id.tvTopItem);
//        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
        if (nav.getSelectedItemId() != R.id.nav_home) {
            nav.setSelectedItemId(R.id.nav_home);
        }
        animateValue(tvSales, db.getTodaySales());
        tvTotalBills.setText("Bills: " + db.getTotalBills());
        animateValue(tvAvgBill, db.getAverageBill());
        tvTopItem.setText("Top: " + db.getTopItem());


        CardView cardCreateBill = view.findViewById(R.id.cardCreateBill);
        CardView cardItems = view.findViewById(R.id.cardItems);

        cardItems.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ItemsActivity.class)));
        cardCreateBill.setOnClickListener(v -> {
            v.animate().rotationYBy(360).setDuration(400)
                    .withEndAction(() -> {
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frameContainer, new BillingFragment())
                                .addToBackStack(null)
                                .commit();
                    });
        });
//        fabAdd.setOnClickListener(v -> {
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.frameContainer, new BillingFragment())
//                    .commit();
//        });
        BarChart barChart = view.findViewById(R.id.barChart);

        List<Float> salesData = db.getMonthlySales();
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < salesData.size(); i++) {
            if (salesData.get(i) > 0) {
                entries.add(new BarEntry(i, salesData.get(i)));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Sales");

        dataSet.setColor(getResources().getColor(R.color.purple_500));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.black));
        dataSet.setValueTextSize(12f);
       // dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        if (entries.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("No sales data yet 📊");
        } else {
            barChart.setData(barData);
        }

// X-Axis labels
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= entries.size(); i++) {
            days.add(String.valueOf(i));
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(Math.min(5, days.size()), false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setTextColor(getResources().getColor(android.R.color.darker_gray));

// Styling
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        barChart.animateY(1000);
        barChart.invalidate();
        return view;
    }
    private void animateValue(TextView tv, double value) {
        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofFloat(0, (float) value);

        animator.setDuration(1000);

        animator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            tv.setText("₹" + String.format("%.2f", val));
        });

        animator.start();
    }
    private void animateClick(View v) {
        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100);
            }
            return false;
        });
    }
}