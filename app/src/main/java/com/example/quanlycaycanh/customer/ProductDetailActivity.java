package com.example.quanlycaycanh.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.button.MaterialButton;

/** 5.4 — Chi tiết sản phẩm. */
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MA_SAN_PHAM = "ma_san_pham";

    private Repository repo;
    private Session session;
    private SanPham sanPham;
    private int soLuong = 1;
    private TextView tvSoLuong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        Ui.anToan(this);

        repo = new Repository(this);
        session = new Session(this);
        int maSanPham = getIntent().getIntExtra(EXTRA_MA_SAN_PHAM, -1);
        sanPham = repo.product(maSanPham);
        if (sanPham == null) {
            Ui.toast(this, "Không tìm thấy sản phẩm");
            finish();
            return;
        }

        ImageButton btnQuayLai = findViewById(R.id.btnQuayLai);
        ImageButton btnGioHang = findViewById(R.id.btnGioHang);
        btnQuayLai.setOnClickListener(v -> finish());
        btnGioHang.setOnClickListener(v -> CustomerActivity.moGioHang(this));

        TextView tvEmoji = findViewById(R.id.tvEmoji);
        TextView tvTen = findViewById(R.id.tvTen);
        TextView tvGia = findViewById(R.id.tvGia);
        TextView tvTon = findViewById(R.id.tvTon);
        TextView tvLoai = findViewById(R.id.tvLoai);
        TextView tvMoTa = findViewById(R.id.tvMoTa);
        TextView tvChamSoc = findViewById(R.id.tvChamSoc);
        tvSoLuong = findViewById(R.id.tvSoLuong);

        tvEmoji.setText(sanPham.hinhAnh);
        tvEmoji.setBackground(Ui.boTron(this, Ui.nenDanhMuc(this, sanPham.maLoai), 20));
        tvTen.setText(sanPham.tenCay);
        tvGia.setText(Ui.vnd(sanPham.giaBan));
        tvLoai.setText(sanPham.tenLoai);
        tvMoTa.setText(sanPham.moTa);
        tvChamSoc.setText(sanPham.huongDanChamSoc);
        if (sanPham.soLuongTon <= 0) {
            tvTon.setText("Hết hàng");
            tvTon.setTextColor(getColor(R.color.red));
        } else {
            tvTon.setText("Còn " + sanPham.soLuongTon + " cây");
        }

        MaterialButton btnGiam = findViewById(R.id.btnGiam);
        MaterialButton btnTang = findViewById(R.id.btnTang);
        MaterialButton btnThemGio = findViewById(R.id.btnThemGio);
        MaterialButton btnMuaNgay = findViewById(R.id.btnMuaNgay);

        btnGiam.setOnClickListener(v -> {
            if (soLuong > 1) {
                soLuong--;
                tvSoLuong.setText(String.valueOf(soLuong));
            }
        });
        btnTang.setOnClickListener(v -> {
            if (soLuong < Math.max(sanPham.soLuongTon, 1)) {
                soLuong++;
                tvSoLuong.setText(String.valueOf(soLuong));
            } else {
                Ui.toast(this, "Chỉ còn " + sanPham.soLuongTon + " cây trong kho");
            }
        });
        btnThemGio.setOnClickListener(v -> themVaoGio(false));
        btnMuaNgay.setOnClickListener(v -> themVaoGio(true));

        boolean conHang = sanPham.soLuongTon > 0;
        btnThemGio.setEnabled(conHang);
        btnMuaNgay.setEnabled(conHang);
    }

    private void themVaoGio(boolean muaNgay) {
        if (!session.daDangNhap()) {
            startActivity(new Intent(this, com.example.quanlycaycanh.LoginActivity.class));
            return;
        }
        repo.addToCart(session.maTaiKhoan(), sanPham.maSanPham, soLuong);
        if (muaNgay) {
            CustomerActivity.moGioHang(this);
        } else {
            Ui.toast(this, "Đã thêm " + soLuong + " " + sanPham.tenCay + " vào giỏ");
        }
    }
}
