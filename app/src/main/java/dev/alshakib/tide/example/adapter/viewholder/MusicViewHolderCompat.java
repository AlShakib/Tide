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

package dev.alshakib.tide.example.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import dev.alshakib.rvcompat.viewholder.ViewHolderCompat;
import dev.alshakib.tide.example.databinding.MusicViewHolderBinding;

public class MusicViewHolderCompat extends ViewHolderCompat {

    private final MusicViewHolderBinding viewBinding;

    public MusicViewHolderCompat(@NonNull View itemView) {
        super(itemView);
        this.viewBinding = MusicViewHolderBinding.bind(itemView);
        viewBinding.getRoot().setOnClickListener(this);
    }

    public MusicViewHolderBinding getViewBinding() {
        return viewBinding;
    }
}
