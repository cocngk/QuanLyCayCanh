package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.admin.AdminActivity;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/** Đăng nhập dành riêng cho quản trị viên. */
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
        TextView tvDangKy = findViewById(R.id.tvDangKy);

        if (session.daDangNhap() && session.ghiNho()) {
            TaiKhoan tk = repo.user(session.maTaiKhoan());
            if (tk != null) {
                moTrangChu();
                return;
            }
            session.dangXuat();
        }

        btnDangNhap.setOnClickListener(v -> dangNhap());
        tvDangKy.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void dangNhap() {
        String taiKhoan = text(edTaiKhoan);
        String matKhau = text(edMatKhau);
        if (TextUtils.isEmpty(taiKhoan) || TextUtils.isEmpty(matKhau)) {
            Ui.toast(this, "Nhập tài khoản quản trị và mật khẩu");
            return;
        }
        TaiKhoan tk = repo.login(taiKhoan, matKhau);
        if (tk == null) {
            Ui.toast(this, "Sai tài khoản hoặc mật khẩu");
            return;
        }
        session.dangNhap(tk.maTaiKhoan, cbGhiNho.isChecked());
        moTrangChu();
    }

    private void moTrangChu() {
        Intent i = new Intent(this, AdminActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
