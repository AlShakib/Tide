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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.alshakib.tide.example.R;
import dev.alshakib.tide.example.databinding.ActivityMainBinding;
import dev.alshakib.tide.example.extension.PermissionExt;

public class MainActivity extends AppCompatActivity {
    private static final int ENABLE_STORAGE_PERMISSION = 101;

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        setSupportActionBar(viewBinding.materialToolbar);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.nav_music_list)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        if (PermissionExt.hasRequiredStoragePermission(this)) {
            MainViewModel.getInstance(this).loadMusicList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_STORAGE_PERMISSION) {
            if (PermissionExt.hasReadExternalStoragePermission(this)) {
                MainViewModel.getInstance(this).loadMusicList();
            } else {
                showStoragePermissionDialogDeniedDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionExt.PERMISSION_REQUEST_MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                boolean isAllGranted = true;
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                if (isAllGranted) {
                    MainViewModel.getInstance(this).loadMusicList();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showNeedStoragePermissionDialog();
                } else {
                    showStoragePermissionDialogDeniedDialog();
                }
            }
        }
    }

    private void showNeedStoragePermissionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_read_storage)
                .setMessage(R.string.dialog_message_read_storage)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> PermissionExt.hasRequiredStoragePermission(MainActivity.this))
                .setCancelable(false)
                .show();
    }

    private void showStoragePermissionDialogDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_read_storage)
                .setMessage(R.string.dialog_message_read_storage_denied)
                .setPositiveButton(R.string.button_exit, (dialog, which) -> finish())
                .setNegativeButton(R.string.button_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, ENABLE_STORAGE_PERMISSION);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
