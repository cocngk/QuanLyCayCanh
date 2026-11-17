package com.example.btl;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Account extends AppCompatActivity {

    // Quản lý phiên đăng nhập
    private SessionManager sessionManager;

    // Cơ sở dữ liệu SQLite
    private DBHelper dbHelper;

    // Hiển thị tên người dùng
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Kết nối giao diện XML
        setContentView(R.layout.activity_account);

        // Khởi tạo SQLite
        dbHelper = new DBHelper(this);

        // Khởi tạo quản lý đăng nhập
        sessionManager = new SessionManager(this);

        // Kiểm tra đăng nhập
        long userId = sessionManager.getUserId();

        if (userId <= 0 || dbHelper.getUser(userId) == null) {

            sessionManager.logout();

            returnToLogin();

            return;
        }

        // =====================================
        // ÁNH XẠ GIAO DIỆN
        // =====================================

        tvUsername = findViewById(R.id.tvUsername);

        // =====================================
        // 1. THÔNG TIN CÁ NHÂN
        // =====================================

        findViewById(R.id.btnProfile).setOnClickListener(v -> {

            Intent intent = new Intent(
                    Account.this,
                    Profile.class
            );

            startActivity(intent);
        });

        // =====================================
        // 2. ĐỔI MẬT KHẨU
        // =====================================

        findViewById(R.id.btnChangePassword)
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            Account.this,
                            ChangePassword.class
                    );

                    startActivity(intent);
                });

        // =====================================
        // 3. THÔNG BÁO
        // =====================================

        findViewById(R.id.btnNotification)
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            Account.this,
                            Notification.class
                    );

                    startActivity(intent);
                });

        // =====================================
        // 4. GIỚI THIỆU ỨNG DỤNG
        // =====================================

        findViewById(R.id.btnAbout)
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            Account.this,
                            About.class
                    );

                    startActivity(intent);
                });

        // =====================================
        // 5. ĐĂNG XUẤT
        // =====================================

        findViewById(R.id.btnLogout)
                .setOnClickListener(v -> {

                    showLogoutDialog();
                });

        // =====================================
        // THANH ĐIỀU HƯỚNG PHÍA DƯỚI
        // =====================================

        // Trở về trang chủ
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Mở màn hình quản lý cây cảnh
        findViewById(R.id.navPlants).setOnClickListener(v ->
                startActivity(new Intent(this, PlantManagementActivity.class)));

        // Hóa đơn
        findViewById(R.id.navOrders).setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Chức năng hóa đơn đang được tích hợp",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Chuyển sang trang thống kê đã có sẵn
        findViewById(R.id.navStats).setOnClickListener(v ->
                startActivity(new Intent(this, Statistics.class)));

        // Đang ở trang Tài khoản
        findViewById(R.id.navAccount).setOnClickListener(v -> {

            // Không cần chuyển màn hình
        });
    }

    // =====================================
    // HIỂN THỊ THÔNG TIN NGƯỜI DÙNG
    // =====================================

    @Override
    protected void onResume() {

        super.onResume();

        if (dbHelper == null || sessionManager == null) {
            return;
        }

        long userId = sessionManager.getUserId();

        if (userId <= 0) {
            returnToLogin();
            return;
        }

        DBHelper.User user = dbHelper.getUser(userId);

        if (user == null) {
            sessionManager.logout();
            returnToLogin();
            return;
        }

        // Hiển thị họ tên đã đăng ký
        if (tvUsername != null) {
            tvUsername.setText(user.fullName);
        }
    }

    // =====================================
    // HỘP THOẠI XÁC NHẬN ĐĂNG XUẤT
    // =====================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Đăng xuất")

                .setMessage(
                        "Bạn có chắc chắn muốn đăng xuất không?"
                )

                .setNegativeButton(
                        "Hủy",
                        (dialog, which) -> dialog.dismiss()
                )

                .setPositiveButton(
                        "Đăng xuất",
                        (dialog, which) -> {

                            sessionManager.logout();

                            Toast.makeText(
                                    this,
                                    "Đăng xuất thành công",
                                    Toast.LENGTH_SHORT
                            ).show();

                            returnToLogin();
                        }
                )

                .show();
    }

    // =====================================
    // QUAY VỀ TRANG ĐĂNG NHẬP
    // =====================================

    private void returnToLogin() {

        Intent intent = new Intent(
                Account.this,
                Login.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}