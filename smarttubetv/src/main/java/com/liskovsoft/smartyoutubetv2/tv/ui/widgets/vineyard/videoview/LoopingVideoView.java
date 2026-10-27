package com.liskovsoft.smartyoutubetv2.tv.ui.widgets.vineyard.videoview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;

public class LoopingVideoView extends VideoView {

    public LoopingVideoView(Context context) {
        super(context);
    }

    public LoopingVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoopingVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public LoopingVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnVideoReadyListener {
        void onVideoReady();
    }

}
