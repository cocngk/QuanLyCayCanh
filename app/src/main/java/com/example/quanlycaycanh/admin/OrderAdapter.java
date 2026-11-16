package com.example.quanlycaycanh.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.DonHang;
import com.example.quanlycaycanh.data.Repository;

import java.util.ArrayList;
import java.util.List;

/** Dòng hóa đơn trong giao diện quản trị. */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface OnOrderClick {
        void onOpen(DonHang don);
    }

    private final List<DonHang> items = new ArrayList<>();
    private final OnOrderClick listener;

    public OrderAdapter(OnOrderClick listener) {
        this.listener = listener;
    }

    public void submit(List<DonHang> ds) {
        items.clear();
        if (ds != null) {
            items.addAll(ds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DonHang d = items.get(position);
        h.tvMaDon.setText("#" + d.maDonHang);
        Ui.ganNhanTrangThai(h.itemView.getContext(), h.tvTrangThai, d.trangThai);
        String thoiGian = Repository.dinhDangThoiGian(d.ngayDat);
        h.tvThoiGian.setText(thoiGian + " · " + d.soSanPham + " sản phẩm");
        h.tvTongTien.setText(Ui.vnd(d.tongTien));
        String name = d.tenKhach == null || d.tenKhach.trim().isEmpty()
                ? "Khách mua lẻ" : d.tenKhach;
        h.tvKhach.setText(name);
        h.itemView.setOnClickListener(v -> listener.onOpen(d));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvMaDon;
        final TextView tvTrangThai;
        final TextView tvThoiGian;
        final TextView tvKhach;
        final TextView tvTongTien;

        VH(@NonNull View v) {
            super(v);
            tvMaDon = v.findViewById(R.id.tvMaDon);
            tvTrangThai = v.findViewById(R.id.tvTrangThai);
            tvThoiGian = v.findViewById(R.id.tvThoiGian);
            tvKhach = v.findViewById(R.id.tvKhach);
            tvTongTien = v.findViewById(R.id.tvTongTien);
        }
    }
}
