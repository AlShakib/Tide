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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class TideView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final String LOG_TAG = TideView.class.getSimpleName();

    private static final int DEFAULT_CHUNK_WIDTH_DP = 4;
    private static final int DEFAULT_CHUNK_MAX_HEIGHT_DP = 56;
    private static final int DEFAULT_CHUNK_MIN_HEIGHT_DP = 8;
    private static final int DEFAULT_CHUNK_SPACING_DP = 2;
    private static final int DEFAULT_CHUNK_RADIUS_DP = 2;
    private static final int DEFAULT_MAX_PROGRESS = 100;
    private static final int DEFAULT_PROGRESS = 0;
    private static final int DEFAULT_ANIMATE_EXPANSION_DURATION = 400;
    private static final boolean DEFAULT_ANIMATE_EXPANSION_STATUS = true;
    private static final boolean DEFAULT_AS_SEEK_BAR_STATUS = true;
    private static final int DEFAULT_PRIMARY_COLOR_ALPHA = 170;

    private OnTideViewChangeListener onTideViewChangeListener;
    private Sampler sampler;

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
    private boolean asSeekBar;

    private long initialDelay;
    private boolean isTouched;

    private byte[] scaledData;

    private ValueAnimator expansionAnimator;
    private Paint wavePaint;
    private Paint waveFilledPaint;
    private Bitmap waveBitmap;

    public TideView(Context context) {
        super(context);
        setWillNotDraw(false);
        if (context != null) {
            init(context);
        }
    }

    public TideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        if (context != null && attrs != null) {
            inflateAttrs(attrs, context);
            init(context);
        }
    }

    public TideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        if (context != null && attrs != null) {
            inflateAttrs(attrs, context);
            init(context);
        }
    }

    public TideView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        if (context != null && attrs != null) {
            inflateAttrs(attrs, context);
            init(context);
        }
    }

    private void inflateAttrs(@NonNull AttributeSet attrs, @NonNull Context context) {
        TypedArray resAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.TideView, 0, 0);
        if (resAttrs != null) {
            primaryColor = resAttrs.getColor(R.styleable.TideView_tidePrimaryColor,
                    ContextCompat.getColor(context,R.color.primary));
            chunkRadius = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkRadius,
                    AndroidExt.dpToPx(context, DEFAULT_CHUNK_RADIUS_DP));
            chunkWidth = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkWidth,
                    AndroidExt.dpToPx(context, DEFAULT_CHUNK_WIDTH_DP));
            chunkMaxHeight = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkMaxHeight,
                    AndroidExt.dpToPx(context, DEFAULT_CHUNK_MAX_HEIGHT_DP));
            chunkMinHeight = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkMinHeight,
                    AndroidExt.dpToPx(context, DEFAULT_CHUNK_MIN_HEIGHT_DP));
            chunkSpacing = resAttrs.getDimensionPixelSize(R.styleable.TideView_tideChunkSpacing,
                    AndroidExt.dpToPx(context, DEFAULT_CHUNK_SPACING_DP));
            maxProgress = resAttrs.getInt(R.styleable.TideView_tideMaxProgress,
                    DEFAULT_MAX_PROGRESS);
            progress = resAttrs.getInt(R.styleable.TideView_tideProgress,
                    DEFAULT_PROGRESS);
            animateExpansionDuration = resAttrs.getInt(R.styleable.TideView_tideAnimateExpansionDuration,
                    DEFAULT_ANIMATE_EXPANSION_DURATION);
            animateExpansion = resAttrs.getBoolean(R.styleable.TideView_tideAnimateExpansion,
                    DEFAULT_ANIMATE_EXPANSION_STATUS);
            asSeekBar = resAttrs.getBoolean(R.styleable.TideView_tideAsSeekBar,
                    DEFAULT_AS_SEEK_BAR_STATUS);
            resAttrs.recycle();
        }
    }

    private void init(@NonNull Context context) {
        this.sampler = Sampler.getInstance();
        this.scaledData = new byte[0];
        this.initialDelay = 50L;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        valueAnimator.setDuration(animateExpansionDuration);
        valueAnimator.setInterpolator(new OvershootInterpolator());
        valueAnimator.addUpdateListener(this);
        this.expansionAnimator = valueAnimator;
        this.wavePaint = Graphics.getSmoothPaint(Graphics.withAlpha(primaryColor, DEFAULT_PRIMARY_COLOR_ALPHA));
        this.waveFilledPaint = Graphics.getFilterPaint(this.primaryColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthMeasureSpec, chunkMaxHeight);
        }
    }

    @Nullable
    public OnTideViewChangeListener getOnTideViewChangeListener() {
        return onTideViewChangeListener;
    }

    public void setOnTideViewChangeListener(@Nullable OnTideViewChangeListener onTideViewChangeListener) {
        this.onTideViewChangeListener = onTideViewChangeListener;
    }

    public int getChunkMaxHeight() {
        return chunkMaxHeight;
    }

    public void setChunkMaxHeight(@Dimension int height) {
        chunkMaxHeight = Math.abs(height) >= getHeight() ? getHeight() : Math.abs(height);
        redrawData();
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public void setChunkWidth(@Dimension int width) {
        chunkWidth = Math.abs(width) >= getWidth() ? getWidth() : Math.abs(width);
        redrawData();
    }

    public int getChunkSpacing() {
        return chunkSpacing;
    }

    public void setChunkSpacing(@Dimension int space) {
        chunkSpacing = Math.abs(space) >= getWidth() ? getWidth() : Math.abs(space);
        redrawData();
    }

    public int getChunkRadius() {
        return chunkRadius;
    }

    public void setChunkRadius(@Dimension int value) {
        chunkRadius = Math.abs(value);
        redrawData();
    }

    public int getChunkMinHeight() {
        return chunkMinHeight;
    }

    public void setChunkMinHeight(@Dimension int value) {
        chunkMinHeight = Math.abs(value);
        redrawData();
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(@ColorInt int color) {
        primaryColor = color;
        wavePaint = Graphics.getSmoothPaint(Graphics.withAlpha(color, DEFAULT_PRIMARY_COLOR_ALPHA));
        waveFilledPaint = Graphics.getFilterPaint(color);
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

    @NonNull
    public byte[] getScaledData() {
        return scaledData;
    }

    public void setScaledData(@NonNull byte[] bytes) {
        scaledData = bytes.length <= this.getChunksCount() ? sampler.paste(new byte[this.getChunksCount()], bytes) : bytes;
        redrawData();
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

    public void setAnimateExpansion(boolean flag) {
        animateExpansion = flag;
    }

    public boolean getAsSeekBar() {
        return asSeekBar;
    }

    public void setAsSeekBar(boolean flag) {
        asSeekBar = flag;
    }

    public boolean getIsTouched() {
        return isTouched;
    }

    public int getChunksCount() {
        return getWidth() / getChunkStep();
    }

    private int getChunkStep() {
        return chunkWidth + chunkSpacing;
    }

    private int getCenterY() {
        return getHeight() / 2;
    }

    private float getProgressFactor() {
        return progress / (float) maxProgress;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(waveBitmap, 0.0F, 0.0F, wavePaint);
            canvas.restore();
            canvas.save();
            canvas.clipRect(0.0F, 0.0F, (float) getWidth() * getProgressFactor(), (float) getHeight());
            canvas.drawBitmap(waveBitmap, 0.0F, 0.0F, waveFilledPaint);
            canvas.restore();
        }
    }

    @SuppressLint({"DrawAllocation"})
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!Graphics.fits(this.waveBitmap, getWidth(), getHeight())) {
            if (changed) {
                Graphics.safeRecycle(this.waveBitmap);
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
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        if (event != null) {
            if (asSeekBar && isEnabled()) {
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
                        return false;
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
        return super.onTouchEvent(event);
    }

    public void setRawData(@NonNull final byte[] raw, final @Nullable OnSamplingListener callback) {
        Sampler.MAIN_THREAD.postDelayed(new Runnable() {
            @Override
            public void run() {
                sampler.downSampleAsync(raw, getChunksCount(), new Sampler.OnSamplerListener() {
                    @Override
                    public void resultAsync(byte[] result) {
                        setScaledData(result);
                        if (getAnimateExpansion()) {
                            animateExpansion();
                        }
                        if (callback != null) {
                            callback.onComplete();
                        }
                    }
                });
            }
        }, initialDelay);
    }

    public final void setRawData(@NonNull final byte[] raw) {
        setRawData(raw, null);
    }

    private int toProgress(@NonNull MotionEvent motionEvent) {
        return (int) (Graphics.clamp(motionEvent.getX(), 0.0F, (float) getWidth()) / (float) getWidth() * maxProgress);
    }

    private void redrawData(Canvas canvas, float factor) {
        if (this.waveBitmap != null) {
            Graphics.flush(this.waveBitmap);
            for (int i = 0; i < this.scaledData.length; ++i) {
                int chunkHeight = (int) ( (float) scaledData[i] / (float) Byte.MAX_VALUE * (chunkMaxHeight / 2));
                int clampedHeight = Math.max(chunkHeight, chunkMinHeight);
                float heightDiff = (float) (clampedHeight - chunkMinHeight);
                int animatedDiff = (int) (heightDiff * factor);
                RectF rectF = Graphics.rectFOf(chunkSpacing / 2f + i * getChunkStep(),
                        getCenterY() - chunkMinHeight - animatedDiff,
                        chunkSpacing / 2f + i * getChunkStep() + chunkWidth,
                        getCenterY() + chunkMinHeight + animatedDiff);
                canvas.drawRoundRect(rectF, chunkRadius, chunkRadius, wavePaint);
            }
            postInvalidate();
        }
    }

    private void redrawData() {
        if (waveBitmap != null) {
            Canvas canvas = Graphics.inCanvas(waveBitmap);
            redrawData(canvas, 1.0F);
        }
    }

    private void animateExpansion() {
        this.expansionAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        redrawData(Graphics.inCanvas(waveBitmap), valueAnimator.getAnimatedFraction());
    }

    public interface OnTideViewChangeListener {
        void onProgressChanged(@NonNull TideView tideView, int progress, boolean fromUser);
        void onStartTrackingTouch(@NonNull TideView tideView);
        void onStopTrackingTouch(@NonNull TideView tideView);
    }

    public interface OnSamplingListener {
        void onComplete();
    }
}
