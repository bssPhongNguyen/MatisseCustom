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
package com.zhihu.matisse.internal.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;

public class MediaGrid extends SquareFrameLayout implements View.OnClickListener {

    public static final int UNCHECKED = Integer.MIN_VALUE;

    private RadiusImageView mThumbnail;
    private View mSelectedBorder;
    private View mSelectedNumberBackground;
    private TextView mSelectedNumber;
    private ImageView mGifTag;
    private TextView mVideoDuration;
    private View mDurationContainer;

    private Item mMedia;
    private PreBindInfo mPreBindInfo;
    private OnMediaGridClickListener mListener;

    public MediaGrid(Context context) {
        super(context);
        init(context);
    }

    public MediaGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.media_grid_content, this, true);

        mThumbnail = findViewById(R.id.media_thumbnail);
        mSelectedBorder = findViewById(R.id.media_grid_selected_border);
        mSelectedNumberBackground = findViewById(R.id.media_grid_selected_number_background);
        mSelectedNumber = findViewById(R.id.media_grid_selected_tv_number);
        mDurationContainer = findViewById(R.id.media_grid_duration_container);
        mGifTag = (ImageView) findViewById(R.id.gif);
        mVideoDuration = (TextView) findViewById(R.id.video_duration);

        mThumbnail.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onThumbnailClicked(mThumbnail, mMedia, mPreBindInfo.mViewHolder);
        }

        ViewCompat.animate(this).rotation(360).setDuration(300).start();
    }

    public void preBindMedia(PreBindInfo info) {
        mPreBindInfo = info;
    }

    public void bindMedia(Item item) {
        mMedia = item;
        setGifTag();
        setImage();
        setVideoDuration();
    }

    public Item getMedia() {
        return mMedia;
    }

    private void setGifTag() {
        mGifTag.setVisibility(mMedia.isGif() ? View.VISIBLE : View.GONE);
    }

    public void setCheckedNum(int checkedNum) {
        if (checkedNum == MediaGrid.UNCHECKED) {
            mSelectedBorder.setVisibility(View.INVISIBLE);
            mSelectedNumberBackground.setBackgroundColor(Color.TRANSPARENT);
            mSelectedNumber.setVisibility(View.INVISIBLE);
            mDurationContainer.setBackgroundColor(getResources().getColor(R.color.black_50_opacity));
        } else {
            mSelectedBorder.setVisibility(View.VISIBLE);
            mSelectedNumberBackground.setBackgroundColor(getResources().getColor(R.color.media_blue));
            mSelectedNumber.setVisibility(View.VISIBLE);
            mSelectedNumber.setText(String.valueOf(checkedNum));
            mDurationContainer.setBackgroundColor(getResources().getColor(R.color.media_blue));
        }
    }

    private void setImage() {
        if (mMedia.isGif()) {
            SelectionSpec.getInstance().imageEngine.loadGifThumbnail(getContext(), mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.getContentUri());
        } else {
            Log.d("loadImage", mMedia.getContentUri().toString());
            SelectionSpec.getInstance().imageEngine.loadThumbnail(getContext(), mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.getContentUri());
        }
    }

    private void setVideoDuration() {
        if (mMedia.isVideo()) {
            mDurationContainer.setVisibility(VISIBLE);
            mVideoDuration.setText(DateUtils.formatElapsedTime(mMedia.duration / 1000));
        } else {
            mDurationContainer.setVisibility(GONE);
        }
    }

    public void setOnMediaGridClickListener(OnMediaGridClickListener listener) {
        mListener = listener;
    }

    public void removeOnMediaGridClickListener() {
        mListener = null;
    }

    public interface OnMediaGridClickListener {

        void onThumbnailClicked(ImageView thumbnail, Item item, RecyclerView.ViewHolder holder);
    }

    public static class PreBindInfo {
        int mResize;
        Drawable mPlaceholder;
        RecyclerView.ViewHolder mViewHolder;

        public PreBindInfo(int resize, Drawable placeholder,
                           RecyclerView.ViewHolder viewHolder) {
            mResize = resize;
            mPlaceholder = placeholder;
            mViewHolder = viewHolder;
        }
    }

}
