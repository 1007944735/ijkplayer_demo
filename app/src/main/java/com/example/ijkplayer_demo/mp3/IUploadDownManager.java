package com.example.ijkplayer_demo.mp3;

public abstract class IUploadDownManager implements IManager {
    private MediaService mService;
    private IMediaPlayListManager mPlayListManager;

    public IUploadDownManager() {
    }

    public void setMediaPlayListManager(IMediaPlayListManager<?> iMediaPlayListManager) {
        this.mPlayListManager = iMediaPlayListManager;
    }

    abstract void upload(long position, long duration, long buffer, long speed);

    abstract void stateChange(int state);

    @Override
    public void bindService(MediaService service) {
        mService = service;
    }
}
