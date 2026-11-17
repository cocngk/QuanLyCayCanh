package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.admin.AdminActivity;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/** Đăng ký tài khoản quản trị viên mới trên thiết bị. */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edHoTen;
    private TextInputEditText edTenDangNhap;
    private TextInputEditText edEmail;
    private TextInputEditText edSoDienThoai;
    private TextInputEditText edMatKhau;
    private TextInputEditText edXacNhanMatKhau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Ui.anToan(this);

        edHoTen = findViewById(R.id.edHoTen);
        edTenDangNhap = findViewById(R.id.edTenDangNhap);
        edEmail = findViewById(R.id.edEmail);
        edSoDienThoai = findViewById(R.id.edSoDienThoai);
        edMatKhau = findViewById(R.id.edMatKhau);
        edXacNhanMatKhau = findViewById(R.id.edXacNhanMatKhau);

        MaterialButton btnDangKy = findViewById(R.id.btnDangKy);
        TextView tvDangNhap = findViewById(R.id.tvDangNhap);
        btnDangKy.setOnClickListener(v -> dangKy());
        tvDangNhap.setOnClickListener(v -> finish());
    }

    private void dangKy() {
        String hoTen = text(edHoTen);
        String username = text(edTenDangNhap).toLowerCase(java.util.Locale.ROOT);
        String email = text(edEmail).toLowerCase(java.util.Locale.ROOT);
        String phone = text(edSoDienThoai).replaceAll("[^0-9+]", "");
        String phoneDigits = phone.replaceAll("\\D", "");
        String password = text(edMatKhau);
        String confirm = text(edXacNhanMatKhau);

        if (TextUtils.isEmpty(hoTen)) {
            edHoTen.setError("Nhập họ và tên");
            edHoTen.requestFocus();
            return;
        }
        if (!username.matches("[a-zA-Z0-9._-]{3,24}")) {
            edTenDangNhap.setError("Tên đăng nhập gồm 3–24 ký tự, chữ, số, dấu chấm hoặc gạch");
            edTenDangNhap.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Email chưa hợp lệ");
            edEmail.requestFocus();
            return;
        }
        if (phoneDigits.length() < 9 || phoneDigits.length() > 12) {
            edSoDienThoai.setError("Số điện thoại cần có từ 9 đến 12 chữ số");
            edSoDienThoai.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edMatKhau.setError("Mật khẩu cần ít nhất 6 ký tự");
            edMatKhau.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            edXacNhanMatKhau.setError("Mật khẩu nhập lại chưa khớp");
            edXacNhanMatKhau.requestFocus();
            return;
        }

        long id = new Repository(this).registerAdmin(hoTen, username, email, phone, password);
        if (id < 1) {
            Ui.toast(this, "Tên đăng nhập, email hoặc số điện thoại đã được sử dụng");
            return;
        }

        new Session(this).dangNhap((int) id, true);
        Ui.toast(this, "Đã tạo tài khoản quản trị");
        Intent intent = new Intent(this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
