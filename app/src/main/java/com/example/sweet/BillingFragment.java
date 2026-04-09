package com.example.sweet;

import android.bluetooth.*;
import android.content.Intent;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.*;

public class BillingFragment extends Fragment {

    private static final UUID PRINTER_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Spinner spItems;
    EditText etQty, etGST, etDiscount, etPhone;
    Button btnAddToBill, btnGeneratePDF;

    TextView tvTotal, tvFinal;
    RecyclerView recyclerBill;
    BillRecyclerAdapter adapter;
    DatabaseHelper db;

    List<Item> itemList;
    List<CartItem> cartList = new ArrayList<>();

    double total = 0;
    double finalAmount = 0;

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

        // ✅ ONLY ONE adapter (class variable)
        adapter = new BillRecyclerAdapter(cartList, () -> {
            updateTotalFromAdapter();
        });

        recyclerBill.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBill.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        recyclerBill.setAdapter(adapter);

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

        helper.attachToRecyclerView(recyclerBill);

        return view;
    }

    // ================= INIT =================

    private void initViews(View view) {
        spItems = view.findViewById(R.id.spItems);
        etQty = view.findViewById(R.id.etQty);
        etGST = view.findViewById(R.id.etGST);
        etDiscount = view.findViewById(R.id.etDiscount);
        etPhone = view.findViewById(R.id.etPhone);

        btnAddToBill = view.findViewById(R.id.btnAddToBill);
        btnGeneratePDF = view.findViewById(R.id.btnGeneratePDF);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvFinal = view.findViewById(R.id.tvFinal);

        db = new DatabaseHelper(getContext());
    }



    private void setupListeners() {

        btnAddToBill.setOnClickListener(v -> addItemToCart());

        etGST.addTextChangedListener(new SimpleTextWatcher(this::calculateFinal));
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

        Item item = itemList.get(spItems.getSelectedItemPosition());
        double qty = Double.parseDouble(qtyStr);

        boolean found = false;

        for (int i = 0; i < cartList.size(); i++) {
            CartItem c = cartList.get(i);

            if (c.getName().equals(item.getName())) {
                c.setQty(c.getQty() + qty);
                adapter.notifyItemChanged(i);
                found = true;
                break;
            }
        }

        if (!found) {
            CartItem cartItem = new CartItem(item.getName(), qty, item.getPrice());
            cartList.add(cartItem);
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

        double gst = etGST.getText().toString().isEmpty()
                ? 0 : Double.parseDouble(etGST.getText().toString());

        double discount = etDiscount.getText().toString().isEmpty()
                ? 0 : Double.parseDouble(etDiscount.getText().toString());

        double gstAmount = total * gst / 100;
        finalAmount = total + gstAmount - discount;

        tvFinal.setText(String.format(
                "Subtotal: ₹%.2f\nGST: ₹%.2f\nDiscount: ₹%.2f\nFinal: ₹%.2f",
                total, gstAmount, discount, finalAmount
        ));
    }

    // ================= SAVE + SHARE =================

    private void generateBill() {

        if (cartList.isEmpty()) {
            Toast.makeText(getContext(), "No items in cart", Toast.LENGTH_SHORT).show();
            return;
        }

        if (finalAmount == 0) {
            Toast.makeText(getContext(), "Calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build summary
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < cartList.size(); i++) {
            CartItem item = cartList.get(i);

            summary.append(item.getName())
                    .append(" ")
                    .append(item.getQty())
                    .append(" ")
                    .append(item.getTotal());

            if (i != cartList.size() - 1) {
                summary.append(", ");
            }
        }

        db.saveSale(finalAmount, summary.toString());

        String phone = etPhone.getText().toString().trim();

        if (phone.length() == 10) {
            sendToWhatsApp(phone);
        } else {
            generatePDF();
        }

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

    private void sendToWhatsApp(String number) {

        StringBuilder msg = new StringBuilder();

        msg.append("*🧾 SWEET SHOP*\n");
        msg.append("Kolkata, India\n");
        msg.append("Date: ")
                .append(new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()))
                .append("\n----------------------------------\n");

        msg.append("*Item      Qty    Amt*\n");
        msg.append("-----------------------------------------\n");

        for (CartItem item : cartList) {
            msg.append(String.format(
                    "%-10s %3.0f %7.0f\n",
                    item.getName(),
                    item.getQty(),
                    item.getTotal()
            ));
        }

        msg.append("-----------------------------------------\n");
        msg.append("*Total: ₹").append(finalAmount).append("*\n");
        msg.append("\n🙏 Thank You!");

        String url = "https://wa.me/91" + number + "?text=" + Uri.encode(msg.toString());

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    // ================= PDF =================

    private void generatePDF() {

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.Page page = pdf.startPage(
                new PdfDocument.PageInfo.Builder(1200, 2010, 1).create());

        Canvas canvas = page.getCanvas();

        int y = 200;

        for (CartItem item : cartList) {
            canvas.drawText(
                    String.format("%-10s %3.0f %7.0f",
                            item.getName(),
                            item.getQty(),
                            item.getTotal()),
                    100, y, paint
            );
            y += 50;
        }

        canvas.drawText("Final: ₹" + finalAmount, 100, y + 100, paint);

        pdf.finishPage(page);

        File file = new File(requireContext().getExternalFilesDir(null), "bill.pdf");

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