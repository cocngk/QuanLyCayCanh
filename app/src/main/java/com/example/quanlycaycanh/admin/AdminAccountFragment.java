package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.quanlycaycanh.LoginActivity;
import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.textfield.TextInputLayout;

/** Hồ sơ quản trị viên, đổi mật khẩu và đăng xuất. */
public class AdminAccountFragment extends Fragment {

    private Repository repo;
    private Session session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());
        napThongTin(view);
        view.findViewById(R.id.btnDoiMatKhau).setOnClickListener(v -> hienDialogDoiMatKhau());
        view.findViewById(R.id.btnAdminDangXuat).setOnClickListener(v -> xacNhanDangXuat());
    }

    private void napThongTin(View view) {
        TaiKhoan admin = repo.user(session.maTaiKhoan());
        if (admin == null) {
            return;
        }
        ((TextView) view.findViewById(R.id.tvAdminName)).setText(admin.hoTen);
        ((TextView) view.findViewById(R.id.tvAdminEmail)).setText(giaTri(admin.email));
        ((TextView) view.findViewById(R.id.tvAdminPhone)).setText(giaTri(admin.soDienThoai));
    }

    private void hienDialogDoiMatKhau() {
        LinearLayout fields = new LinearLayout(requireContext());
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(Ui.dp(requireContext(), 24), Ui.dp(requireContext(), 8),
                Ui.dp(requireContext(), 24), 0);
        EditText oldPassword = themTruong(fields, "Mật khẩu hiện tại");
        EditText newPassword = themTruong(fields, "Mật khẩu mới");
        EditText confirmPassword = themTruong(fields, "Nhập lại mật khẩu mới");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(fields)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Cập nhật", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(button -> {
                    String oldValue = oldPassword.getText().toString();
                    String newValue = newPassword.getText().toString();
                    String confirmValue = confirmPassword.getText().toString();
                    if (newValue.length() < 6) {
                        Ui.toast(requireContext(), "Mật khẩu mới cần ít nhất 6 ký tự");
                    } else if (!newValue.equals(confirmValue)) {
                        Ui.toast(requireContext(), "Mật khẩu nhập lại chưa khớp");
                    } else if (!repo.changePassword(session.maTaiKhoan(), oldValue, newValue)) {
                        Ui.toast(requireContext(), "Mật khẩu hiện tại không đúng");
                    } else {
                        Ui.toast(requireContext(), "Đã cập nhật mật khẩu");
                        dialog.dismiss();
                    }
                }));
        dialog.show();
    }

    private EditText themTruong(LinearLayout parent, String hint) {
        TextInputLayout input = new TextInputLayout(requireContext());
        input.setHint(hint);
        input.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = Ui.dp(requireContext(), 8);
        input.setLayoutParams(params);
        EditText edit = new EditText(requireContext());
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.addView(edit);
        parent.addView(input);
        return edit;
    }

    private void xacNhanDangXuat() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn muốn đăng xuất khỏi tài khoản quản trị?")
                .setNegativeButton("Ở lại", null)
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    session.dangXuat();
                    startActivity(new Intent(requireContext(), LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    requireActivity().finish();
                })
                .show();
    }

    private static String giaTri(String value) {
        return value == null || value.trim().isEmpty() ? "Chưa cập nhật" : value;
    }
}
