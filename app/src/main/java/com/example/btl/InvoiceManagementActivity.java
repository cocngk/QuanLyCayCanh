package com.example.btl;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Searchable invoice history, summary, detail view and admin cancellation. */
public class InvoiceManagementActivity extends AppCompatActivity {
    private final Locale vietnamese = new Locale("vi", "VN");
    private InvoiceDatabase database;
    private EditText etSearch;
    private LinearLayout invoiceContainer;
    private TextView tvEmpty;
    private TextView tvTotal;
    private TextView tvCompleted;
    private TextView tvCancelled;
    private TextView tvRevenue;
    private String selectedFilter = InvoiceDatabase.FILTER_ALL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_management);

        database = new InvoiceDatabase(this);
        etSearch = findViewById(R.id.etSearchInvoices);
        invoiceContainer = findViewById(R.id.layoutInvoiceList);
        tvEmpty = findViewById(R.id.tvInvoiceListEmpty);
        tvTotal = findViewById(R.id.tvInvoiceCountTotal);
        tvCompleted = findViewById(R.id.tvInvoiceCountCompleted);
        tvCancelled = findViewById(R.id.tvInvoiceCountCancelled);
        tvRevenue = findViewById(R.id.tvInvoiceRevenue);

        findViewById(R.id.btnBackInvoiceList).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddInvoice).setOnClickListener(v -> openCreateInvoice());
        findViewById(R.id.btnFilterAllInvoices).setOnClickListener(
                v -> selectFilter(InvoiceDatabase.FILTER_ALL));
        findViewById(R.id.btnFilterTodayInvoices).setOnClickListener(
                v -> selectFilter(InvoiceDatabase.FILTER_TODAY));
        findViewById(R.id.btnFilterMonthInvoices).setOnClickListener(
                v -> selectFilter(InvoiceDatabase.FILTER_MONTH));
        findViewById(R.id.btnFilterCancelledInvoices).setOnClickListener(
                v -> selectFilter(InvoiceDatabase.FILTER_CANCELLED));
        BottomNavHelper.setup(this, R.id.navBills);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshInvoices();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        applyFilterStyle();
        refreshInvoices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && invoiceContainer != null) refreshInvoices();
    }

    private void openCreateInvoice() {
        startActivity(new android.content.Intent(this, CreateInvoiceActivity.class));
    }

    private void selectFilter(String filter) {
        selectedFilter = filter;
        applyFilterStyle();
        refreshInvoices();
    }

    private void applyFilterStyle() {
        setFilterStyle(R.id.btnFilterAllInvoices, InvoiceDatabase.FILTER_ALL);
        setFilterStyle(R.id.btnFilterTodayInvoices, InvoiceDatabase.FILTER_TODAY);
        setFilterStyle(R.id.btnFilterMonthInvoices, InvoiceDatabase.FILTER_MONTH);
        setFilterStyle(R.id.btnFilterCancelledInvoices, InvoiceDatabase.FILTER_CANCELLED);
    }

    private void setFilterStyle(int buttonId, String filter) {
        MaterialButton button = findViewById(buttonId);
        boolean selected = selectedFilter.equals(filter);
        int green = ContextCompat.getColor(this, R.color.green_primary);
        button.setBackgroundTintList(ColorStateList.valueOf(selected
                ? green : ContextCompat.getColor(this, R.color.white)));
        button.setTextColor(selected ? ContextCompat.getColor(this, R.color.white)
                : ContextCompat.getColor(this, R.color.text_primary));
        button.setStrokeColor(ColorStateList.valueOf(selected ? green
                : ContextCompat.getColor(this, R.color.input_border)));
        button.setStrokeWidth(dp(1));
    }

    private void refreshInvoices() {
        InvoiceDatabase.Summary summary = database.getSummary();
        tvTotal.setText(String.valueOf(summary.totalCount));
        tvCompleted.setText(String.valueOf(summary.completedCount));
        tvCancelled.setText(String.valueOf(summary.cancelledCount));
        tvRevenue.setText(money(summary.revenue));

        List<InvoiceDatabase.Invoice> invoices = database.getInvoices(
                etSearch.getText().toString(), selectedFilter);
        invoiceContainer.removeAllViews();
        tvEmpty.setVisibility(invoices.isEmpty() ? android.view.View.VISIBLE
                : android.view.View.GONE);
        for (InvoiceDatabase.Invoice invoice : invoices) {
            invoiceContainer.addView(createInvoiceCard(invoice));
        }
    }

    private MaterialCardView createInvoiceCard(InvoiceDatabase.Invoice invoice) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        card.setRadius(dp(14));
        card.setCardElevation(dp(1));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.input_border));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(10), dp(10), dp(10));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView code = makeText(invoice.code, 17,
                ContextCompat.getColor(this, R.color.text_primary), true);
        top.addView(code, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView total = makeText(money(invoice.total), 17,
                ContextCompat.getColor(this, R.color.danger), true);
        top.addView(total);
        content.addView(top);

        LinearLayout middle = new LinearLayout(this);
        middle.setGravity(Gravity.CENTER_VERTICAL);
        middle.setPadding(0, dp(5), 0, dp(5));
        TextView customer = makeText("♙  " + invoice.customerName, 14,
                ContextCompat.getColor(this, R.color.text_primary), false);
        middle.addView(customer, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView count = makeText(invoice.productCount + " sản phẩm", 13,
                ContextCompat.getColor(this, R.color.text_secondary), false);
        middle.addView(count);
        content.addView(middle);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        TextView date = makeText(formatDate(invoice.createdAt), 12,
                ContextCompat.getColor(this, R.color.text_secondary), false);
        bottom.addView(date, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView status = makeStatusPill(invoice.status);
        bottom.addView(status);
        MaterialButton view = new MaterialButton(this);
        view.setText("Xem");
        view.setAllCaps(false);
        view.setTextSize(12);
        view.setIconResource(R.drawable.ic_eye);
        view.setIconTint(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.green_primary)));
        view.setIconPadding(dp(3));
        view.setCornerRadius(dp(10));
        view.setInsetTop(0);
        view.setInsetBottom(0);
        view.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.white)));
        view.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
        view.setStrokeColor(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.green_primary)));
        view.setStrokeWidth(dp(1));
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                dp(76), dp(38));
        viewParams.setMargins(dp(8), 0, 0, 0);
        bottom.addView(view, viewParams);
        content.addView(bottom);

        card.addView(content);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(9));
        card.setLayoutParams(params);
        card.setOnClickListener(v -> showInvoiceDetail(invoice.id));
        view.setOnClickListener(v -> showInvoiceDetail(invoice.id));
        return card;
    }

    private TextView makeStatusPill(String status) {
        boolean cancelled = InvoiceDatabase.STATUS_CANCELLED.equals(status);
        TextView pill = makeText(cancelled ? "Đã hủy" : "Hoàn thành", 12,
                ContextCompat.getColor(this, cancelled ? R.color.danger : R.color.green_primary),
                true);
        pill.setPadding(dp(9), dp(5), dp(9), dp(5));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ContextCompat.getColor(this,
                cancelled ? R.color.screen_background : R.color.green_soft));
        background.setCornerRadius(dp(18));
        pill.setBackground(background);
        return pill;
    }

    private void showInvoiceDetail(long invoiceId) {
        InvoiceDatabase.Invoice invoice = database.getInvoice(invoiceId);
        if (invoice == null) {
            Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
            refreshInvoices();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Khách hàng: ").append(invoice.customerName)
                .append("\nSố điện thoại: ").append(invoice.customerPhone)
                .append("\nNgày tạo: ").append(formatDate(invoice.createdAt))
                .append("\nTrạng thái: ")
                .append(InvoiceDatabase.STATUS_CANCELLED.equals(invoice.status)
                        ? "Đã hủy" : "Hoàn thành")
                .append("\n\nSản phẩm:\n");

        for (InvoiceDatabase.InvoiceItem item : database.getItems(invoiceId)) {
            message.append("• ").append(item.plantName)
                    .append("  × ").append(item.quantity)
                    .append("  ·  ").append(money(item.getLineTotal()))
                    .append('\n');
        }
        message.append("\nTổng thanh toán: ").append(money(invoice.total));

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(invoice.code)
                .setMessage(message.toString())
                .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());
        if (InvoiceDatabase.STATUS_COMPLETED.equals(invoice.status)) {
            builder.setPositiveButton("Hủy hóa đơn", (dialog, which) -> {
                if (database.cancelInvoice(invoiceId)) {
                    Toast.makeText(this, "Đã hủy hóa đơn và hoàn tồn kho",
                            Toast.LENGTH_SHORT).show();
                    refreshInvoices();
                } else {
                    Toast.makeText(this, "Hóa đơn không thể hủy", Toast.LENGTH_SHORT).show();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(ignored -> {
            if (InvoiceDatabase.STATUS_COMPLETED.equals(invoice.status)) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(this, R.color.danger));
            }
        });
        dialog.show();
    }

    private TextView makeText(String text, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", vietnamese)
                .format(new Date(timestamp));
    }

    private String money(long amount) {
        return NumberFormat.getNumberInstance(vietnamese).format(amount) + " đ";
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}
