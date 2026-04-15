package com.example.sweet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    Context context;
    List<Item> list;
    DatabaseHelper db;

    public static final int PICK_IMAGE_EDIT = 200;
    public static String selectedImagePath = null; // 🔥 shared

    public ItemAdapter(Context context, List<Item> list) {
        this.context = context;
        this.list = list;
        db = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Item item = list.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(
                "₹" + item.getPrice() +
                        " / " + item.getUnit() +
                        " | GST: " + item.getGstRate() + "%"
        );
        holder.tvCategory.setText(item.getCategory());

        // ================= IMAGE =================
        String image = item.getImage();

        if (image != null && !image.trim().isEmpty()) {
            Glide.with(holder.imgItem.getContext())
                    .load(image)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgItem);
        } else {
            holder.imgItem.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // ================= DELETE =================
        holder.btnDelete.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Item currentItem = list.get(pos);

            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {

                        db.getWritableDatabase().delete(
                                "items",
                                "id=?",
                                new String[]{String.valueOf(currentItem.getId())}
                        );

                        list.remove(pos);
                        notifyItemRemoved(pos);

                        Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        // ================= EDIT =================
        holder.btnEdit.setOnClickListener(v -> {

            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_edit_item, null);

            EditText etName = dialogView.findViewById(R.id.etEditName);
            EditText etPrice = dialogView.findViewById(R.id.etEditPrice);
            Spinner spCategory = dialogView.findViewById(R.id.spEditCategory);
            ImageView imgPreview = dialogView.findViewById(R.id.imgEditItem);
            Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);

            // OLD IMAGE
            if (item.getImage() != null && !item.getImage().isEmpty()) {
                Glide.with(context).load(item.getImage()).into(imgPreview);
            }

            // Categories
            String[] categories = {"Mithai", "Snacks", "Soft Drink", "Ice Cream"};
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    categories
            );
            spCategory.setAdapter(catAdapter);

            etName.setText(item.getName());
            etPrice.setText(String.valueOf(item.getPrice()));

            int index = Arrays.asList(categories).indexOf(item.getCategory());
            if (index >= 0) spCategory.setSelection(index);

            // 🔥 IMAGE PICK BUTTON
            btnPickImage.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                ((Activity) context).startActivityForResult(intent, PICK_IMAGE_EDIT);
            });

            // Dialog
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Edit Item")
                    .setView(dialogView)
                    .setPositiveButton("Update", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

            Button btnUpdate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            btnUpdate.setOnClickListener(v2 -> {

                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Item currentItem = list.get(pos);

                String newName = etName.getText().toString();

                if (newName.isEmpty()) {
                    Toast.makeText(context, "Enter name", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newPrice;
                try {
                    newPrice = Double.parseDouble(etPrice.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(context, "Invalid price", Toast.LENGTH_SHORT).show();
                    return;
                }

                String newCategory = spCategory.getSelectedItem().toString();

                // 🔥 FINAL IMAGE (IMPORTANT)
                String finalImage = selectedImagePath != null
                        ? selectedImagePath
                        : currentItem.getImage();

                // UPDATE DB
                db.updateItem(
                        currentItem.getId(),
                        newName,
                        newPrice,
                        currentItem.getUnit(),
                        currentItem.getGstRate(),
                        newCategory,
                        finalImage
                );

                // UPDATE LIST
                list.set(pos, new Item(
                        currentItem.getId(),
                        newName,
                        newPrice,
                        currentItem.getUnit(),
                        currentItem.getGstRate(),
                        newCategory,
                        finalImage
                ));

                notifyItemChanged(pos);

                Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show();

                // RESET
                selectedImagePath = null;

                dialog.dismiss();
            });
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvCategory;
        ImageView imgItem;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            imgItem = itemView.findViewById(R.id.imgItem);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
    public void updateList(List<Item> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
}