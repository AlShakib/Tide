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

package dev.alshakib.tide.example.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;

import dev.alshakib.rvcompat.adapter.ListAdapterCompat;
import dev.alshakib.tide.example.R;
import dev.alshakib.tide.example.adapter.diff.MusicDiffUtilItemCallback;
import dev.alshakib.tide.example.adapter.viewholder.MusicViewHolderCompat;
import dev.alshakib.tide.example.data.model.Music;

public class MusicListAdapterCompat extends ListAdapterCompat<Music, MusicViewHolderCompat> {

    public MusicListAdapterCompat() {
        super(new AsyncDifferConfig.Builder<>(new MusicDiffUtilItemCallback()).build());
    }

    @NonNull
    @Override
    public MusicViewHolderCompat onCreateViewHolderCompat(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolderCompat(inflateView(R.layout.music_view_holder, parent, false));
    }

    @Override
    public void onBindViewHolderCompat(@NonNull MusicViewHolderCompat holder, int position) {
        if (getItem(position) != null) {
            holder.getViewBinding().textView.setText(getItem(position).getTitle());
        }
    }
}
