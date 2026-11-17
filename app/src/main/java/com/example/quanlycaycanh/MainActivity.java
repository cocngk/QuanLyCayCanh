package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

// Trang chủ (Người 2)
public class MainActivity extends BaseActivity {

    private DBHelper dbHelper;

    private EditText edtSearch;
    private LinearLayout layoutSearchResults;

    // Loại cây đang lọc, null là tất cả
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        edtSearch = findViewById(R.id.edtSearch);
        layoutSearchResults = findViewById(R.id.layoutSearchResults);

        findViewById(R.id.btnCreateBill).setOnClickListener(v ->
                startActivity(new Intent(this, TaoHoaDonActivity.class))
        );

        findViewById(R.id.btnBills).setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyHoaDonActivity.class))
        );

        findViewById(R.id.btnStats).setOnClickListener(v ->
                startActivity(new Intent(this, Statistics.class))
        );

        findViewById(R.id.btnManageAccount).setOnClickListener(v ->
                startActivity(new Intent(this, Account.class))
        );

        findViewById(R.id.btnAccount).setOnClickListener(v ->
                startActivity(new Intent(this, Account.class))
        );

        findViewById(R.id.btnNotification).setOnClickListener(v ->
                startActivity(new Intent(this, Notification.class))
        );

        // Màn quản lý cây cảnh là phần của Người 3
        findViewById(R.id.btnPlants).setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Quản lý cây cảnh do Người 3 phụ trách",
                        Toast.LENGTH_SHORT
                ).show()
        );

        findViewById(R.id.btnFilter).setOnClickListener(v ->
                showCategoryFilter()
        );

        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
            }

            @Override
            public void afterTextChanged(Editable text) {

                showSearchResults();
            }
        });

        BottomNav.bind(this);

        HeaderMenu.attach(this, findViewById(R.id.btnMenu));
    }

    @Override
    protected void onResume() {

        super.onResume();

        // Số liệu đổi sau khi tạo hoặc hủy hóa đơn
        showTotals();
    }

    // Bốn thẻ tổng quan
    private void showTotals() {

        List<DBHelper.Plant> plants = dbHelper.getPlants("");

        int stock = 0;

        for (DBHelper.Plant plant : plants) {

            stock += plant.stock;
        }

        List<DBHelper.Invoice> invoices = dbHelper.getInvoices();

        long revenue = 0;

        for (DBHelper.Invoice invoice : invoices) {

            if (!invoice.isCancelled()) {

                revenue += invoice.total;
            }
        }

        ((TextView) findViewById(R.id.tvPlantCount))
                .setText(String.valueOf(plants.size()));

        ((TextView) findViewById(R.id.tvStockCount))
                .setText(String.valueOf(stock));

        ((TextView) findViewById(R.id.tvInvoiceCount))
                .setText(String.valueOf(invoices.size()));

        ((TextView) findViewById(R.id.tvRevenue))
                .setText(Format.money(revenue));
    }

    // Tìm kiếm cây cảnh ngay trên trang chủ
    private void showSearchResults() {

        String keyword = edtSearch.getText().toString().trim();

        layoutSearchResults.removeAllViews();

        boolean empty = keyword.isEmpty() && category == null;

        layoutSearchResults.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {

            return;
        }

        List<DBHelper.Plant> found = new ArrayList<>();

        for (DBHelper.Plant plant : dbHelper.getPlants(keyword)) {

            if (category == null || category.equals(plant.category)) {

                found.add(plant);
            }
        }

        if (found.isEmpty()) {

            addHint("Không tìm thấy cây cảnh phù hợp");

            return;
        }

        int limit = Math.min(found.size(), 6);

        for (int i = 0; i < limit; i++) {

            addPlantRow(found.get(i));
        }

        if (found.size() > limit) {

            addHint("Còn " + (found.size() - limit) + " cây khác");
        }
    }

    private void addPlantRow(DBHelper.Plant plant) {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        TextView name = new TextView(this);

        name.setText(plant.name);
        name.setTextColor(0xFF111111);
        name.setTextSize(14);
        name.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(name, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView info = new TextView(this);

        info.setText(plant.category + " · còn " + plant.stock);
        info.setTextColor(0xFF4A6B4F);
        info.setTextSize(12);
        info.setGravity(Gravity.END);

        row.addView(info);

        layoutSearchResults.addView(row);
    }

    private void addHint(String text) {

        TextView hint = new TextView(this);

        hint.setText(text);
        hint.setTextColor(0xFF6E6E6E);
        hint.setTextSize(13);
        hint.setPadding(0, dp(6), 0, dp(6));

        layoutSearchResults.addView(hint);
    }

    // Danh sách loại cây lấy từ dữ liệu, không viết cứng
    private void showCategoryFilter() {

        List<String> categories = new ArrayList<>();

        for (DBHelper.Plant plant : dbHelper.getPlants("")) {

            if (!categories.contains(plant.category)) {

                categories.add(plant.category);
            }
        }

        String[] options = new String[categories.size() + 1];

        options[0] = "Tất cả";

        for (int i = 0; i < categories.size(); i++) {

            options[i + 1] = categories.get(i);
        }

        new AlertDialog.Builder(this)

                .setTitle("Lọc theo loại cây")

                .setItems(options, (dialog, which) -> {

                    category = which == 0 ? null : options[which];

                    showSearchResults();
                })

                .show();
    }

    private int dp(float value) {

        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}
