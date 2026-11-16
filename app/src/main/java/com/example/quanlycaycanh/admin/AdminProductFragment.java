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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.example.quanlycaycanh.data.LoaiCay;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/** 5.7 (phụ) — Quản lý sản phẩm: tìm kiếm, thêm, sửa, ngừng bán. */
public class AdminProductFragment extends Fragment {

    private Repository repo;
    private AdminProductAdapter adapter;
    private String tuKhoa = "";
    private int maLoai = 0;
    private TextView tvTongSanPham;
    private EditText edTimSanPham;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        tvTongSanPham = v.findViewById(R.id.tvTongSanPham);

        RecyclerView rv = v.findViewById(R.id.rvSanPham);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminProductAdapter(new AdminProductAdapter.OnAdminProduct() {
            @Override
            public void onSua(SanPham p) {
                startActivity(new Intent(requireContext(), ProductEditActivity.class)
                        .putExtra(ProductEditActivity.EXTRA_MA_SAN_PHAM, p.maSanPham));
            }

            @Override
            public void onXoa(SanPham p) {
                xacNhanXoa(p);
            }
        });
        rv.setAdapter(adapter);

        ChipGroup categories = v.findViewById(R.id.chipLoaiCay);
        themChip(categories, "Tất cả", 0);
        for (LoaiCay category : repo.categories()) {
            themChip(categories, category.tenLoai, category.maLoai);
        }

        edTimSanPham = v.findViewById(R.id.edTimSanPham);
        edTimSanPham.addTextChangedListener(new TextWatcher() {
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

        v.findViewById(R.id.btnThemSanPham).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), ProductEditActivity.class)));
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
        List<SanPham> products = repo.adminProducts(tuKhoa, maLoai, "");
        adapter.submit(products);
        tvTongSanPham.setText("Tổng: " + products.size() + " cây cảnh");
    }

    private void themChip(ChipGroup group, String label, int categoryId) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(categoryId == maLoai);
        chip.setOnClickListener(v -> {
            maLoai = categoryId;
            nap();
        });
        group.addView(chip);
    }

    public void setSearchQuery(String query) {
        if (edTimSanPham != null) {
            edTimSanPham.setText(query == null ? "" : query);
            edTimSanPham.setSelection(edTimSanPham.length());
        }
    }

    private void xacNhanXoa(SanPham p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá sản phẩm")
                .setMessage("Xoá \"" + p.tenCay + "\"?\n\nSản phẩm đã từng nằm trong đơn hàng sẽ được "
                        + "chuyển sang trạng thái Ngừng bán để giữ lịch sử.")
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Xoá", (d, w) -> {
                    boolean daXoa = repo.deleteOrHideProduct(p.maSanPham);
                    Ui.toast(requireContext(), daXoa
                            ? "Đã xoá sản phẩm" : "Đã chuyển sang Ngừng bán");
                    nap();
                })
                .show();
    }
}
