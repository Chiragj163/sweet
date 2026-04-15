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

    BottomNavigationView bottomNav;
    boolean forceHideBottomNav = false;

    public void hideBottomBar() {
        forceHideBottomNav = true;

        View bottom = findViewById(R.id.bottomNav);
        if (bottom != null) bottom.setVisibility(View.GONE);
    }

    public void showBottomBar() {
        forceHideBottomNav = false;

        View bottom = findViewById(R.id.bottomNav);
        if (bottom != null) bottom.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        View rootView = findViewById(android.R.id.content);

        loadFragment(new DashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_report) {
                loadFragment(new ReportFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                loadFragment(new DashboardFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_menu) {
                loadFragment(new MenuFragment());
                return true;
            }
            return false;
        });
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {

            Fragment current = getSupportFragmentManager()
                    .findFragmentById(R.id.frameContainer);

            if (current instanceof BillingFragment) {
                hideBottomBar();
            } else {
                showBottomBar();
            }
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            View bottom = findViewById(R.id.bottomNav);
            if (bottom == null) return;

            if (keypadHeight > screenHeight * 0.15) {
                bottom.setVisibility(View.GONE);
            } else {
                if (!forceHideBottomNav) {
                    bottom.setVisibility(View.VISIBLE);
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void loadFragment(Fragment fragment) {
        if (fragment instanceof BillingFragment) {
            hideBottomBar();
        } else {
            showBottomBar();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
    public void openBillDetail(Sale sale) {
        BillingFragment fragment = new BillingFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("sale", sale);
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}

