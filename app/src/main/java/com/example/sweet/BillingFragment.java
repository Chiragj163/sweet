package com.example.sweet;

import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BillingFragment extends Fragment {

    private static final UUID PRINTER_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Spinner spItems , spPaymentMode;
    EditText etQty, etDiscount, etPhone;
    Button btnAddToBill, btnGeneratePDF;
    Button btnConnectPrinter;
    TextView tvTotal, tvFinal;
    RecyclerView recyclerBill;
    BillRecyclerAdapter adapter;
    DatabaseHelper db;
    Spinner spFilterCategory;
    List<Item> itemList;
    List<CartItem> cartList;
    double total = 0;
    double finalAmount = 0;
    private long currentBillNo = 0;
    private String currentBillId = "";
    BluetoothSocket btsocket;
    OutputStream outputStream;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_billing, container, false);

        initViews(view);
        loadItems();
        setupListeners();
        recyclerBill = view.findViewById(R.id.recyclerBill);
        cartList = CartManager.getInstance().getCart();
        adapter = new BillRecyclerAdapter(cartList, () -> {
            updateTotalFromAdapter();
        });
        btnConnectPrinter = view.findViewById(R.id.btnConnectPrinter);
        btnConnectPrinter.setOnClickListener(v -> connectPrinter());
        recyclerBill.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBill.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        recyclerBill.setAdapter(adapter);
        spFilterCategory = view.findViewById(R.id.spFilterCategory);

        // ✅ Swipe delete
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView rv,
                                          RecyclerView.ViewHolder vh,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                        int pos = vh.getAdapterPosition();

                        CartItem deletedItem = cartList.get(pos);
                        cartList.remove(pos);
                        adapter.notifyItemRemoved(pos);

                        updateTotalFromAdapter();

                        showUndoSnackbar(deletedItem, pos);
                    }

                    // 🔥 DRAW RED BACKGROUND
                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {

                        View itemView = viewHolder.itemView;

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);

                        if (dX > 0) {
                            // swipe right
                            c.drawRect(itemView.getLeft(), itemView.getTop(),
                                    itemView.getLeft() + dX, itemView.getBottom(), paint);
                        } else {
                            // swipe left
                            c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                                    itemView.getRight(), itemView.getBottom(), paint);
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                                actionState, isCurrentlyActive);
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_delete);

                        float iconMargin = (itemView.getHeight() - icon.getHeight()) / 2;

                        if (dX > 0) {
                            c.drawBitmap(icon,
                                    itemView.getLeft() + 20,
                                    itemView.getTop() + iconMargin,
                                    null);
                        } else {
                            c.drawBitmap(icon,
                                    itemView.getRight() - icon.getWidth() - 20,
                                    itemView.getTop() + iconMargin,
                                    null);
                        }
                    }
                });

        SharedPreferences prefs = requireContext().getSharedPreferences("printer", 0);
        String mac = prefs.getString("mac", null);

        if (mac != null && BluetoothAdapter.getDefaultAdapter() != null) {
            try {
                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
                connectToDevice(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String[] categories = {"All", "Sweet", "Snacks", "Soft Drink", "Ice Cream"};

        spFilterCategory.setAdapter(new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        ));
        spFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selected = parent.getItemAtPosition(position).toString();

                itemList = db.getItemsByCategory(selected);

                List<String> names = new ArrayList<>();

                for (Item i : itemList) {
                    names.add(i.getName() + " (₹" + i.getPrice() + ")");
                }

                spItems.setAdapter(new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                ));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        helper.attachToRecyclerView(recyclerBill);


        String[] paymentModes = {"Cash", "Online"};

        spPaymentMode.setAdapter(new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                paymentModes
        ));
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Sale sale = (Sale) getArguments().getSerializable("sale");

            if (sale != null) {
                cartList.clear();

                for (SaleItemRow item : sale.items) {
                    cartList.add(new CartItem(
                            0,
                            item.name,
                            item.qty,
                            item.price,
                            item.gstRate
                    ));
                }

                currentBillNo = sale.getBillNo();
                currentBillId = new SimpleDateFormat("ddMMyy", Locale.getDefault())
                        .format(new Date()) + "-" + String.format("%03d", currentBillNo);

                adapter.notifyDataSetChanged();
                // ✅ VIEW MODE
                btnAddToBill.setEnabled(false);
                etQty.setEnabled(false);
                spItems.setEnabled(false);
                spFilterCategory.setEnabled(false);
                etDiscount.setEnabled(false);

// Optional: hide buttons
                btnGeneratePDF.setText("Reprint");
                updateTotalFromAdapter();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomBar();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateTotalFromAdapter();
        }
    }
    // ================= INIT =================

    private void initViews(View view) {
        spItems = view.findViewById(R.id.spItems);
        etQty = view.findViewById(R.id.etQty);
        etDiscount = view.findViewById(R.id.etDiscount);
        etPhone = view.findViewById(R.id.etPhone);
        btnAddToBill = view.findViewById(R.id.btnAddToBill);
        btnGeneratePDF = view.findViewById(R.id.btnGeneratePDF);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvFinal = view.findViewById(R.id.tvFinal);
        spPaymentMode = view.findViewById(R.id.spPaymentMode);
        db = new DatabaseHelper(getContext());
    }



    private void setupListeners() {

        btnAddToBill.setOnClickListener(v -> addItemToCart());
        etDiscount.addTextChangedListener(new SimpleTextWatcher(this::calculateFinal));

        btnGeneratePDF.setOnClickListener(v -> generateBill());
    }

    // ================= BILL LOGIC =================

    private void addItemToCart() {

        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(getContext(), "No items available", Toast.LENGTH_SHORT).show();
            return;
        }

        String qtyStr = etQty.getText().toString();

        if (qtyStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spItems.getSelectedItemPosition();

        if (pos < 0 || pos >= itemList.size()) {
            Toast.makeText(getContext(), "Invalid selection", Toast.LENGTH_SHORT).show();
            return;
        }

        Item item = itemList.get(pos);
        double qty = Double.parseDouble(qtyStr);

        boolean found = false;

        for (int i = 0; i < cartList.size(); i++) {
            CartItem c = cartList.get(i);

            if (c.getId() == item.getId()) {
                c.setQty(c.getQty() + qty);
                adapter.notifyItemChanged(i);
                found = true;
                break;
            }
        }

        if (!found) {
            CartItem cartItem = new CartItem(
                    item.getId(),
                    item.getName(),
                    qty,
                    item.getPrice(),
                    item.getGstRate()
            );

            CartManager.getInstance().addToCart(cartItem);
            adapter.notifyItemInserted(cartList.size() - 1);
        }

        updateTotalFromAdapter();
        etQty.setText("");
    }
    private void updateTotalUI() {
        tvTotal.setText(String.format("Total: ₹%.2f", total));
        calculateFinal();
    }

    private void calculateFinal() {

        double totalGST = 0;
        double totalBase = 0;
        double totalSGST = 0;
        double totalCGST = 0;

        for (CartItem item : cartList) {

            double rate = item.getGstRate(); // 5 / 18 / 40
            double totalPrice = item.getTotal(); // already includes GST

            double gst = (totalPrice * rate) / (100 + rate);
            double sgst = gst / 2;
            double cgst = gst / 2;
            double base = totalPrice - gst;

            totalGST += gst;
            totalSGST += sgst;
            totalCGST += cgst;
            totalBase += base;
        }
        double discount = 0;
        try {
            discount = Double.parseDouble(etDiscount.getText().toString());
        } catch (Exception ignored) {}

        finalAmount = Math.max(0, total - discount);  // already includes GST

        tvFinal.setText(String.format(
                "Subtotal: ₹%.2f\nSGST: ₹%.2f\nCGST: ₹%.2f\n%sFinal: ₹%.2f",
                totalBase,
                totalSGST,
                totalCGST,
                discount > 0 ? String.format("Discount: ₹%.0f\n", discount) : "",
                finalAmount
        ));
    }

    // ================= SAVE + SHARE =================

    private void generateBill() {
        String paymentMode = "Cash"; // default

        if (spPaymentMode != null && spPaymentMode.getSelectedItem() != null) {
            paymentMode = spPaymentMode.getSelectedItem().toString();
        }
        double totalSGST = 0;
        double totalCGST = 0;

        if (cartList.isEmpty()) {
            Toast.makeText(getContext(), "No items in cart", Toast.LENGTH_SHORT).show();
            return;
        }

        if (total == 0) {
            Toast.makeText(getContext(), "Calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }
        double totalGST = 0;
        double totalBase = 0;


        for (CartItem item : cartList) {
            double rate = item.getGstRate();
            double totalPrice = item.getTotal();

            double gst = (totalPrice * rate) / (100 + rate);
            double sgst = gst / 2;
            double cgst = gst / 2;
            double base = totalPrice - gst;

            totalGST += gst;
            totalSGST += sgst;
            totalCGST += cgst;
            totalBase += base;
        }
        double discount = 0;
        try {
            discount = Double.parseDouble(etDiscount.getText().toString());
        } catch (Exception ignored) {}


        finalAmount = Math.max(0, total - discount);


            currentBillNo = db.saveSale(totalBase, totalGST, discount, finalAmount, paymentMode, cartList);


        currentBillId = new SimpleDateFormat("ddMMyy", Locale.getDefault())
                .format(new Date()) + "-" + String.format("%03d", currentBillNo);

        // 🔥 USE BILL NUMBER (optional but powerful)


        // 🔥 OUTPUT
        String phone = etPhone.getText().toString().trim();

        boolean success = false;
        if (isPrinterConnected()) {
            printThermalBill();
        } else if (phone.length() == 10) {
            sendToWhatsApp(phone, paymentMode);
        } else {
            generatePDF();
        }

            CartManager.getInstance().clearCart();
            Toast.makeText(getContext(), "Bill Saved: #" + currentBillId, Toast.LENGTH_SHORT).show();
            resetBill();

    }

    private void resetBill() {
        cartList.clear();
        adapter.notifyDataSetChanged();

        total = 0;
        finalAmount = 0;

        tvTotal.setText("Total: ₹0");
        tvFinal.setText("Final Amount: ₹0");
    }

    // ================= WHATSAPP =================

    private void sendToWhatsApp(String number ,String paymentMode) {

        double totalSGST = 0, totalCGST = 0, totalGST = 0, totalBase = 0;

        StringBuilder msg = new StringBuilder();

        double discount = 0;
        try {
            discount = Double.parseDouble(etDiscount.getText().toString());
        } catch (Exception ignored) {}


        // 🔥 START MONOSPACE BLOCK
        msg.append("```").append("\n");

        // HEADER
        msg.append("        🧾 SWEET SHOP\n");
        msg.append("       Kolkata, India\n\n");

        String date = new java.text.SimpleDateFormat(
                "dd MMM yyyy | hh:mm a",
                java.util.Locale.getDefault()
        ).format(new Date());

        msg.append("Date: ").append(date).append("\n");
        msg.append("--------------------------------\n");
        msg.append("Payment: ").append(paymentMode).append("\n");
        msg.append("Bill No: ").append(currentBillId).append("\n");
        msg.append("--------------------------------\n");

        // COLUMN HEADER
        msg.append(String.format("%-10s %3s %6s %6s\n", "Item", "Qty", "Price", "Amt"));
        msg.append("--------------------------------\n");

        // ITEMS
        for (CartItem item : cartList) {

            String name = item.getName().length() > 10
                    ? item.getName().substring(0, 10)
                    : item.getName();

            msg.append(String.format(
                    "%-10s %3.0f %6.0f %6.0f\n",
                    name,
                    item.getQty(),
                    item.getPrice(),
                    item.getTotal()
            ));

            // GST CALCULATION
            double rate = item.getGstRate();
            double totalPrice = item.getTotal();

            double gst = (totalPrice * rate) / (100 + rate);
            double sgst = gst / 2;
            double cgst = gst / 2;
            double base = totalPrice - gst;

            totalGST += gst;
            totalSGST += sgst;
            totalCGST += cgst;
            totalBase += base;
        }

        msg.append("--------------------------------\n");

        // TOTALS
        msg.append(String.format("%-20s ₹%6.2f\n", "Subtotal:", totalBase));
        msg.append(String.format("%-20s ₹%6.2f\n", "SGST:", totalSGST));
        msg.append(String.format("%-20s ₹%6.2f\n", "CGST:", totalCGST));

        if (discount > 0) {
            msg.append(String.format("%-20s ₹%6.2f\n", "Discount:", discount));
        }

        msg.append("--------------------------------\n");

        double finalAmount = totalBase + totalGST - discount;

        msg.append(String.format("%-20s ₹%6.2f\n", "TOTAL:", finalAmount));
        msg.append("--------------------------------\n");

        // FOOTER
        msg.append("        Thank You 🙏\n");

        // 🔥 END MONOSPACE
        msg.append("```");

        String url = "https://wa.me/91" + number + "?text=" + Uri.encode(msg.toString());
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
    // ================= PDF =================

    private void generatePDF() {
        String paymentMode = spPaymentMode.getSelectedItem().toString();
        int pageWidth = 380; // 🔥 58mm paper
        int startY = 40;
        int lineHeight = 25;
        double totalSGST = 0;
        double totalCGST = 0;
        int itemCount = cartList.size();
        int pageHeight = startY + (itemCount * lineHeight) + 400;
        double discount = 0;
        try {
            discount = Double.parseDouble(etDiscount.getText().toString());
        } catch (Exception ignored) {}
        double totalGST = 0;
        double totalBase = 0;


        PdfDocument pdf = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // 🔥 Paints
        Paint centerPaint = new Paint();
        centerPaint.setTextSize(18);
        centerPaint.setFakeBoldText(true);
        centerPaint.setTextAlign(Paint.Align.CENTER);

        Paint normalPaint2=new Paint();
        normalPaint2.setTextSize(14);
        normalPaint2.setTextAlign(Paint.Align.CENTER);

        Paint normalPaint = new Paint();
        normalPaint.setTextSize(14);

        Paint boldPaint = new Paint();
        boldPaint.setTextSize(14);
        boldPaint.setFakeBoldText(true);

        Paint totalBoldPaint = new Paint();
        totalBoldPaint.setTextSize(20);
        totalBoldPaint.setFakeBoldText(true);
        totalBoldPaint.setTextAlign(Paint.Align.CENTER);

        int y = startY;

        // ================= HEADER =================
        canvas.drawText("SWEET SHOP", pageWidth / 2f, y, centerPaint);
        y += lineHeight;

        canvas.drawText("Kolkata, India", pageWidth / 2f, y, normalPaint2);
        y += lineHeight;

        String formattedDate = new java.text.SimpleDateFormat("'on' dd-MM-yyyy 'at' HH:mm",java.util.Locale.getDefault()).format(new Date());
        normalPaint2.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(formattedDate, pageWidth / 2f, y, normalPaint2);
        y += lineHeight;

        canvas.drawText("==================================================", 10, y, normalPaint);
        y += lineHeight;
        canvas.drawText("Bill No: " + currentBillId, pageWidth / 2f, y, centerPaint);
        canvas.drawText("Payment: " + paymentMode, 10, y, normalPaint);
        y += lineHeight;
        canvas.drawText("==================================================", 10, y, normalPaint);
        y += lineHeight;
        // ================= COLUMN HEADER =================
        int xItem = 10;
        int xQty = 175;
        int xPrice = 240;
        int xAmt = 325;

        canvas.drawText("Item", xItem, y, boldPaint);
        canvas.drawText("Qty", xQty, y, boldPaint);
        canvas.drawText("Price", xPrice, y, boldPaint);
        canvas.drawText("Amt", xAmt, y, boldPaint);

        y += lineHeight;

        canvas.drawText("==================================================", 10, y, normalPaint);
        y += lineHeight;

        // ================= ITEMS =================
        for (CartItem item : cartList) {

            String name = item.getName().length() > 10
                    ? item.getName().substring(0, 10)
                    : item.getName();

            canvas.drawText(name, xItem, y, normalPaint);
            canvas.drawText(String.valueOf((int) item.getQty()), xQty, y, normalPaint);
            canvas.drawText(String.valueOf((int) item.getPrice()), xPrice, y, normalPaint);
            canvas.drawText(String.valueOf((int) item.getTotal()), xAmt, y, normalPaint);

            y += lineHeight;
        }

        // ================= LINE =================
        canvas.drawText("==================================================", 10, y, normalPaint);
        y += lineHeight;

        // ================= TOTALS =================


        for (CartItem item : cartList) {
            double rate = item.getGstRate();
            double totalPrice = item.getTotal();

            double gst = (totalPrice * rate) / (100 + rate);
            double sgst = gst / 2;
            double cgst = gst / 2;
            double base = totalPrice - gst;

            totalGST += gst;
            totalSGST += sgst;
            totalCGST += cgst;
            totalBase += base;
        }

        canvas.drawText("Subtotal: ₹" + String.format("%.2f", totalBase), 10, y, normalPaint);
        y += lineHeight;

        canvas.drawText("SGST: ₹" + String.format("%.2f", totalSGST), 10, y, normalPaint);
        y += lineHeight;

        canvas.drawText("CGST: ₹" + String.format("%.2f", totalCGST), 10, y, normalPaint);
        y += lineHeight;

        if(discount>0) {
            canvas.drawText("Discount: ₹" + String.format("%.2f", discount), 10, y, normalPaint);
            y += lineHeight;
        }

        canvas.drawText("==================================================", 10, y, normalPaint);
        y += lineHeight;

        canvas.drawText("TOTAL: ₹" + String.format("%.2f", finalAmount),pageWidth/2f, y, totalBoldPaint);
        y += lineHeight + 10;

        canvas.drawText("Thank You! 🙏", pageWidth / 2f, y, normalPaint2);



        pdf.finishPage(page);

        File file = new File(requireContext().getExternalFilesDir(null), "bill_pos.pdf");

        try {
            pdf.writeTo(new FileOutputStream(file));
            sharePDF(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pdf.close();
    }
    private void sharePDF(File file) {
        Uri uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share Bill"));
    }
    //=================== print ==================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                connectPrinter(); // retry after permission
            } else {
                Toast.makeText(getContext(), "Bluetooth permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean hasBluetoothPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return requireContext().checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    private void requestBluetoothPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                    101
            );
        }
    }
    @SuppressLint("MissingPermission")
    private void connectPrinter() {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            Toast.makeText(getContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasBluetoothPermission()) {
            requestBluetoothPermission();
            return;
        }

        if (!adapter.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }

        Set<BluetoothDevice> devices;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }
        }

        devices = adapter.getBondedDevices();

        if (devices.isEmpty()) {
            Toast.makeText(getContext(), "No paired devices", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Convert to list
        List<BluetoothDevice> deviceList = new ArrayList<>(devices);
        List<String> deviceNames = new ArrayList<>();

        for (BluetoothDevice d : deviceList) {
            String name = "Unknown Device";

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
                    requireContext().checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                name = d.getName();
            }

            deviceNames.add(name + "\n" + d.getAddress());
        }

        // 🔥 Show dialog
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Select Printer")
                .setItems(deviceNames.toArray(new String[0]), (dialog, which) -> {

                    BluetoothDevice selectedDevice = deviceList.get(which);

                    connectToDevice(selectedDevice);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {

        new Thread(() -> {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (requireContext().checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }

                btsocket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
                btsocket.connect();

                outputStream = btsocket.getOutputStream();

                // ✅ SAVE DEVICE HERE (correct place)
                SharedPreferences prefs = requireContext().getSharedPreferences("printer", 0);
                prefs.edit().putString("mac", device.getAddress()).apply();

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Connected: " + device.getName(),
                                Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Connection Failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    private void printThermalBill() {
        try {
            if (outputStream == null) {
                Toast.makeText(getContext(), "Printer not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            String paymentMode = spPaymentMode.getSelectedItem().toString();
            double totalGST = 0;
            double totalBase = 0;
            double totalSGST = 0;
            double totalCGST = 0;
            double discount = 0;
            try {
                discount = Double.parseDouble(etDiscount.getText().toString());
            } catch (Exception ignored) {}


            // 🔥 CENTER ALIGN (for header)
            outputStream.write(new byte[]{0x1B, 0x45, 0x01});
            outputStream.write(new byte[]{0x1B, 0x61, 0x01});
            outputStream.write("SWEET SHOP\n".getBytes());
            outputStream.write(new byte[]{0x1B, 0x45, 0x00});
            outputStream.write("Kolkata, India\n".getBytes());

            outputStream.write(
                    (new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()) + "\n")
                            .getBytes()
            );
            outputStream.write(("Bill No: " + currentBillId + "\n").getBytes());
            outputStream.write(("Payment: " + paymentMode + "\n").getBytes());

            // 🔥 LEFT ALIGN (for rest)
            outputStream.write(new byte[]{0x1B, 0x61, 0x00});

            outputStream.write("==============================\n".getBytes());
            outputStream.write("Item      Qty   Price   Amt\n".getBytes());
            outputStream.write("==============================\n".getBytes());

            for (CartItem item : cartList) {
                String name = item.getName().length() > 10
                        ? item.getName().substring(0, 10)
                        : item.getName();

                String line = String.format(
                        "%-10s %3.0f %5.0f %6.0f\n",
                        name,
                        item.getQty(),
                        item.getPrice(),
                        item.getTotal()
                );

                outputStream.write(line.getBytes());
            }

            for (CartItem item : cartList) {
                double rate = item.getGstRate();
                double totalPrice = item.getTotal();

                double gst = (totalPrice * rate) / (100 + rate);
                double sgst = gst / 2;
                double cgst = gst / 2;
                double base = totalPrice - gst;

                totalGST += gst;
                totalBase += base;
                totalSGST += sgst;
                totalCGST += cgst;
            }
            outputStream.write("==============================\n".getBytes());
            outputStream.write(("Subtotal: ₹" + String.format("%.2f", totalBase) + "\n").getBytes());
            outputStream.write(("SGST: ₹" + String.format("%.2f", totalSGST) + "\n").getBytes());
            outputStream.write(("CGST: ₹" + String.format("%.2f", totalCGST) + "\n").getBytes());
            outputStream.write(("Discount: ₹" + String.format("%.2f", discount) + "\n").getBytes());
            outputStream.write(("TOTAL: ₹" + String.format("%.2f", finalAmount) + "\n").getBytes());

            // 🔥 CENTER AGAIN for thank you
            outputStream.write(new byte[]{0x1B, 0x61, 0x01});
            outputStream.write("Thank You!\n\n\n".getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Print Failed", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isPrinterConnected() {
        return btsocket != null && btsocket.isConnected() && outputStream != null;
    }
    // ================= DATA =================

    private void loadItems() {

        itemList = db.getAllItems();

        List<String> names = new ArrayList<>();

        for (Item i : itemList) {
            names.add(i.getName() + " (₹" + i.getPrice() + "/" + i.getUnit() + ")");
        }

        spItems.setAdapter(new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                names
        ));
    }

    // ================= DELETE SUPPORT =================

    public void updateTotalFromAdapter() {
        total = 0;
        for (CartItem i : cartList) {
            total += i.getTotal();

        }
        updateTotalUI();
    }
    private void showUndoSnackbar(CartItem deletedItem, int position) {

        com.google.android.material.snackbar.Snackbar
                .make(recyclerBill, "Item removed", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {

                    cartList.add(position, deletedItem);
                    adapter.notifyItemInserted(position);
                    updateTotalFromAdapter();

                })
                .show();
    }

}