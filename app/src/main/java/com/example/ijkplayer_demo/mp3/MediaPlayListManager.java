package com.example.ijkplayer_demo.mp3;

public class MediaPlayListManager extends IMediaPlayListManager<String> {
    private static MediaPlayListManager instance;

    public static MediaPlayListManager getInstance() {
        if (instance == null) {
            synchronized (MediaPlayListManager.class) {
                if (instance == null) {
                    instance = new MediaPlayListManager();
                }
            }
        }
        return instance;
    }

    @Override
    void beforePlayNext() {

    }

    @Override
    void beforePlayLast() {

    }

    @Override
    void preparePlay(MediaService service, int position, String s) {

    }
}
