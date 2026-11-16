package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.ChiTietDonHang;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.example.quanlycaycanh.data.LoaiCay;
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/** Tạo hóa đơn cho giao dịch tại quầy. */
public class CreateOrderActivity extends AppCompatActivity {

    private Repository repo;
    private PickProductAdapter adapter;
    private TextView tvTongTien;
    private Spinner spThanhToan;
    private TextInputEditText edTenKhach;
    private TextInputEditText edSoDienThoai;
    private final List<SanPham> allProducts = new ArrayList<>();
    private String tuKhoa = "";
    private int maLoai = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        Ui.anToan(this);

        repo = new Repository(this);
        tvTongTien = findViewById(R.id.tvTongTien);
        edTenKhach = findViewById(R.id.edTenKhach);
        edSoDienThoai = findViewById(R.id.edSoDienThoai);
        spThanhToan = findViewById(R.id.spThanhToan);

        android.widget.ImageButton btnQuayLai = findViewById(R.id.btnQuayLai);
        btnQuayLai.setOnClickListener(v -> finish());

        ArrayAdapter<String> spThanhToanAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tiền mặt", "Chuyển khoản"});
        spThanhToanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spThanhToan.setAdapter(spThanhToanAdapter);

        allProducts.clear();
        for (SanPham p : repo.adminProducts("", 0, "dang_ban")) {
            if (p.soLuongTon > 0) {
                allProducts.add(p);
            }
        }

        RecyclerView rv = findViewById(R.id.rvSanPham);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PickProductAdapter(() -> tvTongTien.setText(Ui.vnd(tongTienHienTai())));
        rv.setAdapter(adapter);
        adapter.submit(allProducts);

        ChipGroup categories = findViewById(R.id.chipLoaiCay);
        themChip(categories, "Tất cả", 0);
        for (LoaiCay category : repo.categories()) {
            themChip(categories, category.tenLoai, category.maLoai);
        }
        EditText edTim = findViewById(R.id.edTimSanPham);
        edTim.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tuKhoa = s.toString().trim().toLowerCase(java.util.Locale.ROOT);
                locSanPham();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btnTaoDon).setOnClickListener(v -> taoDon());
    }

    private double tongTienHienTai() {
        double tong = 0;
        SparseIntArray chon = adapter.soLuongDaChon();
        List<SanPham> ds = adapter.danhSach();
        for (int i = 0; i < ds.size(); i++) {
            SanPham p = ds.get(i);
            tong += p.giaBan * chon.get(p.maSanPham, 0);
        }
        return tong;
    }

    private void taoDon() {
        String tenKhach = text(edTenKhach);
        String soDienThoai = text(edSoDienThoai).replaceAll("[^0-9+]", "");
        if (tenKhach.isEmpty() || soDienThoai.length() < 9) {
            Ui.toast(this, "Nhập tên khách hàng và số điện thoại hợp lệ");
            return;
        }
        SparseIntArray chon = adapter.soLuongDaChon();
        List<ChiTietDonHang> ds = new ArrayList<>();
        for (SanPham p : adapter.danhSach()) {
            int sl = chon.get(p.maSanPham, 0);
            if (sl > 0) {
                ChiTietDonHang ct = new ChiTietDonHang();
                ct.maSanPham = p.maSanPham;
                ct.soLuong = sl;
                ds.add(ct);
            }
        }
        if (ds.isEmpty()) {
            Ui.toast(this, "Chọn ít nhất một sản phẩm");
            return;
        }
        String thanhToan = spThanhToan.getSelectedItemPosition() == 0 ? "Tiền mặt" : "Chuyển khoản";
        long maDon = repo.createCounterOrder(new Session(this).maTaiKhoan(),
                tenKhach, soDienThoai, ds, thanhToan, "Bán tại cửa hàng");
        if (maDon == -1) {
            Ui.toast(this, "Chưa chọn sản phẩm");
            return;
        }
        if (maDon == -2) {
            Ui.toast(this, "Số lượng vượt quá tồn kho");
            return;
        }
        Ui.toast(this, "Đã tạo hóa đơn #" + maDon + " cho " + tenKhach);
        finish();
    }

    private void themChip(ChipGroup group, String label, int categoryId) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(categoryId == maLoai);
        chip.setOnClickListener(v -> {
            maLoai = categoryId;
            locSanPham();
        });
        group.addView(chip);
    }

    private void locSanPham() {
        if (adapter == null) {
            return;
        }
        List<SanPham> filtered = new ArrayList<>();
        for (SanPham product : allProducts) {
            boolean matchName = product.tenCay != null
                    && product.tenCay.toLowerCase(java.util.Locale.ROOT).contains(tuKhoa);
            boolean matchCategory = maLoai == 0 || product.maLoai == maLoai;
            if (matchName && matchCategory) {
                filtered.add(product);
            }
        }
        adapter.filter(filtered);
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
