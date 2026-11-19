
package com.example.healthmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class HealthChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String[] labels = new String[0];
    private long[] values = new long[0];
    private int barColor = Color.rgb(37, 99, 235);
    private final int textColor = Color.rgb(113, 128, 150);
    private final int gridColor = Color.rgb(229, 237, 250);

    public HealthChartView(Context context) {
        super(context);
        init();
    }
    public HealthChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public HealthChartView(Context context,AttributeSet attrs,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        paint.setAntiAlias(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    public void setData(String[] labels, long[] values, int color) {
        if (labels == null || values == null) {
            this.labels = new String[0];
            this.values = new long[0];
        } else {
            int count = Math.min(labels.length, values.length);
            this.labels = new String[count];
            this.values = new long[count];
            System.arraycopy(labels, 0, this.labels, 0, count);
            System.arraycopy(values, 0, this.values, 0, count);
        }
        this.barColor = color;
        invalidate();
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float chartLeft = 42 * density;
        float chartRight = getWidth() - (10 * density);
        float chartTop = 30 * density;
        float chartBottom = getHeight() - (32 * density);
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (values.length == 0) {
            drawEmptyMessage(canvas, density);
            return;
        }
        long maxValue = 0;
        for (long value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        if (maxValue == 0) {
            maxValue = 1;
        }
        double maximum = maxValue * 1.20;
        float chartHeight = chartBottom - chartTop;
        float chartWidth = chartRight - chartLeft;

        paint.setColor(gridColor);
        paint.setStrokeWidth(1 * density);
        paint.setStyle(Paint.Style.STROKE);

        for (int i = 0; i <= 4; i++) {
            float y = chartTop + (chartHeight / 4) * i;
            canvas.drawLine(chartLeft,y,chartRight,y,paint);
        }
        paint.setColor(gridColor);
        paint.setStrokeWidth(1.5f * density);
        canvas.drawLine(chartLeft,chartBottom,chartRight,chartBottom,paint);
        paint.setStyle(Paint.Style.FILL);

        float slotWidth = chartWidth / values.length;
        float barWidth = Math.min(25 * density,slotWidth * 0.55f);

        for (int i = 0; i < values.length; i++) {
            float centerX = chartLeft + (slotWidth * i) + (slotWidth / 2);
            float barHeight = (float) ((values[i] / maximum) * chartHeight);
            float barTop = chartBottom - barHeight;

            paint.setColor(barColor);
            paint.setStyle(Paint.Style.FILL);

            RectF bar = new RectF(centerX - (barWidth / 2),barTop,centerX + (barWidth / 2),chartBottom);
            canvas.drawRoundRect(bar,5 * density,5 * density,paint);

            paint.setColor(textColor);
            paint.setTextSize(10 * density);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);

            String valueText = formatValue(values[i]);
            float valueY = Math.max(12 * density,barTop - (7 * density));
            canvas.drawText(valueText,centerX,valueY,paint);

            paint.setFakeBoldText(false);
            paint.setColor(textColor);
            paint.setTextSize(10 * density);
            paint.setTextAlign(Paint.Align.CENTER);

            String label = labels[i];
            canvas.drawText(label,centerX,getHeight() - (8 * density),paint);
        }
    }
    private String formatValue(long value) {
        if (value >= 1000000) {
            return String.format(Locale.US,"%.1fM",value / 1000000.0);
        }
        if (value >= 10000) {
            return String.format(Locale.US,"%.1fK",value / 1000.0);
        }
        return String.valueOf(value);
    }
    private void drawEmptyMessage(Canvas canvas, float density) {
        paint.setColor(textColor);
        paint.setTextSize(13 * density);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("No health data available",getWidth() / 2f,getHeight() / 2f,paint);
    }
}
