package com.example.sweet;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build;
import android.content.Context;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.*;
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
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        bottomNav.setOnItemSelectedListener(item -> {

            if (vibrator != null && vibrator.hasVibrator()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // ✅ API 26+
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    );
                } else {
                    // ✅ Below API 26
                    vibrator.vibrate(50);
                }
            }

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
}

