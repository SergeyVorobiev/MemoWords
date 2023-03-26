package com.vsv.game.engine.objects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.vsv.game.engine.objects.properties.TextEnemyProperties;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.SheetDataBuilder;

import java.util.Arrays;

public class TextDrawer {

    private final Rect temp = new Rect();

    private int linesCount;

    private final static int MAX_LINES = 6;

    private static final StringBuilder stringBuilder = new StringBuilder();

    private final String[] lines = new String[MAX_LINES];

    private final int[] lineHeights = new int[MAX_LINES];

    private final int[] lineWidths = new int[MAX_LINES];

    public static int MAX_CHARACTERS = SheetDataBuilder.STRING_LENGTH;

    private final static char[] chars = new char[MAX_CHARACTERS];

    public static int SPACE_BETWEEN = 10;

    private static final String END = "…";

    private static final Paint textPaint;

    private static final Canvas canvas;

    private static final Matrix matrix;

    static {
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        canvas = new Canvas();
        matrix = new Matrix();
    }

    public TextDrawer() {

    }

    private void resetLinesBuffer() {
        Arrays.fill(lines, null);
        linesCount = 0;
        stringBuilder.setLength(0);
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public void setupTextInCenter(Bitmap target, boolean erase, Rect bounds, String text, float textSize) {
        textPaint.setTextSize(textSize);
        StaticLayout textLayout = StaticLayout.Builder.obtain(text, 0, text.length(), (TextPaint) textPaint, bounds.width())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setMaxLines(6).build();

        canvas.setBitmap(target);
        if (erase) {
            target.eraseColor(Color.TRANSPARENT);
        }

        // draw text to the Canvas center
        canvas.save();
        canvas.translate(target.getWidth() / 2.0f - textLayout.getWidth() / 2.0f, target.getHeight() / 2.0f - textLayout.getHeight() / 2.0f);
        textLayout.draw(canvas);
        canvas.restore();
    }

    public void setupTextInCenter2(Bitmap target, boolean erase, Rect bounds, String text, float textSize) {
        textPaint.setTextSize(textSize);
        int maxWidth = bounds.width();
        int maxHeight = bounds.height();
        if (text.length() > MAX_CHARACTERS) {
            text = text.substring(0, MAX_CHARACTERS);
        }
        String[] strings = text.split("\n", MAX_LINES);
        int size = Math.min(MAX_LINES, strings.length);
        int curHeight = 0;
        for (int i = 0; i < size; i++) {
            boolean isLast = (i == (size - 1));
            String line = strings[i].trim();
            stringBuilder.setLength(0);
            stringBuilder.append(line);
            curHeight = addLines(maxWidth, curHeight, maxHeight, isLast);
            if (curHeight == -1) {
                break;
            }
        }
        canvas.setBitmap(target);
        if (erase) {
            target.eraseColor(Color.TRANSPARENT);
        }
        int y = 0;
        float sumHeight = 0;
        for (int i = 0; i < linesCount; i++) {
            sumHeight += (SPACE_BETWEEN + lineHeights[i]);
        }
        float halfSumHeight = sumHeight / 2.0f;
        float heightShift = target.getHeight() / 2.0f - halfSumHeight;
        for (int i = 0; i < linesCount; i++) {
            float widthShift = target.getWidth() / 2.0f - lineWidths[i] / 2.0f;
            y = y + SPACE_BETWEEN;
            y = y + lineHeights[i];
            canvas.drawText(lines[i], widthShift, y + heightShift, textPaint);
        }
        resetLinesBuffer();
    }

    // Return height or -1 if max. This method think that we can add at least 1 symbol in line.
    private int addLines(int maxWidth, int curHeight, int maxHeight, boolean isLast) {
        int begin = 0;
        int count = 0;
        int i;
        int addHeight = 0;
        for (i = 0; i < stringBuilder.length(); i++) {
            int end = i + 1;
            count += 1;
            stringBuilder.getChars(begin, end, chars, 0);
            textPaint.getTextBounds(chars, 0, count, temp);
            if (temp.width() > maxWidth) {
                addHeight = temp.height() + SPACE_BETWEEN;
                curHeight += addHeight;
                if (curHeight > maxHeight) { // We cannot add the line.
                    return -1;
                }
                count = 0;
                String toAdd = stringBuilder.substring(begin, i).trim();
                lineWidths[linesCount] = temp.width();
                lineHeights[linesCount] = temp.height();
                lines[linesCount++] = toAdd;
                if (linesCount == MAX_LINES || (curHeight + addHeight) > maxHeight) { // We cannot add the next line.
                    toAdd = stringBuilder.substring(begin, i - 1).trim();
                    lines[linesCount - 1] = toAdd + END;
                    return -1;
                }
                begin = i;
            }
        }

        if (linesCount < MAX_LINES) {
            String string = stringBuilder.substring(begin, i);
            if (!string.isEmpty()) {
                lineWidths[linesCount] = temp.width();
                lineHeights[linesCount] = temp.height();
                if (!isLast && ((linesCount + 1) == MAX_LINES || (curHeight + addHeight) > maxHeight)) { // We can add this line but we cannon add next, then add ...
                    lines[linesCount++] = stringBuilder.substring(begin, i - 1) + END;
                } else {
                    lines[linesCount++] = string;
                }
            }
        }
        return curHeight;
    }
}
