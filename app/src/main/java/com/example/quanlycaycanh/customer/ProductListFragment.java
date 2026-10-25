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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.LoaiCay;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/** 5.3 — Danh sách sản phẩm: tìm kiếm, chip lọc nhanh, modal Lọc sản phẩm. */
public class ProductListFragment extends Fragment implements FilterDialog.Listener {

    private Repository repo;
    private Session session;
    private ProductAdapter adapter;
    private TextView tvSoLuong;
    private ChipGroup chipNhom;

    private int maLoai;
    private double giaTu;
    private double giaDen;
    private String sort = "mac_dinh";
    private String tuKhoa = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());
        tvSoLuong = v.findViewById(R.id.tvSoLuong);
        chipNhom = v.findViewById(R.id.chipNhom);

        EditText edTimKiem = v.findViewById(R.id.edTimKiem);
        edTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tuKhoa = s.toString();
                napDanhSach();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        MaterialButton btnLoc = v.findViewById(R.id.btnLoc);
        btnLoc.setOnClickListener(view -> {
            FilterDialog d = new FilterDialog();
            d.setThamSo(maLoai, giaTu, giaDen, sort);
            d.show(getChildFragmentManager(), "loc_san_pham");
        });

        RecyclerView rv = v.findViewById(R.id.rvSanPham);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(false, new ProductAdapter.OnProductClick() {
            @Override
            public void onOpen(SanPham sanPham) {
                startActivity(new Intent(requireContext(), ProductDetailActivity.class)
                        .putExtra(ProductDetailActivity.EXTRA_MA_SAN_PHAM, sanPham.maSanPham));
            }

            @Override
            public void onAdd(SanPham sanPham) {
                if (sanPham.soLuongTon <= 0) {
                    Ui.toast(requireContext(), "Sản phẩm đã hết hàng");
                    return;
                }
                repo.addToCart(session.maTaiKhoan(), sanPham.maSanPham, 1);
                Ui.toast(requireContext(), "Đã thêm " + sanPham.tenCay + " vào giỏ");
            }
        });
        rv.setAdapter(adapter);

        dungChip();
    }

    @Override
    public void onResume() {
        super.onResume();
        CustomerActivity a = (CustomerActivity) requireActivity();
        if (a.locChoLoai > 0) {
            maLoai = a.locChoLoai;
            a.locChoLoai = 0;
            chonChipTheoLoai();
        }
        napDanhSach();
    }

    private void dungChip() {
        chipNhom.removeAllViews();
        chipNhom.addView(taoChip("Tất cả", 0));
        List<LoaiCay> ds = repo.categories();
        for (LoaiCay loai : ds) {
            chipNhom.addView(taoChip(loai.tenLoai, loai.maLoai));
        }
        chonChipTheoLoai();
    }

    private Chip taoChip(String ten, int maLoaiChip) {
        Chip chip = new Chip(requireContext());
        chip.setText(ten);
        chip.setCheckable(true);
        chip.setTag(maLoaiChip);
        chip.setChecked(maLoaiChip == maLoai);
        chip.setOnClickListener(v -> {
            maLoai = (int) v.getTag();
            napDanhSach();
        });
        return chip;
    }

    private void chonChipTheoLoai() {
        for (int i = 0; i < chipNhom.getChildCount(); i++) {
            View c = chipNhom.getChildAt(i);
            if (c instanceof Chip) {
                Object tag = c.getTag();
                ((Chip) c).setChecked(tag != null && (int) tag == maLoai);
            }
        }
    }

    private void napDanhSach() {
        if (!isAdded()) {
            return;
        }
        List<SanPham> ds = repo.products(tuKhoa, maLoai, giaTu, giaDen, sort);
        adapter.submit(ds);
        tvSoLuong.setText(ds.size() + " sản phẩm");
    }

    @Override
    public void onApply(int maLoaiMoi, double giaTuMoi, double giaDenMoi, String sortMoi) {
        maLoai = maLoaiMoi;
        giaTu = giaTuMoi;
        giaDen = giaDenMoi;
        sort = sortMoi;
        chonChipTheoLoai();
        napDanhSach();
    }
}
