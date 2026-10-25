package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.customer.CustomerActivity;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/** UC02 — Đăng ký tài khoản khách hàng. */
public class RegisterActivity extends AppCompatActivity {

    private Repository repo;
    private TextInputEditText edHoTen;
    private TextInputEditText edEmail;
    private TextInputEditText edSoDienThoai;
    private TextInputEditText edDiaChi;
    private TextInputEditText edMatKhau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Ui.anToan(this);

        repo = new Repository(this);
        edHoTen = findViewById(R.id.edHoTen);
        edEmail = findViewById(R.id.edEmail);
        edSoDienThoai = findViewById(R.id.edSoDienThoai);
        edDiaChi = findViewById(R.id.edDiaChi);
        edMatKhau = findViewById(R.id.edMatKhau);

        MaterialButton btnTao = findViewById(R.id.btnTaoTaiKhoan);
        MaterialButton btnQuayLai = findViewById(R.id.btnQuayLai);
        btnTao.setOnClickListener(v -> taoTaiKhoan());
        btnQuayLai.setOnClickListener(v -> finish());
    }

    private void taoTaiKhoan() {
        String hoTen = text(edHoTen);
        String email = text(edEmail);
        String sdt = text(edSoDienThoai);
        String diaChi = text(edDiaChi);
        String matKhau = text(edMatKhau);

        if (TextUtils.isEmpty(hoTen) || TextUtils.isEmpty(email) || TextUtils.isEmpty(matKhau)) {
            Ui.toast(this, "Nhập họ tên, email và mật khẩu");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Ui.toast(this, "Email không hợp lệ");
            return;
        }
        if (matKhau.length() < 6) {
            Ui.toast(this, "Mật khẩu tối thiểu 6 ký tự");
            return;
        }
        if (repo.emailExists(email)) {
            Ui.toast(this, "Email đã được đăng ký");
            return;
        }
        long id = repo.register(hoTen, email, sdt, diaChi, matKhau);
        if (id <= 0) {
            Ui.toast(this, "Không tạo được tài khoản");
            return;
        }
        new Session(this).dangNhap((int) id, true);
        TaiKhoan moi = repo.user((int) id);
        Ui.toast(this, "Chào " + (moi == null ? hoTen : moi.hoTen) + "!");
        startActivity(new Intent(this, CustomerActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
