package com.example.ijkplayer_demo.mp3;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayListManager {

    private List<String> playList;
    private int position;
    private MediaService mService;
    private boolean isLoop = true;
    private boolean isPlayNext = true;
    private PlayListCallback mCallback;

    private static MediaPlayListManager instance;

    public static MediaPlayListManager getInstance() {
        return getInstance(null);
    }

    public static MediaPlayListManager getInstance(MediaService service) {
        if (instance == null) {
            synchronized (MediaPlayListManager.class) {
                if (instance == null) {
                    instance = new MediaPlayListManager(service);
                }
            }
        }
        return instance;
    }

    private MediaPlayListManager(MediaService service) {
        mService = service;
        playList = new ArrayList<>();
        position = -1;
    }

    public boolean add(String s) {
        if (s != null) {
            return playList.add(s);
        }
        return false;
    }

    public boolean addAll(List<String> lists) {
        if (lists != null && !lists.isEmpty()) {
            return playList.addAll(lists);
        }
        return false;
    }

    public boolean addAllAndClear(List<String> lists) {
        clear();
        if (lists != null && !lists.isEmpty()) {
            return playList.addAll(lists);
        }
        return false;
    }

    public boolean remove(String s) {
        if (s != null) {
            return playList.remove(s);
        }
        return false;
    }

    public void clear() {
        playList.clear();
        position = -1;
    }

    public void playNext() {
        if (position >= playList.size() - 1) {
            if (!isLoop) {
                return;
            }
            position++;
            position %= playList.size();
        } else {
            position++;
        }
        if (mCallback != null) {
            mCallback.beforePlayNext();
        }
        //自己的逻辑
        if (mService != null) {
            mService.playWithUrl(playList.get(position));
        }
    }

    public void playLast() {
        if (position <= 0) {
            if (!isLoop) {
                return;
            }
            position--;
            position += playList.size();
        } else {
            position--;
        }
        if (mCallback != null) {
            mCallback.beforePlayLast();
        }
        //自己的逻辑
        if (mService != null) {
            mService.playWithUrl(playList.get(position));
        }
    }

    public void play(int position) {
        if (position < 0 || position > playList.size() - 1) {
            return;
        }
        this.position = position;
        if (mService != null) {
            //自己的逻辑
            mService.playWithUrl(playList.get(position));
        }
    }

    public int getPosition() {
        return position;
    }

    public void setCallback(PlayListCallback callback) {
        mCallback = callback;
    }

    public interface PlayListCallback {
        void beforePlayNext();

        void beforePlayLast();
    }
}
