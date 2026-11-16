package com.example.quanlycaycanh.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quanlycaycanh.PlantPhotos;

/**
 * Cơ sở dữ liệu nội bộ cho tài khoản quản trị, danh mục, sản phẩm và hóa đơn tại quầy.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quanlycaycanh.db";
    public static final int DB_VERSION = 3;

    public static final String T_TAIKHOAN = "TaiKhoan";
    public static final String T_LOAICAY = "LoaiCay";
    public static final String T_SANPHAM = "SanPham";
    public static final String T_DONHANG = "DonHang";
    public static final String T_CHITIET = "ChiTietDonHang";

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
        createAccountTable(db, T_TAIKHOAN);
        createCatalogTables(db);
        createInvoiceTables(db, T_DONHANG, T_CHITIET, T_TAIKHOAN);
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            upgradeToAdminOnly(db);
        }
        if (oldVersion < 3) {
            upgradePlantPhotoKeys(db);
        }
    }

    private static void createAccountTable(SQLiteDatabase db, String table) {
        db.execSQL("CREATE TABLE " + table + " ("
                + "MaTaiKhoan INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "HoTen TEXT NOT NULL,"
                + "Email TEXT UNIQUE,"
                + "MatKhau TEXT NOT NULL,"
                + "SoDienThoai TEXT)");
    }

    private static void createCatalogTables(SQLiteDatabase db) {
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
    }

    private static void createInvoiceTables(SQLiteDatabase db, String orderTable,
                                            String detailTable, String accountTable) {
        db.execSQL("CREATE TABLE " + orderTable + " ("
                + "MaDonHang INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "MaTaiKhoan INTEGER NOT NULL,"
                + "TenKhach TEXT NOT NULL DEFAULT '',"
                + "SoDienThoaiKhach TEXT NOT NULL DEFAULT '',"
                + "NgayDat TEXT NOT NULL,"
                + "TongTien REAL NOT NULL DEFAULT 0,"
                + "TrangThai TEXT NOT NULL,"
                + "PhuongThucThanhToan TEXT,"
                + "GhiChu TEXT,"
                + "ThoiGianCapNhat TEXT,"
                + "LichSuTrangThai TEXT,"
                + "FOREIGN KEY (MaTaiKhoan) REFERENCES " + accountTable + "(MaTaiKhoan))");

        db.execSQL("CREATE TABLE " + detailTable + " ("
                + "MaDonHang INTEGER NOT NULL,"
                + "MaSanPham INTEGER NOT NULL,"
                + "SoLuong INTEGER NOT NULL,"
                + "DonGia REAL NOT NULL,"
                + "ThanhTien REAL NOT NULL,"
                + "PRIMARY KEY (MaDonHang, MaSanPham),"
                + "FOREIGN KEY (MaDonHang) REFERENCES " + orderTable + "(MaDonHang),"
                + "FOREIGN KEY (MaSanPham) REFERENCES " + T_SANPHAM + "(MaSanPham))");
    }

    /**
     * Keep the old catalog and counter-sale invoices, but remove customer accounts,
     * carts, online orders, addresses, and chat data from the version-1 database.
     */
    private static void upgradeToAdminOnly(SQLiteDatabase db) {
        createAccountTable(db, "TaiKhoan_admin");
        db.execSQL("INSERT INTO TaiKhoan_admin "
                + "(MaTaiKhoan, HoTen, Email, MatKhau, SoDienThoai) "
                + "SELECT MaTaiKhoan, HoTen, Email, MatKhau, SoDienThoai FROM TaiKhoan "
                + "WHERE VaiTro = 'QUAN_TRI_VIEN'");
        db.execSQL("INSERT INTO TaiKhoan_admin (MaTaiKhoan, HoTen, Email, MatKhau, SoDienThoai) "
                + "SELECT 1, 'Ngô Đức Duy', 'admin@caycanh.vn', '123456', '0901234567' "
                + "WHERE NOT EXISTS (SELECT 1 FROM TaiKhoan_admin)");

        createInvoiceTables(db, "DonHang_invoice", "ChiTietDonHang_invoice", "TaiKhoan_admin");
        db.execSQL("INSERT INTO DonHang_invoice "
                + "(MaDonHang, MaTaiKhoan, TenKhach, SoDienThoaiKhach, NgayDat, TongTien, "
                + "TrangThai, PhuongThucThanhToan, GhiChu, ThoiGianCapNhat, LichSuTrangThai) "
                + "SELECT d.MaDonHang, "
                + "(SELECT MaTaiKhoan FROM TaiKhoan_admin "
                + "ORDER BY CASE WHEN Email = 'admin@caycanh.vn' THEN 0 ELSE 1 END, MaTaiKhoan LIMIT 1), "
                + "COALESCE(t.HoTen, ''), COALESCE(t.SoDienThoai, ''), d.NgayDat, d.TongTien, "
                + "d.TrangThai, d.PhuongThucThanhToan, d.GhiChu, d.ThoiGianCapNhat, d.LichSuTrangThai "
                + "FROM DonHang d LEFT JOIN TaiKhoan t ON t.MaTaiKhoan = d.MaTaiKhoan "
                + "WHERE d.LoaiDonHang = 'TAI_QUAY' AND d.TrangThai <> 'GIO'");
        db.execSQL("INSERT INTO ChiTietDonHang_invoice "
                + "(MaDonHang, MaSanPham, SoLuong, DonGia, ThanhTien) "
                + "SELECT c.MaDonHang, c.MaSanPham, c.SoLuong, c.DonGia, c.ThanhTien "
                + "FROM ChiTietDonHang c JOIN DonHang_invoice d ON d.MaDonHang = c.MaDonHang");

        db.execSQL("DROP TABLE IF EXISTS TinNhan");
        db.execSQL("DROP TABLE IF EXISTS CuocTroChuyen");
        db.execSQL("DROP TABLE ChiTietDonHang");
        db.execSQL("DROP TABLE DonHang");
        db.execSQL("DROP TABLE TaiKhoan");

        db.execSQL("ALTER TABLE TaiKhoan_admin RENAME TO TaiKhoan");
        db.execSQL("ALTER TABLE DonHang_invoice RENAME TO DonHang");
        db.execSQL("ALTER TABLE ChiTietDonHang_invoice RENAME TO ChiTietDonHang");
    }

    private static void seed(SQLiteDatabase db) {
        insertUser(db, "Ngô Đức Duy", "admin@caycanh.vn", "123456", "0901234567");

        insertCategory(db, 1, "Cây để bàn", "Cây nhỏ đặt bàn làm việc, ban công, cửa sổ.");
        insertCategory(db, 2, "Cây nội thất", "Cây trong nhà, thanh lọc không khí, ít nắng.");
        insertCategory(db, 3, "Cây phong thủy", "Cây mang ý nghĩa tài lộc, may mắn.");
        insertCategory(db, 4, "Cây ngoài trời", "Cây sân vườn, ban công lớn, cần nhiều nắng.");

        insertProduct(db, 1, "Sen đá mini", 1, PlantPhotos.SEN_DA_MINI, 45000, 25,
                "Sen đá nhỏ nhiều màu, dễ chăm, phù hợp bàn làm việc.",
                "Ánh sáng: nắng sáng 4–6 giờ\nNước: 1 lần/tuần, tưới quanh gốc\nVị trí: bàn làm việc, cửa sổ");
        insertProduct(db, 2, "Xương rồng tai thỏ", 1, PlantPhotos.XUONG_RONG_TAI_THO, 65000, 18,
                "Xương rồng tai thỏ trong chậu gốm trắng, dáng tròn dễ thương.",
                "Ánh sáng: nắng trực tiếp\nNước: 2 tuần/lần\nVị trí: ban công, cửa sổ hướng đông");
        insertProduct(db, 3, "Lưỡi hổ vàng", 2, PlantPhotos.LUOI_HO_VANG, 180000, 12,
                "Lưỡi hổ viền vàng, lọc khí tốt, sống được nơi thiếu sáng.",
                "Ánh sáng: chịu bóng bán phần\nNước: 2 tuần/lần\nVị trí: phòng khách, phòng ngủ");
        insertProduct(db, 4, "Kim tiền", 3, PlantPhotos.KIM_TIEN, 250000, 9,
                "Kim tiền lá tròn xanh mướt, biểu tượng tài lộc.",
                "Ánh sáng: sáng gián tiếp\nNước: 1 lần/tuần\nVị trí: phòng khách, quầy thu ngân");
        insertProduct(db, 5, "Phát tài", 3, PlantPhotos.PHAT_TAI, 320000, 6,
                "Cây phát tài thân mập, lá xanh, hợp không gian văn phòng.",
                "Ánh sáng: sáng gián tiếp\nNước: 7–10 ngày/lần\nVị trí: góc phòng, cạnh cửa");
        insertProduct(db, 6, "Trầu bà lá xẻ", 2, PlantPhotos.TRAU_BA_LA_XE, 150000, 14,
                "Trầu bà lá xẻ rủ dài, thích hợp treo giỏ hoặc kệ cao.",
                "Ánh sáng: bán phần\nNước: 2 lần/tuần\nVị trí: kệ cao, ban công mát");
        insertProduct(db, 7, "Cau cảnh để bàn", 1, PlantPhotos.CAU_CANH_DE_BAN, 95000, 20,
                "Cau cảnh mini thân mảnh, tạo điểm xanh cho bàn làm việc.",
                "Ánh sáng: sáng nhẹ\nNước: 2 lần/tuần\nVị trí: bàn làm việc");
        insertProduct(db, 8, "Bàng Singapore", 4, PlantPhotos.BANG_SINGAPORE, 450000, 5,
                "Bàng Singapore thân thẳng, tán đẹp, phù hợp sân vườn.",
                "Ánh sáng: nắng đầy đủ\nNước: 2 ngày/lần\nVị trí: sân trước, ban công lớn");
        insertProduct(db, 9, "Thiết mộc lan", 2, PlantPhotos.THIET_MOC_LAN, 380000, 4,
                "Thiết mộc lan cao 1m2, lá to, thanh lọc không khí.",
                "Ánh sáng: sáng gián tiếp\nNước: 1 lần/tuần\nVị trí: phòng khách rộng");
        insertProduct(db, 10, "Hoa giấy", 4, PlantPhotos.HOA_GIAY, 220000, 0,
                "Hoa giấy cánh kép, nở quanh năm khi đủ nắng.",
                "Ánh sáng: nắng trực tiếp 6 giờ\nNước: hằng ngày vào mùa khô\nVị trí: sân thượng");
        insertProduct(db, 11, "Trúc may mắn", 3, PlantPhotos.TRUC_MAY_MAN, 135000, 22,
                "Trúc may mắn trong bình gốm, hợp làm quà tặng.",
                "Ánh sáng: sáng nhẹ\nNước: thay nước 5 ngày/lần\nVị trí: bàn tiếp khách");
        insertProduct(db, 12, "Dương xỉ Boston", 2, PlantPhotos.DUONG_XI_BOSTON, 120000, 3,
                "Dương xỉ Boston lá mảnh, treo giỏ rất đẹp.",
                "Ánh sáng: bóng bán phần\nNước: 2 ngày/lần, ưa ẩm\nVị trí: ban công mát, phòng tắm");
    }

    private static void upgradePlantPhotoKeys(SQLiteDatabase db) {
        String[][] seededPhotos = {
                {"1", "🪴", PlantPhotos.SEN_DA_MINI},
                {"2", "🌵", PlantPhotos.XUONG_RONG_TAI_THO},
                {"3", "🌿", PlantPhotos.LUOI_HO_VANG},
                {"4", "🍀", PlantPhotos.KIM_TIEN},
                {"5", "🌴", PlantPhotos.PHAT_TAI},
                {"6", "🍃", PlantPhotos.TRAU_BA_LA_XE},
                {"7", "🌾", PlantPhotos.CAU_CANH_DE_BAN},
                {"8", "🌳", PlantPhotos.BANG_SINGAPORE},
                {"9", "🪷", PlantPhotos.THIET_MOC_LAN},
                {"10", "🌺", PlantPhotos.HOA_GIAY},
                {"11", "🎋", PlantPhotos.TRUC_MAY_MAN},
                {"12", "🌱", PlantPhotos.DUONG_XI_BOSTON}
        };
        for (String[] photo : seededPhotos) {
            db.execSQL("UPDATE " + T_SANPHAM + " SET HinhAnh = ? "
                            + "WHERE MaSanPham = ? AND HinhAnh = ?",
                    new Object[]{photo[2], photo[0], photo[1]});
        }
    }

    private static void insertUser(SQLiteDatabase db, String ten, String email, String pass, String sdt) {
        ContentValues v = new ContentValues();
        v.put("HoTen", ten);
        v.put("Email", email);
        v.put("MatKhau", pass);
        v.put("SoDienThoai", sdt);
        db.insert(T_TAIKHOAN, null, v);
    }

    private static void insertCategory(SQLiteDatabase db, int id, String ten, String moTa) {
        ContentValues v = new ContentValues();
        v.put("MaLoai", id);
        v.put("TenLoai", ten);
        v.put("MoTa", moTa);
        db.insert(T_LOAICAY, null, v);
    }

    private static void insertProduct(SQLiteDatabase db, int id, String ten, int maLoai, String hinhAnh,
                                      double gia, int ton, String moTa, String chamSoc) {
        ContentValues v = new ContentValues();
        v.put("MaSanPham", id);
        v.put("TenCay", ten);
        v.put("MaLoai", maLoai);
        v.put("HinhAnh", hinhAnh);
        v.put("GiaBan", gia);
        v.put("SoLuongTon", ton);
        v.put("MoTa", moTa);
        v.put("HuongDanChamSoc", chamSoc);
        v.put("DangBan", 1);
        db.insert(T_SANPHAM, null, v);
    }
}
