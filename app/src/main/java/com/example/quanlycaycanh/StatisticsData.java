package com.example.quanlycaycanh;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Gom số liệu cho màn Thống kê từ DBHelper,
// để Statistics.java chỉ còn việc hiển thị.
final class StatisticsData {

    // Một ngày trên biểu đồ doanh thu
    static class DayPoint {

        final String label;
        final long revenue;

        DayPoint(String label, long revenue) {

            this.label = label;
            this.revenue = revenue;
        }
    }

    // Một phần trong biểu đồ tỷ lệ hoặc tồn kho
    static class Part {

        final String name;
        final int quantity;

        Part(String name, int quantity) {

            this.name = name;
            this.quantity = quantity;
        }
    }

    // Một dòng trong top cây bán chạy
    static class TopPlant {

        final String name;
        final String image;
        final int quantity;

        TopPlant(String name, String image, int quantity) {

            this.name = name;
            this.image = image;
            this.quantity = quantity;
        }
    }

    long revenue;
    int invoiceCount;
    int itemsSold;
    int stockTotal;

    final List<DayPoint> byDay = new ArrayList<>();
    final List<Part> byCategory = new ArrayList<>();
    final List<Part> stockByCategory = new ArrayList<>();
    final List<TopPlant> topPlants = new ArrayList<>();

    static StatisticsData load(DBHelper db, long from, long to) {

        StatisticsData data = new StatisticsData();

        // Cây cảnh: số loại, tồn kho, nhóm và ảnh của từng cây
        Map<Long, DBHelper.Plant> plantsById = new LinkedHashMap<>();
        Map<String, Integer> stockGroups = new LinkedHashMap<>();

        for (DBHelper.Plant plant : db.getPlants("")) {

            plantsById.put(plant.id, plant);

            data.stockTotal += plant.stock;

            stockGroups.merge(plant.category, plant.stock, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : stockGroups.entrySet()) {

            data.stockByCategory.add(
                    new Part(entry.getKey(), entry.getValue())
            );
        }

        limitParts(data.stockByCategory);

        // Hóa đơn trong khoảng ngày đã chọn
        Map<String, Long> revenueByDay = new LinkedHashMap<>();

        for (DBHelper.Invoice invoice : db.getInvoices()) {

            if (invoice.createdAt < from || invoice.createdAt > to) {

                continue;
            }

            data.invoiceCount++;

            if (invoice.isCancelled()) {

                continue;
            }

            data.revenue += invoice.total;

            revenueByDay.merge(
                    Format.day(invoice.createdAt),
                    invoice.total,
                    Long::sum
            );
        }

        // Ngày không bán được vẫn có cột 0 để biểu đồ liền mạch
        Calendar day = Calendar.getInstance();

        day.setTimeInMillis(startOfDay(from));

        long lastDay = startOfDay(to);

        while (day.getTimeInMillis() <= lastDay) {

            String label = Format.day(day.getTimeInMillis());
            Long revenue = revenueByDay.get(label);

            data.byDay.add(new DayPoint(
                    label.substring(0, 5),
                    revenue == null ? 0 : revenue
            ));

            day.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Cây đã bán trong khoảng ngày
        List<DBHelper.SoldPlant> sold = db.getSoldPlants(from, to);
        Map<String, Integer> soldGroups = new LinkedHashMap<>();

        for (DBHelper.SoldPlant plant : sold) {

            data.itemsSold += plant.quantity;

            DBHelper.Plant info = plantsById.get(plant.plantId);

            soldGroups.merge(
                    info == null ? "Cây khác" : info.category,
                    plant.quantity,
                    Integer::sum
            );
        }

        for (Map.Entry<String, Integer> entry : soldGroups.entrySet()) {

            data.byCategory.add(
                    new Part(entry.getKey(), entry.getValue())
            );
        }

        limitParts(data.byCategory);

        // Danh sách đã sắp giảm dần theo số lượng bán
        for (int i = 0; i < sold.size() && i < 5; i++) {

            DBHelper.SoldPlant plant = sold.get(i);
            DBHelper.Plant info = plantsById.get(plant.plantId);

            data.topPlants.add(new TopPlant(
                    plant.plantName,
                    info == null ? null : info.image,
                    plant.quantity
            ));
        }

        return data;
    }

    // Tối đa 5 phần trên biểu đồ, phần nhỏ gộp vào "Khác"
    private static void limitParts(List<Part> parts) {

        parts.sort((a, b) -> b.quantity - a.quantity);

        if (parts.size() <= 5) {

            return;
        }

        int other = 0;

        while (parts.size() > 4) {

            other += parts.remove(4).quantity;
        }

        parts.add(new Part("Khác", other));
    }

    // Bỏ phần giờ phút để mỗi ngày là một mốc
    private static long startOfDay(long millis) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}
