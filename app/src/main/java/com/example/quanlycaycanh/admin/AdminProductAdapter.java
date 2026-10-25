package com.example.quanlycaycanh.admin;

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

/** Bảng sản phẩm trong trang quản trị: ảnh, tên, danh mục, giá, tồn, trạng thái. */
public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    public interface OnAdminProduct {
        void onSua(SanPham p);

        void onXoa(SanPham p);
    }

    private final List<SanPham> items = new ArrayList<>();
    private final OnAdminProduct listener;

    public AdminProductAdapter(OnAdminProduct listener) {
        this.listener = listener;
    }

    public void submit(List<SanPham> ds) {
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
                .inflate(R.layout.item_admin_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SanPham p = items.get(position);
        h.tvEmoji.setText(p.hinhAnh == null ? "🪴" : p.hinhAnh);
        h.tvEmoji.setBackground(Ui.boTron(h.itemView.getContext(),
                Ui.nenDanhMuc(h.itemView.getContext(), p.maLoai), 14));
        h.tvTen.setText(p.tenCay);
        h.tvLoaiGia.setText(p.tenLoai + " · " + Ui.vnd(p.giaBan));
        String trangThai = p.dangBan ? "Đang bán" : "Ngừng bán";
        h.tvTon.setText("Tồn " + p.soLuongTon + " · " + trangThai);
        h.tvTon.setTextColor(h.itemView.getContext()
                .getColor(p.soLuongTon <= 5 ? R.color.amber : R.color.green));
        h.btnSua.setOnClickListener(v -> listener.onSua(p));
        h.btnXoa.setOnClickListener(v -> listener.onXoa(p));
        h.itemView.setOnClickListener(v -> listener.onSua(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvEmoji;
        final TextView tvTen;
        final TextView tvLoaiGia;
        final TextView tvTon;
        final ImageButton btnSua;
        final ImageButton btnXoa;

        VH(@NonNull View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvEmoji);
            tvTen = v.findViewById(R.id.tvTen);
            tvLoaiGia = v.findViewById(R.id.tvLoaiGia);
            tvTon = v.findViewById(R.id.tvTon);
            btnSua = v.findViewById(R.id.btnSua);
            btnXoa = v.findViewById(R.id.btnXoa);
        }
    }
}
