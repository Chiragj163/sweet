package com.example.sweet;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.card.MaterialCardView;

public class ReportFragment extends Fragment {

    MaterialCardView cardSalesByItem;
    MaterialCardView cardHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        cardSalesByItem = view.findViewById(R.id.cardSalesByItem);
        cardHistory = view.findViewById(R.id.cardHistory);

        // Click Listeners for the Menu Cards
        cardSalesByItem.setOnClickListener(v -> navigateTo(new SalesByItemFragment()));

        cardHistory.setOnClickListener(v -> navigateTo(new SalesHistoryFragment()));

        return view;
    }

    // Handles the actual screen transition
    private void navigateTo(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                // Assuming R.id.frameContainer is your main activity's fragment container
                // based on your previous SalesHistoryFragment code!
                .replace(R.id.frameContainer, fragment)
                .addToBackStack(null) // Allows the user to hit "Back" to return to the menu
                .commit();
    }
}