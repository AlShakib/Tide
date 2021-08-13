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

package dev.alshakib.tide.example.ui.fragment.tide;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.alshakib.tide.TideView;
import dev.alshakib.tide.example.databinding.FragmentTideBinding;
import dev.alshakib.tide.example.extension.AndroidExt;
import dev.alshakib.tide.example.model.Music;
import dev.alshakib.tide.example.ui.activity.MainViewModel;

public class TideFragment extends Fragment
        implements TideView.OnTideViewChangeListener, SeekBar.OnSeekBarChangeListener {

    private FragmentTideBinding viewBinding;

    private Music music;

    public TideFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        music = TideFragmentArgs.fromBundle(requireArguments()).getMusic();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AndroidExt.setActionBarTitle(requireActivity(), music.getTitle());
        viewBinding = FragmentTideBinding.inflate(inflater, container, false);
        viewBinding.tideView.setOnTideViewChangeListener(this);
        MainViewModel mainViewModel = MainViewModel.getInstance(requireActivity());
        viewBinding.tideView.setMediaUri(music.getMediaUri(), mainViewModel.getHandler());
        viewBinding.tideView.setMaxProgress(1000);
        viewBinding.progress.setText(String.valueOf(viewBinding.tideView.getProgress()));
        viewBinding.progressMax.setText(String.valueOf(viewBinding.tideView.getMaxProgress()));
        viewBinding.seekBar.setOnSeekBarChangeListener(this);
        viewBinding.seekBar.setMax(viewBinding.tideView.getMaxProgress());
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onProgressChanged(@NonNull TideView tideView, int progress, boolean fromUser) {
        viewBinding.progress.setText(String.valueOf(progress));
        if (fromUser) {
            viewBinding.fromUserTextView.setText("Progress is from user.");
            viewBinding.seekBar.setProgress(progress);
        } else {
            viewBinding.fromUserTextView.setText("Progress is not from user.");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            viewBinding.tideView.setProgress(progress);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStartTrackingTouch(@NonNull TideView tideView) {
        viewBinding.trackingStatusTextView.setText("Tracking Touch: Start");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStopTrackingTouch(@NonNull TideView tideView) {
        viewBinding.trackingStatusTextView.setText("Tracking Touch: Stop");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
