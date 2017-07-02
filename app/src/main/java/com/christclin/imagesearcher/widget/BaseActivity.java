package com.christclin.imagesearcher.widget;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.christclin.imagesearcher.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;

    private OnPermissionListener mOnPermissionListener = null;
    public interface OnPermissionListener {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean checkPermissions(String[] permissions, OnPermissionListener listener) {
        String[] deniedPermissions = getDeniedPermissions(permissions);
        if (deniedPermissions != null && deniedPermissions.length > 0) {
            requestPermissions(deniedPermissions, listener);
            return false;
        } else {
            return true;
        }
    }

    public String[] getDeniedPermissions(String[] permissions) {
        if (permissions == null || permissions.length <= 0) return null;
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (String permission: permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        if (deniedPermissions.size() > 0) {
            return deniedPermissions.toArray(new String[deniedPermissions.size()]);
        } else {
            return null;
        }
    }

    private String getRationaleString(final String[] permissions) {
        if (permissions == null || permissions.length <=0) return "";

        HashMap<String, String> permissionHashMap = new HashMap<>();
        for (String permission: permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                switch (permission) {
                    // Storage group
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        permissionHashMap.put("Storage", getString(R.string.permission_storage_reason));
                        break;
                    // Camera group
                    case Manifest.permission.CAMERA:
                        permissionHashMap.put("Camera", getString(R.string.permission_camera_reason));
                        break;
                }
            }
        }
        StringBuilder rationaleDescription = new StringBuilder();
        Set<String> keySet = permissionHashMap.keySet();
        for (String key: keySet) {
            if (rationaleDescription.length() == 0) {
                rationaleDescription.append(permissionHashMap.get(key));
            } else {
                rationaleDescription.append("\n");
                rationaleDescription.append(permissionHashMap.get(key));
            }
        }
        return rationaleDescription.toString();
    }

    public void requestPermissions(final String[] permissions, OnPermissionListener listener) {
        if (permissions == null || permissions.length <=0 || listener == null) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        mOnPermissionListener = listener;

        String rationaleDescription = getRationaleString(permissions);

        if (!TextUtils.isEmpty(rationaleDescription)) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.permission_reason_title)
                    .setMessage(rationaleDescription)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseActivity.this, permissions, REQUEST_PERMISSIONS);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {

        boolean isAllGranted = true;
        if (requestCode == REQUEST_PERMISSIONS) {
            int deniedCount = 0;
            boolean showSettingMessage = false;

            for (int i=0; i< grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        showSettingMessage = true;
                    }
                    isAllGranted = false;
                    deniedCount++;
                }
            }

            if (isAllGranted) {
                mOnPermissionListener.onPermissionGranted();
            } else {
                mOnPermissionListener.onPermissionDenied();
                if (showSettingMessage) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.permission_not_granted_setting)
                            .setPositiveButton(R.string.permission_btn_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .create();
                    dialog.show();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(android.R.string.dialog_alert_title)
                            .setMessage(R.string.permission_not_granted)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }
            }
        }
    }
}
