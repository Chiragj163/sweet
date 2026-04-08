package com.example.sweet;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.ArrayAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.ArrayList;
public class ReportFragment extends Fragment {

    ListView listReport;
    DatabaseHelper db;
    ArrayAdapter<String> adapter;
    List<String> reportList = new ArrayList<>();

    @Nullable

    TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Sales by Item"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));

        loadFragment(new SalesByItemFragment());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadFragment(new SalesByItemFragment());
                } else {
                    loadFragment(new SalesHistoryFragment());
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.reportContainer, fragment)
                .commit();
    }

    private void loadReports() {

        reportList.clear();

        List<Sale> sales = db.getAllSales();

        for (Sale s : sales) {
            String item =
                    "₹" + s.getAmount() +
                            "\n" + s.getSummary() +
                            "\nDate: " + s.getDate();
            reportList.add(item);
        }
    }

}