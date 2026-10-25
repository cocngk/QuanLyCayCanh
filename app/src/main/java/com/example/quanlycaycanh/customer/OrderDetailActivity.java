package com.example.quanlycaycanh.customer;

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
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.button.MaterialButton;

/** 5.6 (phụ) — Chi tiết đơn hàng, dùng chung cho khách hàng và quản trị viên. */
public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MA_DON = "ma_don";
    public static final String EXTRA_ADMIN = "admin";

    private Repository repo;
    private Session session;
    private long maDon;
    private boolean admin;
    private DonHang don;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Ui.anToan(this);

        repo = new Repository(this);
        session = new Session(this);
        maDon = getIntent().getLongExtra(EXTRA_MA_DON, -1);
        admin = getIntent().getBooleanExtra(EXTRA_ADMIN, false);

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

    /** "CHO_XAC_NHAN | 2026-10-25 14:14:48 | Khách đặt hàng" -> nhãn dễ đọc. */
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
            if (DonHang.GIO.equals(ma)) {
                continue;
            }
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
        TextView tvDiaChi = findViewById(R.id.tvDiaChi);
        TextView tvGhiChu = findViewById(R.id.tvGhiChu);
        LinearLayout khungSanPham = findViewById(R.id.khungSanPham);
        TextView tvTamTinh = findViewById(R.id.tvTamTinh);
        TextView tvPhiShip = findViewById(R.id.tvPhiShip);
        TextView tvTongTien = findViewById(R.id.tvTongTien);
        TextView tvLichSu = findViewById(R.id.tvLichSu);
        TextView tvTieuDe = findViewById(R.id.tvTieuDe);

        tvTieuDe.setText(admin ? "Chi tiết đơn (quản trị)" : "Chi tiết đơn hàng");
        Ui.ganNhanTrangThai(this, tvTrangThai, don.trangThai);
        tvMaDon.setText("#" + don.maDonHang + " · " + Repository.dinhDangThoiGian(don.ngayDat)
                + (DonHang.TAI_QUAY.equals(don.loaiDonHang) ? " · Tại quầy" : ""));
        tvKhach.setText("Khách: " + (don.tenKhach == null ? "—" : don.tenKhach));
        tvThanhToan.setText("Thanh toán: " + (don.phuongThucThanhToan == null ? "—" : don.phuongThucThanhToan));
        tvDiaChi.setText("Giao đến: " + (don.diaChiGiao == null || don.diaChiGiao.isEmpty()
                ? "Mua trực tiếp tại cửa hàng" : don.diaChiGiao));
        tvGhiChu.setText("Ghi chú: " + (don.ghiChu == null || don.ghiChu.isEmpty() ? "—" : don.ghiChu));

        khungSanPham.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ChiTietDonHang l : repo.orderLines(maDon)) {
            View row = inflater.inflate(R.layout.item_order_line, khungSanPham, false);
            TextView emoji = row.findViewById(R.id.tvEmoji);
            TextView ten = row.findViewById(R.id.tvTen);
            TextView dongGia = row.findViewById(R.id.tvDongGia);
            TextView thanhTien = row.findViewById(R.id.tvThanhTien);
            emoji.setText(l.hinhAnh == null ? "🪴" : l.hinhAnh);
            ten.setText(l.tenCay);
            dongGia.setText(Ui.vnd(l.donGia) + " × " + l.soLuong);
            thanhTien.setText(Ui.vnd(l.thanhTien));
            khungSanPham.addView(row);
        }

        double phiShip = DonHang.TAI_QUAY.equals(don.loaiDonHang)
                || DonHang.DA_HUY.equals(don.trangThai) ? 0 : Repository.PHI_VAN_CHUYEN;
        tvTamTinh.setText(Ui.vnd(don.tongTien - phiShip));
        tvPhiShip.setText(DonHang.DA_HUY.equals(don.trangThai) ? "—"
                : phiShip <= 0 ? "Miễn phí" : Ui.vnd(phiShip));
        tvTongTien.setText(Ui.vnd(don.tongTien));
        tvLichSu.setText(lichSuDep(don.lichSuTrangThai));

        MaterialButton btnHuy = findViewById(R.id.btnHuyDon);
        MaterialButton btnMuaLai = findViewById(R.id.btnMuaLai);
        MaterialButton btnCapNhat = findViewById(R.id.btnCapNhat);

        btnHuy.setVisibility(!admin && repo.canCancel(don) ? View.VISIBLE : View.GONE);
        btnMuaLai.setVisibility(!admin && don.daDatHang() ? View.VISIBLE : View.GONE);
        btnCapNhat.setVisibility(admin && !DonHang.DA_GIAO.equals(don.trangThai)
                && !DonHang.DA_HUY.equals(don.trangThai) ? View.VISIBLE : View.GONE);

        btnHuy.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage("Huỷ đơn #" + don.maDonHang + "?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Huỷ đơn", (d, w) -> {
                    repo.cancelOrder(don.maDonHang);
                    Ui.toast(this, "Đã huỷ đơn hàng");
                    onResume();
                })
                .show());

        btnMuaLai.setOnClickListener(v -> {
            int added = repo.reorder(session.maTaiKhoan(), don.maDonHang);
            if (added == 0) {
                Ui.toast(this, "Không còn sản phẩm nào khả dụng để mua lại");
            } else {
                Ui.toast(this, "Đã thêm " + added + " sản phẩm vào giỏ");
                CustomerActivity.moGioHang(this);
            }
        });

        btnCapNhat.setOnClickListener(v -> chonTrangThai());
    }

    private void chonTrangThai() {
        final String[] nhan = {"Duyệt đơn → Đang giao", "Đã giao", "Huỷ đơn"};
        final String[] giaTri = {DonHang.DANG_GIAO, DonHang.DA_GIAO, DonHang.DA_HUY};
        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái đơn #" + don.maDonHang)
                .setItems(nhan, (d, which) -> {
                    repo.updateStatus(don.maDonHang, giaTri[which],
                            "Quản trị viên cập nhật: " + Ui.nhanTrangThai(giaTri[which]));
                    Ui.toast(this, "Đã cập nhật: " + Ui.nhanTrangThai(giaTri[which]));
                    onResume();
                })
                .show();
    }
}
