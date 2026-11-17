package com.example.btl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reads and writes sales invoices in the shared caycanh.db database. */
public class InvoiceDatabase {
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_TODAY = "TODAY";
    public static final String FILTER_MONTH = "MONTH";
    public static final String FILTER_CANCELLED = "CANCELLED";

    private static final String TABLE_INVOICES = "invoices";
    private static final String TABLE_ITEMS = "invoice_items";

    private final DBHelper helper;

    public InvoiceDatabase(Context context) {
        helper = new DBHelper(context);
    }

    static void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_INVOICES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "invoice_code TEXT UNIQUE, "
                + "customer_name TEXT NOT NULL, "
                + "customer_phone TEXT NOT NULL, "
                + "created_at INTEGER NOT NULL, "
                + "total_amount INTEGER NOT NULL DEFAULT 0 CHECK(total_amount >= 0), "
                + "status TEXT NOT NULL DEFAULT '" + STATUS_COMPLETED + "' "
                + "CHECK(status IN ('" + STATUS_COMPLETED + "', '" + STATUS_CANCELLED + "'))"
                + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ITEMS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "invoice_id INTEGER NOT NULL, "
                + "plant_id INTEGER, "
                + "plant_name TEXT NOT NULL, "
                + "plant_category TEXT NOT NULL DEFAULT 'Khác', "
                + "unit_price INTEGER NOT NULL CHECK(unit_price >= 0), "
                + "quantity INTEGER NOT NULL CHECK(quantity > 0), "
                + "FOREIGN KEY(invoice_id) REFERENCES " + TABLE_INVOICES
                + "(id) ON DELETE CASCADE"
                + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_invoices_created_at "
                + "ON " + TABLE_INVOICES + "(created_at DESC)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice "
                + "ON " + TABLE_ITEMS + "(invoice_id)");
    }

    static void migrateToVersion5(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE_ITEMS
                + " ADD COLUMN plant_category TEXT NOT NULL DEFAULT 'Khác'");
        db.execSQL("UPDATE " + TABLE_ITEMS + " SET plant_category = COALESCE("
                + "(SELECT category FROM plants WHERE plants.id = "
                + TABLE_ITEMS + ".plant_id), 'Khác') WHERE plant_category = 'Khác'");
    }

    public static final class Invoice {
        public final long id;
        public final String code;
        public final String customerName;
        public final String customerPhone;
        public final long createdAt;
        public final long total;
        public final int productCount;
        public final String status;

        Invoice(long id, String code, String customerName, String customerPhone,
                long createdAt, long total, int productCount, String status) {
            this.id = id;
            this.code = code;
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.createdAt = createdAt;
            this.total = total;
            this.productCount = productCount;
            this.status = status;
        }
    }

    public static final class InvoiceItem {
        public final long id;
        public final long invoiceId;
        public final int plantId;
        public final String plantName;
        public final long unitPrice;
        public final int quantity;

        InvoiceItem(long id, long invoiceId, int plantId, String plantName,
                    long unitPrice, int quantity) {
            this.id = id;
            this.invoiceId = invoiceId;
            this.plantId = plantId;
            this.plantName = plantName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public long getLineTotal() {
            return unitPrice * quantity;
        }
    }

    public static final class Summary {
        public final int totalCount;
        public final int completedCount;
        public final int cancelledCount;
        public final long revenue;
        public final int productsSold;

        Summary(int totalCount, int completedCount, int cancelledCount,
                long revenue, int productsSold) {
            this.totalCount = totalCount;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
            this.revenue = revenue;
            this.productsSold = productsSold;
        }
    }

    public static final class DailyRevenue {
        public final String day;
        public final long amount;

        DailyRevenue(String day, long amount) {
            this.day = day;
            this.amount = amount;
        }
    }

    public static final class CategorySales {
        public final String category;
        public final int quantity;

        CategorySales(String category, int quantity) {
            this.category = category;
            this.quantity = quantity;
        }
    }

    public static final class ProductSales {
        public final String name;
        public final String category;
        public final int quantity;
        public final String imageUri;

        ProductSales(String name, String category, int quantity, String imageUri) {
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.imageUri = imageUri;
        }
    }

    /**
     * Saves an invoice and decrements stock atomically. The item names and prices are
     * read from SQLite here, never trusted from the UI.
     *
     * @return the newly created invoice ID
     * @throws IllegalArgumentException for invalid invoice/cart input
     * @throws IllegalStateException if a plant is missing or stock changed
     */
    public long createInvoice(String customerName, String customerPhone,
                              Map<Integer, Integer> requestedQuantities) {
        String safeName = customerName == null ? "" : customerName.trim();
        String safePhone = customerPhone == null ? "" : customerPhone.trim();
        if (safeName.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên khách hàng");
        }
        if (safePhone.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại");
        }
        if (requestedQuantities == null || requestedQuantities.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn cần có ít nhất một cây cảnh");
        }

        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : requestedQuantities.entrySet()) {
            if (entry.getKey() == null || entry.getKey() <= 0
                    || entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ");
            }
            quantities.put(entry.getKey(), entry.getValue());
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            long createdAt = System.currentTimeMillis();
            ContentValues invoiceValues = new ContentValues();
            invoiceValues.put("customer_name", safeName);
            invoiceValues.put("customer_phone", safePhone);
            invoiceValues.put("created_at", createdAt);
            invoiceValues.put("total_amount", 0);
            invoiceValues.put("status", STATUS_COMPLETED);
            long invoiceId = db.insertOrThrow(TABLE_INVOICES, null, invoiceValues);

            long total = 0;
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                int plantId = entry.getKey();
                int quantity = entry.getValue();
                String plantName;
                String plantCategory;
                long unitPrice;
                int available;
                try (Cursor cursor = db.rawQuery(
                        "SELECT name, price, stock, category FROM plants WHERE id = ? LIMIT 1",
                        new String[]{String.valueOf(plantId)})) {
                    if (!cursor.moveToFirst()) {
                        throw new IllegalStateException("Một cây trong hóa đơn không còn tồn tại");
                    }
                    plantName = cursor.getString(0);
                    unitPrice = Math.round(cursor.getDouble(1));
                    available = cursor.getInt(2);
                    plantCategory = cursor.getString(3);
                }

                if (quantity > available) {
                    throw new IllegalStateException(
                            "Tồn kho của “" + plantName + "” không đủ (còn " + available + ")");
                }

                ContentValues stockValues = new ContentValues();
                stockValues.put("stock", available - quantity);
                int stockUpdated = db.update("plants", stockValues,
                        "id = ? AND stock >= ?",
                        new String[]{String.valueOf(plantId), String.valueOf(quantity)});
                if (stockUpdated != 1) {
                    throw new IllegalStateException(
                            "Tồn kho của “" + plantName + "” vừa thay đổi, vui lòng thử lại");
                }

                ContentValues itemValues = new ContentValues();
                itemValues.put("invoice_id", invoiceId);
                itemValues.put("plant_id", plantId);
                itemValues.put("plant_name", plantName);
                itemValues.put("plant_category", plantCategory);
                itemValues.put("unit_price", unitPrice);
                itemValues.put("quantity", quantity);
                db.insertOrThrow(TABLE_ITEMS, null, itemValues);
                total = Math.addExact(total, Math.multiplyExact(unitPrice, (long) quantity));
            }

            ContentValues finalValues = new ContentValues();
            finalValues.put("invoice_code", formatInvoiceCode(invoiceId));
            finalValues.put("total_amount", total);
            db.update(TABLE_INVOICES, finalValues, "id = ?",
                    new String[]{String.valueOf(invoiceId)});
            db.setTransactionSuccessful();
            return invoiceId;
        } finally {
            db.endTransaction();
        }
    }

    public List<Invoice> getInvoices(String keyword, String filter) {
        List<Invoice> invoices = new ArrayList<>();
        StringBuilder selection = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String safe = escapeLike(keyword.trim());
            selection.append(" AND (i.invoice_code LIKE ? ESCAPE '\\'")
                    .append(" OR i.customer_name LIKE ? ESCAPE '\\'")
                    .append(" OR i.customer_phone LIKE ? ESCAPE '\\')");
            args.add("%" + safe + "%");
            args.add("%" + safe + "%");
            args.add("%" + safe + "%");
        }

        long[] range = getDateRange(filter);
        if (range != null) {
            selection.append(" AND i.created_at >= ? AND i.created_at < ?");
            args.add(String.valueOf(range[0]));
            args.add(String.valueOf(range[1]));
        } else if (FILTER_CANCELLED.equals(filter)) {
            selection.append(" AND i.status = ?");
            args.add(STATUS_CANCELLED);
        }

        String sql = "SELECT i.id, i.invoice_code, i.customer_name, i.customer_phone, "
                + "i.created_at, i.total_amount, "
                + "COALESCE(SUM(ii.quantity), 0) AS product_count, i.status "
                + "FROM " + TABLE_INVOICES + " i LEFT JOIN " + TABLE_ITEMS
                + " ii ON ii.invoice_id = i.id WHERE " + selection
                + " GROUP BY i.id ORDER BY i.created_at DESC, i.id DESC";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql, args.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                invoices.add(readInvoice(cursor));
            }
        }
        return invoices;
    }

    public Invoice getInvoice(long invoiceId) {
        String sql = "SELECT i.id, i.invoice_code, i.customer_name, i.customer_phone, "
                + "i.created_at, i.total_amount, "
                + "COALESCE(SUM(ii.quantity), 0) AS product_count, i.status "
                + "FROM " + TABLE_INVOICES + " i LEFT JOIN " + TABLE_ITEMS
                + " ii ON ii.invoice_id = i.id WHERE i.id = ? GROUP BY i.id LIMIT 1";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql, new String[]{String.valueOf(invoiceId)})) {
            return cursor.moveToFirst() ? readInvoice(cursor) : null;
        }
    }

    public List<InvoiceItem> getItems(long invoiceId) {
        List<InvoiceItem> items = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT id, invoice_id, plant_id, plant_name, unit_price, quantity "
                        + "FROM " + TABLE_ITEMS + " WHERE invoice_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(invoiceId)})) {
            while (cursor.moveToNext()) {
                items.add(new InvoiceItem(cursor.getLong(0), cursor.getLong(1),
                        cursor.isNull(2) ? -1 : cursor.getInt(2), cursor.getString(3),
                        cursor.getLong(4), cursor.getInt(5)));
            }
        }
        return items;
    }

    public Summary getSummary() {
        return getSummary(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public Summary getSummary(long fromInclusive, long toExclusive) {
        String sql = "SELECT COUNT(*), "
                + "COALESCE(SUM(CASE WHEN status = ? THEN 1 ELSE 0 END), 0), "
                + "COALESCE(SUM(CASE WHEN status = ? THEN 1 ELSE 0 END), 0), "
                + "COALESCE(SUM(CASE WHEN status = ? THEN total_amount ELSE 0 END), 0), "
                + "COALESCE((SELECT SUM(ii.quantity) FROM " + TABLE_ITEMS
                + " ii JOIN " + TABLE_INVOICES + " si ON si.id = ii.invoice_id "
                + "WHERE si.status = ? AND si.created_at >= ? AND si.created_at < ?), 0) "
                + "FROM " + TABLE_INVOICES + " WHERE created_at >= ? AND created_at < ?";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql,
                new String[]{STATUS_COMPLETED, STATUS_CANCELLED, STATUS_COMPLETED,
                        STATUS_COMPLETED, String.valueOf(fromInclusive),
                        String.valueOf(toExclusive), String.valueOf(fromInclusive),
                        String.valueOf(toExclusive)})) {
            if (cursor.moveToFirst()) {
                return new Summary(cursor.getInt(0), cursor.getInt(1),
                        cursor.getInt(2), cursor.getLong(3), cursor.getInt(4));
            }
        }
        return new Summary(0, 0, 0, 0, 0);
    }

    public List<DailyRevenue> getDailyRevenue(long fromInclusive, long toExclusive) {
        List<DailyRevenue> rows = new ArrayList<>();
        String sql = "SELECT date(created_at / 1000, 'unixepoch', 'localtime'), "
                + "SUM(total_amount) FROM " + TABLE_INVOICES
                + " WHERE status = ? AND created_at >= ? AND created_at < ?"
                + " GROUP BY date(created_at / 1000, 'unixepoch', 'localtime')"
                + " ORDER BY 1 ASC";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql,
                new String[]{STATUS_COMPLETED, String.valueOf(fromInclusive),
                        String.valueOf(toExclusive)})) {
            while (cursor.moveToNext()) {
                rows.add(new DailyRevenue(cursor.getString(0), cursor.getLong(1)));
            }
        }
        return rows;
    }

    public List<CategorySales> getCategorySales(long fromInclusive, long toExclusive) {
        List<CategorySales> rows = new ArrayList<>();
        String sql = "SELECT ii.plant_category, SUM(ii.quantity) FROM " + TABLE_ITEMS
                + " ii JOIN " + TABLE_INVOICES + " i ON i.id = ii.invoice_id"
                + " WHERE i.status = ? AND i.created_at >= ? AND i.created_at < ?"
                + " GROUP BY ii.plant_category ORDER BY SUM(ii.quantity) DESC";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql,
                new String[]{STATUS_COMPLETED, String.valueOf(fromInclusive),
                        String.valueOf(toExclusive)})) {
            while (cursor.moveToNext()) {
                rows.add(new CategorySales(cursor.getString(0), cursor.getInt(1)));
            }
        }
        return rows;
    }

    public List<ProductSales> getTopSellingPlants(
            long fromInclusive, long toExclusive, int limit) {
        List<ProductSales> rows = new ArrayList<>();
        String sql = "SELECT ii.plant_name, ii.plant_category, SUM(ii.quantity), "
                + "MAX(COALESCE(p.image_uri, '')) FROM "
                + TABLE_ITEMS + " ii JOIN " + TABLE_INVOICES
                + " i ON i.id = ii.invoice_id LEFT JOIN plants p ON p.id = ii.plant_id"
                + " WHERE i.status = ?"
                + " AND i.created_at >= ? AND i.created_at < ?"
                + " GROUP BY ii.plant_name, ii.plant_category"
                + " ORDER BY SUM(ii.quantity) DESC, ii.plant_name ASC LIMIT ?";
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(sql,
                new String[]{STATUS_COMPLETED, String.valueOf(fromInclusive),
                        String.valueOf(toExclusive), String.valueOf(Math.max(1, limit))})) {
            while (cursor.moveToNext()) {
                rows.add(new ProductSales(cursor.getString(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getString(3)));
            }
        }
        return rows;
    }

    /** Cancels one completed invoice and restores stock that still exists. */
    public boolean cancelInvoice(long invoiceId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String status;
            try (Cursor cursor = db.rawQuery(
                    "SELECT status FROM " + TABLE_INVOICES + " WHERE id = ? LIMIT 1",
                    new String[]{String.valueOf(invoiceId)})) {
                if (!cursor.moveToFirst()) return false;
                status = cursor.getString(0);
            }
            if (!STATUS_COMPLETED.equals(status)) return false;

            try (Cursor cursor = db.rawQuery(
                    "SELECT plant_id, quantity FROM " + TABLE_ITEMS + " WHERE invoice_id = ?",
                    new String[]{String.valueOf(invoiceId)})) {
                while (cursor.moveToNext()) {
                    if (cursor.isNull(0)) continue;
                    // Use an SQL expression rather than replacing stock with a stale value.
                    db.execSQL("UPDATE plants SET stock = stock + ? WHERE id = ?",
                            new Object[]{cursor.getInt(1), cursor.getInt(0)});
                }
            }

            ContentValues values = new ContentValues();
            values.put("status", STATUS_CANCELLED);
            int updated = db.update(TABLE_INVOICES, values,
                    "id = ? AND status = ?",
                    new String[]{String.valueOf(invoiceId), STATUS_COMPLETED});
            if (updated != 1) return false;

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private Invoice readInvoice(Cursor cursor) {
        long id = cursor.getLong(0);
        String code = cursor.isNull(1) ? formatInvoiceCode(id) : cursor.getString(1);
        return new Invoice(id, code, cursor.getString(2), cursor.getString(3),
                cursor.getLong(4), cursor.getLong(5), cursor.getInt(6), cursor.getString(7));
    }

    public static String formatInvoiceCode(long id) {
        return String.format(Locale.US, "HD%05d", id);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static long[] getDateRange(String filter) {
        if (!FILTER_TODAY.equals(filter) && !FILTER_MONTH.equals(filter)) return null;
        Calendar start = Calendar.getInstance();
        if (FILTER_MONTH.equals(filter)) {
            start.set(Calendar.DAY_OF_MONTH, 1);
        }
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long from = start.getTimeInMillis();
        if (FILTER_MONTH.equals(filter)) {
            start.add(Calendar.MONTH, 1);
        } else {
            start.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new long[]{from, start.getTimeInMillis()};
    }

    public void close() {
        helper.close();
    }
}
