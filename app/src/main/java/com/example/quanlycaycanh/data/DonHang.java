package com.example.quanlycaycanh.data;

/** Thực thể 4 — DonHang, gộp cả lịch sử trạng thái (cột LichSuTrangThai). */
public class DonHang {
    public static final String GIO = "GIO";
    public static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    public static final String DANG_GIAO = "DANG_GIAO";
    public static final String DA_GIAO = "DA_GIAO";
    public static final String DA_HUY = "DA_HUY";

    public static final String ONLINE = "ONLINE";
    public static final String TAI_QUAY = "TAI_QUAY";

    public int maDonHang;
    public int maTaiKhoan;
    public String ngayDat;
    public double tongTien;
    public String trangThai;
    public String loaiDonHang;
    public String phuongThucThanhToan;
    public String diaChiGiao;
    public String ghiChu;
    public String thoiGianCapNhat;
    public String lichSuTrangThai;  // các dòng "trạng thái | thời gian | ghi chú"

    public String tenKhach;         // join
    public int soSanPham;           // join

    public boolean daDatHang() {
        return !GIO.equals(trangThai);
    }
}
