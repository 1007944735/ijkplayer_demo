package com.example.ijkplayer_demo.mp3;

public class UploadProgressManager {
    private MediaService mService;
    private MediaPlayListManager mPlayListManager;

    public UploadProgressManager(MediaService service) {
        mService = service;
        mPlayListManager = MediaPlayListManager.getInstance(service);
        mPlayListManager.setCallback(mPlayListCallback);
    }

    public void upload(long position, long duration, long buffer, long speed) {

    }

    public void stateChange(int state) {

    }

    private final MediaPlayListManager.PlayListCallback mPlayListCallback = new MediaPlayListManager.PlayListCallback() {
        @Override
        public void beforePlayNext() {

        }

        @Override
        public void beforePlayLast() {

        }
    };

}
