package com.example.quanlycaycanh.data;

/** Thực thể yếu 5 — ChiTietDonHang, khoá chính ghép (MaDonHang, MaSanPham). */
public class ChiTietDonHang {
    public int maDonHang;
    public int maSanPham;
    public int soLuong;
    public double donGia;
    public double thanhTien;

    // tiện cho hiển thị (join từ SanPham)
    public String tenCay;
    public String hinhAnh;
    public int soLuongTon;
}
