package com.example.quanlycaycanh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Biểu đồ doanh thu theo ngày.
// Màn Thống kê truyền vào nhãn ngày và doanh thu của từng ngày.
public class RevenueChartView extends View {

    private static final int LINE = Color.parseColor("#2E9B3F");
    private static final int GRID = Color.parseColor("#E6E6E6");
    private static final int TEXT = Color.parseColor("#6E6E6E");

    private final List<String> labels = new ArrayList<>();
    private final List<Long> values = new ArrayList<>();

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    private final DecimalFormat axisFormat = createAxisFormat();

    public RevenueChartView(Context context, AttributeSet attrs) {

        super(context, attrs);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2));
        linePaint.setColor(LINE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setColor(GRID);

        textPaint.setColor(TEXT);
        textPaint.setTextSize(sp(9));
    }

    public void setData(List<String> labels, List<Long> values) {

        this.labels.clear();
        this.labels.addAll(labels);

        this.values.clear();
        this.values.addAll(values);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float left = dp(36);
        float right = getWidth() - dp(6);
        float top = dp(8);
        float bottom = getHeight() - dp(20);

        if (right <= left || bottom <= top) {

            return;
        }

        long max = 0;

        for (long value : values) {

            max = Math.max(max, value);
        }

        long step = niceStep(max / 3);

        // Trục dọc dùng "tr" khi doanh thu từ một triệu trở lên
        boolean million = max >= 1_000_000L;
        double divisor = million ? 1_000_000d : 1_000d;
        String unit = million ? "tr" : "K";

        // Lưới ngang và nhãn trục dọc
        textPaint.setTextAlign(Paint.Align.RIGHT);

        for (int i = 0; i <= 3; i++) {

            float y = bottom - (bottom - top) * i / 3f;

            canvas.drawLine(left, y, right, y, gridPaint);

            String label = i == 0
                    ? "0"
                    : axisFormat.format(step * i / divisor) + unit;

            canvas.drawText(label, left - dp(5), y + dp(3), textPaint);
        }

        if (values.isEmpty()) {

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(TEXT);
            canvas.drawText(
                    "Chưa có dữ liệu",
                    (left + right) / 2,
                    (top + bottom) / 2,
                    textPaint
            );

            return;
        }

        linePath.reset();
        fillPath.reset();

        float gap = values.size() == 1
                ? 0
                : (right - left) / (values.size() - 1);

        float baseY = bottom;
        float scale = (bottom - top) / (step * 3f);

        for (int i = 0; i < values.size(); i++) {

            float x = values.size() == 1
                    ? (left + right) / 2
                    : left + gap * i;

            float y = bottom - values.get(i) * scale;

            if (i == 0) {

                linePath.moveTo(x, y);
                fillPath.moveTo(x, baseY);
                fillPath.lineTo(x, y);

            } else {

                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        fillPath.lineTo(right, baseY);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(
                0, top, 0, bottom,
                Color.parseColor("#552E9B3F"),
                Color.parseColor("#002E9B3F"),
                Shader.TileMode.CLAMP
        ));

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Chấm tròn trên từng điểm
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(Color.WHITE);
        dot.setStyle(Paint.Style.FILL);

        Paint dotStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotStroke.setColor(LINE);
        dotStroke.setStyle(Paint.Style.STROKE);
        dotStroke.setStrokeWidth(dp(2));

        for (int i = 0; i < values.size(); i++) {

            float x = values.size() == 1
                    ? (left + right) / 2
                    : left + gap * i;

            float y = bottom - values.get(i) * scale;

            canvas.drawCircle(x, y, dp(3), dot);
            canvas.drawCircle(x, y, dp(3), dotStroke);
        }

        // Nhãn ngày: chỉ vẽ một số mốc để không chồng chữ
        textPaint.setTextAlign(Paint.Align.CENTER);

        int every = Math.max(1, (int) Math.ceil(values.size() / 6.0));

        for (int i = 0; i < labels.size(); i += every) {

            drawDayLabel(canvas, i, left, right, gap, bottom);
        }

        // Nhãn cuối chỉ vẽ khi còn đủ chỗ, tránh đè lên nhãn trước đó
        int last = labels.size() - 1;

        if (last > 0 && last % every != 0) {

            float lastX = xOf(last, left, right, gap);

            float lastHalf = textPaint.measureText(labels.get(last)) / 2;

            float previousX = xOf(last - last % every, left, right, gap);

            float previousHalf =
                    textPaint.measureText(labels.get(last - last % every)) / 2;

            boolean room = lastX - lastHalf >
                    previousX + previousHalf + dp(4);

            if (room) {

                drawDayLabel(canvas, last, left, right, gap, bottom);
            }
        }
    }

    private void drawDayLabel(
            Canvas canvas,
            int index,
            float left,
            float right,
            float gap,
            float bottom
    ) {

        float x = xOf(index, left, right, gap);

        float half = textPaint.measureText(labels.get(index)) / 2;

        // Giữ nhãn nằm trong vùng vẽ để không bị cắt ở hai đầu
        x = Math.max(left + half, Math.min(right - half, x));

        canvas.drawText(labels.get(index), x, bottom + dp(15), textPaint);
    }

    // Vị trí ngang của điểm thứ index trên biểu đồ
    private float xOf(int index, float left, float right, float gap) {

        return labels.size() == 1
                ? (left + right) / 2
                : left + gap * index;
    }

    // Làm tròn bước chia cho dễ đọc: 300K, 500K, 1.000K...
    private long niceStep(long raw) {

        if (raw <= 0) {

            return 100_000;
        }

        long magnitude = 1;

        while (magnitude * 10 <= raw) {

            magnitude *= 10;
        }

        for (long multiple : new long[]{1, 2, 5, 10}) {

            if (magnitude * multiple >= raw) {

                return magnitude * multiple;
            }
        }

        return magnitude * 10;
    }

    private float dp(float value) {

        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {

        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private static DecimalFormat createAxisFormat() {

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(Locale.getDefault());

        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        return new DecimalFormat("0.##", symbols);
    }
}
