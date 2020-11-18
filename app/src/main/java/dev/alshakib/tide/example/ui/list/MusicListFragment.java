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

package dev.alshakib.tide.example.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import dev.alshakib.rvcompat.viewholder.ViewHolderCompat;
import dev.alshakib.tide.example.R;
import dev.alshakib.tide.example.adapter.MusicListAdapterCompat;
import dev.alshakib.tide.example.data.model.Music;
import dev.alshakib.tide.example.databinding.FragmentMusicListBinding;
import dev.alshakib.tide.example.extension.AndroidExt;

public class MusicListFragment extends Fragment implements ViewHolderCompat.OnItemClickListener {
    private static final String LOG_TAG = MusicListFragment.class.getSimpleName();

    private FragmentMusicListBinding viewBinding;
    private NavController navController;

    private MusicListViewModel musicListViewModel;

    private MusicListAdapterCompat musicListAdapterCompat;

    public MusicListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicListViewModel = new ViewModelProvider(this).get(MusicListViewModel.class);
        musicListAdapterCompat = new MusicListAdapterCompat(requireContext());
        musicListAdapterCompat.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentMusicListBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        viewBinding.recyclerView.setHasFixedSize(true);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewBinding.recyclerView.setAdapter(musicListAdapterCompat);

        musicListViewModel.getMusicListLiveData().observe(getViewLifecycleOwner(),
                new Observer<List<Music>>() {
                    @Override
                    public void onChanged(List<Music> music) {
                        musicListAdapterCompat.submitList(music);
                    }
                });
    }

    @Override
    public void onItemClick(@NonNull View v, int viewType, int position) {
        Music music = musicListAdapterCompat.getCurrentList().get(position);
        if (music != null) {
            AndroidExt.safeNavigateTo(navController, R.id.nav_music_list,
                    MusicListFragmentDirections.actionNavMusicListToNavTide(music));
        }
    }
}