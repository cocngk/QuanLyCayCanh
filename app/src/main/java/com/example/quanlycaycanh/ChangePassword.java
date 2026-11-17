package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class ChangePassword extends BaseActivity {

    // Các ô mật khẩu
    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmPassword;

    // Các icon hiện / ẩn mật khẩu
    private ImageView imgOldEye;
    private ImageView imgNewEye;
    private ImageView imgConfirmEye;

    // SQLite
    private DBHelper dbHelper;

    // Phiên đăng nhập
    private SessionManager sessionManager;

    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);

        // Khởi tạo SQLite
        dbHelper = new DBHelper(this);

        // Quản lý đăng nhập
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

        edtOldPassword = findViewById(
                R.id.edtOldPassword
        );

        edtNewPassword = findViewById(
                R.id.edtNewPassword
        );

        edtConfirmPassword = findViewById(
                R.id.edtConfirmPassword
        );

        imgOldEye = findViewById(R.id.imgOldEye);

        imgNewEye = findViewById(R.id.imgNewEye);

        imgConfirmEye = findViewById(R.id.imgConfirmEye);

        // =====================================
        // NÚT QUAY LẠI
        // =====================================

        findViewById(R.id.btnBack).setOnClickListener(v -> {

            finish();
        });

        // =====================================
        // HIỆN / ẨN MẬT KHẨU
        // =====================================

        imgOldEye.setOnClickListener(v -> {

            togglePassword(edtOldPassword, imgOldEye);
        });

        imgNewEye.setOnClickListener(v -> {

            togglePassword(edtNewPassword, imgNewEye);
        });

        imgConfirmEye.setOnClickListener(v -> {

            togglePassword(
                    edtConfirmPassword,
                    imgConfirmEye
            );
        });

        // =====================================
        // NÚT CẬP NHẬT MẬT KHẨU
        // =====================================

        findViewById(R.id.btnChangePassword)
                .setOnClickListener(v -> {

                    changePassword();
                });
    }

    // =====================================
    // 1. XỬ LÝ ĐỔI MẬT KHẨU
    // =====================================

    private void changePassword() {

        String oldPassword = edtOldPassword
                .getText()
                .toString();

        String newPassword = edtNewPassword
                .getText()
                .toString();

        String confirmPassword = edtConfirmPassword
                .getText()
                .toString();

        // Kiểm tra mật khẩu hiện tại
        if (oldPassword.isEmpty()) {

            edtOldPassword.setError(
                    "Vui lòng nhập mật khẩu hiện tại"
            );

            edtOldPassword.requestFocus();

            return;
        }

        // Kiểm tra mật khẩu mới
        if (newPassword.length() < 8) {

            edtNewPassword.setError(
                    "Mật khẩu mới phải có ít nhất 8 ký tự"
            );

            edtNewPassword.requestFocus();

            return;
        }

        // Không dùng lại mật khẩu cũ
        if (newPassword.equals(oldPassword)) {

            edtNewPassword.setError(
                    "Mật khẩu mới phải khác mật khẩu cũ"
            );

            edtNewPassword.requestFocus();

            return;
        }

        // Kiểm tra xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {

            edtConfirmPassword.setError(
                    "Xác nhận mật khẩu không khớp"
            );

            edtConfirmPassword.requestFocus();

            return;
        }

        // =====================================
        // KIỂM TRA VÀ CẬP NHẬT SQLITE
        // =====================================

        boolean result = dbHelper.changePassword(
                userId,
                oldPassword,
                newPassword
        );

        if (result) {

            Toast.makeText(
                    this,
                    "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                    Toast.LENGTH_LONG
            ).show();

            // Xóa phiên đăng nhập
            sessionManager.logout();

            // Chuyển về màn hình đăng nhập
            returnToLogin();

        } else {

            edtOldPassword.setError(
                    "Mật khẩu hiện tại không đúng"
            );

            edtOldPassword.requestFocus();
        }
    }

    // =====================================
    // 2. HIỆN / ẨN MẬT KHẨU
    // =====================================

    private void togglePassword(
            EditText editText,
            ImageView imageView
    ) {

        boolean isHidden =
                editText.getTransformationMethod()
                        instanceof PasswordTransformationMethod;

        if (isHidden) {

            // Hiện mật khẩu
            editText.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            imageView.setImageResource(
                    R.drawable.ic_eye
            );

        } else {

            // Ẩn mật khẩu
            editText.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            imageView.setImageResource(
                    R.drawable.ic_eye_off
            );
        }

        // Giữ vị trí con trỏ
        editText.setSelection(
                editText.getText().length()
        );
    }

    // =====================================
    // 3. QUAY VỀ ĐĂNG NHẬP
    // =====================================

    private void returnToLogin() {

        Intent intent = new Intent(
                ChangePassword.this,
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