package com.example.quanlycaycanh.data;

/** Thực thể 1 — TaiKhoan. */
public class TaiKhoan {
    public static final String KHACH_HANG = "KHACH_HANG";
    public static final String QUAN_TRI_VIEN = "QUAN_TRI_VIEN";

    public int maTaiKhoan;
    public String hoTen;
    public String email;
    public String matKhau;
    public String soDienThoai;
    public String diaChi;
    public String vaiTro;

    public boolean isAdmin() {
        return QUAN_TRI_VIEN.equals(vaiTro);
    }
}
