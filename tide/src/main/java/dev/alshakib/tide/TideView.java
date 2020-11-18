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
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class TideView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final String LOG_TAG = TideView.class.getSimpleName();

    private OnProgressListener onProgressListener;
    private Sampler sampler;

    private int chunkHeight;
    private int chunkWidth;
    private int chunkSpacing;
    private int chunkRadius;
    private int minChunkHeight;
    private int waveColor;
    private float progress;
    private byte[] scaledData;
    private long expansionDuration;
    private boolean isExpansionAnimated;
    private boolean isTouchable;
    private boolean isTouched;
    private long initialDelay;
    private ValueAnimator expansionAnimator;
    private Paint wavePaint;
    private Paint waveFilledPaint;
    private Bitmap waveBitmap;
    private int width;
    private int height;

    public TideView(Context context) {
        super(context);
        setWillNotDraw(false);
        init(context);
    }

    public TideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        init(context);
        inflateAttrs(attrs);
    }

    public TideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        init(context);
        inflateAttrs(attrs);
    }

    public TideView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        init(context);
        inflateAttrs(attrs);
    }

    private void init(Context context) {
        this.sampler = Sampler.getInstance();

        this.chunkWidth = Graphics.dpToPx(context, 5);
        this.chunkSpacing = Graphics.dpToPx(context, 2);
        this.minChunkHeight = Graphics.dpToPx(context, 2);
        this.waveColor = ContextCompat.getColor(context, R.color.tideProgress);
        this.scaledData = new byte[0];
        this.expansionDuration = 400L;
        this.isExpansionAnimated = true;
        this.isTouchable = true;
        this.initialDelay = 50L;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        valueAnimator.setDuration(this.expansionDuration);
        valueAnimator.setInterpolator(new OvershootInterpolator());
        valueAnimator.addUpdateListener(this);
        this.expansionAnimator = valueAnimator;
        this.wavePaint = Graphics.getSmoothPaint(Graphics.withAlpha(waveColor,170));
        this.waveFilledPaint = Graphics.getFilterPaint(this.waveColor);
    }

    @Nullable
    public OnProgressListener getOnProgressListener() {
        return this.onProgressListener;
    }

    public void setOnProgressListener(@Nullable OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public int getChunkHeight() {
        return this.chunkHeight == 0 ? this.height : Math.abs(this.chunkHeight);
    }

    public void setChunkHeight(int value) {
        this.chunkHeight = value;
        redrawData();
    }

    public int getChunkWidth() {
        return this.chunkWidth;
    }

    public void setChunkWidth(int value) {
        this.chunkWidth = Math.abs(value);
        redrawData();
    }

    public int getChunkSpacing() {
        return this.chunkSpacing;
    }

    public void setChunkSpacing(int value) {
        this.chunkSpacing = Math.abs(value);
        redrawData();
    }

    public int getChunkRadius() {
        return this.chunkRadius;
    }

    public void setChunkRadius(int value) {
        this.chunkRadius = Math.abs(value);
        redrawData();
    }

    public int getMinChunkHeight() {
        return this.minChunkHeight;
    }

    public void setMinChunkHeight(int value) {
        this.minChunkHeight = Math.abs(value);
        redrawData();
    }

    public int getWaveColor() {
        return this.waveColor;
    }

    public void setWaveColor(int value) {
        this.wavePaint = Graphics.getSmoothPaint(Graphics.withAlpha(value, 170));
        this.waveFilledPaint = Graphics.getFilterPaint(value);
        redrawData();
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float value) {
        if (value >= 0 && value <= 100) {
            this.progress = Math.abs(value);
            if (onProgressListener != null) {
                onProgressListener.onProgressChanged(this, this.progress, this.isTouched);
            }
            postInvalidate();
        }
    }

    @NonNull
    public byte[] getScaledData() {
        return this.scaledData;
    }

    public void setScaledData(@NonNull byte[] value) {
        this.scaledData = value.length <= this.getChunksCount() ? sampler.paste(new byte[this.getChunksCount()], value) : value;
        redrawData();
    }

    public long getExpansionDuration() {
        return this.expansionDuration;
    }

    public final void setExpansionDuration(long value) {
        this.expansionDuration = Math.max(400L, value);
        ValueAnimator valueAnimator = this.expansionAnimator;
        valueAnimator.setDuration(this.expansionDuration);
    }

    public boolean isExpansionAnimated() {
        return this.isExpansionAnimated;
    }

    public void setExpansionAnimated(boolean flag) {
        this.isExpansionAnimated = flag;
    }

    public boolean isTouchable() {
        return this.isTouchable;
    }

    public void setTouchable(boolean flag) {
        this.isTouchable = flag;
    }

    public boolean isTouched() {
        return this.isTouched;
    }

    public int getChunksCount() {
        return this.width / this.getChunkStep();
    }

    private int getChunkStep() {
        return this.chunkWidth + this.chunkSpacing;
    }

    private int getCenterY() {
        return this.height / 2;
    }

    private float getProgressFactor() {
        return this.progress / 100.0F;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.save();
            canvas.clipRect(0, 0, width, height);
            canvas.drawBitmap(waveBitmap, 0.0F, 0.0F, wavePaint);
            canvas.restore();
            canvas.save();
            canvas.clipRect(0.0F, 0.0F, (float) width * getProgressFactor(), (float) height);
            canvas.drawBitmap(waveBitmap, 0.0F, 0.0F, waveFilledPaint);
            canvas.restore();
        }
    }

    @SuppressLint({"DrawAllocation"})
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.width = right - left;
        this.height = bottom - top;
        if (!Graphics.fits(this.waveBitmap, this.width, this.height)) {
            if (changed) {
                Graphics.safeRecycle(this.waveBitmap);
                this.waveBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
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
            if (this.isTouchable && this.isEnabled()) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        this.isTouched = true;
                        this.setProgress(this.toProgress(event));
                        if (onProgressListener != null) {
                            onProgressListener.onStartTracking(this, this.progress);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        this.isTouched = false;
                        if (onProgressListener != null) {
                            onProgressListener.onStopTracking(this, this.progress);
                        }
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        this.isTouched = true;
                        this.setProgress(this.toProgress(event));
                        return true;
                    default:
                        this.isTouched = false;
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
                        if (isExpansionAnimated()) {
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

    private float toProgress(@NonNull MotionEvent motionEvent) {
        return Graphics.clamp(motionEvent.getX(), 0.0F, (float) this.width) / (float) this.width * 100.0F;
    }

    private void redrawData(Canvas canvas, float factor) {
        if (this.waveBitmap != null) {
            Graphics.flush(this.waveBitmap);
            for (int i = 0; i < this.scaledData.length; ++i) {
                byte b = this.scaledData[i];
                int chunkHeight = (int) (((float) sampler.getAbs(b) / (float) Byte.MAX_VALUE) * getChunkHeight());
                int clampedHeight = Math.max(chunkHeight, minChunkHeight);
                float heightDiff = (float) (clampedHeight - minChunkHeight);
                int animatedDiff = (int) (heightDiff * factor);

                RectF rectF = Graphics.rectFOf(chunkSpacing / 2f + i * getChunkStep(),
                        getCenterY() - minChunkHeight - animatedDiff,
                        chunkSpacing / 2f + i * getChunkStep() + chunkWidth,
                        getCenterY() + minChunkHeight + animatedDiff);
                canvas.drawRoundRect(rectF, chunkRadius, chunkRadius, wavePaint);
            }
            postInvalidate();
        }
    }

    private void redrawData() {
        if (waveBitmap != null) {
            Canvas canvas = Graphics.inCanvas(waveBitmap);
            redrawData(canvas, 1);
        }
    }

    private void animateExpansion() {
        this.expansionAnimator.start();
    }

    private void inflateAttrs(AttributeSet attrs) {
        TypedArray resAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.TideView, 0, 0);
        if (resAttrs != null) {
            this.setChunkHeight(resAttrs.getDimensionPixelSize(R.styleable.TideView_chunkHeight, this.getChunkHeight()));
            this.setChunkWidth(resAttrs.getDimensionPixelSize(R.styleable.TideView_chunkWidth, this.chunkWidth));
            this.setChunkSpacing(resAttrs.getDimensionPixelSize(R.styleable.TideView_chunkSpacing, this.chunkSpacing));
            this.setMinChunkHeight(resAttrs.getDimensionPixelSize(R.styleable.TideView_minChunkHeight, this.minChunkHeight));
            this.setChunkRadius(resAttrs.getDimensionPixelSize(R.styleable.TideView_chunkRadius, this.chunkRadius));
            this.isTouchable = resAttrs.getBoolean(R.styleable.TideView_touchable, this.isTouchable);
            this.setWaveColor(resAttrs.getColor(R.styleable.TideView_waveColor, this.waveColor));
            this.setProgress(resAttrs.getFloat(R.styleable.TideView_progress, this.progress));
            this.isExpansionAnimated = resAttrs.getBoolean(R.styleable.TideView_animateExpansion, this.isExpansionAnimated);
            resAttrs.recycle();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        redrawData(Graphics.inCanvas(waveBitmap), valueAnimator.getAnimatedFraction());
    }

    public interface OnProgressListener {
        void onStartTracking(@NonNull TideView tideView, float progress);
        void onStopTracking(@NonNull TideView tideView, float progress);
        void onProgressChanged(@NonNull TideView tideView, float progress, boolean fromUser);
    }

    public interface OnSamplingListener {
        void onComplete();
    }
}
