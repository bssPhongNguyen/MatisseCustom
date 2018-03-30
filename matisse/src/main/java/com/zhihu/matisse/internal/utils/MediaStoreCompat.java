/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.internal.entity.SelectionSpec;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MediaStoreCompat {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;
    private CaptureStrategy mCaptureStrategy;
    private Uri mCurrentMediaUri;
    private String mCurrentMediaPath;

    public MediaStoreCompat(Activity activity) {
        mContext = new WeakReference<>(activity);
        mFragment = null;
    }

    public MediaStoreCompat(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    /**
     * Checks whether the device has a camera feature or not.
     *
     * @param context a context to check for camera feature.
     * @return true if the device has a camera feature. false otherwise.
     */
    public static boolean hasCameraFeature(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void setCaptureStrategy(CaptureStrategy strategy) {
        mCaptureStrategy = strategy;
    }

    public void dispatchCaptureIntent(Context context, int requestCode) {
        Intent captureIntent = getIntent();

        if (captureIntent.resolveActivity(context.getPackageManager()) != null) {

            File file = null;
            try {
                file = createMediaFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                mCurrentMediaPath = file.getAbsolutePath();
                mCurrentMediaUri = FileProvider.getUriForFile(context, context.getPackageName(), file);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentMediaUri);
                grantPermissionsForNeededPackages(context, captureIntent, mCurrentMediaUri);
                if (mFragment != null) {
                    mFragment.get().startActivityForResult(captureIntent, requestCode);
                } else {
                    mContext.get().startActivityForResult(captureIntent, requestCode);
                }
            }
        }
    }

    public void grantPermissionsForNeededPackages(Context context, Intent takePhotoIntent, Uri photoUri) {
        List<ResolveInfo> resInfoList = context.getPackageManager()
                .queryIntentActivities(takePhotoIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    private Intent getIntent() {
        if(SelectionSpec.getInstance().onlyShowVideos()){
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        } else {
            return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }
    }

    private File createMediaFile() throws IOException {
        if(SelectionSpec.getInstance().onlyShowVideos()){
            return createVideoFile();
        } else {
            return createImageFile();
        }
    }

    private File createVideoFile() throws IOException {

        File storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "MP4_" + timeStamp + "_.mp4";

        return new File(storageDir, fileName);
    }

    private File createImageFile() throws IOException {

        File storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timeStamp + "_.jpeg";

        return new File(storageDir, fileName);
    }

    public void addToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mCurrentMediaUri);
        mContext.get().sendBroadcast(mediaScanIntent);
    }

    public Uri getCurrentPhotoUri() {
        return mCurrentMediaUri;
    }

    public String getCurrentPhotoPath() {
        return mCurrentMediaPath;
    }
}
