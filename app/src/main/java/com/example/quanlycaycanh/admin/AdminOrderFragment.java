package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.data.DonHang;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.Ui;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/** Tra cứu và cập nhật hóa đơn nội bộ; tạo hóa đơn tại quầy. */
public class AdminOrderFragment extends Fragment {

    private Repository repo;
    private OrderAdapter adapter;
    private String trangThai = "TAT_CA";
    private String tuKhoa = "";
    private TextView tvTongQuan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        tvTongQuan = v.findViewById(R.id.tvTongQuanHoaDon);

        RecyclerView rv = v.findViewById(R.id.rvDonHang);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(don -> {
            Intent i = new Intent(requireContext(), OrderDetailActivity.class);
            i.putExtra(OrderDetailActivity.EXTRA_MA_DON, (long) don.maDonHang);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        EditText edTim = v.findViewById(R.id.edTimDon);
        edTim.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tuKhoa = s.toString();
                nap();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ChipGroup chip = v.findViewById(R.id.chipTrangThai);
        themChip(chip, "Tất cả", "TAT_CA");
        themChip(chip, "Chờ xử lý", DonHang.CHO_XAC_NHAN);
        themChip(chip, "Đang xử lý", DonHang.DANG_GIAO);
        themChip(chip, "Đã hoàn tất", DonHang.DA_GIAO);
        themChip(chip, "Đã hủy", DonHang.DA_HUY);

        v.findViewById(R.id.btnTaoDon).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), CreateOrderActivity.class)));
    }

    private void themChip(ChipGroup nhom, String ten, String giaTri) {
        Chip chip = new Chip(requireContext());
        chip.setText(ten);
        chip.setCheckable(true);
        chip.setChecked(giaTri.equals(trangThai));
        chip.setOnClickListener(v -> {
            trangThai = giaTri;
            nap();
        });
        nhom.addView(chip);
    }

    @Override
    public void onResume() {
        super.onResume();
        nap();
    }

    private void nap() {
        if (!isAdded() || adapter == null) {
            return;
        }
        adapter.submit(repo.adminOrders(trangThai, tuKhoa));
        int completed = 0;
        int cancelled = 0;
        for (DonHang order : repo.adminOrders("TAT_CA", "")) {
            if (DonHang.DA_GIAO.equals(order.trangThai)) {
                completed++;
            } else if (DonHang.DA_HUY.equals(order.trangThai)) {
                cancelled++;
            }
        }
        tvTongQuan.setText("Hóa đơn: " + repo.countOrders()
                + "  ·  Hoàn thành: " + completed
                + "  ·  Đã hủy: " + cancelled
                + "\nDoanh thu hóa đơn hoàn tất: " + Ui.vnd(repo.totalRevenue()));
    }
}
