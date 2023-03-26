package com.vsv.graphs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.memorizer.R;

public class DictionaryGraph extends View {

    private final Paint fillPaint;

    private final Paint strokePaint;

    private float[] values;

    private float[] yValuesPositions;

    private float[] xValuesPositions;

    private float maximum;

    private final RectF bar = new RectF();

    private float stepPoint;

    private float halfBarWidth;

    private float thickness;

    private final boolean drawAvgLine;

    private int maxBarsCount;

    public DictionaryGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DictionaryGraph,
                0, 0);

        int barColor;
        int lineColor;
        try {
            float defaultThickness = 0.7f;
            int defaultMaxBars = 0;
            thickness = a.getFloat(R.styleable.DictionaryGraph_barThickness, defaultThickness);
            maxBarsCount = a.getInteger(R.styleable.DictionaryGraph_maxBarsCount, defaultMaxBars);
            drawAvgLine = a.getBoolean(R.styleable.DictionaryGraph_avgLine, false);
            barColor = a.getColor(R.styleable.DictionaryGraph_barColor, Color.GRAY);
            lineColor = a.getColor(R.styleable.DictionaryGraph_avgLineColor, Color.BLUE);
            if (maxBarsCount < 0) {
                maxBarsCount = 0;
            } else if (maxBarsCount > 10000) {
                maxBarsCount = 10000;
            }
            if (thickness <= 0 || thickness > 1) {
                thickness = defaultThickness;
            }
        } finally {
            a.recycle();
        }
        fillPaint = new Paint();
        fillPaint.setColor(barColor);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint = new Paint();
        strokePaint.setColor(lineColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        int minh = getSuggestedMinimumHeight() + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
        float width = w - (getPaddingLeft() + getPaddingRight());
        recalculate();
        setMeasuredDimension(w, h);
    }

    public void setupData(@Nullable float[] values) {
        if (maxBarsCount == 0 || values == null || values.length == 0) {
            this.values = null;
            this.yValuesPositions = null;
            this.xValuesPositions = null;
            return;
        }
        int count = Math.min(values.length, maxBarsCount);
        int diff = values.length - maxBarsCount;
        if (diff < 0) {
            diff = 0;
        }
        this.values = new float[count];
        System.arraycopy(values, diff, this.values, 0, count);
        this.yValuesPositions = new float[count];
        this.xValuesPositions = new float[count];
        recalculate();
        invalidate();
    }

    private void recalculate() {
        if (values == null || values.length == 0) {
            return;
        }
        stepPoint = (float) getWidth() / (values.length + 1);
        float barWidth = stepPoint * thickness;
        halfBarWidth = barWidth / 2;
        maximum = values[0];
        for (int i = 1; i < values.length; i++) {
            float value = values[i];
            if (maximum < value) {
                maximum = value;
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values == null || values.length == 0) {
            return;
        }
        float bit = 0;
        float height = getHeight() - (getPaddingBottom() + getPaddingTop());
        if (maximum > 0) {
            bit = height / maximum;
        }
        float leftPadding = getPaddingLeft();
        float sumHeight = 0;
        float averageHeight;
        for (int i = 0; i < values.length; i++) {
            float value = values[i];
            float barHeight = bit * value;
            sumHeight += barHeight;
            averageHeight = sumHeight / (i + 1);
            float positionY = height - barHeight;
            float x = stepPoint + i * stepPoint + leftPadding;
            RectF rect = calculateBar(x, positionY);
            xValuesPositions[i] = x;
            yValuesPositions[i] = height - averageHeight;
            canvas.drawRect(rect, fillPaint);
        }
        if (values.length > 1 && drawAvgLine) {
            for (int i = 1; i < values.length; i++) {
                float startX = xValuesPositions[i - 1];
                float endX = xValuesPositions[i];
                float startY = yValuesPositions[i - 1] - 1;
                float endY = yValuesPositions[i] - 1;
                if (startY < 2) {
                    startY += 2;
                }
                if (endY < 2) {
                    endY += 2;
                }
                canvas.drawLine(startX, startY, endX, endY, strokePaint);
            }
        }
    }

    @NonNull
    private RectF calculateBar(float x, float top) {
        bar.set(x - halfBarWidth, top, x + halfBarWidth, getHeight() - getPaddingBottom());
        return bar;
    }
}
