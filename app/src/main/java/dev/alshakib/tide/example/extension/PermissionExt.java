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

package dev.alshakib.tide.example.extension;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public final class PermissionExt {
    public final static int PERMISSION_REQUEST_MULTIPLE_PERMISSIONS = 1000;
    public final static int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1001;
    public final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1002;

    public static boolean hasRequiredStoragePermission(@NonNull Activity activity) {
        List<String> permissionList = new ArrayList<>();
        if (!PermissionExt.hasReadExternalStoragePermission(activity)) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            PermissionExt.requestPermissions(activity, permissionList, PermissionExt.PERMISSION_REQUEST_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private static boolean isBuildVersionM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void requestPermission(@NonNull Activity activity, String permission, int requestCode) {
        if (isBuildVersionM()) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
        }
    }

    public static void requestPermissions(@NonNull Activity activity, String[] permissions, int requestCode) {
        if (isBuildVersionM()) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public static void requestPermissions(@NonNull Activity activity, List<String> permissionList, int requestCode) {
        if (isBuildVersionM()) {
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[0]), requestCode);
        }
    }

    public static boolean hasReadExternalStoragePermission(@NonNull Context context) {
        if (isBuildVersionM()) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestReadExternalStoragePermission(@NonNull Activity activity) {
        if (isBuildVersionM()) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    public static boolean hasWriteExternalStoragePermission(@NonNull Context context) {
        if (isBuildVersionM()) {
            return ContextCompat
                    .checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestWriteExternalStoragePermission(@NonNull Activity activity) {
        if (isBuildVersionM()) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }
}
