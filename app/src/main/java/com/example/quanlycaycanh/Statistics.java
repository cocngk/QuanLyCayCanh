package com.example.quanlycaycanh;

import android.app.DatePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Màn Thống kê (Người 2): số liệu lấy từ DBHelper theo khoảng ngày đã chọn
public class Statistics extends BaseActivity {

    private DBHelper dbHelper;

    private TextView tvFrom;
    private TextView tvTo;

    private long from;
    private long to;

    private LinearLayout layoutCategoryLegend;
    private LinearLayout layoutTopPlants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_statistics);

        dbHelper = new DBHelper(this);

        tvFrom = findViewById(R.id.tvFrom);
        tvTo = findViewById(R.id.tvTo);

        layoutCategoryLegend = findViewById(R.id.layoutCategoryLegend);
        layoutTopPlants = findViewById(R.id.layoutTopPlants);

        // Mặc định xem từ đầu tháng đến hết hôm nay
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        from = startOfDay(calendar.getTimeInMillis());
        to = endOfDay(System.currentTimeMillis());

        findViewById(R.id.boxFrom).setOnClickListener(v ->
                pickDate(from, millis -> {
                    from = startOfDay(millis);
                    show();
                })
        );

        findViewById(R.id.boxTo).setOnClickListener(v ->
                pickDate(to, millis -> {
                    to = endOfDay(millis);
                    show();
                })
        );

        findViewById(R.id.btnApply).setOnClickListener(v -> show());

        BottomNav.bind(this);

        HeaderMenu.attach(this, findViewById(R.id.btnMenu));

        show();
    }

    // Nạp số liệu và đổ lên toàn màn hình
    private void show() {

        // Ngày bắt đầu sau ngày kết thúc thì đổi lại cho đúng khoảng
        if (from > to) {

            long swap = from;
            from = to;
            to = swap;
        }

        tvFrom.setText(Format.day(from));
        tvTo.setText(Format.day(to));

        StatisticsData data = StatisticsData.load(dbHelper, from, to);

        ((TextView) findViewById(R.id.tvRevenue))
                .setText(Format.money(data.revenue));

        ((TextView) findViewById(R.id.tvInvoiceCount))
                .setText(String.valueOf(data.invoiceCount));

        ((TextView) findViewById(R.id.tvItemsSold))
                .setText(String.valueOf(data.itemsSold));

        ((TextView) findViewById(R.id.tvStockTotal))
                .setText(String.valueOf(data.stockTotal));

        showRevenueChart(data);
        showCategoryChart(data);
        showTopPlants(data);
        showStockChart(data);
    }

    private void showRevenueChart(StatisticsData data) {

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (StatisticsData.DayPoint point : data.byDay) {

            labels.add(point.label);
            values.add(point.revenue);
        }

        ((RevenueChartView) findViewById(R.id.chartRevenue))
                .setData(labels, values);
    }

    // Biểu đồ tròn và chú giải màu đặt cạnh nhau
    private void showCategoryChart(StatisticsData data) {

        int[] values = new int[data.byCategory.size()];
        int total = 0;

        for (int i = 0; i < values.length; i++) {

            values[i] = data.byCategory.get(i).quantity;
            total += values[i];
        }

        ((CategoryDonutView) findViewById(R.id.chartCategory))
                .setData(values);

        layoutCategoryLegend.removeAllViews();

        for (int i = 0; i < data.byCategory.size(); i++) {

            StatisticsData.Part part = data.byCategory.get(i);

            int percent = total == 0
                    ? 0
                    : Math.round(100f * part.quantity / total);

            addLegendRow(part.name, percent + "%", i);
        }
    }

    private void addLegendRow(String name, String percent, int colorIndex) {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(3), 0, dp(3));

        View dot = new View(this);

        GradientDrawable shape = new GradientDrawable();

        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(CategoryDonutView.colorOf(colorIndex));

        dot.setBackground(shape);

        LinearLayout.LayoutParams dotParams =
                new LinearLayout.LayoutParams(dp(9), dp(9));

        row.addView(dot, dotParams);

        TextView label = new TextView(this);

        label.setText(name);
        label.setTextColor(0xFF333333);
        label.setTextSize(10);
        label.setSingleLine(true);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        labelParams.setMarginStart(dp(5));

        row.addView(label, labelParams);

        TextView value = new TextView(this);

        value.setText(percent);
        value.setTextColor(0xFF087A35);
        value.setTextSize(10);

        row.addView(value);

        layoutCategoryLegend.addView(row);
    }

    private void showTopPlants(StatisticsData data) {

        layoutTopPlants.removeAllViews();

        if (data.topPlants.isEmpty()) {

            addHint(layoutTopPlants, "Chưa bán được cây nào trong khoảng này");
            return;
        }

        for (int i = 0; i < data.topPlants.size(); i++) {

            StatisticsData.TopPlant plant = data.topPlants.get(i);

            LinearLayout row = new LinearLayout(this);

            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            TextView rank = new TextView(this);

            rank.setText(String.valueOf(i + 1));
            rank.setTextColor(0xFF087A35);
            rank.setTextSize(13);
            rank.setTypeface(null, android.graphics.Typeface.BOLD);
            rank.setGravity(Gravity.CENTER);

            row.addView(rank, new LinearLayout.LayoutParams(dp(18), dp(18)));

            ImageView photo = new ImageView(this);

            photo.setImageResource(drawableOf(plant.image));
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams photoParams =
                    new LinearLayout.LayoutParams(dp(30), dp(30));

            photoParams.setMarginStart(dp(5));

            row.addView(photo, photoParams);

            TextView name = new TextView(this);

            name.setText(plant.name);
            name.setTextColor(0xFF222222);
            name.setTextSize(12);
            name.setSingleLine(true);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);

            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            nameParams.setMarginStart(dp(7));

            row.addView(name, nameParams);

            TextView quantity = new TextView(this);

            quantity.setText(plant.quantity + " cây");
            quantity.setTextColor(0xFF087A35);
            quantity.setTextSize(11);
            quantity.setTypeface(null, android.graphics.Typeface.BOLD);

            row.addView(quantity);

            layoutTopPlants.addView(row);
        }
    }

    private void showStockChart(StatisticsData data) {

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (StatisticsData.Part part : data.stockByCategory) {

            labels.add(part.name);
            values.add(part.quantity);
        }

        ((StockBarChartView) findViewById(R.id.chartStock))
                .setData(labels, values);
    }

    private void addHint(LinearLayout parent, String text) {

        TextView hint = new TextView(this);

        hint.setText(text);
        hint.setTextColor(0xFF6E6E6E);
        hint.setTextSize(12);

        parent.addView(hint);
    }

    // Hộp chọn ngày, trả ngày đã chọn về cho nơi gọi
    private void pickDate(long current, OnDatePicked onPicked) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(current);

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar picked = Calendar.getInstance();

                    picked.set(year, month, dayOfMonth);

                    onPicked.onDatePicked(picked.getTimeInMillis());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Tên ảnh trong DB là tên drawable, không có thì dùng icon cây
    private int drawableOf(String image) {

        if (image == null || image.isEmpty()) {

            return R.drawable.ic_tree;
        }

        int id = getResources().getIdentifier(
                image,
                "drawable",
                getPackageName()
        );

        return id == 0 ? R.drawable.ic_tree : id;
    }

    private static long startOfDay(long millis) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private static long endOfDay(long millis) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }

    private int dp(float value) {

        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private interface OnDatePicked {

        void onDatePicked(long millis);
    }
}
