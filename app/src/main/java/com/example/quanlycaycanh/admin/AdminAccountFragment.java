package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.quanlycaycanh.LoginActivity;
import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;

/** Hồ sơ quản trị viên, thông tin cá nhân, đổi mật khẩu và đăng xuất. */
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

        configureAction(view, R.id.btnThongTinCaNhan, "Thông tin cá nhân",
                "Xem và cập nhật thông tin cá nhân", R.drawable.ic_person);
        configureAction(view, R.id.btnDoiMatKhau, "Đổi mật khẩu",
                "Thay đổi mật khẩu đăng nhập", R.drawable.ic_lock);
        configureAction(view, R.id.btnThongBao, "Thông báo",
                "Cài đặt và xem thông báo hệ thống", R.drawable.ic_notifications);
        configureAction(view, R.id.btnGioiThieu, "Giới thiệu ứng dụng",
                "Thông tin và chức năng của ứng dụng", R.drawable.ic_info);

        view.findViewById(R.id.btnThongTinCaNhan).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AccountInfoActivity.class)));
        view.findViewById(R.id.btnDoiMatKhau).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        view.findViewById(R.id.btnThongBao).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Thông báo")
                        .setMessage("Hiện chưa có thông báo mới.")
                        .setPositiveButton("Đóng", null)
                        .show());
        view.findViewById(R.id.btnGioiThieu).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutActivity.class)));
        view.findViewById(R.id.btnAdminDangXuat).setOnClickListener(v -> xacNhanDangXuat());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && repo != null) {
            napThongTin(getView());
        }
    }

    private void configureAction(View parent, int cardId, String title, String subtitle,
                                 int iconRes) {
        View card = parent.findViewById(cardId);
        View content = ((ViewGroup) card).getChildAt(0);
        ImageView icon = content.findViewById(R.id.imgAction);
        icon.setImageResource(iconRes);
        ((TextView) content.findViewById(R.id.tvActionTitle)).setText(title);
        ((TextView) content.findViewById(R.id.tvActionSubtitle)).setText(subtitle);
    }

    private void napThongTin(View view) {
        TaiKhoan admin = repo.user(session.maTaiKhoan());
        if (admin == null) {
            return;
        }
        ((TextView) view.findViewById(R.id.tvAdminName)).setText(admin.hoTen);
        ((TextView) view.findViewById(R.id.tvAdminUsername))
                .setText("@" + (admin.tenDangNhap == null ? "" : admin.tenDangNhap));
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
}
