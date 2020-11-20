/*
 * MIT License
 *
 * Copyright (c) 2020 Al Shakib (shakib@alshakib.dev)
 *
 * This file is part of Tide
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.alshakib.tide;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

public class Graphics {

    public static int dpToPx(@NonNull Context context, int value) {
        Resources resources = context.getResources();
        return (int) (value * resources.getDisplayMetrics().density);
    }

    @NonNull
    public static Paint getSmoothPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    @NonNull
    public static Paint getFilterPaint(@ColorInt int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColorFilter(filterOf(color));
        return paint;
    }

    @NonNull
    public static ColorFilter filterOf(int color) {
        return new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @NonNull
    public static RectF rectFOf(float left, float top, float right, float bottom) {
        return new RectF(left, top, right, bottom);
    }

    public static int withAlpha(@ColorInt int color, int alpha) {
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    public static float clamp(float clamp, float min, float max) {
        return Math.min(max, Math.max(clamp, min));
    }

    @NonNull
    public static Canvas inCanvas(@NonNull Bitmap bitmap) {
        return new Canvas(bitmap);
    }

    public static void safeRecycle(@Nullable Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static void flush(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.eraseColor(0);
        }
    }

    public static boolean fits(@Nullable Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            return bitmap.getHeight() == height && bitmap.getWidth() == width;
        }
        return false;
    }
}
