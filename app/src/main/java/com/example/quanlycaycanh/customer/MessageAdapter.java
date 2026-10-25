package com.example.quanlycaycanh.customer;

import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;

import com.example.quanlycaycanh.R;

import java.util.ArrayList;
import java.util.List;

import com.example.quanlycaycanh.data.TinNhan;

/** Bong bóng tin nhắn: của mình bên phải, của người kia bên trái. */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LOAI_TRAI = 0;
    private static final int LOAI_PHAI = 1;

    private final List<TinNhan> items = new ArrayList<>();
    private int maNguoiDung;

    public void submit(List<TinNhan> ds, int maNguoiDung) {
        items.clear();
        if (ds != null) {
            items.addAll(ds);
        }
        this.maNguoiDung = maNguoiDung;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).maTaiKhoanGui == maNguoiDung ? LOAI_PHAI : LOAI_TRAI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new VH(inflater.inflate(viewType == LOAI_PHAI
                ? R.layout.item_message_right : R.layout.item_message_left, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TinNhan t = items.get(position);
        VH h = (VH) holder;
        h.noiDung.setText(t.noiDung);
        h.thoiGian.setText(t.thoiGianGui != null && t.thoiGianGui.length() >= 16
                ? t.thoiGianGui.substring(11, 16) : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final android.widget.TextView noiDung;
        final android.widget.TextView thoiGian;

        VH(@NonNull android.view.View v) {
            super(v);
            noiDung = v.findViewById(R.id.tvNoiDung);
            thoiGian = v.findViewById(R.id.tvThoiGian);
        }
    }
}
