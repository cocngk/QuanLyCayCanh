package com.example.quanlycaycanh.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.quanlycaycanh.LoginActivity;
import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;

/** 5.2 (mục Tài khoản) — hồ sơ, đổi mật khẩu, đăng xuất. */
public class AccountFragment extends Fragment {

    private Repository repo;
    private Session session;
    private TaiKhoan tk;
    private TextView tvHoTen;
    private TextView tvEmail;
    private TextView tvVaiTro;
    private TextView tvSoDienThoai;
    private TextView tvDiaChi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());
        tvHoTen = v.findViewById(R.id.tvHoTen);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvVaiTro = v.findViewById(R.id.tvVaiTro);
        tvSoDienThoai = v.findViewById(R.id.tvSoDienThoai);
        tvDiaChi = v.findViewById(R.id.tvDiaChi);

        v.findViewById(R.id.btnCapNhat).setOnClickListener(x -> suaThongTin());
        v.findViewById(R.id.btnDoiMatKhau).setOnClickListener(x -> doiMatKhau());
        v.findViewById(R.id.btnTroChuyen).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), ChatActivity.class)
                        .putExtra(ChatActivity.EXTRA_KHACH, session.maTaiKhoan())));
        v.findViewById(R.id.btnDonHang).setOnClickListener(x ->
                ((CustomerActivity) requireActivity()).chonTabDonHang());
        v.findViewById(R.id.btnDangXuat).setOnClickListener(x -> dangXuat());
    }

    @Override
    public void onResume() {
        super.onResume();
        tk = repo.user(session.maTaiKhoan());
        if (tk == null) {
            return;
        }
        tvHoTen.setText(tk.hoTen);
        tvEmail.setText(tk.email);
        tvVaiTro.setText(tk.isAdmin() ? "Quản trị viên" : "Khách hàng");
        tvSoDienThoai.setText("Số điện thoại: "
                + (TextUtils.isEmpty(tk.soDienThoai) ? "chưa cập nhật" : tk.soDienThoai));
        tvDiaChi.setText("Địa chỉ: "
                + (TextUtils.isEmpty(tk.diaChi) ? "chưa cập nhật" : tk.diaChi));
    }

    private EditText oNhap(String goiY, String giaTri) {
        EditText e = new EditText(requireContext());
        e.setHint(goiY);
        e.setText(giaTri == null ? "" : giaTri);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        return e;
    }

    private TextView nhan(String chu) {
        TextView tv = new TextView(requireContext());
        tv.setText(chu);
        tv.setTextSize(12);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        tv.setPadding(0, Ui.dp(requireContext(), 10), 0, 0);
        return tv;
    }

    private void suaThongTin() {
        if (tk == null) {
            return;
        }
        LinearLayout khung = new LinearLayout(requireContext());
        khung.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(requireContext(), 20);
        khung.setPadding(p, Ui.dp(requireContext(), 8), p, 0);
        EditText hoTen = oNhap("Họ và tên", tk.hoTen);
        EditText sdt = oNhap("Số điện thoại", tk.soDienThoai);
        sdt.setInputType(InputType.TYPE_CLASS_PHONE);
        EditText diaChi = oNhap("Địa chỉ", tk.diaChi);
        khung.addView(nhan("Họ và tên"));
        khung.addView(hoTen);
        khung.addView(nhan("Số điện thoại"));
        khung.addView(sdt);
        khung.addView(nhan("Địa chỉ"));
        khung.addView(diaChi);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật thông tin")
                .setView(khung)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Lưu", (d, w) -> {
                    tk.hoTen = hoTen.getText().toString().trim();
                    tk.soDienThoai = sdt.getText().toString().trim();
                    tk.diaChi = diaChi.getText().toString().trim();
                    repo.updateProfile(tk);
                    Ui.toast(requireContext(), "Đã lưu thông tin");
                    onResume();
                })
                .show();
    }

    private void doiMatKhau() {
        LinearLayout khung = new LinearLayout(requireContext());
        khung.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(requireContext(), 20);
        khung.setPadding(p, Ui.dp(requireContext(), 8), p, 0);
        EditText cu = oNhap("Mật khẩu hiện tại", "");
        cu.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText moi = oNhap("Mật khẩu mới", "");
        moi.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        khung.addView(cu);
        khung.addView(moi);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(khung)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Đổi", (d, w) -> {
                    String cuS = cu.getText().toString().trim();
                    String moiS = moi.getText().toString().trim();
                    if (moiS.length() < 6) {
                        Ui.toast(requireContext(), "Mật khẩu mới tối thiểu 6 ký tự");
                        return;
                    }
                    if (repo.changePassword(session.maTaiKhoan(), cuS, moiS)) {
                        Ui.toast(requireContext(), "Đã đổi mật khẩu");
                    } else {
                        Ui.toast(requireContext(), "Mật khẩu hiện tại không đúng");
                    }
                })
                .show();
    }

    private void dangXuat() {
        new AlertDialog.Builder(requireContext())
                .setMessage("Đăng xuất khỏi tài khoản?")
                .setNegativeButton("Ở lại", null)
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    session.dangXuat();
                    startActivity(new Intent(requireContext(), LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    requireActivity().finish();
                })
                .show();
    }
}
