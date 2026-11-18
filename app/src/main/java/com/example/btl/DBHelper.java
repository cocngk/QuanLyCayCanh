package com.example.btl;

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
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DBHelper extends SQLiteOpenHelper {

    // Tên cơ sở dữ liệu
    private static final String DB_NAME = "caycanh.db";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin12345";

    // Tăng phiên bản để cập nhật cấu trúc bảng
    private static final int DB_VERSION = 6;

    // Tên bảng
    private static final String TABLE_USERS = "users";
    private final Context appContext;

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

    // Khởi tạo SQLite
    public DBHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        appContext = context.getApplicationContext();
    }

    // ======================================
    // 1. TẠO BẢNG USERS
    // ======================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        createUsersTable(db);
        PlantDatabase.createTable(db);
        PlantDatabase.seedData(db, appContext.getPackageName());
        InvoiceDatabase.createTables(db);
        seedDefaultAdminIfNeeded(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
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
        // Nâng cấp từ bản đăng nhập v2 lên bản có quản lý cây cảnh v3.
        if (oldVersion < 3) {
            PlantDatabase.createTable(db);
            PlantDatabase.seedData(db, appContext.getPackageName());
        }
        if (oldVersion < 4) {
            PlantDatabase.migrateLegacySchema(db, appContext.getPackageName());
            InvoiceDatabase.createTables(db);
        }
        if (oldVersion == 4) {
            InvoiceDatabase.migrateToVersion5(db);
        }
        if (oldVersion < 6) {
            seedDefaultAdminIfNeeded(db);
        }
    }

    private void seedDefaultAdminIfNeeded(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " LIMIT 1", null)) {
            if (cursor.moveToFirst()) return;
        }

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = deriveHash(DEFAULT_ADMIN_PASSWORD, salt);

        ContentValues values = new ContentValues();
        values.put("full_name", "Admin");
        values.put("email", DEFAULT_ADMIN_EMAIL);
        values.putNull("phone");
        values.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP));
        values.put("password_hash", Base64.encodeToString(hash, Base64.NO_WRAP));

        db.insertWithOnConflict(
                TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
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

        if (fullName == null || contact == null || password == null
                || fullName.trim().isEmpty() || password.length() < 8) {
            return -1;
        }

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
        db.beginTransaction();
        try {
            long userId = db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
            return userId;
        } catch (SQLiteConstraintException e) {
            // Email or phone number is already registered.
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public boolean hasUsers() {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " LIMIT 1", null)) {
            return cursor.moveToFirst();
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
    // 11. HÀM BĂM MẬT KHẨU
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
