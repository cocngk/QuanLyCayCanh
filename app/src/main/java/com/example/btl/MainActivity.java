
package com.example.btl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnStats;
    private LinearLayout btnManageAccount;
    private LinearLayout btnPlants, btnCreateBill, btnBills;

    private ImageView btnNotification, btnAccount, btnMenu;
    private PlantDatabase plantDatabase;
    private InvoiceDatabase invoiceDatabase;
    private final Locale vietnamese = new Locale("vi", "VN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plantDatabase = new PlantDatabase(this);
        invoiceDatabase = new InvoiceDatabase(this);

        // ÁNH XẠ CÁC THÀNH PHẦN GIAO DIỆN
        btnStats = findViewById(R.id.btnStats);
        btnManageAccount = findViewById(R.id.btnManageAccount);

        BottomNavHelper.setup(this, R.id.navHome);

        btnPlants = findViewById(R.id.btnPlants);
        btnCreateBill = findViewById(R.id.btnCreateBill);
        btnBills = findViewById(R.id.btnBills);

        btnNotification = findViewById(R.id.btnNotification);
        btnAccount = findViewById(R.id.btnAccount);
        btnMenu = findViewById(R.id.btnMenu);

        // MỞ TRANG THỐNG KÊ
        btnStats.setOnClickListener(v -> openStatistics());

        // MỞ TRANG TÀI KHOẢN
        btnManageAccount.setOnClickListener(v -> openAccount());
        btnAccount.setOnClickListener(v -> openAccount());

        // THÔNG BÁO
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Notification.class);
            startActivity(intent);
        });

        // MENU
        btnMenu.setOnClickListener(v -> {
            Toast.makeText(
                    MainActivity.this,
                    "Chức năng menu",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // CÂY CẢNH
        btnPlants.setOnClickListener(v -> openPlants());

        // Tìm kiếm từ Trang chủ sẽ mở danh sách và điền sẵn từ khóa.
        EditText edtSearch = findViewById(R.id.edtSearch);
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Intent intent = new Intent(this, PlantManagementActivity.class);
                intent.putExtra("SEARCH_QUERY", edtSearch.getText().toString());
                startActivity(intent);
                return true;
            }
            return false;
        });
        findViewById(R.id.btnFilter).setOnClickListener(v -> openPlants());

        // HÓA ĐƠN
        btnCreateBill.setOnClickListener(v -> openCreateInvoice());
        btnBills.setOnClickListener(v -> openInvoiceManagement());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (plantDatabase != null && invoiceDatabase != null) refreshDashboard();
    }

    private void refreshDashboard() {
        List<Plant> plants = plantDatabase.getAllPlants();
        long stock = 0;
        for (Plant plant : plants) stock += plant.getStock();

        InvoiceDatabase.Summary summary = invoiceDatabase.getSummary();
        ((TextView) findViewById(R.id.tvPlantCount)).setText(
                NumberFormat.getIntegerInstance(vietnamese).format(plants.size()));
        ((TextView) findViewById(R.id.tvStockCount)).setText(
                NumberFormat.getIntegerInstance(vietnamese).format(stock));
        ((TextView) findViewById(R.id.tvInvoiceCount)).setText(
                NumberFormat.getIntegerInstance(vietnamese).format(summary.totalCount));
        ((TextView) findViewById(R.id.tvRevenue)).setText(compactMoney(summary.revenue));
    }

    private String compactMoney(long amount) {
        if (amount >= 1_000_000) {
            return NumberFormat.getNumberInstance(vietnamese)
                    .format(amount / 1_000_000d) + "tr";
        }
        if (amount >= 1_000) {
            return NumberFormat.getNumberInstance(vietnamese)
                    .format(amount / 1_000d) + "k";
        }
        return NumberFormat.getIntegerInstance(vietnamese).format(amount) + " đ";
    }

    // CHUYỂN SANG MÀN HÌNH 3: QUẢN LÝ CÂY CẢNH
    private void openPlants() {
        startActivity(new Intent(this, PlantManagementActivity.class));
    }

    // CHUYỂN SANG TRANG THỐNG KÊ
    private void openStatistics() {
        Intent intent = new Intent(MainActivity.this, Statistics.class);
        startActivity(intent);
    }

    // CHUYỂN SANG TRANG TÀI KHOẢN
    private void openAccount() {
        Intent intent = new Intent(MainActivity.this, Account.class);
        startActivity(intent);
    }

    private void openCreateInvoice() {
        startActivity(new Intent(this, CreateInvoiceActivity.class));
    }

    private void openInvoiceManagement() {
        startActivity(new Intent(this, InvoiceManagementActivity.class));
    }

    @Override
    protected void onDestroy() {
        if (plantDatabase != null) plantDatabase.close();
        if (invoiceDatabase != null) invoiceDatabase.close();
        super.onDestroy();
    }
}
