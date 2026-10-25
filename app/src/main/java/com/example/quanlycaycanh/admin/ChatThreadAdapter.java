package com.example.quanlycaycanh.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.data.CuocTroChuyen;
import com.example.quanlycaycanh.data.Repository;

import java.util.ArrayList;
import java.util.List;

/** Danh sách đoạn chat phía quản trị. */
public class ChatThreadAdapter extends RecyclerView.Adapter<ChatThreadAdapter.VH> {

    public interface OnThreadClick {
        void onOpen(CuocTroChuyen c);
    }

    private final List<CuocTroChuyen> items = new ArrayList<>();
    private final OnThreadClick listener;

    public ChatThreadAdapter(OnThreadClick listener) {
        this.listener = listener;
    }

    public void submit(List<CuocTroChuyen> ds) {
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
                .inflate(R.layout.item_chat_thread, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CuocTroChuyen c = items.get(position);
        String ten = c.tenKhach == null ? "Khách hàng" : c.tenKhach;
        h.tvTen.setText(ten);
        h.tvAvatar.setText(ten.isEmpty() ? "K" : ten.substring(ten.lastIndexOf(' ') + 1,
                Math.min(ten.length(), ten.lastIndexOf(' ') + 2)));
        h.tvTinNhanCuoi.setText(c.tinNhanCuoi == null ? "(chưa có tin nhắn)" : c.tinNhanCuoi);
        String capNhat = c.thoiGianCapNhat == null ? "" : c.thoiGianCapNhat;
        h.tvThoiGian.setText(capNhat.startsWith(Repository.today()) && capNhat.length() >= 16
                ? capNhat.substring(11, 16) : Repository.dinhDangNgay(capNhat));
        h.itemView.setOnClickListener(v -> listener.onOpen(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvTen;
        final TextView tvTinNhanCuoi;
        final TextView tvThoiGian;

        VH(@NonNull View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvTen = v.findViewById(R.id.tvTen);
            tvTinNhanCuoi = v.findViewById(R.id.tvTinNhanCuoi);
            tvThoiGian = v.findViewById(R.id.tvThoiGian);
        }
    }
}
