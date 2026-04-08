package com.example.sweet;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemsActivity extends AppCompatActivity {

    EditText etName, etPrice;
    Spinner spUnit;
    Button btnAdd;
    DatabaseHelper db;

    RecyclerView recyclerView;
    ItemAdapter itemAdapter;   // renamed
    List<Item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        spUnit = findViewById(R.id.spUnit);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);

        db = new DatabaseHelper(this);

        // Spinner values
        String[] units = {"KG", "Piece"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                units
        );
        spUnit.setAdapter(spinnerAdapter);

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(v -> {

            String name = etName.getText().toString();
            String priceStr = etPrice.getText().toString();
            String unit = spUnit.getSelectedItem().toString();


            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);

            db.insertItem(name, price, unit);

            Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show();

            etName.setText("");
            etPrice.setText("");

            loadItems(); // refresh list
        });

        loadItems();
    }

    private void loadItems() {
        itemList = db.getAllItems();
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
    }
}