package com.liskovsoft.smartyoutubetv2.tv.ui.widgets.vineyard.videoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.liskovsoft.smartyoutubetv2.tv.R;

public class PreviewCardView extends FrameLayout {
    FrameLayout mMainContainer;

    LoopingVideoView mVideoView;

    ImageView mImageView;

    View mOverlayView;

    ProgressBar mProgressCard;

    public PreviewCardView(Context context) {
        super(context);
        init();
    }

    public PreviewCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.widget_preview_card, this);

        mMainContainer = view.findViewById(R.id.main_container);
        mVideoView = view.findViewById(R.id.main_video);
        mImageView = view.findViewById(R.id.main_image);
        mOverlayView = view.findViewById(R.id.view_overlay);
        mProgressCard = view.findViewById(R.id.progress_card);
    }

}
