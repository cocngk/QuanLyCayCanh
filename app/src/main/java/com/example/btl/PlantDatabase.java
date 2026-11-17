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

    static void seedData(SQLiteDatabase db, String pkg) {
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
