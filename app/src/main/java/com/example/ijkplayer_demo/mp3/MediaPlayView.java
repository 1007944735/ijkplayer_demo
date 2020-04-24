package com.example.ijkplayer_demo.mp3;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ijkplayer_demo.R;

public class MediaPlayView extends FrameLayout implements TextureView.SurfaceTextureListener {
    private MediaService mService;
    private TextureView mTextureView;
    private SurfaceTexture mSurface;
    private MediaInfo mediaInfo;
    private int width;
    private int height;
    private int oldWidth;
    private int oldHeight;
    private boolean fullScreen = false;
    private View controlView;

    private LinearLayout llTop;
    private FrameLayout llCenter;
    private LinearLayout llBottom;
    private ImageView ivVideoBack;
    private TextView tvVideoTitle;
    private ImageView ivVideoPlay;
    private SeekBar sbVideoProgress;
    private TextView tvVideoTime;
    private TextView tvVideoDefinition;
    private ImageView ivVideoSize;
    private ProgressBar pbVideoProgress;
    private ImageView ivVideoFinishPlay;
    private TextView tvVideoSpeed;
    private boolean dragSeek = true;

    private TranslateAnimation topEnterAnim;
    private TranslateAnimation topExitAnim;
    private TranslateAnimation bottomEnterAnim;
    private TranslateAnimation bottomExitAnim;
    private boolean isControlShow = true;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (topExitAnim != null && bottomExitAnim != null) {
                llTop.startAnimation(topExitAnim);
                llBottom.startAnimation(bottomExitAnim);
                isControlShow = false;
            }
        }
    };


    public MediaPlayView(@NonNull Context context) {
        this(context, null);
    }

    public MediaPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaPlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.BLACK);
        initControl();
        createAnim();
    }

    private void createAnim() {
        topEnterAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f);
        topEnterAnim.setDuration(300);
        topEnterAnim.setFillAfter(true);
        topEnterAnim.setInterpolator(new LinearInterpolator());

        topExitAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f);
        topExitAnim.setDuration(300);
        topExitAnim.setFillAfter(true);
        topExitAnim.setInterpolator(new LinearInterpolator());

        bottomEnterAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f);
        bottomEnterAnim.setDuration(300);
        bottomEnterAnim.setFillAfter(true);
        bottomEnterAnim.setInterpolator(new LinearInterpolator());

        bottomExitAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
        bottomExitAnim.setDuration(300);
        bottomExitAnim.setFillAfter(true);
        bottomExitAnim.setInterpolator(new LinearInterpolator());
    }

    private void initControl() {
        if (controlView == null) {
            controlView = LayoutInflater.from(getContext()).inflate(R.layout.layout_media_play_control, this, false);
        }
        removeParent(controlView);
        addView(controlView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initControlView();
    }

    private void initControlView() {
        controlView.setVisibility(GONE);
        llTop = controlView.findViewById(R.id.ll_top);
        llCenter = controlView.findViewById(R.id.ll_center);
        llBottom = controlView.findViewById(R.id.ll_bottom);
        ivVideoBack = controlView.findViewById(R.id.iv_video_back);
        tvVideoTitle = controlView.findViewById(R.id.tv_video_title);
        ivVideoPlay = controlView.findViewById(R.id.iv_video_play);
        sbVideoProgress = controlView.findViewById(R.id.sb_video_progress);
        tvVideoTime = controlView.findViewById(R.id.tv_video_time);
        tvVideoDefinition = controlView.findViewById(R.id.tv_video_definition);
        ivVideoSize = controlView.findViewById(R.id.iv_video_size);
        pbVideoProgress = controlView.findViewById(R.id.pb_video_progress);
        ivVideoFinishPlay = controlView.findViewById(R.id.iv_video_finish_play);
        tvVideoSpeed = controlView.findViewById(R.id.tv_video_speed);

        llCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isControlShow) {
                    llTop.startAnimation(topExitAnim);
                    llBottom.startAnimation(bottomExitAnim);
                    isControlShow = false;
                } else {
                    llTop.startAnimation(topEnterAnim);
                    llBottom.startAnimation(bottomEnterAnim);
                    isControlShow = true;
                    resetControlTimer();
                }
            }
        });
        ivVideoBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    if (fullScreen) {
                        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        ((Activity) getContext()).finish();
                    }
                }
            }
        });
        ivVideoPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null) {
                    if (mService.isPlaying()) {
                        mService.pause();
                    } else if (mService.getMediaStatus() == MediaService.STATE_FINISH || mService.getMediaStatus() == MediaService.STATE_ERROR) {
                        mService.playWithUrl(mediaInfo.getUrl());
                    } else {
                        mService.play();
                    }
                }
            }
        });
        sbVideoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mService != null) {
                    mService.seekTo(seekBar.getProgress() * 1000);
                }
                resetControlTimer();
            }
        });
        tvVideoTime.setText("00:00/00:00");
        tvVideoDefinition.setVisibility(GONE);
        ivVideoSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    if (fullScreen) {
                        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                }
            }
        });
        ivVideoFinishPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null) {
                    if (mService.isPlaying()) {
                        mService.pause();
                    } else if (mService.getMediaStatus() == MediaService.STATE_FINISH || mService.getMediaStatus() == MediaService.STATE_ERROR) {
                        mService.playWithUrl(mediaInfo.getUrl());
                    } else {
                        mService.play();
                    }
                }
            }
        });
    }

    private void resetControlTimer() {
        removeCallbacks(runnable);
        postDelayed(runnable, 5000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        Log.d("TAG", "screenSize: " + this.width + this.height);
    }

    public void bindMediaService(MediaService mediaService) {
        mService = mediaService;
        mService.registerListener(mStatusListener);
        mService.registerListener(mProgressListener);
        addDisplay();
    }

    private void addDisplay() {
        removeParent(mTextureView);
        mTextureView = new TextureView(getContext());
        mTextureView.setSurfaceTextureListener(this);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mTextureView.setLayoutParams(lp);
        addView(mTextureView, 0);
    }

    private void removeParent(View view) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
    }


    private void fitDisplaySize(final int width, final int height, final int videoWidth, final int videoHeight) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }
        LayoutParams lp = (LayoutParams) mTextureView.getLayoutParams();
        float scale = videoWidth * 1.0f / videoHeight * 1.0f;
        float fitWidth = width;
        float fitHeight = width / scale;
        if (fitHeight > height) {
            fitWidth = height * scale;
            fitHeight = height;
        }
        lp.width = (int) fitWidth;
        lp.height = (int) fitHeight;
        lp.gravity = Gravity.CENTER;
        Log.d("TAG", "fitDisplaySize: " + lp.width + "|" + lp.height);
        mTextureView.setLayoutParams(lp);
    }

    private Size setViewSize(int width, int height) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        Size oldSize = new Size(this.width, this.height);
        lp.width = width;
        lp.height = height;
        setLayoutParams(lp);
        return oldSize;
    }

    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        tvVideoTitle.setText(title);
    }

    public void enterFullPlay() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        Size size = setViewSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fitDisplaySize(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, mediaInfo.getWidth(), mediaInfo.getHeight());
        oldWidth = size.getWidth();
        oldHeight = size.getHeight();
        fullScreen = true;
        ivVideoSize.setImageResource(R.mipmap.ico_video_narrow);
    }

    public void exitFullPlay() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setViewSize(oldWidth, oldHeight);
        Log.d("TAG1", "enterFullPlay: " + oldWidth + "|" + oldHeight);
        fitDisplaySize(oldWidth, oldHeight, mediaInfo.getWidth(), mediaInfo.getHeight());
        fullScreen = false;
        ivVideoSize.setImageResource(R.mipmap.ico_video_full);
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    private void initControlState() {
        if (mService != null) {
            controlView.setVisibility(VISIBLE);
            ivVideoPlay.setImageResource(mService.isPlaying() ? R.mipmap.ico_video_pause : R.mipmap.ico_video_play);
            ivVideoSize.setImageResource(isFullScreen() ? R.mipmap.ico_video_narrow : R.mipmap.ico_video_full);
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
        if (mService != null) {
            mediaInfo = mService.getMediaInfo();
            initControlState();
            fitDisplaySize(this.width, this.height, mediaInfo.getWidth(), mediaInfo.getHeight());
            mService.setSurface(new Surface(surface));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        fitDisplaySize(this.width, this.height, mediaInfo.getWidth(), mediaInfo.getHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //这里可能有问题
        if (mService != null) {
            mService.unRegisterListener(mStatusListener);
            mService.unRegisterListener(mProgressListener);
            Log.d("TAG", "onDetachedFromWindow: ");
        }
        releaseAnim(topEnterAnim);
        releaseAnim(topExitAnim);
        releaseAnim(bottomEnterAnim);
        releaseAnim(bottomExitAnim);
    }

    private void releaseAnim(Animation animation) {
        if (animation != null) {
            if (animation.hasStarted()) {
                animation.cancel();
            }
            animation = null;
        }
    }

    private MediaService.MediaPlayStatusListener mStatusListener = new MediaService.MediaPlayStatusListener() {
        @Override
        public void onStatusChange(int status) {
            Log.d("TAG", "onStatusChange: " + status);
            switch (status) {
                case MediaService.STATE_IDLE:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_play);
                    ivVideoFinishPlay.setVisibility(GONE);
                    pbVideoProgress.setVisibility(GONE);
                    tvVideoSpeed.setVisibility(GONE);
                    break;
                case MediaService.STATE_PREPARING:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_play);
                    ivVideoFinishPlay.setVisibility(GONE);
                    pbVideoProgress.setVisibility(VISIBLE);
                    tvVideoSpeed.setVisibility(VISIBLE);
                    resetControlTimer();
                    break;
                case MediaService.STATE_PREPARED:
                    mediaInfo = mService.getMediaInfo();
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_play);
                    ivVideoFinishPlay.setVisibility(GONE);
                    pbVideoProgress.setVisibility(GONE);
                    tvVideoSpeed.setVisibility(GONE);
                    break;
                case MediaService.STATE_PLAYING:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_pause);
                    ivVideoFinishPlay.setVisibility(GONE);
                    pbVideoProgress.setVisibility(GONE);
                    tvVideoSpeed.setVisibility(GONE);
                    break;
                case MediaService.STATE_PAUSED:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_play);
                    ivVideoFinishPlay.setVisibility(VISIBLE);
                    pbVideoProgress.setVisibility(GONE);
                    tvVideoSpeed.setVisibility(GONE);
                    break;
                case MediaService.STATE_LOADING:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_pause);
                    ivVideoFinishPlay.setVisibility(GONE);
                    pbVideoProgress.setVisibility(VISIBLE);
                    tvVideoSpeed.setVisibility(VISIBLE);
                    break;
                case MediaService.STATE_FINISH:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_play);
                    ivVideoFinishPlay.setVisibility(VISIBLE);
                    pbVideoProgress.setVisibility(GONE);
                    tvVideoSpeed.setVisibility(GONE);
                    break;
                case MediaService.STATE_ERROR:
                    ivVideoPlay.setImageResource(R.mipmap.ico_video_play);
                    ivVideoFinishPlay.setVisibility(GONE);
                    pbVideoProgress.setVisibility(GONE);
                    tvVideoSpeed.setVisibility(GONE);
                    break;
            }
        }
    };

    public void canDragSeek(boolean enable) {
        sbVideoProgress.setEnabled(enable);
        dragSeek = enable;
    }

    private MediaService.MediaPlayProgressListener mProgressListener = new MediaService.MediaPlayProgressListener() {
        @Override
        public void progress(long position, long duration, long buffer, long speed) {
            sbVideoProgress.setProgress((int) (position / 1000));
            sbVideoProgress.setMax((int) (duration / 1000));
            tvVideoTime.setText(formatTime(position) + "/" + formatTime(duration));
            sbVideoProgress.setSecondaryProgress((int) buffer);
            if (tvVideoSpeed.getVisibility() == VISIBLE) {
                tvVideoSpeed.setText(speed / 1000 + "k/s");
            }
        }
    };

    private String formatTime(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        String s;
        String m;
        if (minutes < 0) {
            m = "00";
        } else if (minutes < 10) {
            m = "0" + minutes;
        } else {
            m = minutes + "";
        }

        if (seconds < 0) {
            s = "00";
        } else if (seconds < 10) {
            s = "0" + seconds;
        } else {
            s = seconds + "";
        }
        return m + ":" + s;
    }
}
