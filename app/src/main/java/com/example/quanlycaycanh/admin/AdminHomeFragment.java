package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;

import java.util.List;

/** 5.7 — Trang quản trị: 4 thẻ số liệu, đơn gần đây, sản phẩm sắp hết. */
public class AdminHomeFragment extends Fragment {

    private Repository repo;
    private OrderAdapter orderAdapter;
    private TextView tvTongSanPham;
    private TextView tvTongDon;
    private TextView tvDoanhThu;
    private TextView tvTonKho;
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
        tvTongSanPham = v.findViewById(R.id.tvTongSanPham);
        tvTongDon = v.findViewById(R.id.tvTongDon);
        tvDoanhThu = v.findViewById(R.id.tvDoanhThu);
        tvTonKho = v.findViewById(R.id.tvTonKho);
        khungTonThap = v.findViewById(R.id.khungTonThap);

        AdminActivity activity = (AdminActivity) requireActivity();
        v.findViewById(R.id.btnTaiKhoanNhanh).setOnClickListener(x -> activity.chonTabTaiKhoan());
        v.findViewById(R.id.btnTaiKhoanNhanhText).setOnClickListener(x -> activity.chonTabTaiKhoan());
        v.findViewById(R.id.btnQuanLyCayNhanh).setOnClickListener(x ->
                activity.moTab(R.id.tab_admin_products));
        v.findViewById(R.id.btnTaoHoaDonNhanh).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), CreateOrderActivity.class)));
        v.findViewById(R.id.btnQuanLyHoaDonNhanh).setOnClickListener(x -> activity.chonTabDonHang());
        v.findViewById(R.id.btnThongKeNhanh).setOnClickListener(x ->
                activity.moTab(R.id.tab_stats));
        android.widget.EditText edTimNhanh = v.findViewById(R.id.edTimNhanh);
        edTimNhanh.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                activity.moTimSanPham(edTimNhanh.getText().toString().trim());
                return true;
            }
            return false;
        });

        RecyclerView rv = v.findViewById(R.id.rvDonGanDay);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        orderAdapter = new OrderAdapter(don -> {
            Intent i = new Intent(requireContext(), OrderDetailActivity.class);
            i.putExtra(OrderDetailActivity.EXTRA_MA_DON, (long) don.maDonHang);
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
        tvTonKho.setText(String.valueOf(repo.totalStock()));
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
            Ui.hienAnhCay(requireContext(), p.hinhAnh,
                    row.findViewById(R.id.imgPlant), row.findViewById(R.id.tvPhotoFallback), p.maLoai);
            ((TextView) row.findViewById(R.id.tvTen)).setText(p.tenCay);
            ((TextView) row.findViewById(R.id.tvGiaTon)).setText("Chỉ còn " + p.soLuongTon + " cây");
            row.findViewById(R.id.btnGiam).setVisibility(View.GONE);
            row.findViewById(R.id.btnTang).setVisibility(View.GONE);
            row.findViewById(R.id.tvSoLuong).setVisibility(View.GONE);
            khungTonThap.addView(row);
        }
    }
}
