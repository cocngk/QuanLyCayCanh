package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.util.SparseIntArray;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
import com.example.quanlycaycanh.data.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

/** UC24 — Quản trị viên tạo đơn trực tiếp cho khách mua tại quầy. */
public class CreateOrderActivity extends AppCompatActivity {

    private Repository repo;
    private PickProductAdapter adapter;
    private TextView tvTongTien;
    private Spinner spKhach;
    private Spinner spThanhToan;
    private List<TaiKhoan> khachHang = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        Ui.anToan(this);

        repo = new Repository(this);
        tvTongTien = findViewById(R.id.tvTongTien);
        spKhach = findViewById(R.id.spKhach);
        spThanhToan = findViewById(R.id.spThanhToan);

        ImageButton btnQuayLai = findViewById(R.id.btnQuayLai);
        btnQuayLai.setOnClickListener(v -> finish());

        khachHang = repo.customers();
        List<String> tenKhach = new ArrayList<>();
        for (TaiKhoan tk : khachHang) {
            tenKhach.add(tk.hoTen + " · " + (tk.soDienThoai == null ? tk.email : tk.soDienThoai));
        }
        ArrayAdapter<String> spKhachAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tenKhach);
        spKhachAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKhach.setAdapter(spKhachAdapter);

        ArrayAdapter<String> spThanhToanAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tiền mặt", "Chuyển khoản"});
        spThanhToanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spThanhToan.setAdapter(spThanhToanAdapter);

        List<SanPham> conHang = new ArrayList<>();
        for (SanPham p : repo.adminProducts("", 0, "dang_ban")) {
            if (p.soLuongTon > 0) {
                conHang.add(p);
            }
        }

        RecyclerView rv = findViewById(R.id.rvSanPham);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PickProductAdapter(() -> tvTongTien.setText(Ui.vnd(tongTienHienTai())));
        rv.setAdapter(adapter);
        adapter.submit(conHang);

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
        if (khachHang.isEmpty()) {
            Ui.toast(this, "Chưa có tài khoản khách hàng");
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
        TaiKhoan khach = khachHang.get(spKhach.getSelectedItemPosition());
        String thanhToan = spThanhToan.getSelectedItemPosition() == 0 ? "Tiền mặt" : "Chuyển khoản";
        long maDon = repo.createCounterOrder(khach.maTaiKhoan, ds, thanhToan,
                "Khách mua tại cửa hàng");
        if (maDon == -1) {
            Ui.toast(this, "Chưa chọn sản phẩm");
            return;
        }
        if (maDon == -2) {
            Ui.toast(this, "Số lượng vượt quá tồn kho");
            return;
        }
        Ui.toast(this, "Đã tạo đơn #" + maDon + " cho " + khach.hoTen);
        finish();
    }
}
