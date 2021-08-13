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

package dev.alshakib.tide.example.ui.activity;

import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.LinkedList;
import java.util.List;

import dev.alshakib.tide.example.adapter.MusicListAdapterCompat;
import dev.alshakib.tide.example.model.Music;

public class MainViewModel extends AndroidViewModel {
    @NonNull
    public static MainViewModel getInstance(@NonNull FragmentActivity activity) {
        return new ViewModelProvider(activity).get(MainViewModel.class);
    }

    private final Handler handler;
    private final Handler mainHandler;
    private final MusicListAdapterCompat musicListAdapterCompat;
    private final String[] musicListProjections;

    public MainViewModel(@NonNull Application application) {
        super(application);
        HandlerThread handlerThread = new HandlerThread("main_view_model_thread");
        handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
        this.mainHandler = new Handler(Looper.getMainLooper());
        musicListAdapterCompat = new MusicListAdapterCompat();
        List<String> musicListProjectionList = new LinkedList<>();
        musicListProjectionList.add(MediaStore.Audio.Media._ID);
        musicListProjectionList.add(MediaStore.Audio.Media.TITLE);
        musicListProjectionList.add(MediaStore.Audio.Media.ALBUM);
        musicListProjectionList.add(MediaStore.Audio.Media.ARTIST);
        musicListProjectionList.add(MediaStore.Audio.Media.DURATION);
        musicListProjectionList.add(MediaStore.Audio.Media.SIZE);

        musicListProjections = musicListProjectionList.toArray(new String[0]);
    }

    @NonNull
    public Handler getHandler() {
        return handler;
    }

    @NonNull
    public Handler getMainHandler() {
        return mainHandler;
    }

    public MusicListAdapterCompat getListAdapterCompat() {
        return musicListAdapterCompat;
    }

    public void loadMusicList() {
        handler.post(() -> {
            List<Music> musicList = new LinkedList<>();
            Cursor cursor = getApplication().getContentResolver()
                    .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    musicListProjections, null, null, MediaStore.Audio.Media.TITLE);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Music music = new Music();
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    music.setId(id);
                    music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    music.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                    music.setSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                    music.setMediaUri(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
                    musicList.add(music);
                }
                cursor.close();
            }
            mainHandler.post(() -> musicListAdapterCompat.submitList(musicList));
        });
    }
}
