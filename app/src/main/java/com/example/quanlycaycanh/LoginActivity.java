package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.admin.AdminActivity;
import com.example.quanlycaycanh.customer.CustomerActivity;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/** UC01 — Đăng nhập; rẽ hai nhánh theo vai trò (sitemap chương 4). */
public class LoginActivity extends AppCompatActivity {

    private Repository repo;
    private Session session;
    private TextInputEditText edTaiKhoan;
    private TextInputEditText edMatKhau;
    private CheckBox cbGhiNho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Ui.anToan(this);

        repo = new Repository(this);
        session = new Session(this);
        edTaiKhoan = findViewById(R.id.edTaiKhoan);
        edMatKhau = findViewById(R.id.edMatKhau);
        cbGhiNho = findViewById(R.id.cbGhiNho);
        MaterialButton btnDangNhap = findViewById(R.id.btnDangNhap);
        MaterialButton btnDangKy = findViewById(R.id.btnDangKy);
        TextView tvQuenMatKhau = findViewById(R.id.tvQuenMatKhau);

        if (session.daDangNhap() && session.ghiNho()) {
            TaiKhoan tk = repo.user(session.maTaiKhoan());
            if (tk != null) {
                moTrangChu(tk);
                return;
            }
        }

        btnDangNhap.setOnClickListener(v -> dangNhap());
        btnDangKy.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvQuenMatKhau.setOnClickListener(v -> hoiQuenMatKhau());
    }

    private void dangNhap() {
        String taiKhoan = text(edTaiKhoan);
        String matKhau = text(edMatKhau);
        if (TextUtils.isEmpty(taiKhoan) || TextUtils.isEmpty(matKhau)) {
            Ui.toast(this, "Nhập email/số điện thoại và mật khẩu");
            return;
        }
        TaiKhoan tk = repo.login(taiKhoan, matKhau);
        if (tk == null) {
            Ui.toast(this, "Sai tài khoản hoặc mật khẩu");
            return;
        }
        session.dangNhap(tk.maTaiKhoan, cbGhiNho.isChecked());
        moTrangChu(tk);
    }

    private void moTrangChu(TaiKhoan tk) {
        Intent i = new Intent(this, tk.isAdmin() ? AdminActivity.class : CustomerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void hoiQuenMatKhau() {
        new AlertDialog.Builder(this)
                .setTitle("Quên mật khẩu")
                .setMessage("Hệ thống sẽ gửi liên kết đặt lại mật khẩu tới email đã đăng ký.\n\n"
                        + "Bản demo chưa gửi mail — vui lòng liên hệ cửa hàng: 0901 234 567.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
