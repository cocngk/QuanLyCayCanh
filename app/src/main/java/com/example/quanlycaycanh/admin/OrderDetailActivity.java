package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.ChiTietDonHang;
import com.example.quanlycaycanh.data.DonHang;
import com.example.quanlycaycanh.data.Repository;
import com.google.android.material.button.MaterialButton;

/** Chi tiết hóa đơn và cập nhật trạng thái cho quản trị viên. */
public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MA_DON = "ma_don";
    private Repository repo;
    private long maDon;
    private DonHang don;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Ui.anToan(this);

        repo = new Repository(this);
        maDon = getIntent().getLongExtra(EXTRA_MA_DON, -1);

        ImageButton btnQuayLai = findViewById(R.id.btnQuayLai);
        btnQuayLai.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        don = repo.order(maDon);
        if (don == null) {
            Ui.toast(this, "Không tìm thấy đơn hàng");
            finish();
            return;
        }
        hienThi();
    }

    /** Chuyển lịch sử trạng thái thành nội dung dễ đọc. */
    private String lichSuDep(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "—";
        }
        StringBuilder sb = new StringBuilder();
        for (String dong : raw.split("\n")) {
            String[] phan = dong.split("\\|");
            if (phan.length < 2) {
                continue;
            }
            String ma = phan[0].trim();
            String thoiGian = Repository.dinhDangThoiGian(phan[1].trim());
            String ghiChu = phan.length > 2 ? phan[2].trim() : "";
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(Ui.nhanTrangThai(ma));
            if (!thoiGian.isEmpty()) {
                sb.append(" · ").append(thoiGian);
            }
            if (!ghiChu.isEmpty()) {
                sb.append("\n").append(ghiChu);
            }
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }

    private void hienThi() {
        TextView tvTrangThai = findViewById(R.id.tvTrangThai);
        TextView tvMaDon = findViewById(R.id.tvMaDon);
        TextView tvKhach = findViewById(R.id.tvKhach);
        TextView tvThanhToan = findViewById(R.id.tvThanhToan);
        TextView tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        TextView tvGhiChu = findViewById(R.id.tvGhiChu);
        LinearLayout khungSanPham = findViewById(R.id.khungSanPham);
        TextView tvTongTien = findViewById(R.id.tvTongTien);
        TextView tvLichSu = findViewById(R.id.tvLichSu);
        TextView tvTieuDe = findViewById(R.id.tvTieuDe);

        tvTieuDe.setText("Chi tiết hóa đơn");
        Ui.ganNhanTrangThai(this, tvTrangThai, don.trangThai);
        tvMaDon.setText("#" + don.maDonHang + " · " + Repository.dinhDangThoiGian(don.ngayDat));
        tvKhach.setText("Khách: " + (don.tenKhach == null || don.tenKhach.trim().isEmpty()
                ? "Khách mua lẻ" : don.tenKhach));
        tvThanhToan.setText("Thanh toán: " + (don.phuongThucThanhToan == null ? "—" : don.phuongThucThanhToan));
        tvSoDienThoai.setText("Số điện thoại: "
                + (don.soDienThoaiKhach == null || don.soDienThoaiKhach.trim().isEmpty()
                ? "—" : don.soDienThoaiKhach));
        tvGhiChu.setText("Ghi chú: " + (don.ghiChu == null || don.ghiChu.isEmpty() ? "—" : don.ghiChu));

        khungSanPham.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ChiTietDonHang l : repo.orderLines(maDon)) {
            View row = inflater.inflate(R.layout.item_order_line, khungSanPham, false);
            TextView emoji = row.findViewById(R.id.tvEmoji);
            TextView ten = row.findViewById(R.id.tvTen);
            TextView dongGia = row.findViewById(R.id.tvDongGia);
            TextView thanhTien = row.findViewById(R.id.tvThanhTien);
            String icon = l.hinhAnh != null && !l.hinhAnh.startsWith("content://")
                    && !l.hinhAnh.startsWith("file://") ? l.hinhAnh : "🌿";
            emoji.setText(icon);
            ten.setText(l.tenCay);
            dongGia.setText(Ui.vnd(l.donGia) + " × " + l.soLuong);
            thanhTien.setText(Ui.vnd(l.thanhTien));
            khungSanPham.addView(row);
        }

        tvTongTien.setText(Ui.vnd(don.tongTien));
        tvLichSu.setText(lichSuDep(don.lichSuTrangThai));

        MaterialButton btnCapNhat = findViewById(R.id.btnCapNhat);

        btnCapNhat.setVisibility(!DonHang.DA_GIAO.equals(don.trangThai)
                && !DonHang.DA_HUY.equals(don.trangThai) ? View.VISIBLE : View.GONE);

        btnCapNhat.setOnClickListener(v -> chonTrangThai());
    }

    private void chonTrangThai() {
        final String[] nhan = {"Bắt đầu xử lý", "Hoàn tất hóa đơn", "Hủy hóa đơn"};
        final String[] giaTri = {DonHang.DANG_GIAO, DonHang.DA_GIAO, DonHang.DA_HUY};
        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái đơn #" + don.maDonHang)
                .setItems(nhan, (d, which) -> {
                    repo.updateStatus(don.maDonHang, giaTri[which],
                            "Nhân viên cập nhật: " + Ui.nhanTrangThai(giaTri[which]));
                    Ui.toast(this, "Đã cập nhật: " + Ui.nhanTrangThai(giaTri[which]));
                    onResume();
                })
                .show();
    }
}
