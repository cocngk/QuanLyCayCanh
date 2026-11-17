package com.example.quanlycaycanh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

// Biểu đồ thanh ngang: tồn kho theo loại cây.
// Tên loại nằm trên thanh để tên dài không bị cắt trong thẻ hẹp.
public class StockBarChartView extends View {

    private static final int[] COLORS = {
            Color.parseColor("#1B7A2E"),
            Color.parseColor("#5BC236"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#9E9E9E")
    };

    private final List<String> labels = new ArrayList<>();
    private final List<Integer> values = new ArrayList<>();

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF box = new RectF();

    public StockBarChartView(Context context, AttributeSet attrs) {

        super(context, attrs);

        trackPaint.setColor(Color.parseColor("#EDEDED"));

        textPaint.setColor(Color.parseColor("#333333"));
        textPaint.setTextSize(sp(10));
    }

    public void setData(List<String> labels, List<Integer> values) {

        this.labels.clear();
        this.labels.addAll(labels);

        this.values.clear();
        this.values.addAll(values);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (labels.isEmpty()) {

            textPaint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(
                    "Chưa có dữ liệu",
                    getWidth() / 2f,
                    getHeight() / 2f,
                    textPaint
            );

            return;
        }

        int max = 1;

        for (int value : values) {

            max = Math.max(max, value);
        }

        float rowHeight = getHeight() / (float) labels.size();
        float barHeight = Math.min(dp(9), rowHeight * 0.34f);

        for (int i = 0; i < labels.size(); i++) {

            float rowTop = rowHeight * i;
            float barTop = rowTop + rowHeight - barHeight;

            // Tên loại bên trái, số lượng bên phải
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(Color.parseColor("#333333"));

            canvas.drawText(
                    labels.get(i),
                    0,
                    barTop - dp(4),
                    textPaint
            );

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setColor(Color.parseColor("#666666"));

            canvas.drawText(
                    String.valueOf(values.get(i)),
                    getWidth(),
                    barTop - dp(4),
                    textPaint
            );

            // Nền thanh và phần tỷ lệ theo giá trị lớn nhất
            box.set(0, barTop, getWidth(), barTop + barHeight);

            canvas.drawRoundRect(
                    box,
                    barHeight / 2f,
                    barHeight / 2f,
                    trackPaint
            );

            box.set(
                    0,
                    barTop,
                    Math.max(getWidth() * values.get(i) / (float) max, dp(3)),
                    barTop + barHeight
            );

            barPaint.setColor(COLORS[i % COLORS.length]);

            canvas.drawRoundRect(
                    box,
                    barHeight / 2f,
                    barHeight / 2f,
                    barPaint
            );
        }
    }

    private float dp(float value) {

        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {

        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
