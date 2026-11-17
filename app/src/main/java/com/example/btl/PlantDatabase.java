package com.example.btl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/** Các thao tác SQLite với cây cảnh, dùng chung caycanh.db với tài khoản. */
public class PlantDatabase {
    private static final String TABLE_PLANTS = "plants";
    private final DBHelper helper;

    public PlantDatabase(Context context) {
        helper = new DBHelper(context);
    }

    // Được gọi trong DBHelper khi tạo/nâng cấp cơ sở dữ liệu.
    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLANTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, category TEXT NOT NULL, "
                + "price REAL NOT NULL CHECK(price > 0), "
                + "stock INTEGER NOT NULL CHECK(stock >= 0), "
                + "description TEXT, image_uri TEXT)");
    }

    static void migrateLegacySchema(SQLiteDatabase db, String packageName) {
        createTable(db);
        List<String> columns = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_PLANTS + ")", null)) {
            while (cursor.moveToNext()) columns.add(cursor.getString(1));
        }

        if (!columns.contains("description")) {
            db.execSQL("ALTER TABLE " + TABLE_PLANTS + " ADD COLUMN description TEXT");
        }
        if (!columns.contains("image_uri")) {
            db.execSQL("ALTER TABLE " + TABLE_PLANTS + " ADD COLUMN image_uri TEXT");
            if (columns.contains("image")) {
                List<long[]> ids = new ArrayList<>();
                List<String> imageValues = new ArrayList<>();
                try (Cursor cursor = db.rawQuery(
                        "SELECT id, image FROM " + TABLE_PLANTS
                                + " WHERE image IS NOT NULL AND TRIM(image) <> ''", null)) {
                    while (cursor.moveToNext()) {
                        ids.add(new long[]{cursor.getLong(0)});
                        imageValues.add(cursor.getString(1));
                    }
                }
                for (int i = 0; i < ids.size(); i++) {
                    String value = imageValues.get(i).trim();
                    if (!value.contains("://")) {
                        int extension = value.lastIndexOf('.');
                        if (extension > 0) value = value.substring(0, extension);
                        value = "android.resource://" + packageName + "/drawable/" + value;
                    }
                    ContentValues update = new ContentValues();
                    update.put("image_uri", value);
                    db.update(TABLE_PLANTS, update, "id = ?",
                            new String[]{String.valueOf(ids.get(i)[0])});
                }
            }
        }
    }

    static void seedData(SQLiteDatabase db, String pkg) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLANTS, null)) {
            if (cursor.moveToFirst() && cursor.getLong(0) > 0) return;
        }
        seed(db, "Cây lưỡi hổ", "Cây nội thất", 120000, 35,
                "Dễ chăm sóc, thích hợp đặt trong nhà.", resourceUri(pkg, "seed_luoi_ho"));
        seed(db, "Cây trầu bà", "Cây để bàn", 90000, 40,
                "Cây xanh dễ trồng, phù hợp bàn làm việc.", resourceUri(pkg, "seed_trau_ba"));
        seed(db, "Cây kim tiền", "Cây phong thủy", 150000, 20,
                "Biểu tượng may mắn và tài lộc.", resourceUri(pkg, "seed_kim_tien"));
        seed(db, "Cẩm tú cầu", "Cây cảnh hoa", 250000, 12,
                "Cụm hoa nổi bật, phù hợp trang trí.", resourceUri(pkg, "seed_cam_tu_cau"));
        seed(db, "Cây tùng mini", "Cây phong thủy", 180000, 15,
                "Cây dáng nhỏ gọn, trang trí góc học tập.", resourceUri(pkg, "seed_tung_mini"));
        seed(db, "Cây sen đá", "Cây để bàn", 65000, 50,
                "Cây mọng nước, ít cần tưới.", "");
    }

    private static String resourceUri(String pkg, String fileName) {
        return "android.resource://" + pkg + "/drawable/" + fileName;
    }

    private static void seed(SQLiteDatabase db, String name, String category,
                      double price, int stock, String description, String imageUri) {
        Plant plant = new Plant(0, name, category, price, stock, description, imageUri);
        db.insertOrThrow(TABLE_PLANTS, null, toValues(plant));
    }

    private static ContentValues toValues(Plant plant) {
        ContentValues values = new ContentValues();
        values.put("name", plant.getName());
        values.put("category", plant.getCategory());
        values.put("price", plant.getPrice());
        values.put("stock", plant.getStock());
        values.put("description", plant.getDescription());
        values.put("image_uri", plant.getImageUri());
        return values;
    }

    // CREATE: trả về ID mới hoặc -1 nếu thêm thất bại.
    public long insertPlant(Plant plant) {
        return helper.getWritableDatabase().insert(TABLE_PLANTS, null, toValues(plant));
    }

    // READ: tất cả cây theo thứ tự đã thêm.
    public List<Plant> getAllPlants() {
        return searchAndFilterPlants("", "Tất cả", "Mặc định");
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT DISTINCT category FROM " + TABLE_PLANTS
                        + " WHERE TRIM(category) <> '' ORDER BY category COLLATE NOCASE ASC",
                null)) {
            while (cursor.moveToNext()) categories.add(cursor.getString(0));
        }
        return categories;
    }

    // READ: lấy một cây theo ID để điền lại form sửa.
    public Plant getPlantById(int id) {
        try (Cursor cursor = helper.getReadableDatabase().query(
                TABLE_PLANTS, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    // UPDATE: trả về số dòng thực sự được cập nhật.
    public int updatePlant(Plant plant) {
        return helper.getWritableDatabase().update(TABLE_PLANTS, toValues(plant),
                "id = ?", new String[]{String.valueOf(plant.getId())});
    }

    // DELETE: chỉ xóa bản ghi có ID tương ứng.
    public int deletePlant(int id) {
        return helper.getWritableDatabase().delete(TABLE_PLANTS,
                "id = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Tìm tên + lọc loại + sắp xếp trong cùng một truy vấn.
     * Dùng selectionArgs để không ghép trực tiếp dữ liệu người dùng vào SQL.
     */
    public List<Plant> searchAndFilterPlants(String keyword, String category, String sortOption) {
        List<Plant> results = new ArrayList<>();
        StringBuilder where = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND name LIKE ? ESCAPE '\\'");
            // Escape %, _ và \\ để người dùng tìm đúng ký tự đã nhập.
            String safe = keyword.trim().replace("\\", "\\\\")
                    .replace("%", "\\%").replace("_", "\\_");
            args.add("%" + safe + "%");
        }
        if (category != null && !category.equals("Tất cả") && !category.isEmpty()) {
            where.append(" AND category = ?");
            args.add(category);
        }

        // Chỉ dùng các ORDER BY có sẵn, không đưa chuỗi lạ vào SQL.
        String orderBy;
        if ("Giá tăng dần".equals(sortOption)) {
            orderBy = "price ASC, id ASC";
        } else if ("Giá giảm dần".equals(sortOption)) {
            orderBy = "price DESC, id ASC";
        } else if ("Tồn kho tăng dần".equals(sortOption)) {
            orderBy = "stock ASC, id ASC";
        } else if ("Tồn kho giảm dần".equals(sortOption)) {
            orderBy = "stock DESC, id ASC";
        } else {
            orderBy = "id ASC";
        }

        try (Cursor cursor = helper.getReadableDatabase().query(
                TABLE_PLANTS, null, where.toString(),
                args.toArray(new String[0]), null, null, orderBy)) {
            while (cursor.moveToNext()) {
                results.add(fromCursor(cursor));
            }
        }
        return results;
    }

    private Plant fromCursor(Cursor cursor) {
        return new Plant(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stock")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
        );
    }
    public void close() { helper.close(); }
}
