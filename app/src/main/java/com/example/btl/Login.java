
package com.example.btl;

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

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

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

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Kiểm tra tài khoản đã ghi nhớ
        long savedUserId = sessionManager.getUserId();

        if (sessionManager.isRemembered()
                && savedUserId > 0
                && dbHelper.getUser(savedUserId) != null) {

            openAccount();
            return;
        }

        sessionManager.logout();

        // Ánh xạ giao diện
        edtContact = findViewById(R.id.edtContact);
        edtPassword = findViewById(R.id.edtPassword);
        chkRemember = findViewById(R.id.chkRemember);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        imgEye = findViewById(R.id.imgEye);
        updateRegistrationAvailability();

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {

            String contact = edtContact.getText()
                    .toString().trim();

            String password = edtPassword.getText()
                    .toString();

            if (contact.isEmpty()) {
                edtContact.setError(
                        "Vui lòng nhập email hoặc số điện thoại");
                edtContact.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError(
                        "Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            // Kiểm tra tài khoản trong SQLite
            long userId = dbHelper.authenticate(
                    contact, password);

            if (userId > 0) {

                sessionManager.login(
                        userId,
                        chkRemember.isChecked());

                Toast.makeText(
                        Login.this,
                        "Đăng nhập thành công!",
                        Toast.LENGTH_SHORT
                ).show();

                // Chuyển đến trang chủ
                openAccount();

            } else {

                Toast.makeText(
                        Login.this,
                        "Email, số điện thoại hoặc mật khẩu không đúng!",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Chuyển sang đăng ký
        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Login.this,
                    Register.class);

            startActivity(intent);
        });

        // Hiện / ẩn mật khẩu
        imgEye.setOnClickListener(v -> {

            boolean hidden =
                    edtPassword.getTransformationMethod()
                            instanceof PasswordTransformationMethod;

            if (hidden) {

                edtPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance());

                imgEye.setImageResource(R.drawable.ic_eye);

            } else {

                edtPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance());

                imgEye.setImageResource(R.drawable.ic_eye_off);
            }

            edtPassword.setSelection(
                    edtPassword.getText().length());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRegistrationAvailability();
    }

    private void updateRegistrationAvailability() {
        if (tvRegister == null || dbHelper == null) return;
        if (dbHelper.hasUsers()) {
            tvRegister.setVisibility(TextView.GONE);
        } else {
            tvRegister.setVisibility(TextView.VISIBLE);
            tvRegister.setText("Khởi tạo tài khoản quản trị");
        }
    }

    // MỞ TRANG CHỦ SAU ĐĂNG NHẬP
    private void openAccount() {

        Intent intent = new Intent(
                Login.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
}
