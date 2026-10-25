package com.example.quanlycaycanh.customer;

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
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/** 5.6 — Danh sách đơn hàng của khách: tab trạng thái + tìm theo mã đơn. */
public class OrderListFragment extends Fragment {

    private Repository repo;
    private Session session;
    private OrderAdapter adapter;
    private TextView tvTrong;
    private String trangThai = "TAT_CA";
    private String tuKhoa = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());
        tvTrong = v.findViewById(R.id.tvTrong);

        RecyclerView rv = v.findViewById(R.id.rvDonHang);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(false, don -> {
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
        themChip(chip, "Chờ xác nhận", DonHang.CHO_XAC_NHAN);
        themChip(chip, "Đang giao", DonHang.DANG_GIAO);
        themChip(chip, "Đã giao", DonHang.DA_GIAO);
        themChip(chip, "Đã huỷ", DonHang.DA_HUY);
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
        List<DonHang> ds = repo.orders(session.maTaiKhoan(), trangThai, tuKhoa);
        adapter.submit(ds);
        tvTrong.setVisibility(ds.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
