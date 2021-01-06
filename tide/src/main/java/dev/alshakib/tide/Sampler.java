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

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Sampler {
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
