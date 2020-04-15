package com.example.ijkplayer_demo.mp3;

import android.widget.Toast;

public class UploadProgressManager {
    private MediaService mService;

    public UploadProgressManager(MediaService service) {
        mService = service;
    }

    public void upload(long position, long duration, long buffer, long speed) {
    }

    public void stateChange(int state) {
        Toast.makeText(mService, mService.getMediaInfo().getHeight()+"", Toast.LENGTH_SHORT).show();
    }


}
