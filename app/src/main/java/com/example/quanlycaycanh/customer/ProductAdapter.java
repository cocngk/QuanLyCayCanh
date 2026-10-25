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
import com.example.quanlycaycanh.data.SanPham;

import java.util.ArrayList;
import java.util.List;

/** Thẻ sản phẩm dùng cho lưới 2 cột (trang chủ, danh sách) và dải ngang (bán chạy). */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductClick {
        void onOpen(SanPham sanPham);

        void onAdd(SanPham sanPham);
    }

    private final List<SanPham> items = new ArrayList<>();
    private final boolean compact;
    private final OnProductClick listener;

    public ProductAdapter(boolean compact, OnProductClick listener) {
        this.compact = compact;
        this.listener = listener;
    }

    public void submit(List<SanPham> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(compact ? R.layout.item_product_compact : R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SanPham p = items.get(position);
        h.tvEmoji.setText(p.hinhAnh == null || p.hinhAnh.isEmpty() ? "🪴" : p.hinhAnh);
        h.tvEmoji.setBackground(Ui.boTron(h.itemView.getContext(),
                Ui.nenDanhMuc(h.itemView.getContext(), p.maLoai), 14));
        h.tvTen.setText(p.tenCay);
        h.tvGia.setText(Ui.vnd(p.giaBan));
        if (h.tvTon != null) {
            if (p.soLuongTon <= 0) {
                h.tvTon.setText("Hết hàng");
                h.tvTon.setTextColor(h.itemView.getContext().getColor(R.color.red));
            } else {
                h.tvTon.setText("Còn " + p.soLuongTon + " cây");
                h.tvTon.setTextColor(h.itemView.getContext().getColor(R.color.muted));
            }
        }
        if (h.btnThem != null) {
            h.btnThem.setEnabled(p.soLuongTon > 0);
            h.btnThem.setAlpha(p.soLuongTon > 0 ? 1f : 0.4f);
            h.btnThem.setOnClickListener(v -> listener.onAdd(p));
        }
        h.itemView.setOnClickListener(v -> listener.onOpen(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvEmoji;
        final TextView tvTen;
        final TextView tvGia;
        final TextView tvTon;
        final ImageButton btnThem;

        VH(@NonNull View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvEmoji);
            tvTen = v.findViewById(R.id.tvTen);
            tvGia = v.findViewById(R.id.tvGia);
            tvTon = v.findViewById(R.id.tvTon);
            btnThem = v.findViewById(R.id.btnThem);
        }
    }
}
