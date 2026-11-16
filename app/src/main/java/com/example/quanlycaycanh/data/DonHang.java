package com.example.quanlycaycanh.data;

/** Thông tin hóa đơn và lịch sử trạng thái. */
public class DonHang {
    public static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    public static final String DANG_GIAO = "DANG_GIAO";
    public static final String DA_GIAO = "DA_GIAO";
    public static final String DA_HUY = "DA_HUY";

    public int maDonHang;
    public int maTaiKhoan;
    public String tenKhach;
    public String soDienThoaiKhach;
    public String ngayDat;
    public double tongTien;
    public String trangThai;
    public String phuongThucThanhToan;
    public String ghiChu;
    public String thoiGianCapNhat;
    public String lichSuTrangThai;  // các dòng "trạng thái | thời gian | ghi chú"

    public int soSanPham;           // join
}
