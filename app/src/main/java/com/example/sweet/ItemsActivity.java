package com.example.sweet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
    Spinner spCategory;
    Spinner spFilterCategory;
    RecyclerView recyclerView;
    Spinner spGst;

    private static final int PICK_IMAGE = 101;
    Uri imageUri;
    Button btnPickImage;
    ImageView imgPreview;

    String selectedImagePath = null;
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
        spGst = findViewById(R.id.spGst);
        spCategory = findViewById(R.id.spCategory);
        btnPickImage = findViewById(R.id.btnPickImage);
        imgPreview = findViewById(R.id.imgPreview);

        db = new DatabaseHelper(this);
        spFilterCategory = findViewById(R.id.spFilterCategory);
        itemList = db.getAllItems();
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
        // Spinner values
        String[] units = {"KG", "Piece"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                units
        );
        spUnit.setAdapter(spinnerAdapter);

        String[] categories = {"Sweet", "Snacks", "Soft Drink", "Ice Cream"};

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

        spCategory.setAdapter(catAdapter);
        String[] filterCategories = {"All", "Sweet", "Snacks", "Soft Drink", "Ice Cream"};

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                filterCategories
        );

        spFilterCategory.setAdapter(filterAdapter);

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(v -> {

            String name = etName.getText().toString();
            String priceStr = etPrice.getText().toString();
            String unit = spUnit.getSelectedItem().toString();
            String gstStr = spGst.getSelectedItem().toString();
            String category = spCategory.getSelectedItem().toString();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            double gst = Double.parseDouble(gstStr);

            // ✅ IMPORTANT FIX
            String image = selectedImagePath != null ? selectedImagePath : null;
            db.insertItem(name, price, unit, gst, category, image != null ? image : "");

            Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show();

            // Reset
            etName.setText("");
            etPrice.setText("");
            spGst.setSelection(0);
            imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            selectedImagePath = null;

            loadItems();
        });
        String[] gstRates = {"0", "5", "18", "40"};

        ArrayAdapter<String> gstAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                gstRates
        );
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES
            }, 101);
        } else {
            requestPermissions(new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, 101);
        }
        btnPickImage.setOnClickListener(v2 -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            startActivityForResult(intent, PICK_IMAGE);
        });
        spFilterCategory.setSelection(0); // "All"
        spFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selected = parent.getItemAtPosition(position).toString();

                if (selected.equals("All")) {
                    itemList = db.getAllItems();
                } else {
                    itemList = db.getItemsByCategory(selected);
                }

                // ✅ Update adapter
                itemAdapter.updateList(itemList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spGst.setAdapter(gstAdapter);
        loadItems();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();
            selectedImagePath = uri.toString();

            imgPreview.setImageURI(uri); // show preview
        }
        if (requestCode == ItemAdapter.PICK_IMAGE_EDIT &&
                resultCode == RESULT_OK &&
                data != null) {

            Uri uri = data.getData();
            ItemAdapter.selectedImagePath = uri.toString();
        }
    }
    private void loadItems() {
        itemList = db.getAllItems();
        itemAdapter.updateList(itemList);
    }

}