package com.example.quanlycaycanh.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lớp truy cập dữ liệu duy nhất của ứng dụng. Mọi truy vấn đều bám theo 7 thực thể
 * ở chương 3 báo cáo; giỏ hàng là {@code DonHang} trạng thái {@code GIO}.
 */
public class Repository {

    public static final double PHI_VAN_CHUYEN = 30000;
    public static final int NGUONG_TON_THAP = 5;

    private final DBHelper helper;

    public Repository(Context context) {
        this.helper = new DBHelper(context.getApplicationContext());
    }

    private SQLiteDatabase db() {
        return helper.getWritableDatabase();
    }

    public static String now() {
        return new SimpleDateFormat(DBHelper.DATE_FMT, Locale.US).format(new Date());
    }

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static String dayBefore(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -days);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    // ======================================================== Tài khoản (P1)

    public TaiKhoan login(String taiKhoan, String matKhau) {
        Cursor c = db().rawQuery("SELECT * FROM TaiKhoan WHERE (Email = ? OR SoDienThoai = ?) "
                + "AND MatKhau = ? LIMIT 1", new String[]{taiKhoan, taiKhoan, matKhau});
        try {
            return c.moveToFirst() ? readUser(c) : null;
        } finally {
            c.close();
        }
    }

    public TaiKhoan user(int id) {
        Cursor c = db().rawQuery("SELECT * FROM TaiKhoan WHERE MaTaiKhoan = ?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? readUser(c) : null;
        } finally {
            c.close();
        }
    }

    public boolean emailExists(String email) {
        Cursor c = db().rawQuery("SELECT 1 FROM TaiKhoan WHERE Email = ?", new String[]{email});
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    public long register(String hoTen, String email, String soDienThoai, String diaChi, String matKhau) {
        ContentValues v = new ContentValues();
        v.put("HoTen", hoTen);
        v.put("Email", email);
        v.put("SoDienThoai", soDienThoai);
        v.put("DiaChi", diaChi);
        v.put("MatKhau", matKhau);
        v.put("VaiTro", TaiKhoan.KHACH_HANG);
        return db().insert(DBHelper.T_TAIKHOAN, null, v);
    }

    public void updateProfile(TaiKhoan tk) {
        ContentValues v = new ContentValues();
        v.put("HoTen", tk.hoTen);
        v.put("Email", tk.email);
        v.put("SoDienThoai", tk.soDienThoai);
        v.put("DiaChi", tk.diaChi);
        db().update(DBHelper.T_TAIKHOAN, v, "MaTaiKhoan = ?", new String[]{String.valueOf(tk.maTaiKhoan)});
    }

    public boolean changePassword(int maTaiKhoan, String matKhauCu, String matKhauMoi) {
        ContentValues v = new ContentValues();
        v.put("MatKhau", matKhauMoi);
        int rows = db().update(DBHelper.T_TAIKHOAN, v,
                "MaTaiKhoan = ? AND MatKhau = ?",
                new String[]{String.valueOf(maTaiKhoan), matKhauCu});
        return rows > 0;
    }

    private TaiKhoan readUser(Cursor c) {
        TaiKhoan t = new TaiKhoan();
        t.maTaiKhoan = c.getInt(c.getColumnIndexOrThrow("MaTaiKhoan"));
        t.hoTen = c.getString(c.getColumnIndexOrThrow("HoTen"));
        t.email = c.getString(c.getColumnIndexOrThrow("Email"));
        t.matKhau = c.getString(c.getColumnIndexOrThrow("MatKhau"));
        t.soDienThoai = c.getString(c.getColumnIndexOrThrow("SoDienThoai"));
        t.diaChi = c.getString(c.getColumnIndexOrThrow("DiaChi"));
        t.vaiTro = c.getString(c.getColumnIndexOrThrow("VaiTro"));
        return t;
    }

    public List<TaiKhoan> customers() {
        List<TaiKhoan> list = new ArrayList<>();
        Cursor c = db().rawQuery("SELECT * FROM TaiKhoan WHERE VaiTro = ? ORDER BY HoTen",
                new String[]{TaiKhoan.KHACH_HANG});
        try {
            while (c.moveToNext()) {
                list.add(readUser(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    // ======================================================== Sản phẩm (P2)

    public List<LoaiCay> categories() {
        List<LoaiCay> list = new ArrayList<>();
        Cursor c = db().rawQuery("SELECT * FROM LoaiCay ORDER BY MaLoai", null);
        try {
            while (c.moveToNext()) {
                LoaiCay l = new LoaiCay();
                l.maLoai = c.getInt(c.getColumnIndexOrThrow("MaLoai"));
                l.tenLoai = c.getString(c.getColumnIndexOrThrow("TenLoai"));
                l.moTa = c.getString(c.getColumnIndexOrThrow("MoTa"));
                list.add(l);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public List<SanPham> products(String query, int maLoai, double giaTu, double giaDen, String sort) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.*, l.TenLoai AS TenLoai FROM SanPham s JOIN LoaiCay l ON l.MaLoai = s.MaLoai "
                        + "WHERE s.DangBan = 1");
        List<String> args = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND s.TenCay LIKE ?");
            args.add("%" + query.trim() + "%");
        }
        if (maLoai > 0) {
            sql.append(" AND s.MaLoai = ?");
            args.add(String.valueOf(maLoai));
        }
        if (giaTu > 0) {
            sql.append(" AND s.GiaBan >= ?");
            args.add(String.valueOf(giaTu));
        }
        if (giaDen > 0) {
            sql.append(" AND s.GiaBan <= ?");
            args.add(String.valueOf(giaDen));
        }
        sql.append(" ORDER BY ").append(sortClause(sort));
        return queryProducts(sql.toString(), args.toArray(new String[0]));
    }

    private static String sortClause(String sort) {
        if ("gia_tang".equals(sort)) {
            return "s.GiaBan ASC";
        }
        if ("gia_giam".equals(sort)) {
            return "s.GiaBan DESC";
        }
        if ("moi_nhat".equals(sort)) {
            return "s.MaSanPham DESC";
        }
        if ("ban_chay".equals(sort)) {
            return "s.SoLuongTon ASC";
        }
        return "s.MaLoai ASC, s.MaSanPham ASC";
    }

    public List<SanPham> featured(int limit) {
        return queryProducts("SELECT s.*, l.TenLoai AS TenLoai FROM SanPham s "
                + "JOIN LoaiCay l ON l.MaLoai = s.MaLoai WHERE s.DangBan = 1 "
                + "ORDER BY s.MaSanPham DESC LIMIT " + limit, new String[0]);
    }

    public List<SanPham> bestSelling(int limit) {
        return queryProducts("SELECT s.*, l.TenLoai AS TenLoai, "
                + "IFNULL(SUM(ct.SoLuong), 0) AS DaBan FROM SanPham s "
                + "JOIN LoaiCay l ON l.MaLoai = s.MaLoai "
                + "LEFT JOIN ChiTietDonHang ct ON ct.MaSanPham = s.MaSanPham "
                + "LEFT JOIN DonHang d ON d.MaDonHang = ct.MaDonHang "
                + "AND d.TrangThai IN ('DANG_GIAO','DA_GIAO') "
                + "WHERE s.DangBan = 1 GROUP BY s.MaSanPham "
                + "ORDER BY DaBan DESC, s.MaSanPham ASC LIMIT " + limit, new String[0]);
    }

    public List<SanPham> adminProducts(String query, int maLoai, String trangThai) {
        StringBuilder sql = new StringBuilder("SELECT s.*, l.TenLoai AS TenLoai FROM SanPham s "
                + "JOIN LoaiCay l ON l.MaLoai = s.MaLoai WHERE 1 = 1");
        List<String> args = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND s.TenCay LIKE ?");
            args.add("%" + query.trim() + "%");
        }
        if (maLoai > 0) {
            sql.append(" AND s.MaLoai = ?");
            args.add(String.valueOf(maLoai));
        }
        if ("dang_ban".equals(trangThai)) {
            sql.append(" AND s.DangBan = 1");
        } else if ("ngung_ban".equals(trangThai)) {
            sql.append(" AND s.DangBan = 0");
        }
        sql.append(" ORDER BY s.MaSanPham");
        return queryProducts(sql.toString(), args.toArray(new String[0]));
    }

    public SanPham product(int id) {
        List<SanPham> list = queryProducts("SELECT s.*, l.TenLoai AS TenLoai FROM SanPham s "
                + "JOIN LoaiCay l ON l.MaLoai = s.MaLoai WHERE s.MaSanPham = ?",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    private List<SanPham> queryProducts(String sql, String[] args) {
        List<SanPham> list = new ArrayList<>();
        Cursor c = db().rawQuery(sql, args);
        try {
            while (c.moveToNext()) {
                SanPham s = new SanPham();
                s.maSanPham = c.getInt(c.getColumnIndexOrThrow("MaSanPham"));
                s.tenCay = c.getString(c.getColumnIndexOrThrow("TenCay"));
                s.maLoai = c.getInt(c.getColumnIndexOrThrow("MaLoai"));
                s.hinhAnh = c.getString(c.getColumnIndexOrThrow("HinhAnh"));
                s.giaBan = c.getDouble(c.getColumnIndexOrThrow("GiaBan"));
                s.soLuongTon = c.getInt(c.getColumnIndexOrThrow("SoLuongTon"));
                s.moTa = c.getString(c.getColumnIndexOrThrow("MoTa"));
                s.huongDanChamSoc = c.getString(c.getColumnIndexOrThrow("HuongDanChamSoc"));
                s.dangBan = c.getInt(c.getColumnIndexOrThrow("DangBan")) == 1;
                s.tenLoai = c.getString(c.getColumnIndexOrThrow("TenLoai"));
                int idx = c.getColumnIndex("DaBan");
                if (idx >= 0) {
                    s.soLuongDaBan = c.getInt(idx);
                }
                list.add(s);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public long saveProduct(SanPham s) {
        ContentValues v = new ContentValues();
        v.put("TenCay", s.tenCay);
        v.put("MaLoai", s.maLoai);
        v.put("HinhAnh", s.hinhAnh);
        v.put("GiaBan", s.giaBan);
        v.put("SoLuongTon", s.soLuongTon);
        v.put("MoTa", s.moTa);
        v.put("HuongDanChamSoc", s.huongDanChamSoc);
        v.put("DangBan", s.dangBan ? 1 : 0);
        if (s.maSanPham > 0) {
            db().update(DBHelper.T_SANPHAM, v, "MaSanPham = ?",
                    new String[]{String.valueOf(s.maSanPham)});
            return s.maSanPham;
        }
        return db().insert(DBHelper.T_SANPHAM, null, v);
    }

    /** Xoá hẳn nếu chưa từng nằm trong đơn; nếu đã có lịch sử thì chuyển sang "ngừng bán". */
    public boolean deleteOrHideProduct(int id) {
        try {
            db().delete(DBHelper.T_SANPHAM, "MaSanPham = ?", new String[]{String.valueOf(id)});
            return true;
        } catch (SQLiteException e) {
            ContentValues v = new ContentValues();
            v.put("DangBan", 0);
            db().update(DBHelper.T_SANPHAM, v, "MaSanPham = ?", new String[]{String.valueOf(id)});
            return false;
        }
    }

    // ======================================================== Giỏ hàng (P3)

    /** Giỏ là DonHang trạng thái GIO; tạo mới nếu khách chưa có. */
    public long cartId(int maTaiKhoan) {
        Cursor c = db().rawQuery("SELECT MaDonHang FROM DonHang WHERE MaTaiKhoan = ? AND TrangThai = 'GIO' "
                + "ORDER BY MaDonHang DESC LIMIT 1", new String[]{String.valueOf(maTaiKhoan)});
        long id = -1;
        try {
            if (c.moveToFirst()) {
                id = c.getLong(0);
            }
        } finally {
            c.close();
        }
        if (id > 0) {
            return id;
        }
        ContentValues v = new ContentValues();
        v.put("MaTaiKhoan", maTaiKhoan);
        v.put("NgayDat", now());
        v.put("TongTien", 0);
        v.put("TrangThai", DonHang.GIO);
        v.put("LoaiDonHang", DonHang.ONLINE);
        v.put("PhuongThucThanhToan", "COD");
        v.put("ThoiGianCapNhat", now());
        v.put("LichSuTrangThai", DonHang.GIO + " | " + now() + " | Giỏ hàng mới");
        return db().insert(DBHelper.T_DONHANG, null, v);
    }

    public List<ChiTietDonHang> cartLines(long maDonHang) {
        return lines(maDonHang);
    }

    public void addToCart(int maTaiKhoan, int maSanPham, int soLuong) {
        long cart = cartId(maTaiKhoan);
        SanPham p = product(maSanPham);
        if (p == null) {
            return;
        }
        Cursor c = db().rawQuery("SELECT SoLuong FROM ChiTietDonHang WHERE MaDonHang = ? AND MaSanPham = ?",
                new String[]{String.valueOf(cart), String.valueOf(maSanPham)});
        int hienCo = 0;
        try {
            if (c.moveToFirst()) {
                hienCo = c.getInt(0);
            }
        } finally {
            c.close();
        }
        int moi = Math.min(hienCo + soLuong, Math.max(p.soLuongTon, 1));
        setLineQty(cart, maSanPham, moi);
    }

    public void setLineQty(long maDonHang, int maSanPham, int soLuong) {
        if (soLuong <= 0) {
            removeLine(maDonHang, maSanPham);
            return;
        }
        SanPham p = product(maSanPham);
        if (p == null) {
            return;
        }
        ContentValues v = new ContentValues();
        v.put("MaDonHang", maDonHang);
        v.put("MaSanPham", maSanPham);
        v.put("SoLuong", soLuong);
        v.put("DonGia", p.giaBan);
        v.put("ThanhTien", soLuong * p.giaBan);
        db().insertWithOnConflict(DBHelper.T_CHITIET, null, v, SQLiteDatabase.CONFLICT_REPLACE);
        refreshCartTotal(maDonHang);
    }

    public void removeLine(long maDonHang, int maSanPham) {
        db().delete(DBHelper.T_CHITIET, "MaDonHang = ? AND MaSanPham = ?",
                new String[]{String.valueOf(maDonHang), String.valueOf(maSanPham)});
        refreshCartTotal(maDonHang);
    }

    public int cartCount(int maTaiKhoan) {
        Cursor c = db().rawQuery("SELECT IFNULL(SUM(ct.SoLuong),0) FROM ChiTietDonHang ct "
                + "JOIN DonHang d ON d.MaDonHang = ct.MaDonHang "
                + "WHERE d.MaTaiKhoan = ? AND d.TrangThai = 'GIO'",
                new String[]{String.valueOf(maTaiKhoan)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public double linesTotal(long maDonHang) {
        Cursor c = db().rawQuery("SELECT IFNULL(SUM(ThanhTien),0) FROM ChiTietDonHang WHERE MaDonHang = ?",
                new String[]{String.valueOf(maDonHang)});
        try {
            return c.moveToFirst() ? c.getDouble(0) : 0;
        } finally {
            c.close();
        }
    }

    private void refreshCartTotal(long maDonHang) {
        ContentValues v = new ContentValues();
        v.put("TongTien", linesTotal(maDonHang));
        v.put("ThoiGianCapNhat", now());
        db().update(DBHelper.T_DONHANG, v, "MaDonHang = ?", new String[]{String.valueOf(maDonHang)});
    }

    // ======================================================== Đơn hàng (P4)

    public long placeOrder(int maTaiKhoan, String diaChi, String ghiChu, String thanhToan) {
        long cart = cartId(maTaiKhoan);
        List<ChiTietDonHang> lines = cartLines(cart);
        if (lines.isEmpty()) {
            return -1;
        }
        for (ChiTietDonHang l : lines) {
            if (l.soLuong > l.soLuongTon) {
                return -2; // không đủ tồn kho
            }
        }
        String t = now();
        ContentValues v = new ContentValues();
        v.put("TrangThai", DonHang.CHO_XAC_NHAN);
        v.put("NgayDat", t);
        v.put("ThoiGianCapNhat", t);
        v.put("DiaChiGiao", diaChi);
        v.put("GhiChu", ghiChu);
        v.put("PhuongThucThanhToan", thanhToan);
        v.put("TongTien", linesTotal(cart) + PHI_VAN_CHUYEN);
        DonHang cu = order(cart);
        String lichSu = (cu == null || cu.lichSuTrangThai == null || cu.lichSuTrangThai.isEmpty())
                ? "" : cu.lichSuTrangThai + "\n";
        v.put("LichSuTrangThai", lichSu + DonHang.CHO_XAC_NHAN + " | " + t + " | Khách đặt hàng");
        db().update(DBHelper.T_DONHANG, v, "MaDonHang = ?", new String[]{String.valueOf(cart)});
        truKho(cart);
        return cart;
    }

    public List<DonHang> orders(int maTaiKhoan, String trangThai, String query) {
        StringBuilder sql = new StringBuilder("SELECT d.*, t.HoTen AS TenKhach, "
                + "(SELECT IFNULL(SUM(SoLuong),0) FROM ChiTietDonHang ct WHERE ct.MaDonHang = d.MaDonHang) AS SoSP "
                + "FROM DonHang d JOIN TaiKhoan t ON t.MaTaiKhoan = d.MaTaiKhoan "
                + "WHERE d.MaTaiKhoan = ? AND d.TrangThai <> 'GIO'");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maTaiKhoan));
        appendOrderFilters(sql, args, trangThai, query);
        return queryOrders(sql.toString(), args.toArray(new String[0]));
    }

    public List<DonHang> adminOrders(String trangThai, String query) {
        StringBuilder sql = new StringBuilder("SELECT d.*, t.HoTen AS TenKhach, "
                + "(SELECT IFNULL(SUM(SoLuong),0) FROM ChiTietDonHang ct WHERE ct.MaDonHang = d.MaDonHang) AS SoSP "
                + "FROM DonHang d JOIN TaiKhoan t ON t.MaTaiKhoan = d.MaTaiKhoan "
                + "WHERE d.TrangThai <> 'GIO'");
        List<String> args = new ArrayList<>();
        appendOrderFilters(sql, args, trangThai, query);
        return queryOrders(sql.toString(), args.toArray(new String[0]));
    }

    private void appendOrderFilters(StringBuilder sql, List<String> args, String trangThai, String query) {
        if (trangThai != null && !trangThai.isEmpty() && !"TAT_CA".equals(trangThai)) {
            sql.append(" AND d.TrangThai = ?");
            args.add(trangThai);
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (CAST(d.MaDonHang AS TEXT) LIKE ? OR t.HoTen LIKE ?)");
            args.add("%" + query.trim() + "%");
            args.add("%" + query.trim() + "%");
        }
        sql.append(" ORDER BY d.ThoiGianCapNhat DESC, d.MaDonHang DESC");
    }

    public List<DonHang> recentOrders(int limit) {
        return queryOrders("SELECT d.*, t.HoTen AS TenKhach, "
                + "(SELECT IFNULL(SUM(SoLuong),0) FROM ChiTietDonHang ct WHERE ct.MaDonHang = d.MaDonHang) AS SoSP "
                + "FROM DonHang d JOIN TaiKhoan t ON t.MaTaiKhoan = d.MaTaiKhoan "
                + "WHERE d.TrangThai <> 'GIO' ORDER BY d.ThoiGianCapNhat DESC, d.MaDonHang DESC LIMIT " + limit, new String[0]);
    }

    public DonHang order(long id) {
        List<DonHang> list = queryOrders("SELECT d.*, t.HoTen AS TenKhach, "
                + "(SELECT IFNULL(SUM(SoLuong),0) FROM ChiTietDonHang ct WHERE ct.MaDonHang = d.MaDonHang) AS SoSP "
                + "FROM DonHang d JOIN TaiKhoan t ON t.MaTaiKhoan = d.MaTaiKhoan WHERE d.MaDonHang = ?",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public List<ChiTietDonHang> orderLines(long maDonHang) {
        return lines(maDonHang);
    }

    private List<ChiTietDonHang> lines(long maDonHang) {
        List<ChiTietDonHang> list = new ArrayList<>();
        Cursor c = db().rawQuery("SELECT ct.*, s.TenCay, s.HinhAnh, s.SoLuongTon "
                + "FROM ChiTietDonHang ct JOIN SanPham s ON s.MaSanPham = ct.MaSanPham "
                + "WHERE ct.MaDonHang = ? ORDER BY ct.MaSanPham",
                new String[]{String.valueOf(maDonHang)});
        try {
            while (c.moveToNext()) {
                ChiTietDonHang l = new ChiTietDonHang();
                l.maDonHang = c.getInt(c.getColumnIndexOrThrow("MaDonHang"));
                l.maSanPham = c.getInt(c.getColumnIndexOrThrow("MaSanPham"));
                l.soLuong = c.getInt(c.getColumnIndexOrThrow("SoLuong"));
                l.donGia = c.getDouble(c.getColumnIndexOrThrow("DonGia"));
                l.thanhTien = c.getDouble(c.getColumnIndexOrThrow("ThanhTien"));
                l.tenCay = c.getString(c.getColumnIndexOrThrow("TenCay"));
                l.hinhAnh = c.getString(c.getColumnIndexOrThrow("HinhAnh"));
                l.soLuongTon = c.getInt(c.getColumnIndexOrThrow("SoLuongTon"));
                list.add(l);
            }
        } finally {
            c.close();
        }
        return list;
    }

    private List<DonHang> queryOrders(String sql, String[] args) {
        List<DonHang> list = new ArrayList<>();
        Cursor c = db().rawQuery(sql, args);
        try {
            while (c.moveToNext()) {
                list.add(readOrder(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    private DonHang readOrder(Cursor c) {
        DonHang d = new DonHang();
        d.maDonHang = c.getInt(c.getColumnIndexOrThrow("MaDonHang"));
        d.maTaiKhoan = c.getInt(c.getColumnIndexOrThrow("MaTaiKhoan"));
        d.ngayDat = c.getString(c.getColumnIndexOrThrow("NgayDat"));
        d.tongTien = c.getDouble(c.getColumnIndexOrThrow("TongTien"));
        d.trangThai = c.getString(c.getColumnIndexOrThrow("TrangThai"));
        d.loaiDonHang = c.getString(c.getColumnIndexOrThrow("LoaiDonHang"));
        d.phuongThucThanhToan = c.getString(c.getColumnIndexOrThrow("PhuongThucThanhToan"));
        d.diaChiGiao = c.getString(c.getColumnIndexOrThrow("DiaChiGiao"));
        d.ghiChu = c.getString(c.getColumnIndexOrThrow("GhiChu"));
        d.thoiGianCapNhat = c.getString(c.getColumnIndexOrThrow("ThoiGianCapNhat"));
        d.lichSuTrangThai = c.getString(c.getColumnIndexOrThrow("LichSuTrangThai"));
        d.tenKhach = c.getString(c.getColumnIndexOrThrow("TenKhach"));
        int idx = c.getColumnIndex("SoSP");
        if (idx >= 0) {
            d.soSanPham = c.getInt(idx);
        }
        return d;
    }

    public boolean canCancel(DonHang d) {
        return DonHang.CHO_XAC_NHAN.equals(d.trangThai);
    }

    public void cancelOrder(long maDonHang) {
        updateStatus(maDonHang, DonHang.DA_HUY, "Khách huỷ đơn");
    }

    public void updateStatus(long maDonHang, String trangThai, String ghiChu) {
        DonHang d = order(maDonHang);
        if (d == null) {
            return;
        }
        String t = now();
        ContentValues v = new ContentValues();
        v.put("TrangThai", trangThai);
        v.put("ThoiGianCapNhat", t);
        String lichSu = (d.lichSuTrangThai == null || d.lichSuTrangThai.isEmpty())
                ? "" : d.lichSuTrangThai + "\n";
        v.put("LichSuTrangThai", lichSu + trangThai + " | " + t + " | " + ghiChu);
        if (DonHang.DA_HUY.equals(trangThai) && DonHang.ONLINE.equals(d.loaiDonHang)) {
            v.put("TongTien", linesTotal(maDonHang));
        }
        db().update(DBHelper.T_DONHANG, v, "MaDonHang = ?", new String[]{String.valueOf(maDonHang)});
        if (DonHang.DA_HUY.equals(trangThai)) {
            hoanKho(maDonHang);
        }
    }

    private void truKho(long maDonHang) {
        db().execSQL("UPDATE SanPham SET SoLuongTon = SoLuongTon - ("
                + "SELECT SoLuong FROM ChiTietDonHang ct WHERE ct.MaSanPham = SanPham.MaSanPham "
                + "AND ct.MaDonHang = ?) WHERE MaSanPham IN ("
                + "SELECT MaSanPham FROM ChiTietDonHang WHERE MaDonHang = ?)",
                new Object[]{maDonHang, maDonHang});
    }

    private void hoanKho(long maDonHang) {
        db().execSQL("UPDATE SanPham SET SoLuongTon = SoLuongTon + ("
                + "SELECT SoLuong FROM ChiTietDonHang ct WHERE ct.MaSanPham = SanPham.MaSanPham "
                + "AND ct.MaDonHang = ?) WHERE MaSanPham IN ("
                + "SELECT MaSanPham FROM ChiTietDonHang WHERE MaDonHang = ?)",
                new Object[]{maDonHang, maDonHang});
    }

    /** UC24 — Quản trị viên tạo đơn trực tiếp cho khách tại quầy. */
    public long createCounterOrder(int maTaiKhoan, List<ChiTietDonHang> chon, String thanhToan, String ghiChu) {
        if (chon == null || chon.isEmpty()) {
            return -1;
        }
        for (ChiTietDonHang l : chon) {
            SanPham p = product(l.maSanPham);
            if (p == null || l.soLuong > p.soLuongTon) {
                return -2;
            }
        }
        String t = now();
        ContentValues v = new ContentValues();
        v.put("MaTaiKhoan", maTaiKhoan);
        v.put("NgayDat", t);
        v.put("TongTien", 0);
        v.put("TrangThai", DonHang.DA_GIAO);
        v.put("LoaiDonHang", DonHang.TAI_QUAY);
        v.put("PhuongThucThanhToan", thanhToan);
        v.put("GhiChu", ghiChu);
        v.put("ThoiGianCapNhat", t);
        v.put("LichSuTrangThai", DonHang.DA_GIAO + " | " + t + " | Tạo đơn tại quầy");
        long id = db().insert(DBHelper.T_DONHANG, null, v);
        double tong = 0;
        for (ChiTietDonHang l : chon) {
            SanPham p = product(l.maSanPham);
            ContentValues ct = new ContentValues();
            ct.put("MaDonHang", id);
            ct.put("MaSanPham", l.maSanPham);
            ct.put("SoLuong", l.soLuong);
            ct.put("DonGia", p.giaBan);
            ct.put("ThanhTien", p.giaBan * l.soLuong);
            tong += p.giaBan * l.soLuong;
            db().insert(DBHelper.T_CHITIET, null, ct);
        }
        ContentValues upd = new ContentValues();
        upd.put("TongTien", tong);
        db().update(DBHelper.T_DONHANG, upd, "MaDonHang = ?", new String[]{String.valueOf(id)});
        truKho(id);
        return id;
    }

    /** "Mua lại": chép các dòng của đơn cũ vào giỏ hiện tại. */
    public int reorder(int maTaiKhoan, long maDonHangCu) {
        int added = 0;
        for (ChiTietDonHang l : orderLines(maDonHangCu)) {
            SanPham p = product(l.maSanPham);
            if (p != null && p.dangBan && p.soLuongTon > 0) {
                addToCart(maTaiKhoan, l.maSanPham, Math.min(l.soLuong, p.soLuongTon));
                added++;
            }
        }
        return added;
    }

    // ======================================================== Thống kê (P5)

    public static class ThongKe {
        public double doanhThu;
        public int soDon;
        public int sanPhamDaBan;
        public int tonKho;
        public double doanhThuKyTruoc;
    }

    public ThongKe thongKe(String tuNgay, String denNgay) {
        ThongKe t = new ThongKe();
        String[] args = {tuNgay + " 00:00:00", denNgay + " 23:59:59"};
        Cursor c = db().rawQuery("SELECT IFNULL(SUM(TongTien),0), COUNT(*) FROM DonHang "
                + "WHERE TrangThai = 'DA_GIAO' AND NgayDat BETWEEN ? AND ?", args);
        try {
            if (c.moveToFirst()) {
                t.doanhThu = c.getDouble(0);
                t.soDon = c.getInt(1);
            }
        } finally {
            c.close();
        }
        Cursor c2 = db().rawQuery("SELECT IFNULL(SUM(ct.SoLuong),0) FROM ChiTietDonHang ct "
                + "JOIN DonHang d ON d.MaDonHang = ct.MaDonHang "
                + "WHERE d.TrangThai = 'DA_GIAO' AND d.NgayDat BETWEEN ? AND ?", args);
        try {
            if (c2.moveToFirst()) {
                t.sanPhamDaBan = c2.getInt(0);
            }
        } finally {
            c2.close();
        }
        Cursor c3 = db().rawQuery("SELECT IFNULL(SUM(SoLuongTon),0) FROM SanPham WHERE DangBan = 1", null);
        try {
            if (c3.moveToFirst()) {
                t.tonKho = c3.getInt(0);
            }
        } finally {
            c3.close();
        }
        return t;
    }

    /** Doanh thu theo từng ngày trong khoảng, dùng cho biểu đồ cột. */
    public Map<String, Double> doanhThuTheoNgay(String tuNgay, String denNgay) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String d : daysBetween(tuNgay, denNgay)) {
            map.put(d, 0d);
        }
        Cursor c = db().rawQuery("SELECT substr(NgayDat,1,10) AS Ngay, IFNULL(SUM(TongTien),0) "
                + "FROM DonHang WHERE TrangThai = 'DA_GIAO' AND NgayDat BETWEEN ? AND ? "
                + "GROUP BY Ngay ORDER BY Ngay",
                new String[]{tuNgay + " 00:00:00", denNgay + " 23:59:59"});
        try {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getDouble(1));
            }
        } finally {
            c.close();
        }
        return map;
    }

    private static List<String> daysBetween(String tuNgay, String denNgay) {
        List<String> days = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date from = fmt.parse(tuNgay);
            Date to = fmt.parse(denNgay);
            Calendar c = Calendar.getInstance();
            c.setTime(from);
            int guard = 0;
            while (!c.getTime().after(to) && guard++ < 366) {
                days.add(fmt.format(c.getTime()));
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (ParseException e) {
            days.add(denNgay);
        }
        return days;
    }

    public List<SanPham> topProducts(String tuNgay, String denNgay, int limit) {
        return queryProducts("SELECT s.*, l.TenLoai AS TenLoai, IFNULL(SUM(ct.SoLuong),0) AS DaBan "
                + "FROM ChiTietDonHang ct "
                + "JOIN DonHang d ON d.MaDonHang = ct.MaDonHang "
                + "JOIN SanPham s ON s.MaSanPham = ct.MaSanPham "
                + "JOIN LoaiCay l ON l.MaLoai = s.MaLoai "
                + "WHERE d.TrangThai = 'DA_GIAO' AND d.NgayDat BETWEEN ? AND ? "
                + "GROUP BY s.MaSanPham ORDER BY DaBan DESC LIMIT " + limit,
                new String[]{tuNgay + " 00:00:00", denNgay + " 23:59:59"});
    }

    public List<SanPham> lowStock(int nguong) {
        return queryProducts("SELECT s.*, l.TenLoai AS TenLoai FROM SanPham s "
                + "JOIN LoaiCay l ON l.MaLoai = s.MaLoai "
                + "WHERE s.DangBan = 1 AND s.SoLuongTon <= " + nguong
                + " ORDER BY s.SoLuongTon ASC", new String[0]);
    }

    public int countProducts() {
        return scalar("SELECT COUNT(*) FROM SanPham WHERE DangBan = 1");
    }

    public int countOrders() {
        return scalar("SELECT COUNT(*) FROM DonHang WHERE TrangThai <> 'GIO'");
    }

    public double totalRevenue() {
        Cursor c = db().rawQuery("SELECT IFNULL(SUM(TongTien),0) FROM DonHang WHERE TrangThai = 'DA_GIAO'", null);
        try {
            return c.moveToFirst() ? c.getDouble(0) : 0;
        } finally {
            c.close();
        }
    }

    private int scalar(String sql) {
        Cursor c = db().rawQuery(sql, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    // ======================================================== Trò chuyện (P6)

    public long conversationForCustomer(int maTaiKhoan) {
        Cursor c = db().rawQuery("SELECT MaCuocTroChuyen FROM CuocTroChuyen "
                + "WHERE MaTaiKhoanKhachHang = ? ORDER BY MaCuocTroChuyen DESC LIMIT 1",
                new String[]{String.valueOf(maTaiKhoan)});
        long id = -1;
        try {
            if (c.moveToFirst()) {
                id = c.getLong(0);
            }
        } finally {
            c.close();
        }
        if (id > 0) {
            return id;
        }
        ContentValues v = new ContentValues();
        v.put("MaTaiKhoanKhachHang", maTaiKhoan);
        v.put("MaTaiKhoanQuanTriVien", 1);
        v.put("NgayTao", now());
        v.put("ThoiGianCapNhat", now());
        return db().insert(DBHelper.T_CUOCTROCHUYEN, null, v);
    }

    public List<TinNhan> messages(long maCuocTroChuyen) {
        List<TinNhan> list = new ArrayList<>();
        Cursor c = db().rawQuery("SELECT * FROM TinNhan WHERE MaCuocTroChuyen = ? ORDER BY MaTinNhan",
                new String[]{String.valueOf(maCuocTroChuyen)});
        try {
            while (c.moveToNext()) {
                TinNhan t = new TinNhan();
                t.maTinNhan = c.getInt(c.getColumnIndexOrThrow("MaTinNhan"));
                t.maCuocTroChuyen = c.getInt(c.getColumnIndexOrThrow("MaCuocTroChuyen"));
                t.maTaiKhoanGui = c.getInt(c.getColumnIndexOrThrow("MaTaiKhoanGui"));
                t.noiDung = c.getString(c.getColumnIndexOrThrow("NoiDung"));
                t.thoiGianGui = c.getString(c.getColumnIndexOrThrow("ThoiGianGui"));
                t.trangThai = c.getString(c.getColumnIndexOrThrow("TrangThai"));
                list.add(t);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void sendMessage(long maCuocTroChuyen, int maNguoiGui, String noiDung) {
        ContentValues v = new ContentValues();
        v.put("MaCuocTroChuyen", maCuocTroChuyen);
        v.put("MaTaiKhoanGui", maNguoiGui);
        v.put("NoiDung", noiDung);
        v.put("ThoiGianGui", now());
        v.put("TrangThai", "DA_GUI");
        db().insert(DBHelper.T_TINNHAN, null, v);
        ContentValues upd = new ContentValues();
        upd.put("ThoiGianCapNhat", now());
        db().update(DBHelper.T_CUOCTROCHUYEN, upd, "MaCuocTroChuyen = ?",
                new String[]{String.valueOf(maCuocTroChuyen)});
    }

    public List<CuocTroChuyen> conversations() {
        List<CuocTroChuyen> list = new ArrayList<>();
        Cursor c = db().rawQuery("SELECT ct.*, tk.HoTen AS TenKhach, "
                + "(SELECT NoiDung FROM TinNhan tn WHERE tn.MaCuocTroChuyen = ct.MaCuocTroChuyen "
                + "ORDER BY tn.MaTinNhan DESC LIMIT 1) AS TinNhanCuoi "
                + "FROM CuocTroChuyen ct JOIN TaiKhoan tk ON tk.MaTaiKhoan = ct.MaTaiKhoanKhachHang "
                + "ORDER BY ct.ThoiGianCapNhat DESC", null);
        try {
            while (c.moveToNext()) {
                CuocTroChuyen cv = new CuocTroChuyen();
                cv.maCuocTroChuyen = c.getInt(c.getColumnIndexOrThrow("MaCuocTroChuyen"));
                cv.maTaiKhoanKhachHang = c.getInt(c.getColumnIndexOrThrow("MaTaiKhoanKhachHang"));
                cv.maTaiKhoanQuanTriVien = c.getInt(c.getColumnIndexOrThrow("MaTaiKhoanQuanTriVien"));
                cv.ngayTao = c.getString(c.getColumnIndexOrThrow("NgayTao"));
                cv.thoiGianCapNhat = c.getString(c.getColumnIndexOrThrow("ThoiGianCapNhat"));
                cv.tenKhach = c.getString(c.getColumnIndexOrThrow("TenKhach"));
                cv.tinNhanCuoi = c.getString(c.getColumnIndexOrThrow("TinNhanCuoi"));
                list.add(cv);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public String tenKhachCuaCuocTroChuyen(long maCuocTroChuyen) {
        Cursor c = db().rawQuery("SELECT tk.HoTen FROM CuocTroChuyen ct "
                + "JOIN TaiKhoan tk ON tk.MaTaiKhoan = ct.MaTaiKhoanKhachHang WHERE ct.MaCuocTroChuyen = ?",
                new String[]{String.valueOf(maCuocTroChuyen)});
        try {
            return c.moveToFirst() ? c.getString(0) : "Khách hàng";
        } finally {
            c.close();
        }
    }

    // ======================================================== Dùng chung

    /** Chuyển "2026-10-24 09:12:00" thành "24/10/2026 09:12". */
    public static String dinhDangThoiGian(String iso) {
        if (iso == null || iso.length() < 16) {
            return "";
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4)
                + " " + iso.substring(11, 16);
    }

    public static String dinhDangNgay(String iso) {
        if (iso == null || iso.length() < 10) {
            return "";
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }
}
