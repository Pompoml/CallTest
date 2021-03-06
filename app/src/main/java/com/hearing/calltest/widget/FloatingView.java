package com.hearing.calltest.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.hearing.calltest.R;
import com.hearing.calltest.business.VideoDBHelper;

/**
 * @author liujiadong
 * @since 2019/12/17
 */
public class FloatingView extends FrameLayout {
    private Context mContext;
    private WindowManager mWindowManager;
    private View mView;
    private VideoView mVideoView;
    private LockSlidingView mAcceptView;
    private LockSlidingView mEndCallView;
    private TextView mNameView;
    private TextView mNumberView;
    private ImageView mHeadView;
    private OnCallListener mListener;
    private boolean mShown = false;

    public FloatingView(Context context) {
        super(context);
        mContext = context;

        mView = LayoutInflater.from(context).inflate(R.layout.floating_view, null);

        mAcceptView = mView.findViewById(R.id.get_call);
        mEndCallView = mView.findViewById(R.id.end_call);
        mNameView = mView.findViewById(R.id.name_tv);
        mNumberView = mView.findViewById(R.id.number_tv);
        mHeadView = mView.findViewById(R.id.head_icon);

        mAcceptView.setListener(() -> {
            hide();
            if (mListener != null) {
                mListener.onGet();
            }
        });
        mAcceptView.setOnSingleTapListener(e -> {
            hide();
            if (mListener != null) {
                mListener.onGet();
            }
        });

        mEndCallView.setListener(() -> {
            hide();
            if (mListener != null) {
                mListener.onEnd();
            }
        });
        mEndCallView.setOnSingleTapListener(e -> {
            hide();
            if (mListener != null) {
                mListener.onEnd();
            }
        });

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mVideoView = mView.findViewById(R.id.video_view);
        mVideoView.setOnPreparedListener(mp -> {
            mp.start();
            mp.setLooping(true);
        });
    }

    public void setPerson(String name, String number) {
        if (!TextUtils.isEmpty(name)) {
            mNameView.setText(name);
        }
        if (!TextUtils.isEmpty(number)) {
            mNumberView.setText(number);
        }
    }

    public void setHead(Drawable drawable) {
        if (drawable != null) {
            mHeadView.setImageDrawable(drawable);
        }
    }

    public void setListener(OnCallListener listener) {
        this.mListener = listener;
    }

    public void show(String number) {
        mVideoView.setVideoPath(VideoDBHelper.getInstance().getSelectVideo(mContext, number));
        mAcceptView.setVisible();
        mEndCallView.setVisible();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        try {
            mWindowManager.addView(mView, params);
        } catch (Exception e) {
        }
        mShown = true;

        mVideoView.start();
    }

    public void hide() {
        if (mShown) {
            try {
                mWindowManager.removeView(mView);
                mShown = false;
                mNameView.setText("");
                mNumberView.setText("");
                mHeadView.setImageDrawable(null);
                mVideoView.pause();
            } catch (Exception e) {
            }
        }
    }

    public interface OnCallListener {
        void onGet();

        void onEnd();
    }
}
