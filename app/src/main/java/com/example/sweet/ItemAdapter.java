package com.example.sweet;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    Context context;
    List<Item> list;
    DatabaseHelper db;

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
        holder.tvPrice.setText("₹" + item.getPrice() + " / " + item.getUnit());

        holder.btnDelete.setOnClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) return;

            Item currentItem = list.get(currentPosition);

            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (d, w) -> {

                        db.getWritableDatabase().execSQL(
                                "DELETE FROM items WHERE id=" + currentItem.getId()
                        );

                        list.remove(currentPosition);
                        notifyItemRemoved(currentPosition);

                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.btnEdit.setOnClickListener(v -> {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dialog_edit_item, null);

            EditText etName = view.findViewById(R.id.etEditName);
            EditText etPrice = view.findViewById(R.id.etEditPrice);
            Spinner spUnit = view.findViewById(R.id.spEditUnit);

            // Spinner setup
            String[] units = {"KG", "Piece"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    units
            );
            spUnit.setAdapter(adapter);

            // Set old values
            etName.setText(item.getName());
            etPrice.setText(String.valueOf(item.getPrice()));

            if (item.getUnit().equals("KG")) {
                spUnit.setSelection(0);
            } else {
                spUnit.setSelection(1);
            }

            new AlertDialog.Builder(context)
                    .setTitle("Edit Item")
                    .setView(view)
                    .setPositiveButton("Update", (dialog, which) -> {

                        String newName = etName.getText().toString();
                        double newPrice = Double.parseDouble(etPrice.getText().toString());
                        String newUnit = spUnit.getSelectedItem().toString();

                        db.updateItem(item.getId(), newName, newPrice, newUnit);

                        list.set(position, new Item(item.getId(), newName, newPrice, newUnit));
                        notifyDataSetChanged();

                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}