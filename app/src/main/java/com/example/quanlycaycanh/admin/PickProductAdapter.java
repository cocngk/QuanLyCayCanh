package com.example.quanlycaycanh.admin;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.SanPham;

import java.util.ArrayList;
import java.util.List;

/** Chọn số lượng sản phẩm khi lập hóa đơn tại quầy. */
public class PickProductAdapter extends RecyclerView.Adapter<PickProductAdapter.VH> {

    public interface OnQuantityChange {
        void onChanged();
    }

    private final List<SanPham> items = new ArrayList<>();
    private final List<SanPham> allItems = new ArrayList<>();
    private final SparseIntArray soLuong = new SparseIntArray();
    private final OnQuantityChange listener;

    public PickProductAdapter(OnQuantityChange listener) {
        this.listener = listener;
    }

    public void submit(List<SanPham> ds) {
        allItems.clear();
        items.clear();
        soLuong.clear();
        if (ds != null) {
            allItems.addAll(ds);
            items.addAll(ds);
        }
        notifyDataSetChanged();
        listener.onChanged();
    }

    public void filter(List<SanPham> ds) {
        items.clear();
        if (ds != null) {
            items.addAll(ds);
        }
        notifyDataSetChanged();
    }

    public SparseIntArray soLuongDaChon() {
        return soLuong;
    }

    public SanPham sanPhamTai(int viTri) {
        return items.get(viTri);
    }

    public List<SanPham> danhSach() {
        return allItems;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pick_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SanPham p = items.get(position);
        int sl = soLuong.get(p.maSanPham, 0);
        Ui.hienAnhCay(h.itemView.getContext(), p.hinhAnh, h.imgPlant, h.tvPhotoFallback, p.maLoai);
        h.tvTen.setText(p.tenCay);
        h.tvGiaTon.setText(Ui.vnd(p.giaBan) + " · còn " + p.soLuongTon);
        h.tvSoLuong.setText(String.valueOf(sl));
        h.btnGiam.setAlpha(sl > 0 ? 1f : 0.4f);
        h.btnTang.setAlpha(sl < p.soLuongTon ? 1f : 0.4f);

        h.btnGiam.setOnClickListener(v -> {
            int hienTai = soLuong.get(p.maSanPham, 0);
            if (hienTai <= 0) {
                return;
            }
            soLuong.put(p.maSanPham, hienTai - 1);
            notifyItemChanged(h.getAdapterPosition());
            listener.onChanged();
        });
        h.btnTang.setOnClickListener(v -> {
            int hienTai = soLuong.get(p.maSanPham, 0);
            if (hienTai >= p.soLuongTon) {
                Ui.toast(v.getContext(), "Chỉ còn " + p.soLuongTon + " cây trong kho");
                return;
            }
            soLuong.put(p.maSanPham, hienTai + 1);
            notifyItemChanged(h.getAdapterPosition());
            listener.onChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvPhotoFallback;
        final ImageView imgPlant;
        final TextView tvTen;
        final TextView tvGiaTon;
        final TextView tvSoLuong;
        final TextView btnGiam;
        final TextView btnTang;

        VH(@NonNull View v) {
            super(v);
            tvPhotoFallback = v.findViewById(R.id.tvPhotoFallback);
            imgPlant = v.findViewById(R.id.imgPlant);
            tvTen = v.findViewById(R.id.tvTen);
            tvGiaTon = v.findViewById(R.id.tvGiaTon);
            tvSoLuong = v.findViewById(R.id.tvSoLuong);
            btnGiam = v.findViewById(R.id.btnGiam);
            btnTang = v.findViewById(R.id.btnTang);
        }
    }
}
