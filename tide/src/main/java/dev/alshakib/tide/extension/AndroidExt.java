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

package dev.alshakib.tide.extension;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

public class AndroidExt {
    public static boolean isRtl(@NonNull Resources res) {
        return res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static float convertDpToPx(@NonNull Resources res, float dp) {
        return dp * res.getDisplayMetrics().density;
    }

    public static float convertDpToPx(@NonNull Context context, float dp) {
        return convertDpToPx(context.getResources(), dp);
    }

    public static int convertDpToPxInt(@NonNull Resources res, float dp) {
        return (int) convertDpToPx(res, dp);
    }

    public static int convertDpToPxInt(@NonNull Context context, float dp) {
        return (int) convertDpToPx(context, dp);
    }

    public static float convertSpToPx(@NonNull Resources res, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.getDisplayMetrics());
    }

    public static float convertSpToPx(@NonNull Context context, float sp) {
        return convertSpToPx(context.getResources(), sp);
    }

    public static int convertSpToPxInt(@NonNull Resources res, float sp) {
        return (int) convertSpToPx(res, sp);
    }

    public static int convertSpToPxInt(@NonNull Context context, float sp) {
        return (int) convertSpToPx(context,sp);
    }
}
