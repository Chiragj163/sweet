package com.example.sweet;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    List<String> list;
    OnCategoryClick listener;
    int selectedPosition = 0;

    public interface OnCategoryClick {
        void onClick(String category);
    }

    public CategoryAdapter(List<String> list, OnCategoryClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String category = list.get(position);
        holder.tvCategory.setText(category);

        // 🔥 SET IMAGE
        switch (category) {
            case "All":
                holder.imgCategory.setImageResource(R.drawable.all);
                break;
            case "Sweet":
                holder.imgCategory.setImageResource(R.drawable.mithai);
                break;
            case "Snacks":
                holder.imgCategory.setImageResource(R.drawable.snacks);
                break;
            case "Soft Drink":
                holder.imgCategory.setImageResource(R.drawable.drink);
                break;
            case "Ice Cream":
                holder.imgCategory.setImageResource(R.drawable.icecream);
                break;
            default:
                holder.imgCategory.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 🔥 SELECTION UI
        if (position == selectedPosition) {
            holder.cardCategory.setStrokeColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_500)
            );
            holder.cardCategory.setCardElevation(6f);
        } else {
            holder.cardCategory.setStrokeColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent)
            );
            holder.cardCategory.setCardElevation(2f);
        }

        // Inside onBindViewHolder, update the Click listener
        holder.cardCategory.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION || currentPos == selectedPosition) return;

            int prev = selectedPosition;
            selectedPosition = currentPos;

            // Refresh both old and new selection
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);

            // Provide the category to the fragment
            listener.onClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCategory;
        TextView tvCategory;
        MaterialCardView cardCategory; // 🔥 IMPORTANT

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            cardCategory = itemView.findViewById(R.id.cardCategory);
        }
    }
}