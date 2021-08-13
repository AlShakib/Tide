/*
 * MIT License
 *
 * Copyright (c) 2021 Al Shakib (shakib@alshakib.dev)
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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

public class TideView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final int DEFAULT_CHUNK_WIDTH_DP = 3;
    private static final int DEFAULT_CHUNK_MAX_HEIGHT_DP = 56;
    private static final int DEFAULT_CHUNK_MIN_HEIGHT_DP = 1;
    private static final int DEFAULT_CHUNK_SPACING_DP = 1;
    private static final int DEFAULT_CHUNK_RADIUS_DP = 2;
    private static final int DEFAULT_MAX_PROGRESS = 100;
    private static final int DEFAULT_PROGRESS = 0;
    private static final int DEFAULT_ANIMATE_EXPANSION_DURATION = 400;
    private static final boolean DEFAULT_ANIMATE_EXPANSION_STATUS = true;
    private static final boolean DEFAULT_AS_SEEK_BAR_STATUS = true;
    private static final int DEFAULT_PRIMARY_COLOR_ALPHA = 170;

    private static final float VALUE_ANIMATOR_FROM = 0.0F;
    private static final float VALUE_ANIMATOR_TO = 1.0F;

    private final Random random;

    private OnTideViewChangeListener onTideViewChangeListener;

    private int primaryColor;
    private int chunkMaxHeight;
    private int chunkMinHeight;
    private int chunkWidth;
    private int chunkSpacing;
    private int chunkRadius;
    private int maxProgress;
    private int progress;
    private long animateExpansionDuration;
    private boolean animateExpansion;
    private boolean isSeekBar;

    private long initialDelay;
    private boolean isTouched;

    private byte[] scaledData;

    private ValueAnimator expansionAnimator;
    private Paint wavePaint;
    private Paint waveFilledPaint;
    private Bitmap waveBitmap;

    public TideView(Context context) {
        this(context, null);
    }

    public TideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TideView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TideView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        this.random = new Random();
        if (context != null && attrs != null) {
            TypedArray resAttrs = getContext().getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.TideView, defStyleAttr, defStyleRes);
            if (resAttrs != null) {
                primaryColor = resAttrs.getColor(R.styleable.TideView_tidePrimaryColor,
                        ContextCompat.getColor(context, R.color.primary));
                chunkRadius = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkRadius,
                        dpToPx(context, DEFAULT_CHUNK_RADIUS_DP));
                chunkWidth = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkWidth,
                        dpToPx(context, DEFAULT_CHUNK_WIDTH_DP));
                chunkMaxHeight = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkMaxHeight,
                        dpToPx(context, DEFAULT_CHUNK_MAX_HEIGHT_DP));
                chunkMinHeight = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkMinHeight,
                        dpToPx(context, DEFAULT_CHUNK_MIN_HEIGHT_DP));
                chunkSpacing = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkSpacing,
                        dpToPx(context, DEFAULT_CHUNK_SPACING_DP));
                maxProgress = resAttrs.getInt(R.styleable.TideView_tideMaxProgress,
                        DEFAULT_MAX_PROGRESS);
                progress = resAttrs.getInt(R.styleable.TideView_tideProgress,
                        DEFAULT_PROGRESS);
                animateExpansionDuration = resAttrs.getInt(R.styleable.TideView_tideAnimateExpansionDuration,
                        DEFAULT_ANIMATE_EXPANSION_DURATION);
                animateExpansion = resAttrs.getBoolean(R.styleable.TideView_tideAnimateExpansion,
                        DEFAULT_ANIMATE_EXPANSION_STATUS);
                isSeekBar = resAttrs.getBoolean(R.styleable.TideView_tideAsSeekBar,
                        DEFAULT_AS_SEEK_BAR_STATUS);
                resAttrs.recycle();
            }
            this.scaledData = new byte[0];
            this.initialDelay = 50L;
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(VALUE_ANIMATOR_FROM, VALUE_ANIMATOR_TO);
            valueAnimator.setDuration(animateExpansionDuration);
            valueAnimator.setInterpolator(new OvershootInterpolator());
            valueAnimator.addUpdateListener(this);
            this.expansionAnimator = valueAnimator;
            this.wavePaint = getSmoothPaint(ColorUtils.setAlphaComponent(primaryColor, TideView.DEFAULT_PRIMARY_COLOR_ALPHA));
            this.waveFilledPaint = getFilterPaint(this.primaryColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.save();
            canvas.clipRect(0.0F, 0.0F, (float) getWidth(), (float) getHeight());
            canvas.drawBitmap(waveBitmap, 0.0F, 0.0F, wavePaint);
            canvas.restore();
            canvas.save();
            canvas.clipRect(0.0F, 0.0F, (float) getWidth() * getProgressFactor(), (float) getHeight());
            canvas.drawBitmap(waveBitmap, 0.0F, 0.0F, waveFilledPaint);
            canvas.restore();
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!isBitmapFits(this.waveBitmap, getWidth(), getHeight())) {
            if (changed) {
                safeRecycle(this.waveBitmap);
                this.waveBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                if (this.scaledData.length == 0) {
                    this.setScaledData(new byte[0]);
                } else {
                    this.setScaledData(this.scaledData);
                }
            }
        }
    }

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if (event != null) {
            if (isSeekBar && isEnabled()) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        setProgress(toProgress(event));
                        if (onTideViewChangeListener != null) {
                            onTideViewChangeListener.onStartTrackingTouch(this);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        if (onTideViewChangeListener != null) {
                            onTideViewChangeListener.onStopTrackingTouch(this);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        isTouched = true;
                        setProgress(toProgress(event));
                        return true;
                    default:
                        isTouched = false;
                        return super.onTouchEvent(event);
                }
            } else {
                return false;
            }
        }
        return super.onTouchEvent(null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthMeasureSpec, chunkMaxHeight);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (valueAnimator != null) {
            redrawData(new Canvas(waveBitmap), valueAnimator.getAnimatedFraction());
        }
    }

    @Nullable
    public OnTideViewChangeListener getOnTideViewChangeListener() {
        return onTideViewChangeListener;
    }

    public void setOnTideViewChangeListener(@Nullable OnTideViewChangeListener listener) {
        this.onTideViewChangeListener = listener;
    }

    public int getChunkMaxHeight() {
        return chunkMaxHeight;
    }

    public void setChunkMaxHeight(@Px int height) {
        chunkMaxHeight = Math.min(Math.abs(height), getHeight());
        redrawData();
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public void setChunkWidth(@Px int width) {
        chunkWidth = Math.min(Math.abs(width), getWidth());
        redrawData();
    }

    public int getChunkSpacing() {
        return chunkSpacing;
    }

    public void setChunkSpacing(@Px int space) {
        chunkSpacing = Math.min(Math.abs(space), getWidth());
        redrawData();
    }

    public int getChunkRadius() {
        return chunkRadius;
    }

    public void setChunkRadius(@Px int value) {
        chunkRadius = Math.abs(value);
        redrawData();
    }

    public int getChunkMinHeight() {
        return chunkMinHeight;
    }

    public void setChunkMinHeight(@Px int value) {
        chunkMinHeight = Math.abs(value);
        redrawData();
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(@ColorInt int color) {
        primaryColor = color;
        wavePaint = getSmoothPaint(ColorUtils.setAlphaComponent(color, DEFAULT_PRIMARY_COLOR_ALPHA));
        waveFilledPaint = getFilterPaint(color);
        redrawData();
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = Math.abs(maxProgress);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress <= maxProgress) {
            this.progress = Math.abs(progress);
            if (onTideViewChangeListener != null) {
                onTideViewChangeListener.onProgressChanged(this, this.progress, isTouched);
            }
            postInvalidate();
        }
    }

    public long getAnimateExpansionDuration() {
        return animateExpansionDuration;
    }

    public void setAnimateExpansionDuration(long duration) {
        animateExpansionDuration = Math.max(DEFAULT_ANIMATE_EXPANSION_DURATION, duration);
        expansionAnimator.setDuration(animateExpansionDuration);
    }

    public boolean getAnimateExpansion() {
        return animateExpansion;
    }

    public void setAnimateExpansion(boolean expansion) {
        animateExpansion = expansion;
    }

    public boolean isSeekBar() {
        return isSeekBar;
    }

    public void setSeekBar(boolean seekBar) {
        isSeekBar = seekBar;
    }

    public boolean isTouched() {
        return isTouched;
    }

    public int getChunksCount() {
        return getWidth() / getChunkStepWidth();
    }

    public void setRawData(@NonNull byte[] raw) {
        postDelayed(() -> {
            setScaledData(getSample(raw, getChunksCount()));
            if (getAnimateExpansion()) {
                animateExpansion();
            }
        }, initialDelay);
    }

    public void setMediaUri(@NonNull Uri uri) {
        try {
            InputStream stream = getContext()
                    .getContentResolver().openInputStream(uri);
            if (stream != null) {
                byte[] bytes = new byte[stream.available()];
                int numberOfBytes = stream.read(bytes);
                stream.close();
                setRawData(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMediaUri(@NonNull Uri uri, @NonNull Handler handler) {
        handler.post(() -> setMediaUri(uri));
    }

    private int getChunkStepWidth() {
        return chunkWidth + chunkSpacing;
    }

    private int getCenterY() {
        return getHeight() / 2;
    }

    private float getProgressFactor() {
        return (float) progress / (float) maxProgress;
    }

    private void setScaledData(@NonNull byte[] bytes) {
        byte[] tempData = bytes.length <= getChunksCount() ? paste(new byte[this.getChunksCount()], bytes) : bytes;
        if (!Arrays.equals(scaledData, tempData)) {
            scaledData = bytes.length <= getChunksCount() ? paste(new byte[this.getChunksCount()], bytes) : bytes;
            redrawData();
        }
    }

    private int toProgress(@NonNull MotionEvent motionEvent) {
        return (int) (Math.min(motionEvent.getX(), Math.max(getWidth(), 0.0)) / getWidth() * maxProgress);
    }

    private void redrawData(Canvas canvas, float factor) {
        if (this.waveBitmap != null) {
            safeEraseColor(this.waveBitmap);
            for (int i = 0; i < this.scaledData.length; ++i) {
                int chunkHeight = (int) ((float) scaledData[i] / (float) Byte.MAX_VALUE * (chunkMaxHeight / 2.0F));
                int clampedHeight = Math.max(chunkHeight, chunkMinHeight);
                float heightDiff = (float) (clampedHeight - chunkMinHeight);
                int animatedDiff = (int) (heightDiff * factor);
                RectF rectF = new RectF(chunkSpacing / 2F + i * getChunkStepWidth(),
                        getCenterY() - chunkMinHeight - animatedDiff,
                        chunkSpacing / 2F + i * getChunkStepWidth() + chunkWidth,
                        getCenterY() + chunkMinHeight + animatedDiff);
                canvas.drawRoundRect(rectF, chunkRadius, chunkRadius, wavePaint);
            }
            postInvalidate();
        }
    }

    private void redrawData() {
        if (waveBitmap != null) {
            Canvas canvas = new Canvas(waveBitmap);
            redrawData(canvas, VALUE_ANIMATOR_TO);
        }
    }

    private void animateExpansion() {
        if (expansionAnimator != null) {
            this.expansionAnimator.start();
        }
    }

    @NonNull
    private Paint getSmoothPaint(@ColorInt int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    @NonNull
    private Paint getFilterPaint(@ColorInt int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return paint;
    }

    private void safeRecycle(@Nullable Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private void safeEraseColor(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.eraseColor(0);
        }
    }

    private boolean isBitmapFits(@Nullable Bitmap bitmap, int width, int height) {
        if (bitmap != null) {
            return bitmap.getHeight() == height && bitmap.getWidth() == width;
        }
        return false;
    }

    private byte getAbsByte(byte b) {
        if (b == Byte.MIN_VALUE) {
            return Byte.MAX_VALUE;
        } else if (b < 0) {
            return (byte) (-b);
        }
        return b;
    }

    @NonNull
    private byte[] paste(@NonNull byte[] bytes, @NonNull byte[] from) {
        if (bytes.length == 0) {
            return new byte[0];
        }
        for(int i = 0; i < bytes.length; ++i) {
            if (i < from.length) {
                bytes[i] = from[i];
            } else {
                bytes[i] = getAbsByte(bytes[i]);
            }
        }
        return bytes;
    }

    private byte[] getSample(byte[] bytes, int chunkCount) {
        if (chunkCount <= 1) {
            return new byte[0];
        }
        byte[] sample = new byte[chunkCount];
        if (chunkCount >= bytes.length) {
            return paste(sample, bytes);
        }
        int step = Math.abs((bytes.length - 1) / (chunkCount - 1));
        for (int i = 0; i < chunkCount; ++i) {
            if (i == 0) {
                sample[i] = getAbsByte(bytes, i, step / 2);
            } else if (i == chunkCount - 1) {
                sample[i] = getAbsByte(bytes, (bytes.length - 1) - (step / 2), bytes.length - 1);
            } else {
                sample[i] = getAbsByte(bytes, (i * step) - (step / 2), (i * step) + (step / 2));
            }
        }
        return sample;
    }

    private byte getAbsByte(byte[] bytes, int from, int to) {
        int step = (to - from) / 5;
        float absByte = 0.0F;
        int count = 0;
        for (int i = from; i < to; i += step) {
            absByte += getAbsByte(bytes[i]);
            ++count;
        }
        absByte /= count;
        if (absByte <= 5.0F) {
            absByte = getRandomByte();
        }
        return (byte) absByte;
    }

    // Dirty hack to fill invalid byte with random byte
    private float getRandomByte() {
        return random.nextInt(Byte.MAX_VALUE - 60) + 30;
    }

    private int dpToPx(@NonNull Context context, @Dimension int value) {
        Resources resources = context.getResources();
        return (int) (value * resources.getDisplayMetrics().density);
    }

    public interface OnTideViewChangeListener {
        void onProgressChanged(@NonNull TideView tideView, int progress, boolean fromUser);
        default void onStartTrackingTouch(@NonNull TideView tideView) { }
        default void onStopTrackingTouch(@NonNull TideView tideView) { }
    }
}
