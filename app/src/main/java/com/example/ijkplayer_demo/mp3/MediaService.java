package com.example.ijkplayer_demo.mp3;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MediaService extends Service {
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_LOADING = 5;
    public static final int STATE_FINISH = 6;

    private final MediaBinder mBinder = new MediaBinder();
    private IjkMediaPlayer mediaPlayer;
    private MediaInfo mediaInfo;
    private boolean autoPlay;
    private boolean needSeekTo = false;
    private long position;
    private Surface mSurface;
    private List<MediaPlayStatusListener> mStatusListeners;
    private List<MediaPlayProgressListener> mProgressListeners;
    private int mediaStatus = STATE_IDLE;
    private Handler timerHandler;
    private long delayMillis = 1000;
    private long bufferDuration = 0;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                notifyAllListeners(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration(), bufferDuration, mediaPlayer.getTcpSpeed());
            }
            timerHandler.postDelayed(runnable, delayMillis);
        }
    };

    private IUploadDownManager iUploadDownManager;
    private IMediaPlayListManager iMediaPlayListManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MediaBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaInfo = new MediaInfo();
        timerHandler = new Handler();
        mStatusListeners = new ArrayList<>();
        mProgressListeners = new ArrayList<>();
        iUploadDownManager = new UploadDownManager();
        iUploadDownManager.bindService(this);
        iMediaPlayListManager = MediaPlayListManager.getInstance();
        iMediaPlayListManager.bindService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //调用多次

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        pause();
        mediaPlayer.release();
        mediaPlayer = null;
        bufferDuration = 0;
        removeTimer(true);
        notifyAllListeners(STATE_IDLE);
        mStatusListeners.clear();
        mProgressListeners.clear();
        super.onDestroy();
    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new IjkMediaPlayer();
        }
        mediaPlayer.reset();
        notifyAllListeners(STATE_IDLE);
        if (mSurface != null) {
            setSurface(mSurface);
        }
        mediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mediaPlayer.setOnErrorListener(mOnErrorListener);
        mediaPlayer.setOnInfoListener(mOnInfoListener);
        mediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        //取消自动播放
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
    }

    public void playWithUrl(String url) {
        playWithUrl(url, true);
    }

    public void playWithUrl(String url, boolean autoPlay) {
        mediaInfo.reset();
        mediaInfo.setUrl(url);
        this.autoPlay = autoPlay;
        try {
            pause();
            initMediaPlayer();
            mediaPlayer.setDataSource(url);
            if (mSurface != null) {
                mediaPlayer.setSurface(mSurface);
            }
            bufferDuration = 0;
            startTimer();
            mediaPlayer.prepareAsync();
            notifyAllListeners(STATE_PREPARING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaStatus == STATE_PLAYING;
    }

    public int getMediaStatus() {
        return mediaStatus;
    }

    /**
     * 播放
     */
    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (needSeekTo && position > 0 && position < mediaInfo.getDuration()) {
                mediaPlayer.seekTo(position);
                needSeekTo = false;
            }
            mediaPlayer.start();
            notifyAllListeners(STATE_PLAYING);
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyAllListeners(STATE_PAUSED);
        }
    }

    public void seekTo(long position) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(position);
            } else {
                needSeekTo = true;
                this.position = position;
            }
        }
    }

    public void setDisplay(SurfaceHolder sh) {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(sh);
        }
    }

    public void setSurface(Surface surface) {
        if (mediaPlayer != null) {
            mSurface = surface;
            mediaPlayer.setSurface(surface);
        }
    }

    public long getCurrentPostion() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public void setAudioData(Object o) {
        if (mediaInfo != null) {
            mediaInfo.setData(o);
        }
    }

    public void setSpeed(float speed) {
        if (speed > 0 && mediaPlayer != null) {
            delayMillis = (long) (delayMillis / speed);
            mediaPlayer.setSpeed(speed);
        }
    }

    public float getSpeed() {
        if (mediaPlayer != null) {
            mediaPlayer.getSpeed(0.0f);
        }
        return 0.0f;
    }

    private final IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            notifyAllListeners(STATE_PREPARED);
            //设置视频信息
            mediaInfo.setDuration(mediaPlayer.getDuration());
            mediaInfo.setWidth(mediaPlayer.getVideoWidth());
            mediaInfo.setHeight(mediaPlayer.getVideoHeight());
            if (mediaInfo.getWidth() != 0 && mediaInfo.getHeight() != 0) {
                mediaInfo.setAspectVideo(mediaPlayer.getVideoWidth() * 1.0f / mediaPlayer.getVideoHeight());
            }
            if (autoPlay) {
                play();
            }
        }
    };

    private final IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            Log.d("TAG", "onCompletion:");
            notifyAllListeners(STATE_FINISH);
            removeTimer(false);
        }
    };

    private final IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            notifyAllListeners(STATE_ERROR);
            bufferDuration = 0;
            removeTimer(false);
            return false;
        }
    };

    private final IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            switch (i) {
                case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    Log.d("TAG", "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.d("TAG", "MEDIA_INFO_VIDEO_RENDERING_START:");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.d("TAG", "MEDIA_INFO_BUFFERING_START:");
                    notifyAllListeners(STATE_LOADING);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.d("TAG", "MEDIA_INFO_BUFFERING_END:");
                    if (mediaStatus != STATE_PAUSED && mediaStatus != STATE_ERROR) {
                        notifyAllListeners(STATE_PLAYING);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                    Log.d("TAG", "MEDIA_INFO_NETWORK_BANDWIDTH: " + i1);
                    break;
                case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    Log.d("TAG", "MEDIA_INFO_BAD_INTERLEAVING:");
                    break;
                case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    Log.d("TAG", "MEDIA_INFO_NOT_SEEKABLE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.d("TAG", "MEDIA_INFO_METADATA_UPDATE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    Log.d("TAG", "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                    Log.d("TAG", "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.d("TAG", "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + i1);
                    break;
                case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    Log.d("TAG", "MEDIA_INFO_AUDIO_RENDERING_START:");
                    break;
            }
            return true;
        }
    };

    private final IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            bufferDuration = i;
        }
    };

    private final IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {

        }
    };

    private void startTimer() {
        if (timerHandler != null) {
            timerHandler.post(runnable);
        }
    }

    private void removeTimer(boolean release) {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(runnable);
            if (release) {
                timerHandler = null;
            }
        }
    }

    public void registerListener(MediaPlayStatusListener listener) {
        if (listener != null) {
            mStatusListeners.remove(listener);
            mStatusListeners.add(listener);
        }
    }

    public void unRegisterListener(MediaPlayStatusListener listener) {
        if (listener != null) {
            mStatusListeners.remove(listener);
        }
    }

    public void registerListener(MediaPlayProgressListener listener) {
        if (listener != null) {
            mProgressListeners.remove(listener);
            mProgressListeners.add(listener);
        }
    }

    public void unRegisterListener(MediaPlayProgressListener listener) {
        if (listener != null) {
            mProgressListeners.remove(listener);
        }
    }

    private void notifyAllListeners(int status) {
        mediaStatus = status;
        iUploadDownManager.stateChange(status);
        for (MediaPlayStatusListener listener : mStatusListeners) {
            listener.onStatusChange(status);
        }
    }

    private void notifyAllListeners(long position, long duration, long buffer, long speed) {
        iUploadDownManager.upload(position, duration, buffer, speed);
        for (MediaPlayProgressListener listener : mProgressListeners) {
            listener.progress(position, duration, buffer, speed);
        }
    }

    public interface MediaPlayStatusListener {
        void onStatusChange(int status);
    }

    public interface MediaPlayProgressListener {
        void progress(long position, long duration, long buffer, long speed);
    }
}
