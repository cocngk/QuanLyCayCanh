
package com.example.btl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnStats, navStats;
    private LinearLayout btnManageAccount, navAccount;
    private LinearLayout navHome, navPlants, navBills;
    private LinearLayout btnPlants, btnCreateBill, btnBills;

    private ImageView btnNotification, btnAccount, btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ÁNH XẠ CÁC THÀNH PHẦN GIAO DIỆN
        btnStats = findViewById(R.id.btnStats);
        btnManageAccount = findViewById(R.id.btnManageAccount);

        navHome = findViewById(R.id.navHome);
        navPlants = findViewById(R.id.navPlants);
        navBills = findViewById(R.id.navBills);
        navStats = findViewById(R.id.navStats);
        navAccount = findViewById(R.id.navAccount);

        btnPlants = findViewById(R.id.btnPlants);
        btnCreateBill = findViewById(R.id.btnCreateBill);
        btnBills = findViewById(R.id.btnBills);

        btnNotification = findViewById(R.id.btnNotification);
        btnAccount = findViewById(R.id.btnAccount);
        btnMenu = findViewById(R.id.btnMenu);

        // MỞ TRANG THỐNG KÊ
        btnStats.setOnClickListener(v -> openStatistics());
        navStats.setOnClickListener(v -> openStatistics());

        // MỞ TRANG TÀI KHOẢN
        btnManageAccount.setOnClickListener(v -> openAccount());
        navAccount.setOnClickListener(v -> openAccount());
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
        btnPlants.setOnClickListener(v -> showNotAvailable());
        navPlants.setOnClickListener(v -> showNotAvailable());

        // HÓA ĐƠN
        btnCreateBill.setOnClickListener(v -> showNotAvailable());
        btnBills.setOnClickListener(v -> showNotAvailable());
        navBills.setOnClickListener(v -> showNotAvailable());

        // TRANG CHỦ
        navHome.setOnClickListener(v -> {
            Toast.makeText(
                    MainActivity.this,
                    "Bạn đang ở Trang chủ",
                    Toast.LENGTH_SHORT
            ).show();
        });
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

    // THÔNG BÁO CHỨC NĂNG CHƯA TẠO
    private void showNotAvailable() {
        Toast.makeText(
                MainActivity.this,
                "Chức năng đang được xây dựng!",
                Toast.LENGTH_SHORT
        ).show();
    }
}
