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

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Sampler {
    private static final String LOG_TAG = Sampler.class.getSimpleName();
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());
    private static final ExecutorService SAMPLER_THREAD = Executors.newSingleThreadExecutor();

    private static volatile Sampler INSTANCE;

    private final Random random;

    public Sampler() {
        this.random = new Random();
    }

    public Handler getMainThread() {
        return MAIN_THREAD;
    }

    public ExecutorService getSamplerThread() {
        return SAMPLER_THREAD;
    }

    public byte getAbsByte(byte b) {
        if (b == Byte.MIN_VALUE) {
            return Byte.MAX_VALUE;
        } else if (b < 0) {
            return (byte) (-b);
        }
        return b;
    }

    @NonNull
    public byte[] paste(@NonNull byte[] bytes, @NonNull byte[] from) {
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

    public byte[] getSample(byte[] bytes, int chunkCount) {
        if (chunkCount <= 1) {
            return new byte[0];
        }
        byte[] sample = new byte[chunkCount];
        int maxSampleIndex = chunkCount - 1;
        if (chunkCount >= bytes.length) {
            return paste(sample, bytes);
        }
        int step = Math.abs(bytes.length / maxSampleIndex);
        int index = 0;
        for (int i = 0; i <= bytes.length; i += step) {
            float absByte = getAbsByte(bytes[i]);
            if (index < maxSampleIndex) {
                absByte += getAbsByte(bytes[i + (step / 5)]);
                absByte += getAbsByte(bytes[i + (step / 5)]);
                absByte += getAbsByte(bytes[i + (step / 5)]);
                absByte += getAbsByte(bytes[i + (step / 5)]);
                absByte = absByte / 5.0F;
            }
            if (absByte == 0) {
                absByte = getRandomByte();
            }
            sample[index] = (byte) absByte;
            ++index;
        }
        return sample;
    }

    // Dirty hack to fill 0 byte with sample data
    private float getRandomByte() {
        return random.nextInt(Byte.MAX_VALUE - 1);
    }

    public void getSampleAsync(@NonNull final byte[] data, final int targetSize,
                               @NonNull final OnResultCallback onResultCallback) {
        getSamplerThread().submit(new Runnable() {

            @Override
            public void run() {
                final byte[] sample = getSample(data, targetSize);
                getMainThread().post(new Runnable() {

                    @Override
                    public void run() {
                        onResultCallback.onResult(sample);
                    }
                });
            }
        });
    }

    public static Sampler getInstance() {
        if (INSTANCE == null) {
            synchronized (Sampler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Sampler();
                }
            }
        }
        return INSTANCE;
    }

    public interface OnResultCallback {
        void onResult(byte[] result);
    }
}
