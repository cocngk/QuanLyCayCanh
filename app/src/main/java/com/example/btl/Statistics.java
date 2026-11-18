package com.example.btl;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private final int TEXT = Color.rgb(25, 25, 25);
    private final int GRAY = Color.rgb(110, 110, 110);
    private final int[] chartColors = {
            0xFF00852E, 0xFF54C934, 0xFFFFC800, 0xFF2587DC, 0xFFAA72D6
    };
    private TextView tvFrom, tvTo;
    private TextView tvRevenueMetric, tvInvoiceMetric, tvProductsMetric, tvStockMetric;
    private TextView tvTopPlantsEmpty;
    private LinearLayout topPlantsList;
    private FrameLayout chartRevenueContainer, chartCategoryContainer, chartInventoryContainer;
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

        setContentView(R.layout.activity_statistics);
        bindScreen();
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

    private void bindScreen() {
        tvFrom = findViewById(R.id.tvFrom);
        tvTo = findViewById(R.id.tvTo);
        tvRevenueMetric = findViewById(R.id.tvRevenueMetric);
        tvInvoiceMetric = findViewById(R.id.tvInvoiceMetric);
        tvProductsMetric = findViewById(R.id.tvProductsMetric);
        tvStockMetric = findViewById(R.id.tvStockMetric);
        tvTopPlantsEmpty = findViewById(R.id.tvTopPlantsEmpty);
        topPlantsList = findViewById(R.id.topPlantsList);
        chartRevenueContainer = findViewById(R.id.chartRevenueContainer);
        chartCategoryContainer = findViewById(R.id.chartCategoryContainer);
        chartInventoryContainer = findViewById(R.id.chartInventoryContainer);

        revenueChart = new LineChart(this);
        categoryChart = new DonutChart(this);
        inventoryChart = new BarChart(this);
        chartRevenueContainer.addView(revenueChart,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
        chartCategoryContainer.addView(categoryChart,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
        chartInventoryContainer.addView(inventoryChart,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        Calendar firstDay = Calendar.getInstance();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        setStartOfDay(firstDay);
        Calendar today = Calendar.getInstance();
        setStartOfDay(today);
        tvFrom.setText(formatDate(firstDay));
        tvTo.setText(formatDate(today));

        findViewById(R.id.boxFromDate).setOnClickListener(
                v -> showDatePicker(tvFrom));
        findViewById(R.id.boxToDate).setOnClickListener(
                v -> showDatePicker(tvTo));
        findViewById(R.id.btnStatistics).setOnClickListener(
                v -> refreshStatistics());

        BottomNavHelper.setup(this, R.id.navStats);
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

    private void addTopPlant(LinearLayout parent, InvoiceDatabase.ProductSales sale,
                            int maxQuantity) {
        View row = LayoutInflater.from(this)
                .inflate(R.layout.item_statistics_top_plant, parent, false);
        ImageView image = row.findViewById(R.id.imgTopPlant);
        ImageStorage.display(image, sale.imageUri);
        ((TextView) row.findViewById(R.id.tvTopPlantName)).setText(sale.name);
        ((TextView) row.findViewById(R.id.tvTopPlantQuantity))
                .setText(sale.quantity + " cây");
        ProgressBar progress = row.findViewById(R.id.progressTopPlant);
        progress.setProgress(maxQuantity > 0
                ? Math.round(sale.quantity * 100f / maxQuantity) : 0);
        progress.setProgressTintList(ColorStateList.valueOf(
                sale.quantity >= 10 ? GREEN : 0xFF65C83D));
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
            tvTopPlantsEmpty.setVisibility(View.VISIBLE);
            topPlantsList.setVisibility(View.GONE);
            return;
        }
        tvTopPlantsEmpty.setVisibility(View.GONE);
        topPlantsList.setVisibility(View.VISIBLE);
        int highest = sales.get(0).quantity;
        for (InvoiceDatabase.ProductSales sale : sales) {
            addTopPlant(topPlantsList, sale, highest);
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
