package com.example.quanlycaycanh.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.ChiTietDonHang;

import java.util.ArrayList;
import java.util.List;

/** Dòng hàng trong giỏ — chính là ChiTietDonHang của đơn nháp. */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnCartChange {
        void onSoLuong(ChiTietDonHang dong, int soLuongMoi);

        void onXoa(ChiTietDonHang dong);
    }

    private final List<ChiTietDonHang> items = new ArrayList<>();
    private final OnCartChange listener;

    public CartAdapter(OnCartChange listener) {
        this.listener = listener;
    }

    public void submit(List<ChiTietDonHang> ds) {
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
                .inflate(R.layout.item_cart_line, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ChiTietDonHang d = items.get(position);
        h.tvEmoji.setText(d.hinhAnh == null ? "🪴" : d.hinhAnh);
        h.tvTen.setText(d.tenCay);
        h.tvGia.setText(Ui.vnd(d.donGia));
        h.tvSoLuong.setText(String.valueOf(d.soLuong));
        h.tvThanhTien.setText(Ui.vnd(d.thanhTien));
        h.btnGiam.setOnClickListener(v -> listener.onSoLuong(d, d.soLuong - 1));
        h.btnTang.setOnClickListener(v -> {
            if (d.soLuong >= d.soLuongTon) {
                Ui.toast(v.getContext(), "Chỉ còn " + d.soLuongTon + " cây trong kho");
                return;
            }
            listener.onSoLuong(d, d.soLuong + 1);
        });
        h.btnXoa.setOnClickListener(v -> listener.onXoa(d));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvEmoji;
        final TextView tvTen;
        final TextView tvGia;
        final TextView tvSoLuong;
        final TextView tvThanhTien;
        final TextView btnGiam;
        final TextView btnTang;
        final ImageButton btnXoa;

        VH(@NonNull View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvEmoji);
            tvTen = v.findViewById(R.id.tvTen);
            tvGia = v.findViewById(R.id.tvGia);
            tvSoLuong = v.findViewById(R.id.tvSoLuong);
            tvThanhTien = v.findViewById(R.id.tvThanhTien);
            btnGiam = v.findViewById(R.id.btnGiam);
            btnTang = v.findViewById(R.id.btnTang);
            btnXoa = v.findViewById(R.id.btnXoa);
        }
    }
}
