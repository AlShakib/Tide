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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import dev.alshakib.tide.extension.AndroidExt;

import static java.lang.Math.abs;

public class TideView extends View {
    private final Context context;

    private int canvasWidth;
    private int canvasHeight;

    private final Paint tidePaint;
    private final RectF tideRect;
    private float tideWidth;
    private float tideGap;
    private float tideCornerRadius;
    private float tideMinHeight;
    private int tideBackgroundColor;
    private int tideProgressColor;
    private int tideProgress;
    private TideGravity tideGravity;

    private final Canvas progressCanvas;

    private int max;
    private float touchDownX;
    private final int scaledTouchSlop;

    private int[] amplitudeData;

    private OnTideProgressChangeListener onTideProgressChangeListener;

    public TideView(Context context) {
        super(context);
        this.context = context;
        this.tidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tideRect = new RectF();
        this.progressCanvas = new Canvas();
        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public TideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.tidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tideRect = new RectF();
        this.progressCanvas = new Canvas();
        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init(attrs);
    }

    public TideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.tidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tideRect = new RectF();
        this.progressCanvas = new Canvas();
        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init(attrs);
    }

    public TideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        this.tidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tideRect = new RectF();
        this.progressCanvas = new Canvas();
        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        TypedArray styledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.TideView);

        tideWidth = styledAttributes.getDimensionPixelOffset(R.styleable.TideView_tide_width,
                        AndroidExt.convertDpToPxInt(context,5));
        tideGap = styledAttributes.getDimensionPixelOffset(R.styleable.TideView_tide_gap,
                        AndroidExt.convertDpToPxInt(context,2));
        tideCornerRadius = styledAttributes.getDimensionPixelOffset(R.styleable.TideView_tide_corner_radius,
                AndroidExt.convertDpToPxInt(context, 2));
        tideMinHeight = styledAttributes.getDimensionPixelOffset(R.styleable.TideView_tide_min_height,
                        AndroidExt.convertDpToPxInt(context, 10));
        tideBackgroundColor = styledAttributes.getColor(R.styleable.TideView_tide_background_color,
                ContextCompat.getColor(context, R.color.tideBackground));
        tideProgressColor = styledAttributes.getColor(R.styleable.TideView_tide_progress_color,
                ContextCompat.getColor(context, R.color.tideProgress));

        max = styledAttributes.getInteger(R.styleable.TideView_tide_max, 1000000);
        tideProgress = styledAttributes
                .getInteger(R.styleable.TideView_tide_progress, 25);

        tideGravity = TideGravity.CENTER;

        String gravity = styledAttributes
                .getString(R.styleable.TideView_tide_gravity);

        if (gravity != null) {
            switch (gravity) {
                case "1": {
                    tideGravity = TideGravity.TOP;
                    break;
                }
                case "2": {
                    tideGravity = TideGravity.CENTER;
                    break;
                }
                case "3": {
                    tideGravity = TideGravity.BOTTOM;
                    break;
                }
                default: {
                    tideGravity = TideGravity.CENTER;
                }
            }
        }

        styledAttributes.recycle();
    }

    public void setOnTideProgressChangeListener(@Nullable OnTideProgressChangeListener onTideProgressChangeListener) {
        this.onTideProgressChangeListener = onTideProgressChangeListener;
    }

    public void setAmplitudeData(@NonNull int[] data) {
        this.amplitudeData = data;
        invalidate();
    }

    @Nullable
    public int[] getAmplitudeData() {
        return amplitudeData;
    }

    public float getTideWidth() {
        return tideWidth;
    }

    public void setTideWidth(float tideWidth) {
        this.tideWidth = tideWidth;
        invalidate();
    }

    public float getTideGap() {
        return tideGap;
    }

    public void setTideGap(float tideGap) {
        this.tideGap = tideGap;
        invalidate();
    }

    public float getTideCornerRadius() {
        return tideCornerRadius;
    }

    public void setTideCornerRadius(float tideCornerRadius) {
        this.tideCornerRadius = tideCornerRadius;
        invalidate();
    }

    public float getTideMinHeight() {
        return tideMinHeight;
    }

    public void setTideMinHeight(float tideMinHeight) {
        this.tideMinHeight = tideMinHeight;
        invalidate();
    }

    public int getTideBackgroundColor() {
        return tideBackgroundColor;
    }

    public void setTideBackgroundColor(int tideBackgroundColor) {
        this.tideBackgroundColor = tideBackgroundColor;
        invalidate();
    }

    public int getProgressColor() {
        return tideProgressColor;
    }

    public void setProgressColor(int tideProgressColor) {
        this.tideProgressColor = tideProgressColor;
        invalidate();
    }

    public int getProgress() {
        return tideProgress;
    }

    public void setProgress(int tideProgress) {
        this.tideProgress = tideProgress;
        invalidate();
        if (onTideProgressChangeListener != null) {
            onTideProgressChangeListener.onTideProgressChange(this, tideProgress, false);
        }
    }

    public TideGravity getTideGravity() {
        return tideGravity;
    }

    public void setTideGravity(TideGravity tideGravity) {
        this.tideGravity = tideGravity;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthMeasureSpec, AndroidExt.convertDpToPxInt(context, 56));
        }
        setPaddingRelative((int) (tideGap / 2), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasWidth = w;
        canvasHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudeData == null || amplitudeData.length <= 0) {
            return;
        }

        float step = (getAvailableWidth() / (tideGap + tideWidth)) / amplitudeData.length;
        float lastTideRight = getPaddingLeft();
        for (float i = 0; i < amplitudeData.length; i += 1 / step) {
            float tideHeight = getAvailableHeight() * amplitudeData[(int) i] / amplitudeData.length;
            if (tideHeight < tideMinHeight) {
                tideHeight = tideMinHeight;
            }

            float top;
            switch (tideGravity) {
                case TOP: {
                    top = getPaddingTop();
                    break;
                }
                case CENTER: {
                    top = getPaddingTop() + getAvailableHeight() / 2 - tideHeight / 2;
                    break;
                }
                case BOTTOM: {
                    top = canvasHeight - getPaddingBottom() - tideHeight;
                    break;
                }
                default: {
                    top = getPaddingTop() + getAvailableHeight() / 2 - tideHeight / 2;
                }
            }
            tideRect.set(lastTideRight, top, lastTideRight + tideWidth, top + tideHeight);

            if (tideRect.contains(getAvailableWidth() * tideProgress / max, tideRect.centerY())) {
                float bitHeight = tideRect.height();

                if (bitHeight <= 0) {
                    bitHeight = tideHeight;
                }

                Bitmap bitmap = createBitmap(bitHeight);

                progressCanvas.setBitmap(bitmap);

                float fillWidth = getAvailableWidth() * tideProgress / max;

                tidePaint.setColor(tideProgressColor);
                progressCanvas.drawRect(0, 0, fillWidth, tideRect.bottom, tidePaint);
                tidePaint.setColor(tideBackgroundColor);
                progressCanvas.drawRect(fillWidth, 0, getAvailableWidth(), tideRect.bottom, tidePaint);

                tidePaint.setShader(createBitmapShader(bitmap));
            } else if (tideRect.right <= getAvailableWidth() * tideProgress / max) {
                tidePaint.setColor(tideProgressColor);
                tidePaint.setShader(null);
            } else {
                tidePaint.setColor(tideBackgroundColor);
                tidePaint.setShader(null);
            }

            canvas.drawRoundRect(tideRect, tideCornerRadius, tideCornerRadius, tidePaint);
            lastTideRight = tideRect.right + tideGap;

            if (lastTideRight + tideWidth > getAvailableWidth() + getPaddingLeft()) {
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isParentScrolling()) {
                    touchDownX = event.getX();
                } else {
                    updateProgress(event);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                updateProgress(event);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (abs(event.getX() - touchDownX) > scaledTouchSlop) {
                    updateProgress(event);
                }
                performClick();
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private Bitmap createBitmap(float tideHeight) {
        return Bitmap.createBitmap((int) getAvailableWidth(), (int) tideHeight, Bitmap.Config.ARGB_8888);
    }

    private BitmapShader createBitmapShader(Bitmap bitmap) {
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    private float getAvailableWidth() {
        return canvasWidth - getPaddingLeft() - getPaddingRight();
    }

    private float getAvailableHeight() {
        return canvasHeight - getPaddingTop() - getPaddingBottom();
    }

    private boolean isParentScrolling() {
        View parent = (View) getParent();

        while (true) {
            if (parent.canScrollHorizontally(1)) {
                return true;
            }
            if (parent.canScrollHorizontally(-1)) {
                return true;
            }
            if (parent.canScrollVertically(1)) {
                return true;
            }
            if (parent.canScrollVertically(-1)) {
                return true;
            }
            if (parent == getRootView()) {
                return false;
            }
            parent = (View) parent.getParent();
        }
    }

    private void updateProgress(MotionEvent event) {
        tideProgress = (int) (max * event.getX() / getAvailableWidth());
        invalidate();
        if (onTideProgressChangeListener != null) {
            onTideProgressChangeListener.onTideProgressChange(this, tideProgress, true);
        }
    }

    public interface OnTideProgressChangeListener {
        void onTideProgressChange(@NonNull TideView tideView, int progress, boolean fromUser);
    }
}
