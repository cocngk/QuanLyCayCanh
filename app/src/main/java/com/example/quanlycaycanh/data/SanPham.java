package com.example.quanlycaycanh.data;

/** Thực thể 3 — SanPham. */
public class SanPham {
    public int maSanPham;
    public String tenCay;
    public int maLoai;
    public String hinhAnh;          // biểu tượng minh hoạ (emoji) của cây
    public double giaBan;
    public int soLuongTon;
    public int soLuongDaBan;   // join: số lượng đã bán (thống kê)
    public String moTa;
    public String huongDanChamSoc;  // nhiều dòng, mỗi dòng "Nhãn: giá trị"
    public boolean dangBan = true;
    public String tenLoai;          // tiện cho hiển thị (join)
}
