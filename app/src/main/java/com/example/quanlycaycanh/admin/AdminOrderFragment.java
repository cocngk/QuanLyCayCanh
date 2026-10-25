package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.customer.OrderAdapter;
import com.example.quanlycaycanh.customer.OrderDetailActivity;
import com.example.quanlycaycanh.data.DonHang;
import com.example.quanlycaycanh.data.Repository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/** UC23–UC27 — Quản lý đơn: tìm kiếm, xem chi tiết, cập nhật trạng thái, tạo đơn tại quầy. */
public class AdminOrderFragment extends Fragment {

    private Repository repo;
    private OrderAdapter adapter;
    private String trangThai = "TAT_CA";
    private String tuKhoa = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());

        RecyclerView rv = v.findViewById(R.id.rvDonHang);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(true, don -> {
            Intent i = new Intent(requireContext(), OrderDetailActivity.class);
            i.putExtra(OrderDetailActivity.EXTRA_MA_DON, (long) don.maDonHang);
            i.putExtra(OrderDetailActivity.EXTRA_ADMIN, true);
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
        themChip(chip, "Chờ xác nhận", DonHang.CHO_XAC_NHAN);
        themChip(chip, "Đang giao", DonHang.DANG_GIAO);
        themChip(chip, "Đã giao", DonHang.DA_GIAO);
        themChip(chip, "Đã huỷ", DonHang.DA_HUY);

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
    }
}
