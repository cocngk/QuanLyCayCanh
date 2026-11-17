package com.example.quanlycaycanh;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Màn hình 5: Tạo hóa đơn (Người 4)
public class TaoHoaDonActivity extends BaseActivity {

    private final List<DBHelper.InvoiceItem> selectedItems = new ArrayList<>();

    private DBHelper dbHelper;

    private EditText edtCustomerName;
    private EditText edtCustomerPhone;
    private EditText edtSearchPlants;

    private LinearLayout layoutPlantList;
    private LinearLayout layoutInvoiceItems;

    private TextView tvEmptyPlants;
    private TextView tvEmptyItems;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tao_hoa_don);

        dbHelper = new DBHelper(this);

        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtCustomerPhone = findViewById(R.id.edtCustomerPhone);
        edtSearchPlants = findViewById(R.id.edtSearchPlants);

        layoutPlantList = findViewById(R.id.layoutPlantList);
        layoutInvoiceItems = findViewById(R.id.layoutInvoiceItems);

        tvEmptyPlants = findViewById(R.id.tvEmptyPlants);
        tvEmptyItems = findViewById(R.id.tvEmptyItems);
        tvTotal = findViewById(R.id.tvTotal);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancelForm).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveInvoice).setOnClickListener(v -> saveInvoice());

        edtSearchPlants.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                loadPlants();
            }
        });

        loadPlants();
        renderSelectedItems();
    }

    // ======================================
    // DANH SÁCH CÂY CẢNH ĐỂ CHỌN
    // ======================================

    private void loadPlants() {

        layoutPlantList.removeAllViews();

        List<DBHelper.Plant> plants =
                dbHelper.getPlants(edtSearchPlants.getText().toString());

        tvEmptyPlants.setVisibility(plants.isEmpty() ? View.VISIBLE : View.GONE);

        for (DBHelper.Plant plant : plants) {

            layoutPlantList.addView(plantRow(plant));
        }
    }

    private View plantRow(DBHelper.Plant plant) {

        View row = getLayoutInflater().inflate(
                R.layout.item_plant_pick,
                layoutPlantList,
                false
        );

        ImageView imgPlant = row.findViewById(R.id.imgPlant);
        imgPlant.setImageResource(plantImage(plant.image));

        ((TextView) row.findViewById(R.id.tvPlantName)).setText(plant.name);
        ((TextView) row.findViewById(R.id.tvPlantPrice)).setText(
                "Giá: " + Format.money(plant.price)
        );
        ((TextView) row.findViewById(R.id.tvPlantStock)).setText(
                "Tồn kho: " + plant.stock
        );

        TextView tvQuantity = row.findViewById(R.id.tvQuantity);

        int[] quantity = {1};

        row.findViewById(R.id.btnMinus).setOnClickListener(v -> {

            if (quantity[0] > 1) {

                quantity[0]--;

                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        row.findViewById(R.id.btnPlus).setOnClickListener(v -> {

            quantity[0]++;

            tvQuantity.setText(String.valueOf(quantity[0]));
        });

        row.findViewById(R.id.btnAddPlant).setOnClickListener(v ->
                addToInvoice(plant, quantity[0])
        );

        return row;
    }

    // Tên file drawable trong DB, quay về icon mặc định nếu không có
    private int plantImage(String image) {

        if (image == null) {

            return R.drawable.ic_tree;
        }

        int id = getResources().getIdentifier(image, "drawable", getPackageName());

        return id == 0 ? R.drawable.ic_tree : id;
    }

    // ======================================
    // CÂY ĐÃ CHỌN
    // ======================================

    private void addToInvoice(DBHelper.Plant plant, int quantity) {

        DBHelper.InvoiceItem existing = findSelectedItem(plant.id);

        int inInvoice = existing == null ? 0 : existing.quantity;

        if (inInvoice + quantity > plant.stock) {

            Toast.makeText(
                    this,
                    "Chỉ còn " + plant.stock + " cây trong kho",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (existing == null) {

            selectedItems.add(new DBHelper.InvoiceItem(
                    plant.id,
                    plant.name,
                    plant.price,
                    quantity
            ));

        } else {

            existing.quantity += quantity;
        }

        renderSelectedItems();
    }

    private DBHelper.InvoiceItem findSelectedItem(long plantId) {

        for (DBHelper.InvoiceItem item : selectedItems) {

            if (item.plantId == plantId) {

                return item;
            }
        }

        return null;
    }

    private void renderSelectedItems() {

        layoutInvoiceItems.removeAllViews();

        for (DBHelper.InvoiceItem item : selectedItems) {

            layoutInvoiceItems.addView(selectedItemRow(item));
        }

        tvEmptyItems.setVisibility(selectedItems.isEmpty() ? View.VISIBLE : View.GONE);

        tvTotal.setText(Format.money(totalAmount()));
    }

    private View selectedItemRow(DBHelper.InvoiceItem item) {

        View row = getLayoutInflater().inflate(
                R.layout.item_invoice_line,
                layoutInvoiceItems,
                false
        );

        ((TextView) row.findViewById(R.id.tvLineName)).setText(item.plantName);
        ((TextView) row.findViewById(R.id.tvLinePrice)).setText(
                Format.money(item.unitPrice) + " / cây"
        );
        ((TextView) row.findViewById(R.id.tvLineQuantity)).setText(
                String.valueOf(item.quantity)
        );
        ((TextView) row.findViewById(R.id.tvLineAmount)).setText(
                Format.money(item.getAmount())
        );

        row.findViewById(R.id.btnLineMinus).setOnClickListener(v ->
                changeQuantity(item, -1)
        );

        row.findViewById(R.id.btnLinePlus).setOnClickListener(v ->
                changeQuantity(item, 1)
        );

        row.findViewById(R.id.btnRemoveLine).setOnClickListener(v -> {

            selectedItems.remove(item);

            renderSelectedItems();
        });

        return row;
    }

    private void changeQuantity(DBHelper.InvoiceItem item, int delta) {

        int newQuantity = item.quantity + delta;

        if (newQuantity < 1) {

            return;
        }

        DBHelper.Plant plant = dbHelper.getPlant(item.plantId);

        if (plant != null && newQuantity > plant.stock) {

            Toast.makeText(
                    this,
                    "Chỉ còn " + plant.stock + " cây trong kho",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        item.quantity = newQuantity;

        renderSelectedItems();
    }

    private long totalAmount() {

        long total = 0;

        for (DBHelper.InvoiceItem item : selectedItems) {

            total += item.getAmount();
        }

        return total;
    }

    // ======================================
    // LƯU HÓA ĐƠN
    // ======================================

    private void saveInvoice() {

        String customerName = edtCustomerName.getText().toString().trim();
        String customerPhone = edtCustomerPhone.getText().toString().trim();

        if (customerName.isEmpty()) {

            edtCustomerName.setError("Vui lòng nhập tên khách hàng");
            edtCustomerName.requestFocus();

            return;
        }

        if (!customerPhone.isEmpty() && !customerPhone.matches("0[0-9]{9}")) {

            edtCustomerPhone.setError(
                    "Số điện thoại phải gồm 10 chữ số bắt đầu bằng 0"
            );
            edtCustomerPhone.requestFocus();

            return;
        }

        if (selectedItems.isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn ít nhất một cây cảnh",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long invoiceId = dbHelper.createInvoice(
                customerName,
                customerPhone,
                selectedItems
        );

        if (invoiceId <= 0) {

            Toast.makeText(
                    this,
                    "Không lưu được hóa đơn",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "Đã lưu hóa đơn " + String.format(Locale.ROOT, "HD%06d", invoiceId),
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
