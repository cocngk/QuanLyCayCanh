package com.example.quanlycaycanh;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Màn hình 6: Quản lý hóa đơn (Người 4)
public class QuanLyHoaDonActivity extends AppCompatActivity {

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_TODAY = "TODAY";
    private static final String FILTER_MONTH = "MONTH";
    private static final String FILTER_CANCELLED = "CANCELLED";

    private static final int GREEN = Color.parseColor("#2E7D32");
    private static final int WHITE = Color.parseColor("#FFFFFF");
    private static final int LIGHT_GREEN = Color.parseColor("#E8F5E9");

    private final List<DBHelper.Invoice> invoices = new ArrayList<>();

    private DBHelper dbHelper;

    private EditText edtSearchInvoices;

    private LinearLayout layoutInvoiceList;

    private TextView tvEmptyInvoices;
    private TextView tvStatTotal;
    private TextView tvStatDone;
    private TextView tvStatCancelled;
    private TextView tvStatRevenue;

    private Button btnFilterAll;
    private Button btnFilterToday;
    private Button btnFilterMonth;
    private Button btnFilterCancelled;

    private String filter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quan_ly_hoa_don);

        dbHelper = new DBHelper(this);

        edtSearchInvoices = findViewById(R.id.edtSearchInvoices);

        layoutInvoiceList = findViewById(R.id.layoutInvoiceList);

        tvEmptyInvoices = findViewById(R.id.tvEmptyInvoices);
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatDone = findViewById(R.id.tvStatDone);
        tvStatCancelled = findViewById(R.id.tvStatCancelled);
        tvStatRevenue = findViewById(R.id.tvStatRevenue);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterToday = findViewById(R.id.btnFilterToday);
        btnFilterMonth = findViewById(R.id.btnFilterMonth);
        btnFilterCancelled = findViewById(R.id.btnFilterCancelled);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddInvoice).setOnClickListener(v ->
                startActivity(new Intent(this, TaoHoaDonActivity.class))
        );

        edtSearchInvoices.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                renderInvoices();
            }
        });

        btnFilterAll.setOnClickListener(v -> setFilter(FILTER_ALL));
        btnFilterToday.setOnClickListener(v -> setFilter(FILTER_TODAY));
        btnFilterMonth.setOnClickListener(v -> setFilter(FILTER_MONTH));
        btnFilterCancelled.setOnClickListener(v -> setFilter(FILTER_CANCELLED));
    }

    // Tải lại khi quay về từ màn tạo hóa đơn
    @Override
    protected void onResume() {

        super.onResume();

        loadInvoices();
    }

    private void setFilter(String newFilter) {

        filter = newFilter;

        renderInvoices();
    }

    private void loadInvoices() {

        invoices.clear();

        invoices.addAll(dbHelper.getInvoices());

        renderInvoices();
    }

    // ======================================
    // DANH SÁCH HÓA ĐƠN
    // ======================================

    private void renderInvoices() {

        layoutInvoiceList.removeAllViews();

        String keyword = edtSearchInvoices.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        long now = System.currentTimeMillis();
        int shown = 0;

        for (DBHelper.Invoice invoice : invoices) {

            if (!matchesFilter(invoice, now) || !matchesKeyword(invoice, keyword)) {

                continue;
            }

            layoutInvoiceList.addView(invoiceRow(invoice));

            shown++;
        }

        tvEmptyInvoices.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);

        updateFilterButtons();
        renderStats();
    }

    private boolean matchesFilter(DBHelper.Invoice invoice, long now) {

        switch (filter) {

            case FILTER_CANCELLED:
                return invoice.isCancelled();

            case FILTER_TODAY:
                return Format.day(invoice.createdAt).equals(Format.day(now));

            case FILTER_MONTH:
                return Format.month(invoice.createdAt).equals(Format.month(now));

            default:
                return true;
        }
    }

    private boolean matchesKeyword(DBHelper.Invoice invoice, String keyword) {

        if (keyword.isEmpty()) {

            return true;
        }

        String code = invoice.code.toLowerCase(Locale.ROOT);
        String name = invoice.customerName.toLowerCase(Locale.ROOT);

        String phone = invoice.customerPhone == null
                ? ""
                : invoice.customerPhone;

        return code.contains(keyword)
                || name.contains(keyword)
                || phone.contains(keyword);
    }

    private View invoiceRow(DBHelper.Invoice invoice) {

        View row = getLayoutInflater().inflate(
                R.layout.item_invoice,
                layoutInvoiceList,
                false
        );

        ((TextView) row.findViewById(R.id.tvInvoiceCode)).setText(invoice.code);

        ((TextView) row.findViewById(R.id.tvInvoiceCustomer)).setText(
                invoice.customerPhone == null
                        ? invoice.customerName
                        : invoice.customerName + " · " + invoice.customerPhone
        );

        ((TextView) row.findViewById(R.id.tvInvoiceDate)).setText(
                Format.dateTime(invoice.createdAt)
        );

        ((TextView) row.findViewById(R.id.tvInvoiceItemCount)).setText(
                invoice.itemCount + " sản phẩm"
        );

        ((TextView) row.findViewById(R.id.tvInvoiceTotal)).setText(
                Format.money(invoice.total)
        );

        TextView tvStatus = row.findViewById(R.id.tvInvoiceStatus);

        if (invoice.isCancelled()) {

            tvStatus.setText("Đã hủy");
            tvStatus.setTextColor(Color.parseColor("#C62828"));
            tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);

        } else {

            tvStatus.setText("Hoàn thành");
            tvStatus.setTextColor(Color.parseColor("#1B5E20"));
            tvStatus.setBackgroundResource(R.drawable.bg_status_done);
        }

        View.OnClickListener openDetail = v -> showDetail(invoice);

        row.setOnClickListener(openDetail);
        row.findViewById(R.id.btnViewInvoice).setOnClickListener(openDetail);

        return row;
    }

    // ======================================
    // CHI TIẾT VÀ HỦY HÓA ĐƠN
    // ======================================

    private void showDetail(DBHelper.Invoice invoice) {

        StringBuilder message = new StringBuilder();

        for (DBHelper.InvoiceItem item : dbHelper.getInvoiceItems(invoice.id)) {

            message.append(item.plantName)
                    .append("  ×").append(item.quantity)
                    .append("  =  ").append(Format.money(item.getAmount()))
                    .append("\n");
        }

        message.append("\nTổng cộng: ").append(Format.money(invoice.total));

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(invoice.code + " · " + invoice.customerName)
                .setMessage(message.toString())
                .setPositiveButton("Đóng", null);

        if (!invoice.isCancelled()) {

            dialog.setNegativeButton("Hủy hóa đơn", (d, which) -> confirmCancel(invoice));
        }

        dialog.show();
    }

    private void confirmCancel(DBHelper.Invoice invoice) {

        new AlertDialog.Builder(this)
                .setTitle("Hủy hóa đơn " + invoice.code + "?")
                .setMessage("Số lượng cây trong hóa đơn sẽ được trả lại tồn kho.")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy hóa đơn", (d, which) -> cancelInvoice(invoice))
                .show();
    }

    private void cancelInvoice(DBHelper.Invoice invoice) {

        if (dbHelper.cancelInvoice(invoice.id)) {

            Toast.makeText(this, "Đã hủy hóa đơn", Toast.LENGTH_SHORT).show();

            loadInvoices();

        } else {

            Toast.makeText(
                    this,
                    "Không hủy được hóa đơn này",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ======================================
    // BỘ LỌC VÀ THỐNG KÊ
    // ======================================

    private void updateFilterButtons() {

        Button[] buttons = {
                btnFilterAll,
                btnFilterToday,
                btnFilterMonth,
                btnFilterCancelled
        };

        String[] values = {
                FILTER_ALL,
                FILTER_TODAY,
                FILTER_MONTH,
                FILTER_CANCELLED
        };

        for (int i = 0; i < buttons.length; i++) {

            boolean selected = values[i].equals(filter);

            buttons[i].setBackgroundTintList(
                    ColorStateList.valueOf(selected ? GREEN : LIGHT_GREEN)
            );

            buttons[i].setTextColor(selected ? WHITE : GREEN);
        }
    }

    private void renderStats() {

        int done = 0;
        int cancelled = 0;
        long revenue = 0;

        for (DBHelper.Invoice invoice : invoices) {

            if (invoice.isCancelled()) {

                cancelled++;

            } else {

                done++;
                revenue += invoice.total;
            }
        }

        tvStatTotal.setText(String.valueOf(invoices.size()));
        tvStatDone.setText(String.valueOf(done));
        tvStatCancelled.setText(String.valueOf(cancelled));
        tvStatRevenue.setText(Format.money(revenue));
    }
}
