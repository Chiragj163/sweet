package com.example.sweet;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class MainActivity extends AppCompatActivity {

    CardView cardCreateBill, cardItems;
    FloatingActionButton fabAdd;
    TextView tvTotalBills, tvAvgBill, tvTopItem;
    DatabaseHelper db;
    BottomNavigationView bottomNav;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().hide();
//        }
//
//
//        db = new DatabaseHelper(this);
//
//        double todaySales = db.getTodaySales();
//
//        TextView tvSales = findViewById(R.id.tvSales);
//        tvSales.setText("₹" + String.format("%.2f", todaySales));
//
//        cardCreateBill = findViewById(R.id.cardCreateBill);
//        cardItems = findViewById(R.id.cardItems);
//        fabAdd = findViewById(R.id.fabAdd);
//        tvTotalBills = findViewById(R.id.tvTotalBills);
//         tvAvgBill = findViewById(R.id.tvAvgBill);
//         tvTopItem = findViewById(R.id.tvTopItem);
//
//        tvTotalBills.setText("Bills: " + db.getTotalBills());
//        tvAvgBill.setText("Avg: ₹" + String.format("%.2f", db.getAverageBill()));
//        tvTopItem.setText("Top Bill: " + db.getTopItem());
//
//        cardItems.setOnClickListener(v -> {
//            startActivity(new Intent(this, ItemsActivity.class));
//        });
//
//        cardCreateBill.setOnClickListener(v -> {
//             startActivity(new Intent(this, BillingActivity.class));
//        });
//
//        fabAdd.setOnClickListener(v ->
//                Toast.makeText(this, "Add Item", Toast.LENGTH_SHORT).show());
//        BarChart barChart = findViewById(R.id.barChart);
//
//        List<Float> salesData = db.getMonthlySales();
//        List<BarEntry> entries = new ArrayList<>();
//
//
//        for (int i = 0; i < salesData.size(); i++) {
//            entries.add(new BarEntry(i, salesData.get(i)));
//        }
//
//
//        BarDataSet dataSet = new BarDataSet(entries, "Monthly Sales");
//        dataSet.setDrawValues(false);
//        dataSet.setValueTextSize(10f);
//
//        BarData barData = new BarData(dataSet);
//        barChart.setData(barData);
//
//
//        List<String> days = new ArrayList<>();
//        for (int i = 1; i <= salesData.size(); i++) {
//            days.add(String.valueOf(i));
//        }
//
//        XAxis xAxis = barChart.getXAxis();
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//
//
//        barChart.getAxisRight().setEnabled(false);
//        barChart.getDescription().setEnabled(false);
//        barChart.getLegend().setEnabled(false);
//
//        barChart.animateY(1000);
//        barChart.invalidate(); // 🔥 IMPORTANT
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        View rootView = findViewById(android.R.id.content);
        loadFragment(new DashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_bill) {
                loadFragment(new BillingFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_report) {
                loadFragment(new ReportFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                loadFragment(new DashboardFragment());
                return true;
            }
                

            return false;
        });
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();

            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open
                findViewById(R.id.bottomNav).setVisibility(View.GONE);
            } else {
                // Keyboard is closed
                findViewById(R.id.bottomNav).setVisibility(View.VISIBLE);
            }
        });
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}

