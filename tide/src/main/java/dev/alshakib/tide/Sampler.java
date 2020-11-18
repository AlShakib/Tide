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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sampler {
    private static final String LOG_TAG = Sampler.class.getSimpleName();
    public static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());
    public static final ExecutorService SAMPLER_THREAD = Executors.newSingleThreadExecutor();

    private static volatile Sampler INSTANCE;

    public Sampler() { }

    public Handler getMainThread() {
        return MAIN_THREAD;
    }

    public ExecutorService getSamplerThread() {
        return SAMPLER_THREAD;
    }

    public byte getAbs(byte b) {
        byte result;
        if (b == -128) {
            result = 127;
        } else {
            if (0 >= b) {
                result = (byte) (-b);
                return result;
            }
            result = b;
        }
        return result;
    }

    @NonNull
    public byte[] paste(@NonNull byte[] bytes, @NonNull byte[] other) {
        if (bytes.length == 0) {
            return new byte[0];
        } else {
            int index = 0;
            for(int i = 0; i < bytes.length; ++i) {
                int currentIndex = index++;
                byte b;
                if (currentIndex >= 0 && currentIndex < other.length) {
                    b = other[currentIndex];
                } else {
                    b = getAbs(bytes[currentIndex]);
                }
                bytes[currentIndex] = b;
            }
            return bytes;
        }
    }

    public void downSampleAsync(@NonNull final byte[] data, final int targetSize,
                                @NonNull final OnSamplerListener onSamplerListener) {
        getSamplerThread().submit(new Runnable() {

            @Override
            public void run() {
                final byte[] scaled = downSample(data, targetSize);
                getMainThread().post(new Runnable() {

                    @Override
                    public void run() {
                        onSamplerListener.resultAsync(scaled);
                    }
                });
            }
        });
    }

    private byte[] downSample(byte[] data, int targetSize) {
        byte[] targetSized = new byte[targetSize];
        int chunkSize = data.length / targetSize;
        int chunkStep = (int) Math.max(Math.floor(chunkSize/ 10.f ), 1.0);
        int prevDataIndex = 0;
        float sampledPerChunk = 0f;
        float sumPerChunk = 0f;

        if (targetSize >= data.length) {
            return paste(targetSized, data);
        }
        for (int i = 0; i <= data.length; i += chunkStep) {
            int currentDataIndex = targetSize * i / data.length;
            if (prevDataIndex == currentDataIndex) {
                sampledPerChunk += 1;
                sumPerChunk += getAbs(data[i]);
            } else {
                targetSized[prevDataIndex] = (byte) (sumPerChunk / sampledPerChunk);
                sumPerChunk = 0f;
                sampledPerChunk = 0f;
                prevDataIndex = currentDataIndex;
            }
        }
        return targetSized;
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


    public interface OnSamplerListener {
        void resultAsync(byte[] result);
    }
}
