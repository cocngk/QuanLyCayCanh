package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Profile extends AppCompatActivity {

    // Các ô nhập liệu
    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhone;

    // Nút chức năng
    private Button btnSaveProfile;
    private ImageView btnBack;

    // SQLite
    private DBHelper dbHelper;

    // Phiên đăng nhập
    private SessionManager sessionManager;

    // ID người đang đăng nhập
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        // Khởi tạo SQLite
        dbHelper = new DBHelper(this);

        sessionManager = new SessionManager(this);

        userId = sessionManager.getUserId();

        // Kiểm tra tài khoản
        if (userId <= 0 || dbHelper.getUser(userId) == null) {

            sessionManager.logout();

            returnToLogin();

            return;
        }

        // =====================================
        // ÁNH XẠ XML
        // =====================================

        edtFullName = findViewById(R.id.edtFullName);

        edtEmail = findViewById(R.id.edtEmail);

        edtPhone = findViewById(R.id.edtPhone);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBack = findViewById(R.id.btnBack);

        // =====================================
        // LẤY THÔNG TIN TỪ SQLITE
        // =====================================

        loadUserInfo();

        // =====================================
        // NÚT LƯU THAY ĐỔI
        // =====================================

        btnSaveProfile.setOnClickListener(v -> {

            saveProfile();
        });

        // =====================================
        // NÚT QUAY LẠI
        // =====================================

        btnBack.setOnClickListener(v -> {

            finish();
        });
    }

    // =====================================
    // 1. HIỂN THỊ THÔNG TIN CÁ NHÂN
    // =====================================

    private void loadUserInfo() {

        DBHelper.User user = dbHelper.getUser(userId);

        if (user == null) {

            Toast.makeText(
                    this,
                    "Không tìm thấy tài khoản",
                    Toast.LENGTH_SHORT
            ).show();

            sessionManager.logout();

            returnToLogin();

            return;
        }

        edtFullName.setText(
                user.fullName == null ? "" : user.fullName
        );

        edtEmail.setText(
                user.email == null ? "" : user.email
        );

        edtPhone.setText(
                user.phone == null ? "" : user.phone
        );
    }

    // =====================================
    // 2. LƯU THÔNG TIN
    // =====================================

    private void saveProfile() {

        String fullName = edtFullName
                .getText()
                .toString()
                .trim();

        String email = edtEmail
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String phone = edtPhone
                .getText()
                .toString()
                .trim();

        // Kiểm tra họ tên
        if (fullName.isEmpty()) {

            edtFullName.setError("Vui lòng nhập họ tên");

            edtFullName.requestFocus();

            return;
        }

        // Email hoặc SĐT: cần ít nhất một
        if (email.isEmpty() && phone.isEmpty()) {

            edtEmail.setError(
                    "Cần nhập email hoặc số điện thoại"
            );

            edtPhone.setError(
                    "Cần nhập email hoặc số điện thoại"
            );

            return;
        }

        // Kiểm tra email nếu có nhập
        if (!email.isEmpty()) {

            if (!Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                edtEmail.setError("Email không hợp lệ");

                edtEmail.requestFocus();

                return;
            }
        }

        // Kiểm tra SĐT nếu có nhập
        if (!phone.isEmpty()) {

            if (!phone.matches("0[0-9]{9}")) {

                edtPhone.setError(
                        "SĐT phải gồm 10 chữ số, bắt đầu bằng 0"
                );

                edtPhone.requestFocus();

                return;
            }
        }

        // =====================================
        // CẬP NHẬT SQLITE
        // =====================================

        boolean result = dbHelper.updateProfile(
                userId,
                fullName,
                email,
                phone
        );

        if (result) {

            Toast.makeText(
                    this,
                    "Cập nhật thông tin thành công!",
                    Toast.LENGTH_SHORT
            ).show();

            // Quay về Tài khoản
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Không lưu được. Email hoặc SĐT có thể đã tồn tại.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================
    // 3. QUAY LẠI ĐĂNG NHẬP
    // =====================================

    private void returnToLogin() {

        Intent intent = new Intent(
                Profile.this,
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