package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/** Màn hình đổi mật khẩu tài khoản quản trị viên. */
public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText edMatKhauCu;
    private TextInputEditText edMatKhauMoi;
    private TextInputEditText edXacNhanMatKhau;
    private Repository repo;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Ui.anToan(this);

        repo = new Repository(this);
        session = new Session(this);
        edMatKhauCu = findViewById(R.id.edMatKhauCu);
        edMatKhauMoi = findViewById(R.id.edMatKhauMoi);
        edXacNhanMatKhau = findViewById(R.id.edXacNhanMatKhau);

        ImageButton back = findViewById(R.id.btnQuayLai);
        MaterialButton update = findViewById(R.id.btnCapNhatMatKhau);
        back.setOnClickListener(v -> finish());
        update.setOnClickListener(v -> doiMatKhau());
    }

    private void doiMatKhau() {
        String current = text(edMatKhauCu);
        String next = text(edMatKhauMoi);
        String confirm = text(edXacNhanMatKhau);
        if (TextUtils.isEmpty(current)) {
            edMatKhauCu.setError("Nhập mật khẩu hiện tại");
            edMatKhauCu.requestFocus();
            return;
        }
        if (next.length() < 6) {
            edMatKhauMoi.setError("Mật khẩu mới cần ít nhất 6 ký tự");
            edMatKhauMoi.requestFocus();
            return;
        }
        if (!next.equals(confirm)) {
            edXacNhanMatKhau.setError("Mật khẩu nhập lại chưa khớp");
            edXacNhanMatKhau.requestFocus();
            return;
        }
        if (!repo.changePassword(session.maTaiKhoan(), current, next)) {
            edMatKhauCu.setError("Mật khẩu hiện tại không đúng");
            edMatKhauCu.requestFocus();
            return;
        }
        Ui.toast(this, "Đã cập nhật mật khẩu");
        setResult(RESULT_OK);
        finish();
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}
