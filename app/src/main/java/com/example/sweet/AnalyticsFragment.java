package com.example.sweet;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsFragment extends Fragment {

    TextView tvTotal, tvTodayCard, tvMonthCard, tvYearCard;
    Spinner spCategory;
    PieChart pieItems, pieCategory;
    Button btnFilter;

    String startDate, endDate;
    DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        db = new DatabaseHelper(getContext());

        // UI Bind
        tvTotal = view.findViewById(R.id.tvTotal);
        tvTodayCard = view.findViewById(R.id.tvTodayCard);
        tvMonthCard = view.findViewById(R.id.tvMonthCard);
        tvYearCard = view.findViewById(R.id.tvYearCard);
        spCategory = view.findViewById(R.id.spCategory);
        btnFilter = view.findViewById(R.id.btnFilter);
        pieItems = view.findViewById(R.id.pieItems);
        pieCategory = view.findViewById(R.id.pieCategory);

        // Default = Today
        startDate = "2000-01-01";
        endDate = getDate(0);

        // 🔥 Spinner Setup FIRST
        String[] categories = {"All", "Sweet", "Snacks", "Soft Drink", "Ice Cream"};

        spCategory.setAdapter(new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        ));

        // 🔥 Initial Load (SAFE)
        loadItemChart("All");
        loadCategoryChart();
        loadTotal();
        loadCards();

        // 🔥 Spinner Change
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selected = parent.getItemAtPosition(position).toString();
                loadItemChart(selected);
                loadTotal(); // update total also
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🔥 Filter Button
        btnFilter.setOnClickListener(v -> {

            PopupMenu menu = new PopupMenu(getContext(), btnFilter);

            menu.getMenu().add("Today");
            menu.getMenu().add("This Week");
            menu.getMenu().add("This Month");
            menu.getMenu().add("This Year");
            menu.getMenu().add("Custom");

            menu.setOnMenuItemClickListener(item -> {

                switch (item.getTitle().toString()) {

                    case "Today":
                        startDate = endDate = getDate(0);
                        break;

                    case "This Week":
                        startDate = getDate(7);
                        endDate = getDate(0);
                        break;

                    case "This Month":
                        startDate = getDate(30);
                        endDate = getDate(0);
                        break;

                    case "This Year":
                        startDate = getDate(365);
                        endDate = getDate(0);
                        break;

                    case "Custom":
                        showDateRangePicker();
                        return true;
                }

                String selected = spCategory.getSelectedItem() != null
                        ? spCategory.getSelectedItem().toString()
                        : "All";

                loadItemChart(selected);
                loadCategoryChart();
                loadTotal();
                loadCards();

                return true;
            });

            menu.show();
        });

        return view;
    }

    // ================= DATE PICKER =================

    private void showDateRangePicker() {

        Calendar cal = Calendar.getInstance();

        DatePickerDialog startDialog = new DatePickerDialog(getContext(),
                (view, y, m, d) -> {

                    startDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", y, m + 1, d);

                    DatePickerDialog endDialog = new DatePickerDialog(getContext(),
                            (view2, y2, m2, d2) -> {

                                endDate = String.format(Locale.getDefault(),
                                        "%04d-%02d-%02d", y2, m2 + 1, d2);

                                String selected = spCategory.getSelectedItem() != null
                                        ? spCategory.getSelectedItem().toString()
                                        : "All";

                                loadItemChart(selected);
                                loadCategoryChart();
                                loadTotal();
                                loadCards();

                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                    );

                    endDialog.show();

                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        startDialog.show();
    }

    private String getDate(int daysAgo) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis() - daysAgo * 86400000L));
    }

    // ================= CHARTS =================

    private void loadItemChart(String category) {

        List<PieEntry> itemData =
                db.getItemWiseSalesFiltered(startDate, endDate, category);

        PieDataSet set = new PieDataSet(itemData, "Items");
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(set);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹" + String.format("%.0f", value);
            }
        });

        pieItems.setUsePercentValues(false);
        pieItems.setData(data);
        pieItems.setCenterTextColor(getResources().getColor(android.R.color.darker_gray));
        Legend legend = pieItems.getLegend();
        TypedValue tv = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface, tv, true
        );

        legend.setTextColor(tv.data);
        pieItems.setCenterText("Revenue");
        pieItems.animateY(800);
        pieItems.invalidate();
    }

    private void loadCategoryChart() {

        List<PieEntry> catData =
                db.getCategoryWiseSales(startDate, endDate);

        PieDataSet set = new PieDataSet(catData, "Categories");
        set.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(set);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹" + String.format("%.0f", value);
            }
        });
        Legend legend = pieCategory.getLegend();
        TypedValue tv = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface, tv, true
        );
        legend.setTextColor(tv.data);
        pieCategory.setUsePercentValues(false);
        pieCategory.setCenterTextColor(getResources().getColor(android.R.color.darker_gray));
        pieCategory.setData(data);
        pieCategory.setCenterText("Categories");
        pieCategory.animateY(800);
        pieCategory.invalidate();
    }

    // ================= TOTAL =================

    private void loadTotal() {
        double total = db.getSalesBetweenDatesTotal(startDate, endDate);
        tvTotal.setText("₹" + String.format(Locale.getDefault(), "%.2f", total));
    }

    // ================= CARDS =================

    private void loadCards() {

        double today = db.getTodaySales();
        double month = db.getMonthSales();
        double year = db.getYearSales();

        tvTodayCard.setText("Today\n₹" + String.format("%.0f", today));
        tvMonthCard.setText("Month\n₹" + String.format("%.0f", month));
        tvYearCard.setText("Year\n₹" + String.format("%.0f", year));
    }
}