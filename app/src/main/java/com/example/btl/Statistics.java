package com.example.btl;

import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Statistics extends AppCompatActivity {

    private final int GREEN = Color.rgb(0, 115, 43);
    private final int DARK_GREEN = Color.rgb(0, 75, 29);
    private final int LIGHT_GREEN = Color.rgb(232, 248, 228);
    private final int TEXT = Color.rgb(25, 25, 25);
    private final int GRAY = Color.rgb(110, 110, 110);
    private final int[] chartColors = {
            0xFF00852E, 0xFF54C934, 0xFFFFC800, 0xFF2587DC, 0xFFAA72D6
    };
    private TextView tvFrom, tvTo;
    private TextView tvRevenueMetric, tvInvoiceMetric, tvProductsMetric, tvStockMetric;
    private LinearLayout topPlantsList;
    private LineChart revenueChart;
    private DonutChart categoryChart;
    private BarChart inventoryChart;
    private InvoiceDatabase invoiceDatabase;
    private PlantDatabase plantDatabase;
    private final Locale vietnamese = new Locale("vi", "VN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        invoiceDatabase = new InvoiceDatabase(this);
        plantDatabase = new PlantDatabase(this);

        getWindow().setStatusBarColor(DARK_GREEN);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(0);

        createScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (revenueChart != null) refreshStatistics();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private TextView text(String value, int size,
                          int color, boolean bold) {

        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        if (bold) {
            tv.setTypeface(null, Typeface.BOLD);
        }

        return tv;
    }

    private LinearLayout horizontal() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private LinearLayout vertical() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private LinearLayout.LayoutParams lp(
            int width, int height) {

        return new LinearLayout.LayoutParams(
                width < 0 ? width : dp(width),
                height < 0 ? height : dp(height)
        );
    }

    private void createScreen() {

        LinearLayout root = vertical();
        root.setBackgroundColor(Color.WHITE);

        // =========================================
        // 1. THANH TIÊU ĐỀ
        // =========================================

        LinearLayout header = horizontal();
        header.setPadding(dp(18), 0, dp(18), 0);
        header.setBackgroundColor(GREEN);

        TextView menu = text("☰", 30, Color.WHITE, false);
        header.addView(menu, lp(48, 55));

        TextView title = text(
                "Thống kê", 25, Color.WHITE, true);

        header.addView(title,
                new LinearLayout.LayoutParams(
                        0, dp(55), 1));

        root.addView(header, lp(-1, 55));

        // =========================================
        // 2. KHU VỰC CÓ THỂ CUỘN
        // =========================================

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);

        LinearLayout content = vertical();
        content.setPadding(dp(12), dp(10), dp(12), dp(12));

        scroll.addView(content);

        root.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1));

        // =========================================
        // 3. BANNER THỐNG KÊ KINH DOANH
        // =========================================

        LinearLayout banner = horizontal();
        banner.setPadding(dp(12), dp(8), dp(8), dp(8));
        banner.setBackground(bg(LIGHT_GREEN, 18));

        TextView bannerIcon = text(
                "▥", 49, GREEN, true);

        banner.addView(bannerIcon, lp(65, 105));

        LinearLayout bannerText = vertical();

        TextView bannerTitle = text(
                "Thống kê kinh doanh",
                20, DARK_GREEN, true);

        TextView bannerSub = text(
                "Doanh thu, hóa đơn, sản phẩm và tồn kho",
                13, TEXT, false);

        bannerText.addView(bannerTitle);
        bannerText.addView(bannerSub);

        banner.addView(bannerText,
                new LinearLayout.LayoutParams(
                        0, -2, 1));

        ImageView plant = new ImageView(this);
        plant.setImageResource(R.drawable.kim_tien);
        plant.setScaleType(ImageView.ScaleType.CENTER_CROP);

        banner.addView(plant, lp(80, 90));

        content.addView(banner, lp(-1, 105));

        // =========================================
        // 4. BỘ LỌC NGÀY
        // =========================================

        LinearLayout dateRow = horizontal();
        dateRow.setPadding(0, dp(16), 0, dp(12));

        Calendar firstDay = Calendar.getInstance();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        setStartOfDay(firstDay);
        Calendar today = Calendar.getInstance();
        setStartOfDay(today);
        tvFrom = text(formatDate(firstDay), 13, TEXT, false);
        tvTo = text(formatDate(today), 13, TEXT, false);

        LinearLayout fromBox = dateBox("Từ ngày", tvFrom);
        LinearLayout toBox = dateBox("Đến ngày", tvTo);

        dateRow.addView(fromBox,
                new LinearLayout.LayoutParams(0, dp(55), 1));

        View space = new View(this);
        dateRow.addView(space, lp(7, 1));

        dateRow.addView(toBox,
                new LinearLayout.LayoutParams(0, dp(55), 1));

        content.addView(dateRow);

        TextView btnStatistics = text(
                "⌕  Thống kê", 15, Color.WHITE, true);

        btnStatistics.setGravity(Gravity.CENTER);
        btnStatistics.setBackground(bg(GREEN, 12));

        content.addView(btnStatistics, lp(-1, 45));

        btnStatistics.setOnClickListener(v -> refreshStatistics());

        // =========================================
        // 5. BỐN THẺ SỐ LIỆU
        // =========================================

        LinearLayout metrics1 = horizontal();
        LinearLayout metrics2 = horizontal();

        metrics1.setPadding(0, dp(12), 0, 0);
        metrics2.setPadding(0, dp(8), 0, dp(12));

        metrics1.addView(metric(
                        "◉", "Doanh thu",
                        "0 đ", LIGHT_GREEN, GREEN),
                new LinearLayout.LayoutParams(0, dp(88), 1));

        addSpace(metrics1, 5);

        metrics1.addView(metric(
                        "▤", "Số hóa đơn",
                        "0", 0xFFE5F0FF, 0xFF2584CE),
                new LinearLayout.LayoutParams(0, dp(88), 1));

        addSpace(metrics2, 0);

        metrics2.addView(metric(
                        "♧", "Sản phẩm bán",
                        "0", 0xFFFFF5DC, 0xFF269A38),
                new LinearLayout.LayoutParams(0, dp(88), 1));

        addSpace(metrics2, 5);

        metrics2.addView(metric(
                        "⬡", "Tồn kho",
                        "0", 0xFFF2E5FF, 0xFF9250D8),
                new LinearLayout.LayoutParams(0, dp(88), 1));

        content.addView(metrics1);
        content.addView(metrics2);

        // =========================================
        // 6. BIỂU ĐỒ DOANH THU
        // =========================================

        LinearLayout chartRow1 = horizontal();

        LinearLayout revenueCard = panel(
                "▥  Doanh thu theo ngày");

        revenueChart = new LineChart(this);
        revenueCard.addView(revenueChart,
                lp(-1, 175));

        chartRow1.addView(revenueCard,
                new LinearLayout.LayoutParams(0, dp(255), 1));

        addSpace(chartRow1, 8);

        // =========================================
        // 7. BIỂU ĐỒ TỶ LỆ LOẠI CÂY
        // =========================================

        LinearLayout categoryCard = panel(
                "◷  Tỷ lệ loại cây bán");

        categoryChart = new DonutChart(this);
        categoryCard.addView(categoryChart,
                lp(-1, 175));

        chartRow1.addView(categoryCard,
                new LinearLayout.LayoutParams(0, dp(255), 1));

        content.addView(chartRow1);

        // =========================================
        // 8. TOP 5 CÂY BÁN CHẠY
        // =========================================

        LinearLayout chartRow2 = horizontal();
        chartRow2.setPadding(0, dp(10), 0, 0);

        LinearLayout topCard = panel(
                "♜  Top 5 cây bán chạy");

        topPlantsList = vertical();
        topCard.addView(topPlantsList);

        chartRow2.addView(topCard,
                new LinearLayout.LayoutParams(0, dp(260), 1));

        addSpace(chartRow2, 8);

        // =========================================
        // 9. BIỂU ĐỒ TỒN KHO
        // =========================================

        LinearLayout stockCard = panel(
                "⬡  Tồn kho theo loại");

        inventoryChart = new BarChart(this);
        stockCard.addView(inventoryChart,
                lp(-1, 215));

        chartRow2.addView(stockCard,
                new LinearLayout.LayoutParams(0, dp(260), 1));

        content.addView(chartRow2);

        // =========================================
        // 10. THANH ĐIỀU HƯỚNG DƯỚI CÙNG
        // =========================================

        LinearLayout bottom = horizontal();
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(dp(5), dp(5), dp(5), dp(5));
        bottom.setBackgroundColor(Color.WHITE);

        addNavigation(bottom, "⌂", "Trang chủ", false,
                MainActivity.class);

        addNavigation(bottom, "♧", "Cây cảnh", false,
                PlantManagementActivity.class);

        addNavigation(bottom, "▤", "Hóa đơn", false,
                InvoiceManagementActivity.class);

        addNavigation(bottom, "▥", "Thống kê", true,
                Statistics.class);

        addNavigation(bottom, "♙", "Tài khoản", false,
                Account.class);

        root.addView(bottom, lp(-1, 65));

        setContentView(root);
    }

    // =========================================
    // HÀM TẠO Ô CHỌN NGÀY
    // =========================================

    private LinearLayout dateBox(
            String label, TextView date) {

        LinearLayout box = vertical();
        box.setPadding(dp(7), dp(3), dp(5), dp(3));
        box.setBackground(bg(Color.WHITE, 10));

        box.setBackground(borderBackground());

        TextView title = text(label, 12, GRAY, false);

        box.addView(title);

        LinearLayout row = horizontal();

        TextView calendar = text("▦", 19, GRAY, true);

        row.addView(calendar, lp(25, 28));
        row.addView(date,
                new LinearLayout.LayoutParams(0, dp(28), 1));

        box.addView(row);

        box.setOnClickListener(v -> showDatePicker(date));

        return box;
    }

    private GradientDrawable borderBackground() {
        GradientDrawable d = bg(Color.WHITE, 10);
        d.setStroke(dp(1), 0xFFDADADA);
        return d;
    }

    private void showDatePicker(TextView target) {

        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    target.setText(String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            day, month + 1, year
                    ));

                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // =========================================
    // HÀM TẠO THẺ SỐ LIỆU
    // =========================================

    private LinearLayout metric(
            String icon, String label, String value,
            int background, int iconColor) {

        LinearLayout card = vertical();
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(3), dp(5), dp(3), dp(5));
        card.setBackground(bg(background, 15));

        TextView ic = text(icon, 26, iconColor, true);
        ic.setGravity(Gravity.CENTER);

        TextView name = text(label, 12, TEXT, false);
        name.setGravity(Gravity.CENTER);

        TextView number = text(value, 16,
                label.equals("Doanh thu") ||
                        label.equals("Số hóa đơn") ? GREEN : TEXT,
                true);

        number.setGravity(Gravity.CENTER);
        switch (label) {
            case "Doanh thu":
                tvRevenueMetric = number;
                break;
            case "Số hóa đơn":
                tvInvoiceMetric = number;
                break;
            case "Sản phẩm bán":
                tvProductsMetric = number;
                break;
            case "Tồn kho":
                tvStockMetric = number;
                break;
        }
        switch (label) {
            case "Doanh thu":
                tvRevenueMetric = number;
                break;
            case "Số hóa đơn":
                tvInvoiceMetric = number;
                break;
            case "Sản phẩm bán":
                tvProductsMetric = number;
                break;
            case "Tồn kho":
                tvStockMetric = number;
                break;
        }

        card.addView(ic);
        card.addView(name);
        card.addView(number);

        return card;
    }

    private void addSpace(LinearLayout row, int width) {
        View space = new View(this);
        row.addView(space, lp(width, 1));
    }

    // =========================================
    // HÀM TẠO KHUNG BIỂU ĐỒ
    // =========================================

    private LinearLayout panel(String title) {

        LinearLayout card = vertical();
        card.setPadding(dp(8), dp(8), dp(8), dp(6));
        card.setBackground(bg(Color.WHITE, 16));

        card.setElevation(dp(2));

        TextView heading = text(
                title, 14, DARK_GREEN, true);

        card.addView(heading, lp(-1, 32));

        return card;
    }

    // =========================================
    // HÀM TẠO DANH SÁCH TOP 5
    // =========================================

    private void addTopPlant(LinearLayout parent, InvoiceDatabase.ProductSales sale,
                            int maxQuantity) {

        LinearLayout row = horizontal();
        row.setPadding(0, dp(3), 0, dp(3));

        ImageView image = new ImageView(this);
        ImageStorage.display(image, sale.imageUri);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(image, lp(35, 35));

        LinearLayout info = vertical();
        info.setPadding(dp(5), 0, 0, 0);

        LinearLayout nameRow = horizontal();

        TextView plantName = text(
                sale.name, 11, TEXT, true);

        nameRow.addView(plantName,
                new LinearLayout.LayoutParams(0, dp(20), 1));

        TextView number = text(
                sale.quantity + " cây", 10, TEXT, false);

        nameRow.addView(number);

        info.addView(nameRow);

        LinearLayout progressBg = new LinearLayout(this);
        progressBg.setBackground(bg(0xFFE5E5E5, 20));
        progressBg.setWeightSum(1);

        View progress = new View(this);
        progress.setBackground(bg(
                sale.quantity >= 10 ? GREEN : 0xFF65C83D, 20));

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(0, dp(6));

        progressParams.width = 0;
        progressParams.weight = sale.quantity / (float) maxQuantity;

        progressBg.addView(progress, progressParams);

        info.addView(progressBg, lp(-1, 6));

        row.addView(info,
                new LinearLayout.LayoutParams(0, dp(38), 1));

        parent.addView(row);
    }

    private void setStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private String formatDate(Calendar calendar) {
        return new SimpleDateFormat("dd/MM/yyyy", vietnamese)
                .format(calendar.getTime());
    }

    private Calendar parseDate(TextView field) throws java.text.ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", vietnamese);
        format.setLenient(false);
        Date parsed = format.parse(field.getText().toString());
        if (parsed == null) throw new java.text.ParseException("Ngày không hợp lệ", 0);
        Calendar result = Calendar.getInstance();
        result.setTime(parsed);
        setStartOfDay(result);
        return result;
    }

    private String formatMoney(long amount) {
        return NumberFormat.getIntegerInstance(vietnamese).format(amount) + " đ";
    }

    private void refreshStatistics() {
        Calendar from;
        Calendar to;
        try {
            from = parseDate(tvFrom);
            to = parseDate(tvTo);
        } catch (java.text.ParseException error) {
            Toast.makeText(this, "Ngày lọc không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (from.after(to)) {
            Toast.makeText(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar toExclusive = (Calendar) to.clone();
        toExclusive.add(Calendar.DAY_OF_MONTH, 1);
        long fromMillis = from.getTimeInMillis();
        long toMillis = toExclusive.getTimeInMillis();

        InvoiceDatabase.Summary summary =
                invoiceDatabase.getSummary(fromMillis, toMillis);
        List<Plant> plants = plantDatabase.getAllPlants();
        long stockCount = 0;
        Map<String, Integer> stockByCategory = new LinkedHashMap<>();
        for (Plant plant : plants) {
            stockCount += plant.getStock();
            String category = plant.getCategory() == null || plant.getCategory().trim().isEmpty()
                    ? "Khác" : plant.getCategory().trim();
            stockByCategory.put(category,
                    stockByCategory.getOrDefault(category, 0) + plant.getStock());
        }

        tvRevenueMetric.setText(formatMoney(summary.revenue));
        tvInvoiceMetric.setText(NumberFormat.getIntegerInstance(vietnamese)
                .format(summary.completedCount));
        tvProductsMetric.setText(NumberFormat.getIntegerInstance(vietnamese)
                .format(summary.productsSold));
        tvStockMetric.setText(NumberFormat.getIntegerInstance(vietnamese).format(stockCount));

        Map<String, Long> dailyAmounts = new LinkedHashMap<>();
        for (InvoiceDatabase.DailyRevenue revenue :
                invoiceDatabase.getDailyRevenue(fromMillis, toMillis)) {
            dailyAmounts.put(revenue.day, revenue.amount);
        }
        updateRevenueChart(dailyAmounts, from, to);
        categoryChart.setData(invoiceDatabase.getCategorySales(fromMillis, toMillis));
        updateTopPlants(invoiceDatabase.getTopSellingPlants(fromMillis, toMillis, 5));
        inventoryChart.setData(stockByCategory);
    }

    private void updateRevenueChart(Map<String, Long> dailyAmounts,
                                    Calendar from, Calendar to) {
        Calendar chartStart = (Calendar) to.clone();
        chartStart.add(Calendar.DAY_OF_MONTH, -6);
        if (chartStart.before(from)) chartStart = (Calendar) from.clone();

        List<String> labels = new ArrayList<>();
        List<Long> amounts = new ArrayList<>();
        Calendar day = (Calendar) chartStart.clone();
        SimpleDateFormat sqlDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat axisDate = new SimpleDateFormat("dd/MM", vietnamese);
        while (!day.after(to)) {
            labels.add(axisDate.format(day.getTime()));
            amounts.add(dailyAmounts.getOrDefault(sqlDate.format(day.getTime()), 0L));
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
        revenueChart.setData(labels, amounts);
    }

    private void updateTopPlants(List<InvoiceDatabase.ProductSales> sales) {
        topPlantsList.removeAllViews();
        if (sales.isEmpty()) {
            TextView empty = text("Chưa có dữ liệu bán hàng trong khoảng ngày này",
                    12, GRAY, false);
            empty.setGravity(Gravity.CENTER);
            topPlantsList.addView(empty, lp(-1, 150));
            return;
        }
        int highest = sales.get(0).quantity;
        for (InvoiceDatabase.ProductSales sale : sales) {
            addTopPlant(topPlantsList, sale, highest);
        }
    }

    // =========================================
    // THANH ĐIỀU HƯỚNG
    // =========================================

    private void addNavigation(
            LinearLayout parent,
            String icon,
            String label,
            boolean selected,
            Class<?> destination) {

        LinearLayout item = vertical();
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(2), dp(4), dp(2), dp(4));

        if (selected) {
            item.setBackground(bg(LIGHT_GREEN, 12));
        }

        TextView ic = text(
                icon, 25,
                selected ? GREEN : 0xFF555555,
                selected);

        ic.setGravity(Gravity.CENTER);

        TextView name = text(
                label, 10,
                selected ? GREEN : TEXT,
                selected);

        name.setGravity(Gravity.CENTER);

        item.addView(ic);
        item.addView(name);

        parent.addView(item,
                new LinearLayout.LayoutParams(0, dp(58), 1));

        if (destination != null &&
                destination != Statistics.class) {

            item.setOnClickListener(v -> {

                android.content.Intent intent =
                        new android.content.Intent(
                                Statistics.this, destination);

                startActivity(intent);
            });
        }
    }

    // =========================================
    // BIỂU ĐỒ ĐƯỜNG DOANH THU
    // =========================================

    public class LineChart extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String[] days = new String[0];
        long[] values = new long[0];

        public LineChart(android.content.Context context) {
            super(context);
        }

        void setData(List<String> labels, List<Long> amounts) {
            days = labels.toArray(new String[0]);
            values = new long[amounts.size()];
            for (int i = 0; i < amounts.size(); i++) values[i] = amounts.get(i);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();

            float left = dp(27);
            float right = w - dp(5);
            float top = dp(12);
            float bottom = h - dp(28);

            long maxValue = 0;
            for (long value : values) maxValue = Math.max(maxValue, value);
            if (values.length == 0 || maxValue == 0) {
                p.setColor(GRAY);
                p.setTextSize(dp(12));
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Chưa có doanh thu", w / 2, h / 2, p);
                return;
            }
            float chartMax = maxValue * 1.15f;

            p.setStrokeWidth(dp(1));
            p.setColor(0xFFE3E3E3);

            for (int i = 0; i <= 4; i++) {

                float y = top + (bottom - top) * i / 4;

                canvas.drawLine(left, y, right, y, p);

                p.setTextSize(dp(9));
                p.setColor(TEXT);
                p.setTextAlign(Paint.Align.RIGHT);
                long axisValue = Math.round(chartMax * (4 - i) / 4f);
                canvas.drawText(formatAxisValue(axisValue),
                        left - dp(4), y + dp(3), p);

                p.setColor(0xFFE3E3E3);
            }

            float step = values.length > 1
                    ? (right - left) / (values.length - 1) : 0;

            Path line = new Path();
            Path area = new Path();

            for (int i = 0; i < values.length; i++) {

                float x = left + step * i;
                float y = bottom -
                        (values[i] / chartMax) * (bottom - top);

                if (i == 0) {
                    line.moveTo(x, y);
                    area.moveTo(x, bottom);
                    area.lineTo(x, y);
                } else {
                    line.lineTo(x, y);
                    area.lineTo(x, y);
                }
            }

            area.lineTo(left + step * (values.length - 1), bottom);
            area.close();

            p.setColor(0x553CBF55);
            canvas.drawPath(area, p);

            p.setColor(GREEN);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2.5f));
            canvas.drawPath(line, p);

            p.setStyle(Paint.Style.FILL);

            for (int i = 0; i < values.length; i++) {

                float x = left + step * i;
                float y = bottom -
                        (values[i] / chartMax) * (bottom - top);

                p.setColor(GREEN);
                canvas.drawCircle(x, y, dp(4), p);

                p.setColor(Color.WHITE);
                canvas.drawCircle(x, y, dp(2), p);

                p.setColor(TEXT);
                p.setTextSize(dp(8));
                p.setTextAlign(Paint.Align.CENTER);

                canvas.drawText(days[i],
                        x, bottom + dp(17), p);
            }
        }

    private String formatAxisValue(long amount) {
            if (amount >= 1_000_000) {
                return NumberFormat.getNumberInstance(vietnamese)
                        .format(amount / 1_000_000f) + "tr";
            }
            if (amount >= 1_000) {
                return NumberFormat.getNumberInstance(vietnamese)
                        .format(amount / 1_000f) + "k";
            }
            return String.valueOf(amount);
        }
    }

    // =========================================
    // BIỂU ĐỒ TRÒN TỶ LỆ LOẠI CÂY
    // =========================================

    public class DonutChart extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        String[] names = new String[0];
        int[] values = new int[0];
        int total = 0;

        public DonutChart(android.content.Context context) {
            super(context);
        }

        void setData(List<InvoiceDatabase.CategorySales> rows) {
            int count = Math.min(rows.size(), chartColors.length);
            names = new String[count];
            values = new int[count];
            total = 0;
            for (InvoiceDatabase.CategorySales row : rows) total += row.quantity;
            for (int i = 0; i < count; i++) {
                names[i] = rows.get(i).category;
                values[i] = rows.get(i).quantity;
            }
            if (rows.size() > count && count > 0) {
                names[count - 1] = "Khác";
                values[count - 1] = 0;
                for (int i = count - 1; i < rows.size(); i++) {
                    values[count - 1] += rows.get(i).quantity;
                }
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() * 0.30f;
            float cy = getHeight() * 0.48f;
            float radius = Math.min(
                    getWidth() * 0.21f,
                    getHeight() * 0.32f);

            if (total <= 0 || values.length == 0) {
                p.setColor(GRAY);
                p.setTextSize(dp(11));
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, cy, p);
                return;
            }

            RectF rect = new RectF(
                    cx - radius, cy - radius,
                    cx + radius, cy + radius);

            float start = -90;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(18));

            for (int i = 0; i < values.length; i++) {

                p.setColor(chartColors[i % chartColors.length]);

                float sweep = values[i] * 360f / total;

                canvas.drawArc(
                        rect, start, sweep,
                        false, p);

                start += sweep;
            }

            p.setStyle(Paint.Style.FILL);
            p.setColor(TEXT);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(dp(19));
            p.setTypeface(Typeface.DEFAULT_BOLD);

            canvas.drawText(String.valueOf(total), cx, cy, p);

            p.setTextSize(dp(11));
            p.setTypeface(Typeface.DEFAULT);

            canvas.drawText("cây", cx, cy + dp(17), p);

            // Chú thích bên phải
            for (int i = 0; i < names.length; i++) {

                float y = dp(20) + i * dp(31);

                p.setColor(chartColors[i % chartColors.length]);

                canvas.drawCircle(
                        getWidth() * 0.55f,
                        y, dp(5), p);

                p.setColor(TEXT);
                p.setTextAlign(Paint.Align.LEFT);
                p.setTextSize(dp(9));
                p.setTypeface(Typeface.DEFAULT);
                String categoryName = fitText(names[i], getWidth() * 0.25f);

                canvas.drawText(
                        categoryName,
                        getWidth() * 0.62f,
                        y + dp(3), p);

                p.setTextAlign(Paint.Align.RIGHT);
                p.setTypeface(Typeface.DEFAULT_BOLD);

                canvas.drawText(
                        Math.round(values[i] * 100f / total) + "%",
                        getWidth() - dp(3),
                        y + dp(3), p);

                p.setTypeface(Typeface.DEFAULT);
            }
        }

        private String fitText(String value, float maxWidth) {
            String original = value == null ? "" : value;
            String result = original;
            while (!result.isEmpty() && p.measureText(result + "…") > maxWidth) {
                result = result.substring(0, result.length() - 1);
            }
            return result.equals(original) ? result : result + "…";
        }
    }

    // =========================================
    // BIỂU ĐỒ THANH TỒN KHO
    // =========================================

    public class BarChart extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String[] names = new String[0];
        int[] values = new int[0];
        int maxValue;

        public BarChart(android.content.Context context) {
            super(context);
        }

        void setData(Map<String, Integer> stockByCategory) {
            List<Map.Entry<String, Integer>> entries =
                    new ArrayList<>(stockByCategory.entrySet());
            Collections.sort(entries, (first, second) ->
                    Integer.compare(second.getValue(), first.getValue()));
            int count = Math.min(entries.size(), chartColors.length);
            names = new String[count];
            values = new int[count];
            maxValue = 0;
            for (int i = 0; i < count; i++) {
                names[i] = entries.get(i).getKey();
                values[i] = entries.get(i).getValue();
                maxValue = Math.max(maxValue, values[i]);
            }
            if (entries.size() > count && count > 0) {
                names[count - 1] = "Khác";
                values[count - 1] = 0;
                for (int i = count - 1; i < entries.size(); i++) {
                    values[count - 1] += entries.get(i).getValue();
                }
                maxValue = Math.max(maxValue, values[count - 1]);
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float row = dp(37);
            if (names.length == 0 || maxValue <= 0) {
                p.setColor(GRAY);
                p.setTextSize(dp(11));
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Chưa có cây trong kho", w / 2f, getHeight() / 2f, p);
                return;
            }

            p.setTextSize(dp(10));
            p.setColor(TEXT);
            p.setTextAlign(Paint.Align.RIGHT);

            for (int i = 0; i < names.length; i++) {

                float y = dp(23) + i * row;
                String label = fitLabel(names[i], w * 0.35f);
                canvas.drawText(label, w * 0.39f, y + dp(4), p);

                float startX = w * 0.42f;
                float maxWidth = w * 0.34f;

                p.setColor(0xFFEAEAEA);
                canvas.drawRoundRect(
                        startX, y - dp(8),
                        startX + maxWidth, y + dp(7),
                        dp(5), dp(5), p);

                p.setColor(chartColors[i % chartColors.length]);

                float barWidth = maxWidth * values[i] / maxValue;

                canvas.drawRoundRect(
                        startX, y - dp(8),
                        startX + barWidth, y + dp(7),
                        dp(5), dp(5), p);

                p.setColor(TEXT);
                p.setTextAlign(Paint.Align.LEFT);
                String value = String.valueOf(values[i]);
                float valueX = Math.min(startX + barWidth + dp(4),
                        w - p.measureText(value) - dp(2));
                canvas.drawText(value, valueX, y + dp(4), p);

                p.setTextAlign(Paint.Align.RIGHT);
            }

            p.setColor(GRAY);
            p.setTextSize(dp(9));

            for (int i = 0; i <= 4; i++) {

                float x = w * 0.42f +
                        w * 0.34f * i / 4;

                p.setTextAlign(Paint.Align.CENTER);

                canvas.drawText(
                        String.valueOf(Math.round(maxValue * i / 4f)),
                        x, dp(213), p);
            }
        }

        private String fitLabel(String value, float maxWidth) {
            String result = value == null ? "" : value;
            while (!result.isEmpty() && p.measureText(result + "…") > maxWidth) {
                result = result.substring(0, result.length() - 1);
            }
            return result.equals(value) ? result : result + "…";
        }
    }

    @Override
    protected void onDestroy() {
        if (invoiceDatabase != null) invoiceDatabase.close();
        if (plantDatabase != null) plantDatabase.close();
        super.onDestroy();
    }
}
