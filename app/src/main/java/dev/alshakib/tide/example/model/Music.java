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

package dev.alshakib.tide.example.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

@Keep
public class Music implements Parcelable {
    private int id;
    private String title;
    private String album;
    private String artist;
    private Uri mediaUri;
    private long duration;
    private int size;

    public Music() { }

    protected Music(Parcel in) {
        id = in.readInt();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        mediaUri = in.readParcelable(Uri.class.getClassLoader());
        duration = in.readLong();
        size = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeParcelable(mediaUri, flags);
        dest.writeLong(duration);
        dest.writeInt(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return id == music.id &&
                duration == music.duration &&
                size == music.size &&
                Objects.equals(title, music.title) &&
                Objects.equals(album, music.album) &&
                Objects.equals(artist, music.artist) &&
                Objects.equals(mediaUri, music.mediaUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, album, artist, mediaUri, duration, size);
    }

    @NonNull
    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    public static class DiffItemCallback extends DiffUtil.ItemCallback<Music> {

        @Override
        public boolean areItemsTheSame(@NonNull Music oldItem, @NonNull Music newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Music oldItem, @NonNull Music newItem) {
            return oldItem.equals(newItem);
        }
    }
}
