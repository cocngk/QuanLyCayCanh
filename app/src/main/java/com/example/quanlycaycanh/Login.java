package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class Login extends BaseActivity {

    private EditText edtContact;
    private EditText edtPassword;

    private CheckBox chkRemember;

    private Button btnLogin;

    private TextView tvRegister;

    private ImageView imgEye;

    private DBHelper dbHelper;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Khởi tạo SQLite
        dbHelper = new DBHelper(this);

        // Quản lý phiên đăng nhập
        sessionManager = new SessionManager(this);

        // Kiểm tra ghi nhớ đăng nhập
        long savedUserId =
                sessionManager.getUserId();

        if (sessionManager.isRemembered()
                && savedUserId > 0
                && dbHelper.getUser(savedUserId) != null) {

            openHome();

            return;
        }

        // Xóa phiên cũ không được ghi nhớ
        sessionManager.logout();

        // ===================================
        // ÁNH XẠ XML
        // ===================================

        edtContact = findViewById(R.id.edtContact);

        edtPassword = findViewById(R.id.edtPassword);

        chkRemember = findViewById(R.id.chkRemember);

        btnLogin = findViewById(R.id.btnLogin);

        tvRegister = findViewById(R.id.tvRegister);

        imgEye = findViewById(R.id.imgEye);

        // ===================================
        // XỬ LÝ ĐĂNG NHẬP
        // ===================================

        btnLogin.setOnClickListener(v -> {

            String contact =
                    edtContact.getText()
                            .toString()
                            .trim();

            String password =
                    edtPassword.getText()
                            .toString();

            // Kiểm tra email hoặc SĐT
            if (contact.isEmpty()) {

                edtContact.setError(
                        "Vui lòng nhập email hoặc số điện thoại"
                );

                edtContact.requestFocus();

                return;
            }

            // Kiểm tra mật khẩu
            if (password.isEmpty()) {

                edtPassword.setError(
                        "Vui lòng nhập mật khẩu"
                );

                edtPassword.requestFocus();

                return;
            }

            // ===================================
            // KIỂM TRA SQLITE
            // ===================================

            long userId = dbHelper.authenticate(
                    contact,
                    password
            );

            if (userId > 0) {

                // Đăng nhập thành công
                sessionManager.login(
                        userId,
                        chkRemember.isChecked()
                );

                Toast.makeText(
                        Login.this,
                        "Đăng nhập thành công!",
                        Toast.LENGTH_SHORT
                ).show();

                openHome();

            } else {

                Toast.makeText(
                        Login.this,
                        "Email, số điện thoại hoặc mật khẩu không đúng!",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // ===================================
        // CHUYỂN SANG ĐĂNG KÝ
        // ===================================

        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Login.this,
                    Register.class
            );

            startActivity(intent);
        });

        // ===================================
        // HIỆN / ẨN MẬT KHẨU
        // ===================================

        imgEye.setOnClickListener(v -> {

            boolean hidden =
                    edtPassword.getTransformationMethod()
                            instanceof PasswordTransformationMethod;

            if (hidden) {

                edtPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );

                imgEye.setImageResource(
                        R.drawable.ic_eye
                );

            } else {

                edtPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );

                imgEye.setImageResource(
                        R.drawable.ic_eye_off
                );
            }

            edtPassword.setSelection(
                    edtPassword.getText().length()
            );
        });
    }

    // ===================================
    // MỞ TRANG SAU ĐĂNG NHẬP
    // ===================================

    private void openHome() {

        // Trang chủ tạm (MainActivity) giữ các lối vào chức năng.
        // Trang chủ chính thức là phần của Người 2.

        Intent intent = new Intent(
                Login.this,
                MainActivity.class
        );

        startActivity(intent);

        finish();
    }
}