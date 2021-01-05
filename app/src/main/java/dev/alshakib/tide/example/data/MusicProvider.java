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

package dev.alshakib.tide.example.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;
import java.util.List;

import dev.alshakib.tasker.Tasker;
import dev.alshakib.tide.example.data.model.Music;

public class MusicProvider {
    private static final String LOG_TAG = MusicProvider.class.getSimpleName();

    private static volatile MusicProvider INSTANCE;

    private final Tasker tasker;
    private final MutableLiveData<List<Music>> musicListMutableLiveData;
    private final ContentResolver contentResolver;

    private final String[] musicListProjections;

    private MusicProvider(@NonNull Context context) {
        this.tasker = new Tasker();
        this.musicListMutableLiveData = new MutableLiveData<>();
        this.contentResolver = context.getContentResolver();

        List<String> musicListProjectionList = new LinkedList<>();
        musicListProjectionList.add(MediaStore.Audio.Media._ID);
        musicListProjectionList.add(MediaStore.Audio.Media.TITLE);
        musicListProjectionList.add(MediaStore.Audio.Media.ALBUM);
        musicListProjectionList.add(MediaStore.Audio.Media.ARTIST);
        musicListProjectionList.add(MediaStore.Audio.Media.DURATION);
        musicListProjectionList.add(MediaStore.Audio.Media.SIZE);

        musicListProjections = musicListProjectionList.toArray(new String[0]);
    }

    public LiveData<List<Music>> getMusicListLiveData() {
        return musicListMutableLiveData;
    }

    public void loadMusicList() {
        tasker.executeAsync(new Tasker.Task<List<Music>>() {
            @Override
            protected List<Music> doInBackground() {
                List<Music> musicList = new LinkedList<>();

                Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
                        music.setPath(String.valueOf(ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)));
                        musicList.add(music);
                    }
                    cursor.close();
                }
                return musicList;
            }

            @Override
            protected void onPostExecute(List<Music> result) {
                super.onPostExecute(result);
                musicListMutableLiveData.setValue(result);
            }
        });
    }

    @NonNull
    public static MusicProvider getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (MusicProvider.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicProvider(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
}
