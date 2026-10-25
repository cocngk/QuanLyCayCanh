package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.LoginActivity;
import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.customer.OrderAdapter;
import com.example.quanlycaycanh.customer.OrderDetailActivity;
import com.example.quanlycaycanh.data.DonHang;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;

import java.util.List;

/** 5.7 — Trang quản trị: 4 thẻ số liệu, đơn gần đây, sản phẩm sắp hết. */
public class AdminHomeFragment extends Fragment {

    private Repository repo;
    private Session session;
    private OrderAdapter orderAdapter;
    private TextView tvTongSanPham;
    private TextView tvTongDon;
    private TextView tvDoanhThu;
    private TextView tvTonThap;
    private LinearLayout khungTonThap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());
        tvTongSanPham = v.findViewById(R.id.tvTongSanPham);
        tvTongDon = v.findViewById(R.id.tvTongDon);
        tvDoanhThu = v.findViewById(R.id.tvDoanhThu);
        tvTonThap = v.findViewById(R.id.tvTonThap);
        khungTonThap = v.findViewById(R.id.khungTonThap);

        TaiKhoan tk = repo.user(session.maTaiKhoan());
        TextView tvChao = v.findViewById(R.id.tvChao);
        if (tk != null) {
            String ten = tk.hoTen == null ? "" : tk.hoTen.trim();
            int space = ten.lastIndexOf(' ');
            if (space > 0) {
                ten = ten.substring(space + 1);
            }
            tvChao.setText("Xin chào, " + ten);
        }

        v.findViewById(R.id.btnDangXuat).setOnClickListener(x -> new AlertDialog.Builder(requireContext())
                .setMessage("Đăng xuất khỏi tài khoản quản trị?")
                .setNegativeButton("Ở lại", null)
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    session.dangXuat();
                    startActivity(new Intent(requireContext(), LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    requireActivity().finish();
                })
                .show());

        RecyclerView rv = v.findViewById(R.id.rvDonGanDay);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        orderAdapter = new OrderAdapter(true, don -> {
            Intent i = new Intent(requireContext(), OrderDetailActivity.class);
            i.putExtra(OrderDetailActivity.EXTRA_MA_DON, (long) don.maDonHang);
            i.putExtra(OrderDetailActivity.EXTRA_ADMIN, true);
            startActivity(i);
        });
        rv.setAdapter(orderAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded()) {
            return;
        }
        tvTongSanPham.setText(String.valueOf(repo.countProducts()));
        tvTongDon.setText(String.valueOf(repo.countOrders()));
        tvDoanhThu.setText(Ui.vnd(repo.totalRevenue()));

        List<SanPham> sapHet = repo.lowStock(Repository.NGUONG_TON_THAP);
        tvTonThap.setText(String.valueOf(sapHet.size()));
        orderAdapter.submit(repo.recentOrders(5));

        khungTonThap.removeAllViews();
        if (sapHet.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Không có sản phẩm nào dưới ngưỡng " + Repository.NGUONG_TON_THAP);
            tv.setTextColor(requireContext().getColor(R.color.muted));
            khungTonThap.addView(tv);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SanPham p : sapHet) {
            View row = inflater.inflate(R.layout.item_pick_product, khungTonThap, false);
            ((TextView) row.findViewById(R.id.tvEmoji)).setText(p.hinhAnh);
            ((TextView) row.findViewById(R.id.tvTen)).setText(p.tenCay);
            ((TextView) row.findViewById(R.id.tvGiaTon)).setText("Chỉ còn " + p.soLuongTon + " cây");
            row.findViewById(R.id.btnGiam).setVisibility(View.GONE);
            row.findViewById(R.id.btnTang).setVisibility(View.GONE);
            row.findViewById(R.id.tvSoLuong).setVisibility(View.GONE);
            khungTonThap.addView(row);
        }
    }
}
