package com.example.quanlycaycanh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

// Biểu đồ tròn tỷ lệ loại cây đã bán.
// Chú giải màu nằm ở màn hình để không bị chồng chữ trong hình.
public class CategoryDonutView extends View {

    private static final int[] COLORS = {
            Color.parseColor("#1B7A2E"),
            Color.parseColor("#5BC236"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#9E9E9E")
    };

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] values = new int[0];
    private int total;
    private final RectF box = new RectF();

    public CategoryDonutView(Context context, AttributeSet attrs) {

        super(context, attrs);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        textPaint.setColor(Color.parseColor("#191919"));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public static int colorOf(int index) {

        return COLORS[index % COLORS.length];
    }

    public void setData(int[] values) {

        this.values = values;
        this.total = 0;

        for (int value : values) {

            total += value;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (total <= 0) {

            textPaint.setTextSize(getResources()
                    .getDisplayMetrics().scaledDensity * 10);

            canvas.drawText(
                    "Chưa có dữ liệu",
                    getWidth() / 2f,
                    getHeight() / 2f,
                    textPaint
            );

            return;
        }

        float stroke = Math.min(getWidth(), getHeight()) * 0.2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - stroke / 2f - 2;

        arcPaint.setStrokeWidth(stroke);

        box.set(
                getWidth() / 2f - radius,
                getHeight() / 2f - radius,
                getWidth() / 2f + radius,
                getHeight() / 2f + radius
        );

        float start = -90;

        for (int i = 0; i < values.length; i++) {

            float sweep = 360f * values[i] / total;

            arcPaint.setColor(colorOf(i));

            // Chừa một khe nhỏ giữa các phần cho dễ nhìn
            canvas.drawArc(box, start + 1, Math.max(sweep - 2, 1), false, arcPaint);

            start += sweep;
        }

        textPaint.setTextSize(getResources()
                .getDisplayMetrics().scaledDensity * 17);

        canvas.drawText(
                String.valueOf(total),
                getWidth() / 2f,
                getHeight() / 2f + 2,
                textPaint
        );

        textPaint.setTextSize(getResources()
                .getDisplayMetrics().scaledDensity * 10);

        canvas.drawText(
                "cây",
                getWidth() / 2f,
                getHeight() / 2f + textPaint.getTextSize() + 2,
                textPaint
        );
    }
}
