package com.example.quanlycaycanh.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Cơ sở dữ liệu SQLite hiện thực đúng 7 thực thể của chương 3 báo cáo:
 * TaiKhoan, LoaiCay, SanPham, DonHang, ChiTietDonHang, CuocTroChuyen, TinNhan.
 *
 * <p>Giỏ hàng KHÔNG có bảng riêng: đó là một DonHang ở trạng thái {@code GIO}.
 * Chi tiết giỏ và chi tiết đơn dùng chung bảng {@code ChiTietDonHang}.</p>
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quanlycaycanh.db";
    public static final int DB_VERSION = 1;

    public static final String T_TAIKHOAN = "TaiKhoan";
    public static final String T_LOAICAY = "LoaiCay";
    public static final String T_SANPHAM = "SanPham";
    public static final String T_DONHANG = "DonHang";
    public static final String T_CHITIET = "ChiTietDonHang";
    public static final String T_CUOCTROCHUYEN = "CuocTroChuyen";
    public static final String T_TINNHAN = "TinNhan";

    public static final String DATE_FMT = "yyyy-MM-dd HH:mm:ss";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_TAIKHOAN + " ("
                + "MaTaiKhoan INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "HoTen TEXT NOT NULL,"
                + "Email TEXT UNIQUE,"
                + "MatKhau TEXT NOT NULL,"
                + "SoDienThoai TEXT,"
                + "DiaChi TEXT,"
                + "VaiTro TEXT NOT NULL DEFAULT 'KHACH_HANG')");

        db.execSQL("CREATE TABLE " + T_LOAICAY + " ("
                + "MaLoai INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "TenLoai TEXT NOT NULL,"
                + "MoTa TEXT)");

        db.execSQL("CREATE TABLE " + T_SANPHAM + " ("
                + "MaSanPham INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "TenCay TEXT NOT NULL,"
                + "MaLoai INTEGER NOT NULL,"
                + "HinhAnh TEXT,"
                + "GiaBan REAL NOT NULL DEFAULT 0,"
                + "SoLuongTon INTEGER NOT NULL DEFAULT 0,"
                + "MoTa TEXT,"
                + "HuongDanChamSoc TEXT,"
                + "DangBan INTEGER NOT NULL DEFAULT 1,"
                + "FOREIGN KEY (MaLoai) REFERENCES " + T_LOAICAY + "(MaLoai))");

        db.execSQL("CREATE TABLE " + T_DONHANG + " ("
                + "MaDonHang INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "MaTaiKhoan INTEGER NOT NULL,"
                + "NgayDat TEXT NOT NULL,"
                + "TongTien REAL NOT NULL DEFAULT 0,"
                + "TrangThai TEXT NOT NULL,"
                + "LoaiDonHang TEXT NOT NULL DEFAULT 'ONLINE',"
                + "PhuongThucThanhToan TEXT,"
                + "DiaChiGiao TEXT,"
                + "GhiChu TEXT,"
                + "ThoiGianCapNhat TEXT,"
                + "LichSuTrangThai TEXT,"
                + "FOREIGN KEY (MaTaiKhoan) REFERENCES " + T_TAIKHOAN + "(MaTaiKhoan))");

        db.execSQL("CREATE TABLE " + T_CHITIET + " ("
                + "MaDonHang INTEGER NOT NULL,"
                + "MaSanPham INTEGER NOT NULL,"
                + "SoLuong INTEGER NOT NULL,"
                + "DonGia REAL NOT NULL,"
                + "ThanhTien REAL NOT NULL,"
                + "PRIMARY KEY (MaDonHang, MaSanPham),"
                + "FOREIGN KEY (MaDonHang) REFERENCES " + T_DONHANG + "(MaDonHang),"
                + "FOREIGN KEY (MaSanPham) REFERENCES " + T_SANPHAM + "(MaSanPham))");

        db.execSQL("CREATE TABLE " + T_CUOCTROCHUYEN + " ("
                + "MaCuocTroChuyen INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "MaTaiKhoanKhachHang INTEGER NOT NULL,"
                + "MaTaiKhoanQuanTriVien INTEGER,"
                + "NgayTao TEXT NOT NULL,"
                + "ThoiGianCapNhat TEXT,"
                + "FOREIGN KEY (MaTaiKhoanKhachHang) REFERENCES " + T_TAIKHOAN + "(MaTaiKhoan),"
                + "FOREIGN KEY (MaTaiKhoanQuanTriVien) REFERENCES " + T_TAIKHOAN + "(MaTaiKhoan))");

        db.execSQL("CREATE TABLE " + T_TINNHAN + " ("
                + "MaTinNhan INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "MaCuocTroChuyen INTEGER NOT NULL,"
                + "MaTaiKhoanGui INTEGER NOT NULL,"
                + "NoiDung TEXT NOT NULL,"
                + "ThoiGianGui TEXT NOT NULL,"
                + "TrangThai TEXT NOT NULL DEFAULT 'DA_GUI',"
                + "FOREIGN KEY (MaCuocTroChuyen) REFERENCES " + T_CUOCTROCHUYEN + "(MaCuocTroChuyen),"
                + "FOREIGN KEY (MaTaiKhoanGui) REFERENCES " + T_TAIKHOAN + "(MaTaiKhoan))");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Đồ án: dữ liệu mẫu, tạo lại từ đầu khi đổi cấu trúc.
        db.execSQL("DROP TABLE IF EXISTS " + T_TINNHAN);
        db.execSQL("DROP TABLE IF EXISTS " + T_CUOCTROCHUYEN);
        db.execSQL("DROP TABLE IF EXISTS " + T_CHITIET);
        db.execSQL("DROP TABLE IF EXISTS " + T_DONHANG);
        db.execSQL("DROP TABLE IF EXISTS " + T_SANPHAM);
        db.execSQL("DROP TABLE IF EXISTS " + T_LOAICAY);
        db.execSQL("DROP TABLE IF EXISTS " + T_TAIKHOAN);
        onCreate(db);
    }

    // ---------------------------------------------------------------- dữ liệu mẫu

    private static String timeAgo(int daysAgo, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -daysAgo);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        return new SimpleDateFormat(DATE_FMT, Locale.US).format(c.getTime());
    }

    private static String now() {
        return new SimpleDateFormat(DATE_FMT, Locale.US).format(new Date());
    }

    private static void seed(SQLiteDatabase db) {
        // --- TaiKhoan
        insertUser(db, 1, "Ngô Đức Duy", "admin@caycanh.vn", "123456", "0901234567",
                "12 Nguyễn Trãi, Quận 1, TP.HCM", TaiKhoan.QUAN_TRI_VIEN);
        insertUser(db, 2, "Nguyễn Hữu Khánh", "khach@caycanh.vn", "123456", "0912345678",
                "45 Lê Lợi, Quận 3, TP.HCM", TaiKhoan.KHACH_HANG);
        insertUser(db, 3, "Tiêu Thị Vân Anh", "vananh@caycanh.vn", "123456", "0987654321",
                "88 Trần Hưng Đạo, Đà Nẵng", TaiKhoan.KHACH_HANG);

        // --- LoaiCay
        insertCategory(db, 1, "Cây để bàn", "Cây nhỏ đặt bàn làm việc, ban công, cửa sổ.");
        insertCategory(db, 2, "Cây nội thất", "Cây trong nhà, thanh lọc không khí, ít nắng.");
        insertCategory(db, 3, "Cây phong thủy", "Cây mang ý nghĩa tài lộc, may mắn.");
        insertCategory(db, 4, "Cây ngoài trời", "Cây sân vườn, ban công lớn, cần nhiều nắng.");

        // --- SanPham
        insertProduct(db, 1, "Sen đá mini", 1, "🪴", 45000, 25,
                "Sen đá nhỏ nhiều màu, dễ chăm, phù hợp bàn làm việc.",
                "Ánh sáng: nắng sáng 4–6 giờ\nNước: 1 lần/tuần, tưới quanh gốc\nVị trí: bàn làm việc, cửa sổ");
        insertProduct(db, 2, "Xương rồng tai thỏ", 1, "🌵", 65000, 18,
                "Xương rồng tai thỏ trong chậu gốm trắng, dáng tròn dễ thương.",
                "Ánh sáng: nắng trực tiếp\nNước: 2 tuần/lần\nVị trí: ban công, cửa sổ hướng đông");
        insertProduct(db, 3, "Lưỡi hổ vàng", 2, "🌿", 180000, 12,
                "Lưỡi hổ viền vàng, lọc khí tốt, sống được nơi thiếu sáng.",
                "Ánh sáng: chịu bóng bán phần\nNước: 2 tuần/lần\nVị trí: phòng khách, phòng ngủ");
        insertProduct(db, 4, "Kim tiền", 3, "🍀", 250000, 9,
                "Kim tiền lá tròn xanh mướt, biểu tượng tài lộc.",
                "Ánh sáng: sáng gián tiếp\nNước: 1 lần/tuần\nVị trí: phòng khách, quầy thu ngân");
        insertProduct(db, 5, "Phát tài", 3, "🌴", 320000, 6,
                "Cây phát tài thân mập, lá xanh, hợp không gian văn phòng.",
                "Ánh sáng: sáng gián tiếp\nNước: 7–10 ngày/lần\nVị trí: góc phòng, cạnh cửa");
        insertProduct(db, 6, "Trầu bà lá xẻ", 2, "🍃", 150000, 14,
                "Trầu bà lá xẻ rủ dài, thích hợp treo giỏ hoặc kệ cao.",
                "Ánh sáng: bán phần\nNước: 2 lần/tuần\nVị trí: kệ cao, ban công mát");
        insertProduct(db, 7, "Cau cảnh để bàn", 1, "🌾", 95000, 20,
                "Cau cảnh mini thân mảnh, tạo điểm xanh cho bàn làm việc.",
                "Ánh sáng: sáng nhẹ\nNước: 2 lần/tuần\nVị trí: bàn làm việc");
        insertProduct(db, 8, "Bàng Singapore", 4, "🌳", 450000, 5,
                "Bàng Singapore thân thẳng, tán đẹp, phù hợp sân vườn.",
                "Ánh sáng: nắng đầy đủ\nNước: 2 ngày/lần\nVị trí: sân trước, ban công lớn");
        insertProduct(db, 9, "Thiết mộc lan", 2, "🪷", 380000, 4,
                "Thiết mộc lan cao 1m2, lá to, thanh lọc không khí.",
                "Ánh sáng: sáng gián tiếp\nNước: 1 lần/tuần\nVị trí: phòng khách rộng");
        insertProduct(db, 10, "Hoa giấy", 4, "🌺", 220000, 0,
                "Hoa giấy cánh kép, nở quanh năm khi đủ nắng.",
                "Ánh sáng: nắng trực tiếp 6 giờ\nNước: hằng ngày vào mùa khô\nVị trí: sân thượng");
        insertProduct(db, 11, "Trúc may mắn", 3, "🎋", 135000, 22,
                "Trúc may mắn trong bình gốm, hợp làm quà tặng.",
                "Ánh sáng: sáng nhẹ\nNước: thay nước 5 ngày/lần\nVị trí: bàn tiếp khách");
        insertProduct(db, 12, "Dương xỉ Boston", 2, "🌱", 120000, 3,
                "Dương xỉ Boston lá mảnh, treo giỏ rất đẹp.",
                "Ánh sáng: bóng bán phần\nNước: 2 ngày/lần, ưa ẩm\nVị trí: ban công mát, phòng tắm");

        // --- Đơn hàng (gồm cả 1 giỏ nháp của khách số 2)
        // Giỏ nháp: chính là DonHang trạng thái GIO
        long cart = insertOrder(db, 2, timeAgo(0, 8, 15), 0, DonHang.GIO,
                DonHang.ONLINE, "COD", "45 Lê Lợi, Quận 3, TP.HCM", null,
                "GIO | " + timeAgo(0, 8, 15) + " | Giỏ nháp của khách");
        addLine(db, cart, 1, 2, 45000);
        addLine(db, cart, 6, 1, 150000);

        // Đơn chờ xác nhận
        long o2 = insertOrder(db, 2, timeAgo(1, 9, 12), 0, DonHang.CHO_XAC_NHAN,
                DonHang.ONLINE, "COD", "45 Lê Lợi, Quận 3, TP.HCM", "Giao trong giờ hành chính",
                "CHO_XAC_NHAN | " + timeAgo(1, 9, 12) + " | Khách đặt hàng");
        addLine(db, o2, 4, 1, 250000);

        // Đang giao
        long o3 = insertOrder(db, 2, timeAgo(2, 14, 30), 0, DonHang.DANG_GIAO,
                DonHang.ONLINE, "Chuyển khoản", "45 Lê Lợi, Quận 3, TP.HCM", null,
                "CHO_XAC_NHAN | " + timeAgo(2, 14, 30) + " | Khách đặt hàng\n"
                        + "DANG_GIAO | " + timeAgo(1, 8, 0) + " | Quản trị viên duyệt đơn");
        addLine(db, o3, 5, 1, 320000);

        // Đã giao (khách 3)
        long o4 = insertOrder(db, 3, timeAgo(6, 10, 5), 0, DonHang.DA_GIAO,
                DonHang.ONLINE, "COD", "88 Trần Hưng Đạo, Đà Nẵng", null,
                "CHO_XAC_NHAN | " + timeAgo(6, 10, 5) + " | Khách đặt hàng\n"
                        + "DANG_GIAO | " + timeAgo(5, 15, 20) + " | Đã giao cho đơn vị vận chuyển\n"
                        + "DA_GIAO | " + timeAgo(4, 9, 40) + " | Giao thành công");
        addLine(db, o4, 2, 2, 65000);
        addLine(db, o4, 7, 1, 95000);

        // Đã huỷ (khách 3)
        long o5 = insertOrder(db, 3, timeAgo(9, 16, 45), 0, DonHang.DA_HUY,
                DonHang.ONLINE, "COD", "88 Trần Hưng Đạo, Đà Nẵng", null,
                "CHO_XAC_NHAN | " + timeAgo(9, 16, 45) + " | Khách đặt hàng\n"
                        + "DA_HUY | " + timeAgo(9, 18, 0) + " | Khách huỷ đơn");
        addLine(db, o5, 3, 1, 180000);

        // Đơn tại quầy (bán trực tiếp)
        long o6 = insertOrder(db, 2, timeAgo(0, 10, 20), 0, DonHang.DA_GIAO,
                DonHang.TAI_QUAY, "Tiền mặt", null, "Khách mua tại cửa hàng",
                "DA_GIAO | " + timeAgo(0, 10, 20) + " | Tạo đơn tại quầy");
        addLine(db, o6, 8, 1, 450000);

        // Đơn đã giao (khách 2, 3 ngày trước)
        long o7 = insertOrder(db, 2, timeAgo(3, 11, 55), 0, DonHang.DA_GIAO,
                DonHang.ONLINE, "COD", "45 Lê Lợi, Quận 3, TP.HCM", null,
                "CHO_XAC_NHAN | " + timeAgo(3, 11, 55) + " | Khách đặt hàng\n"
                        + "DA_GIAO | " + timeAgo(2, 9, 10) + " | Giao thành công");
        addLine(db, o7, 7, 1, 95000);
        addLine(db, o7, 1, 2, 45000);

        // Đơn chờ xác nhận (khách 3, hôm nay)
        long o8 = insertOrder(db, 3, timeAgo(0, 7, 40), 0, DonHang.CHO_XAC_NHAN,
                DonHang.ONLINE, "Chuyển khoản", "88 Trần Hưng Đạo, Đà Nẵng", null,
                "CHO_XAC_NHAN | " + timeAgo(0, 7, 40) + " | Khách đặt hàng");
        addLine(db, o8, 11, 1, 135000);
        addLine(db, o8, 6, 1, 150000);

        // Đơn đang giao (khách 2, 1 ngày trước)
        long o9 = insertOrder(db, 2, timeAgo(1, 15, 5), 0, DonHang.DANG_GIAO,
                DonHang.ONLINE, "COD", "45 Lê Lợi, Quận 3, TP.HCM", null,
                "CHO_XAC_NHAN | " + timeAgo(1, 15, 5) + " | Khách đặt hàng\n"
                        + "DANG_GIAO | " + timeAgo(0, 8, 30) + " | Đang trên đường giao");
        addLine(db, o9, 9, 1, 380000);

        // --- Trò chuyện
        long conv = insertConversation(db, 2, 1, timeAgo(2, 10, 0));
        insertMessage(db, conv, 2, "Chào shop, cây Kim tiền còn hàng không ạ?", timeAgo(2, 10, 0), "DA_XEM");
        insertMessage(db, conv, 1, "Dạ còn 9 cây anh nhé, cây cao khoảng 40cm.", timeAgo(2, 10, 6), "DA_XEM");
        insertMessage(db, conv, 2, "Vậy em lấy 1 cây, giao chiều nay giúp em.", timeAgo(2, 10, 9), "DA_GUI");
    }

    private static void insertUser(SQLiteDatabase db, int id, String ten, String email, String pass,
                                   String sdt, String diaChi, String vaiTro) {
        ContentValues v = new ContentValues();
        v.put("MaTaiKhoan", id);
        v.put("HoTen", ten);
        v.put("Email", email);
        v.put("MatKhau", pass);
        v.put("SoDienThoai", sdt);
        v.put("DiaChi", diaChi);
        v.put("VaiTro", vaiTro);
        db.insert(T_TAIKHOAN, null, v);
    }

    private static void insertCategory(SQLiteDatabase db, int id, String ten, String moTa) {
        ContentValues v = new ContentValues();
        v.put("MaLoai", id);
        v.put("TenLoai", ten);
        v.put("MoTa", moTa);
        db.insert(T_LOAICAY, null, v);
    }

    private static void insertProduct(SQLiteDatabase db, int id, String ten, int maLoai, String emoji,
                                      double gia, int ton, String moTa, String chamSoc) {
        ContentValues v = new ContentValues();
        v.put("MaSanPham", id);
        v.put("TenCay", ten);
        v.put("MaLoai", maLoai);
        v.put("HinhAnh", emoji);
        v.put("GiaBan", gia);
        v.put("SoLuongTon", ton);
        v.put("MoTa", moTa);
        v.put("HuongDanChamSoc", chamSoc);
        v.put("DangBan", 1);
        db.insert(T_SANPHAM, null, v);
    }

    private static long insertOrder(SQLiteDatabase db, int maTK, String ngayDat, double tongTien,
                                    String trangThai, String loai, String thanhToan,
                                    String diaChi, String ghiChu, String lichSu) {
        ContentValues v = new ContentValues();
        v.put("MaTaiKhoan", maTK);
        v.put("NgayDat", ngayDat);
        v.put("TongTien", tongTien);
        v.put("TrangThai", trangThai);
        v.put("LoaiDonHang", loai);
        v.put("PhuongThucThanhToan", thanhToan);
        v.put("DiaChiGiao", diaChi);
        v.put("GhiChu", ghiChu);
        v.put("ThoiGianCapNhat", ngayDat);
        v.put("LichSuTrangThai", lichSu);
        return db.insert(T_DONHANG, null, v);
    }

    private static void addLine(SQLiteDatabase db, long maDon, int maSP, int soLuong, double donGia) {
        ContentValues v = new ContentValues();
        v.put("MaDonHang", maDon);
        v.put("MaSanPham", maSP);
        v.put("SoLuong", soLuong);
        v.put("DonGia", donGia);
        v.put("ThanhTien", soLuong * donGia);
        db.insert(T_CHITIET, null, v);
        // Cập nhật tổng tiền: đơn online cộng 30.000 phí vận chuyển, đơn tại quầy không phí.
        db.execSQL("UPDATE " + T_DONHANG + " SET TongTien = ("
                + "SELECT IFNULL(SUM(ThanhTien),0) FROM " + T_CHITIET
                + " WHERE MaDonHang = ?) + CASE WHEN LoaiDonHang = 'ONLINE' AND TrangThai <> 'GIO'"
                + " AND TrangThai <> 'DA_HUY' THEN 30000 ELSE 0 END WHERE MaDonHang = ?",
                new Object[]{maDon, maDon});
    }

    private static long insertConversation(SQLiteDatabase db, int khach, Integer admin, String tao) {
        ContentValues v = new ContentValues();
        v.put("MaTaiKhoanKhachHang", khach);
        if (admin != null) {
            v.put("MaTaiKhoanQuanTriVien", admin);
        }
        v.put("NgayTao", tao);
        v.put("ThoiGianCapNhat", tao);
        return db.insert(T_CUOCTROCHUYEN, null, v);
    }

    private static void insertMessage(SQLiteDatabase db, long conv, int nguoiGui, String noiDung,
                                      String thoiGian, String trangThai) {
        ContentValues v = new ContentValues();
        v.put("MaCuocTroChuyen", conv);
        v.put("MaTaiKhoanGui", nguoiGui);
        v.put("NoiDung", noiDung);
        v.put("ThoiGianGui", thoiGian);
        v.put("TrangThai", trangThai);
        db.insert(T_TINNHAN, null, v);
    }
}
