package com.example.quanlycaycanh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Patterns;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DBHelper extends SQLiteOpenHelper {

    // Tên cơ sở dữ liệu
    private static final String DB_NAME = "caycanh.db";

    // Tăng phiên bản để cập nhật cấu trúc bảng
    private static final int DB_VERSION = 3;

    // Tên bảng
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PLANTS = "plants";
    private static final String TABLE_INVOICES = "invoices";
    private static final String TABLE_INVOICE_ITEMS = "invoice_items";

    // Trạng thái hóa đơn
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Lớp chứa thông tin người dùng
    public static class User {

        public final long id;
        public final String fullName;
        public final String email;
        public final String phone;

        // Giữ trường này để tương thích với Profile.java cũ
        public final String username;

        public User(
                long id,
                String fullName,
                String email,
                String phone
        ) {

            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;

            // Không còn username thật.
            // Dùng email hoặc SĐT làm thông tin nhận diện.
            this.username = email != null ? email : phone;
        }
    }

    // Lớp chứa thông tin cây cảnh
    public static class Plant {

        public final long id;
        public final String name;
        public final String category;
        public final long price;
        public final int stock;

        // Tên file drawable của ảnh cây, có thể null
        public final String image;

        public Plant(
                long id,
                String name,
                String category,
                long price,
                int stock,
                String image
        ) {

            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.image = image;
        }
    }

    // Một dòng cây cảnh trong hóa đơn.
    // Chỉ số lượng thay đổi được, giá và tên giữ nguyên tại thời điểm lập hóa đơn.
    public static class InvoiceItem {

        public final long plantId;
        public final String plantName;
        public final long unitPrice;
        public int quantity;

        public InvoiceItem(
                long plantId,
                String plantName,
                long unitPrice,
                int quantity
        ) {

            this.plantId = plantId;
            this.plantName = plantName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public long getAmount() {

            return unitPrice * quantity;
        }
    }

    // Thông tin một hóa đơn đã lưu
    public static class Invoice {

        public final long id;
        public final String code;
        public final String customerName;
        public final String customerPhone;
        public final long createdAt;
        public final long total;
        public final String status;
        public final int itemCount;

        public Invoice(
                long id,
                String code,
                String customerName,
                String customerPhone,
                long createdAt,
                long total,
                String status,
                int itemCount
        ) {

            this.id = id;
            this.code = code;
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.createdAt = createdAt;
            this.total = total;
            this.status = status;
            this.itemCount = itemCount;
        }

        public boolean isCancelled() {

            return STATUS_CANCELLED.equals(status);
        }
    }

    // Khởi tạo SQLite
    public DBHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    // Bật kiểm tra khóa ngoại
    @Override
    public void onConfigure(SQLiteDatabase db) {

        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    // ======================================
    // 1. TẠO BẢNG USERS
    // ======================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        createUsersTable(db);

        // Bảng cho phần hóa đơn (Người 4)
        createInvoiceTables(db);

        seedPlants(db);
    }

    private void createUsersTable(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE " + TABLE_USERS + " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        "full_name TEXT NOT NULL, " +

                        "email TEXT COLLATE NOCASE UNIQUE, " +

                        "phone TEXT UNIQUE, " +

                        "salt TEXT NOT NULL, " +

                        "password_hash TEXT NOT NULL, " +

                        "CHECK(email IS NOT NULL OR phone IS NOT NULL)" +

                        ")";

        db.execSQL(sql);
    }

    // ======================================
    // 2. NÂNG CẤP DATABASE TỪ V1 SANG V2
    // ======================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 2) {

            // Đổi tên bảng cũ
            db.execSQL(
                    "ALTER TABLE users RENAME TO users_old"
            );

            // Tạo bảng mới
            createUsersTable(db);

            // Chuyển dữ liệu cũ sang bảng mới
            // Giữ lại ID, họ tên, email, SĐT, mật khẩu băm
            db.execSQL(
                    "INSERT INTO users " +
                            "(id, full_name, email, phone, salt, password_hash) " +

                            "SELECT " +
                            "id, " +
                            "full_name, " +
                            "NULLIF(LOWER(TRIM(email)), ''), " +
                            "NULLIF(TRIM(phone), ''), " +
                            "salt, " +
                            "password_hash " +

                            "FROM users_old"
            );

            // Chỉ xóa bảng cũ sau khi chuyển thành công
            db.execSQL("DROP TABLE users_old");
        }

        if (oldVersion < 3) {

            createInvoiceTables(db);

            seedPlants(db);
        }
    }

    // ======================================
    // 3. KIỂM TRA EMAIL HOẶC SĐT HỢP LỆ
    // ======================================

    public static boolean isValidContact(String contact) {

        if (contact == null) {
            return false;
        }

        contact = contact.trim();

        // Email hợp lệ
        boolean validEmail =
                Patterns.EMAIL_ADDRESS
                        .matcher(contact)
                        .matches();

        // Số điện thoại Việt Nam:
        // 10 chữ số, bắt đầu bằng 0
        boolean validPhone =
                contact.matches("0[0-9]{9}");

        return validEmail || validPhone;
    }

    // ======================================
    // 4. CHUẨN HÓA EMAIL / SĐT
    // ======================================

    private String normalizeContact(String contact) {

        return contact.trim()
                .toLowerCase(Locale.ROOT);
    }

    // ======================================
    // 5. KIỂM TRA EMAIL / SĐT ĐÃ TỒN TẠI
    // ======================================

    public boolean contactExists(String contact) {

        String value = normalizeContact(contact);

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(

                "SELECT id FROM users " +
                        "WHERE email = ? OR phone = ? " +
                        "LIMIT 1",

                new String[]{value, value}
        )) {

            return cursor.moveToFirst();
        }
    }

    // ======================================
    // 6. ĐĂNG KÝ TÀI KHOẢN
    // ======================================

    public long addUser(
            String fullName,
            String contact,
            String password
    ) {

        String value = normalizeContact(contact);

        if (!isValidContact(value)) {
            return -1;
        }

        if (fullName.trim().isEmpty()) {
            return -1;
        }

        // Phân biệt email và số điện thoại
        boolean isEmail =
                Patterns.EMAIL_ADDRESS
                        .matcher(value)
                        .matches();

        String email = null;
        String phone = null;

        if (isEmail) {

            email = value;

        } else {

            phone = value;
        }

        // Tạo salt
        byte[] salt = new byte[16];

        new SecureRandom().nextBytes(salt);

        // Băm mật khẩu
        byte[] hash = deriveHash(password, salt);

        // Chuẩn bị dữ liệu
        ContentValues values = new ContentValues();

        values.put("full_name", fullName.trim());

        // Cột không sử dụng sẽ là NULL
        values.put("email", email);
        values.put("phone", phone);

        values.put(
                "salt",
                Base64.encodeToString(
                        salt,
                        Base64.NO_WRAP
                )
        );

        values.put(
                "password_hash",
                Base64.encodeToString(
                        hash,
                        Base64.NO_WRAP
                )
        );

        SQLiteDatabase db = getWritableDatabase();

        try {

            return db.insertOrThrow(
                    TABLE_USERS,
                    null,
                    values
            );

        } catch (SQLiteConstraintException e) {

            // Email hoặc SĐT đã tồn tại
            return -1;
        }
    }

    // ======================================
    // 7. ĐĂNG NHẬP BẰNG EMAIL HOẶC SĐT
    // ======================================

    public long authenticate(
            String contact,
            String password
    ) {

        String value = normalizeContact(contact);

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(

                "SELECT id, salt, password_hash " +
                        "FROM users " +
                        "WHERE email = ? OR phone = ? " +
                        "LIMIT 1",

                new String[]{value, value}
        )) {

            if (!cursor.moveToFirst()) {

                return -1;
            }

            long userId = cursor.getLong(0);

            // Salt đã lưu
            byte[] salt = Base64.decode(
                    cursor.getString(1),
                    Base64.NO_WRAP
            );

            // Giá trị băm mật khẩu đã lưu
            byte[] storedHash = Base64.decode(
                    cursor.getString(2),
                    Base64.NO_WRAP
            );

            // Băm mật khẩu vừa nhập
            byte[] inputHash = deriveHash(
                    password,
                    salt
            );

            // So sánh mật khẩu
            boolean correct =
                    MessageDigest.isEqual(
                            storedHash,
                            inputHash
                    );

            if (correct) {

                return userId;
            }

            return -1;
        }
    }

    // ======================================
    // 8. LẤY THÔNG TIN NGƯỜI DÙNG
    // ======================================

    public User getUser(long userId) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(

                "SELECT id, full_name, email, phone " +
                        "FROM users WHERE id = ? LIMIT 1",

                new String[]{
                        String.valueOf(userId)
                }
        )) {

            if (!cursor.moveToFirst()) {

                return null;
            }

            return new User(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
        }
    }

    // ======================================
    // 9. CẬP NHẬT HỒ SƠ
    // ======================================

    public boolean updateProfile(
            long userId,
            String fullName,
            String email,
            String phone
    ) {

        String newEmail =
                email == null ? "" : email.trim();

        String newPhone =
                phone == null ? "" : phone.trim();

        if (fullName.trim().isEmpty()) {
            return false;
        }

        // Phải có ít nhất một thông tin liên hệ
        if (newEmail.isEmpty() && newPhone.isEmpty()) {
            return false;
        }

        if (!newEmail.isEmpty() &&
                !Patterns.EMAIL_ADDRESS
                        .matcher(newEmail)
                        .matches()) {

            return false;
        }

        if (!newPhone.isEmpty() &&
                !newPhone.matches("0[0-9]{9}")) {

            return false;
        }

        ContentValues values = new ContentValues();

        values.put("full_name", fullName.trim());

        if (newEmail.isEmpty()) {

            values.putNull("email");

        } else {

            values.put(
                    "email",
                    newEmail.toLowerCase(Locale.ROOT)
            );
        }

        if (newPhone.isEmpty()) {

            values.putNull("phone");

        } else {

            values.put("phone", newPhone);
        }

        try {

            int result = getWritableDatabase().update(
                    TABLE_USERS,
                    values,
                    "id = ?",
                    new String[]{
                            String.valueOf(userId)
                    }
            );

            return result == 1;

        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    // ======================================
    // 10. ĐỔI MẬT KHẨU
    // ======================================

    public boolean changePassword(
            long userId,
            String oldPassword,
            String newPassword
    ) {

        SQLiteDatabase db = getWritableDatabase();

        byte[] oldSalt;
        byte[] oldHash;

        try (Cursor cursor = db.rawQuery(

                "SELECT salt, password_hash " +
                        "FROM users WHERE id = ?",

                new String[]{
                        String.valueOf(userId)
                }
        )) {

            if (!cursor.moveToFirst()) {

                return false;
            }

            oldSalt = Base64.decode(
                    cursor.getString(0),
                    Base64.NO_WRAP
            );

            oldHash = Base64.decode(
                    cursor.getString(1),
                    Base64.NO_WRAP
            );
        }

        // Kiểm tra mật khẩu cũ
        byte[] inputHash = deriveHash(
                oldPassword,
                oldSalt
        );

        if (!MessageDigest.isEqual(
                oldHash,
                inputHash
        )) {

            return false;
        }

        // Tạo salt mới
        byte[] newSalt = new byte[16];

        new SecureRandom().nextBytes(newSalt);

        // Băm mật khẩu mới
        byte[] newHash = deriveHash(
                newPassword,
                newSalt
        );

        ContentValues values = new ContentValues();

        values.put(
                "salt",
                Base64.encodeToString(
                        newSalt,
                        Base64.NO_WRAP
                )
        );

        values.put(
                "password_hash",
                Base64.encodeToString(
                        newHash,
                        Base64.NO_WRAP
                )
        );

        int result = db.update(
                TABLE_USERS,
                values,
                "id = ?",
                new String[]{
                        String.valueOf(userId)
                }
        );

        return result == 1;
    }

    // ======================================
    // 11. TẠO BẢNG CÂY CẢNH VÀ HÓA ĐƠN
    // ======================================

    private void createInvoiceTables(SQLiteDatabase db) {

        // Cây cảnh trong cửa hàng
        db.execSQL(
                "CREATE TABLE " + TABLE_PLANTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "price INTEGER NOT NULL, " +
                        "stock INTEGER NOT NULL DEFAULT 0, " +
                        "image TEXT)"
        );

        // Hóa đơn bán hàng
        db.execSQL(
                "CREATE TABLE " + TABLE_INVOICES + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "code TEXT NOT NULL UNIQUE, " +
                        "customer_name TEXT NOT NULL, " +
                        "customer_phone TEXT, " +
                        "created_at INTEGER NOT NULL, " +
                        "total INTEGER NOT NULL, " +
                        "status TEXT NOT NULL)"
        );

        // Các dòng cây cảnh của hóa đơn
        db.execSQL(
                "CREATE TABLE " + TABLE_INVOICE_ITEMS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "invoice_id INTEGER NOT NULL, " +
                        "plant_id INTEGER, " +
                        "plant_name TEXT NOT NULL, " +
                        "unit_price INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "FOREIGN KEY(invoice_id) REFERENCES " +
                        TABLE_INVOICES + "(id) ON DELETE CASCADE)"
        );
    }

    // ======================================
    // 12. DỮ LIỆU MẪU CHO BẢNG CÂY CẢNH
    // (màn quản lý cây cảnh là phần của Người 3)
    // ======================================

    private void seedPlants(SQLiteDatabase db) {

        // Bảng đã có cây cảnh thì không thêm nữa
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PLANTS,
                null
        )) {

            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {

                return;
            }
        }

        addPlant(db, "Cây lưỡi hổ", "Cây nội thất", 120000, 35, "luoi_ho");
        addPlant(db, "Cây trầu bà", "Cây để bàn", 90000, 40, "sen");
        addPlant(db, "Cây kim tiền", "Cây phong thủy", 150000, 20, "kim_tien");
        addPlant(db, "Cẩm tú cầu", "Cây cảnh hoa", 250000, 12, "sen_da");
        addPlant(db, "Cây tùng mini", "Cây phong thủy", 180000, 15, "bonsai");
        addPlant(db, "Cây kim ngân", "Cây phong thủy", 210000, 8, "kim_ngan");
    }

    private void addPlant(
            SQLiteDatabase db,
            String name,
            String category,
            long price,
            int stock,
            String image
    ) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("category", category);
        values.put("price", price);
        values.put("stock", stock);
        values.put("image", image);

        db.insert(TABLE_PLANTS, null, values);
    }

    // ======================================
    // 13. LẤY CÂY CẢNH
    // ======================================

    public Plant getPlant(long plantId) {

        try (Cursor cursor = getReadableDatabase().rawQuery(

                "SELECT id, name, category, price, stock, image " +
                        "FROM " + TABLE_PLANTS + " WHERE id = ? LIMIT 1",

                new String[]{String.valueOf(plantId)}
        )) {

            if (!cursor.moveToFirst()) {

                return null;
            }

            return new Plant(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getInt(4),
                    cursor.getString(5)
            );
        }
    }

    public List<Plant> getPlants(String keyword) {

        String like = "%" + keyword.trim() + "%";

        List<Plant> plants = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(

                "SELECT id, name, category, price, stock, image " +
                        "FROM " + TABLE_PLANTS + " " +
                        "WHERE name LIKE ? OR category LIKE ? " +
                        "ORDER BY name",

                new String[]{like, like}
        )) {

            while (cursor.moveToNext()) {

                plants.add(new Plant(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getInt(4),
                        cursor.getString(5)
                ));
            }
        }

        return plants;
    }

    // ======================================
    // 14. LẤY DANH SÁCH HÓA ĐƠN (MỚI NHẤT TRƯỚC)
    // ======================================

    public List<Invoice> getInvoices() {

        List<Invoice> invoices = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(

                "SELECT i.id, i.code, i.customer_name, i.customer_phone, " +
                        "i.created_at, i.total, i.status, " +
                        "(SELECT COUNT(*) FROM " + TABLE_INVOICE_ITEMS +
                        " WHERE invoice_id = i.id) " +
                        "FROM " + TABLE_INVOICES + " i " +
                        "ORDER BY i.created_at DESC, i.id DESC",

                null
        )) {

            while (cursor.moveToNext()) {

                invoices.add(new Invoice(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6),
                        cursor.getInt(7)
                ));
            }
        }

        return invoices;
    }

    // ======================================
    // 15. LẤY CÁC DÒNG CỦA MỘT HÓA ĐƠN
    // ======================================

    public List<InvoiceItem> getInvoiceItems(long invoiceId) {

        List<InvoiceItem> items = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(

                "SELECT plant_id, plant_name, unit_price, quantity " +
                        "FROM " + TABLE_INVOICE_ITEMS + " " +
                        "WHERE invoice_id = ? ORDER BY id",

                new String[]{String.valueOf(invoiceId)}
        )) {

            while (cursor.moveToNext()) {

                items.add(new InvoiceItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getInt(3)
                ));
            }
        }

        return items;
    }

    // ======================================
    // 16. TẠO HÓA ĐƠN VÀ TRỪ TỒN KHO
    // ======================================

    public long createInvoice(
            String customerName,
            String customerPhone,
            List<InvoiceItem> items
    ) {

        if (customerName.trim().isEmpty() || items.isEmpty()) {

            return -1;
        }

        long total = 0;

        for (InvoiceItem item : items) {

            total += item.getAmount();
        }

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            // Số thứ tự tiếp theo, dùng làm id và mã hóa đơn
            long id = db.compileStatement(
                    "SELECT IFNULL(MAX(id), 0) + 1 FROM " + TABLE_INVOICES
            ).simpleQueryForLong();

            String phone = customerPhone.trim();

            ContentValues invoice = new ContentValues();

            invoice.put("id", id);
            invoice.put(
                    "code",
                    String.format(Locale.ROOT, "HD%06d", id)
            );
            invoice.put("customer_name", customerName.trim());
            invoice.put("customer_phone", phone.isEmpty() ? null : phone);
            invoice.put("created_at", System.currentTimeMillis());
            invoice.put("total", total);
            invoice.put("status", STATUS_DONE);

            db.insertOrThrow(TABLE_INVOICES, null, invoice);

            for (InvoiceItem item : items) {

                ContentValues line = new ContentValues();

                line.put("invoice_id", id);
                line.put("plant_id", item.plantId);
                line.put("plant_name", item.plantName);
                line.put("unit_price", item.unitPrice);
                line.put("quantity", item.quantity);

                db.insertOrThrow(TABLE_INVOICE_ITEMS, null, line);

                db.execSQL(
                        "UPDATE " + TABLE_PLANTS +
                                " SET stock = stock - ? WHERE id = ?",
                        new Object[]{item.quantity, item.plantId}
                );
            }

            db.setTransactionSuccessful();

            return id;

        } finally {

            db.endTransaction();
        }
    }

    // ======================================
    // 17. HỦY HÓA ĐƠN VÀ TRẢ LẠI TỒN KHO
    // ======================================

    public boolean cancelInvoice(long invoiceId) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            ContentValues values = new ContentValues();

            values.put("status", STATUS_CANCELLED);

            // Chỉ hóa đơn đang ở trạng thái hoàn thành mới hủy được
            int updated = db.update(
                    TABLE_INVOICES,
                    values,
                    "id = ? AND status = ?",
                    new String[]{
                            String.valueOf(invoiceId),
                            STATUS_DONE
                    }
            );

            if (updated != 1) {

                return false;
            }

            for (InvoiceItem item : getInvoiceItems(invoiceId)) {

                if (item.plantId > 0) {

                    db.execSQL(
                            "UPDATE " + TABLE_PLANTS +
                                    " SET stock = stock + ? WHERE id = ?",
                            new Object[]{item.quantity, item.plantId}
                    );
                }
            }

            db.setTransactionSuccessful();

            return true;

        } finally {

            db.endTransaction();
        }
    }

    // ======================================
    // 18. HÀM BĂM MẬT KHẨU
    // ======================================

    private static byte[] deriveHash(
            String password,
            byte[] salt
    ) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                120000,
                256
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory
                    .generateSecret(spec)
                    .getEncoded();

        } catch (GeneralSecurityException e) {

            throw new IllegalStateException(
                    "Không thể xử lý mật khẩu",
                    e
            );

        } finally {

            spec.clearPassword();
        }
    }
}