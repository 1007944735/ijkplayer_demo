package com.example.ijkplayer_demo.mp3;

import android.widget.Toast;

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
        Toast.makeText(mService, mService.getMediaInfo().getHeight() + "", Toast.LENGTH_SHORT).show();

    }

    private final MediaPlayListManager.PlayListCallback mPlayListCallback=new MediaPlayListManager.PlayListCallback() {
        @Override
        public void beforePlayNext() {

        }

        @Override
        public void beforePlayLast() {

        }
    };

}
