package com.example.quanlycaycanh;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class Register extends BaseActivity {

    private EditText edtFullName;
    private EditText edtContact;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private Button btnRegister;

    private TextView tvLogin;

    private ImageView imgEye;
    private ImageView imgEyeConfirm;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // Khởi tạo SQLite
        dbHelper = new DBHelper(this);

        // Ánh xạ XML
        edtFullName = findViewById(R.id.edtFullName);

        edtContact = findViewById(R.id.edtContact);

        edtPassword = findViewById(R.id.edtPassword);

        edtConfirmPassword =
                findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        tvLogin = findViewById(R.id.tvLogin);

        imgEye = findViewById(R.id.imgEye);

        imgEyeConfirm = findViewById(R.id.imgEyeConfirm);

        // ===================================
        // NÚT ĐĂNG KÝ
        // ===================================

        btnRegister.setOnClickListener(v -> {

            // Lấy dữ liệu
            String fullName =
                    edtFullName.getText()
                            .toString()
                            .trim();

            String contact =
                    edtContact.getText()
                            .toString()
                            .trim();

            String password =
                    edtPassword.getText()
                            .toString();

            String confirmPassword =
                    edtConfirmPassword.getText()
                            .toString();

            // Kiểm tra họ tên
            if (fullName.isEmpty()) {

                edtFullName.setError(
                        "Vui lòng nhập họ và tên"
                );

                edtFullName.requestFocus();

                return;
            }

            // Kiểm tra email / số điện thoại
            if (contact.isEmpty()) {

                edtContact.setError(
                        "Vui lòng nhập email hoặc số điện thoại"
                );

                edtContact.requestFocus();

                return;
            }

            // Kiểm tra định dạng email / SĐT
            if (!DBHelper.isValidContact(contact)) {

                edtContact.setError(
                        "Nhập email hợp lệ hoặc SĐT 10 chữ số bắt đầu bằng 0"
                );

                edtContact.requestFocus();

                return;
            }

            // Kiểm tra mật khẩu
            if (password.length() < 8) {

                edtPassword.setError(
                        "Mật khẩu phải có ít nhất 8 ký tự"
                );

                edtPassword.requestFocus();

                return;
            }

            // Kiểm tra xác nhận mật khẩu
            if (!password.equals(confirmPassword)) {

                edtConfirmPassword.setError(
                        "Xác nhận mật khẩu không khớp"
                );

                edtConfirmPassword.requestFocus();

                return;
            }

            // Kiểm tra email hoặc SĐT đã đăng ký
            if (dbHelper.contactExists(contact)) {

                edtContact.setError(
                        "Email hoặc số điện thoại này đã được đăng ký"
                );

                edtContact.requestFocus();

                return;
            }

            // ===================================
            // LƯU TÀI KHOẢN VÀO SQLITE
            // ===================================

            long result = dbHelper.addUser(
                    fullName,
                    contact,
                    password
            );

            if (result > 0) {

                Toast.makeText(
                        Register.this,
                        "Đăng ký thành công!",
                        Toast.LENGTH_LONG
                ).show();

                // Quay về trang đăng nhập
                finish();

            } else {

                Toast.makeText(
                        Register.this,
                        "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // ===================================
        // QUAY LẠI ĐĂNG NHẬP
        // ===================================

        tvLogin.setOnClickListener(v -> {
            finish();
        });

        // ===================================
        // HIỆN / ẨN MẬT KHẨU
        // ===================================

        imgEye.setOnClickListener(v -> {

            togglePassword(
                    edtPassword,
                    imgEye
            );
        });

        imgEyeConfirm.setOnClickListener(v -> {

            togglePassword(
                    edtConfirmPassword,
                    imgEyeConfirm
            );
        });
    }

    // Hàm hiện hoặc ẩn mật khẩu
    private void togglePassword(
            EditText editText,
            ImageView imageView
    ) {

        boolean hidden =
                editText.getTransformationMethod()
                        instanceof PasswordTransformationMethod;

        if (hidden) {

            editText.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            imageView.setImageResource(
                    R.drawable.ic_eye
            );

        } else {

            editText.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            imageView.setImageResource(
                    R.drawable.ic_eye_off
            );
        }

        editText.setSelection(
                editText.getText().length()
        );
    }
}