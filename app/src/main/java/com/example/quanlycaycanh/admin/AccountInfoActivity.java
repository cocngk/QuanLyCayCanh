package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/** Chỉnh sửa tên, tên đăng nhập, email và số điện thoại quản trị viên. */
public class AccountInfoActivity extends AppCompatActivity {

    private Repository repo;
    private Session session;
    private TextInputEditText edHoTen;
    private TextInputEditText edTenDangNhap;
    private TextInputEditText edEmail;
    private TextInputEditText edSoDienThoai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        Ui.anToan(this);

        repo = new Repository(this);
        session = new Session(this);
        edHoTen = findViewById(R.id.edHoTen);
        edTenDangNhap = findViewById(R.id.edTenDangNhap);
        edEmail = findViewById(R.id.edEmail);
        edSoDienThoai = findViewById(R.id.edSoDienThoai);

        ImageButton back = findViewById(R.id.btnQuayLai);
        MaterialButton save = findViewById(R.id.btnLuuThongTin);
        back.setOnClickListener(v -> finish());
        save.setOnClickListener(v -> luuThongTin());
        hienThiTaiKhoan();
    }

    private void hienThiTaiKhoan() {
        TaiKhoan user = repo.user(session.maTaiKhoan());
        if (user == null) {
            finish();
            return;
        }
        edHoTen.setText(user.hoTen);
        edTenDangNhap.setText(user.tenDangNhap);
        edEmail.setText(user.email);
        edSoDienThoai.setText(user.soDienThoai);
    }

    private void luuThongTin() {
        String hoTen = text(edHoTen);
        String username = text(edTenDangNhap).toLowerCase(java.util.Locale.ROOT);
        String email = text(edEmail).toLowerCase(java.util.Locale.ROOT);
        String phone = text(edSoDienThoai).replaceAll("[^0-9+]", "");
        String digits = phone.replaceAll("\\D", "");

        if (TextUtils.isEmpty(hoTen)) {
            edHoTen.setError("Nhập họ và tên");
            edHoTen.requestFocus();
            return;
        }
        if (!username.matches("[a-zA-Z0-9._-]{3,24}")) {
            edTenDangNhap.setError("Tên đăng nhập cần từ 3 đến 24 ký tự hợp lệ");
            edTenDangNhap.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Email chưa hợp lệ");
            edEmail.requestFocus();
            return;
        }
        if (digits.length() < 9 || digits.length() > 12) {
            edSoDienThoai.setError("Số điện thoại cần có từ 9 đến 12 chữ số");
            edSoDienThoai.requestFocus();
            return;
        }

        if (!repo.updateAccount(session.maTaiKhoan(), hoTen, username, email, phone)) {
            Ui.toast(this, "Tên đăng nhập, email hoặc số điện thoại đã được sử dụng");
            return;
        }
        Ui.toast(this, "Đã lưu thông tin cá nhân");
        setResult(RESULT_OK);
        finish();
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
