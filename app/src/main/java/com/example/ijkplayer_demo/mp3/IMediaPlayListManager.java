package com.example.ijkplayer_demo.mp3;

import java.util.ArrayList;
import java.util.List;

public abstract class IMediaPlayListManager<T> implements IManager {
    private List<T> playList;
    private int position;
    private MediaService mService;
    private boolean isLoop = true;
    private boolean isPlayNext = true;

    public IMediaPlayListManager() {
        playList = new ArrayList<>();
        position = -1;
    }

    public boolean add(T t) {
        if (t != null) {
            return playList.add(t);
        }
        return false;
    }

    public boolean addAll(List<T> lists) {
        if (lists != null && !lists.isEmpty()) {
            return playList.addAll(lists);
        }
        return false;
    }

    public boolean addAllAndClear(List<T> lists) {
        clear();
        if (lists != null && !lists.isEmpty()) {
            return playList.addAll(lists);
        }
        return false;
    }

    public boolean remove(T t) {
        if (t != null) {
            return playList.remove(t);
        }
        return false;
    }

    public void clear() {
        playList.clear();
        position = -1;
    }

    public final void playNext() {
        if (position >= playList.size() - 1) {
            if (!isLoop) {
                return;
            }
            position++;
            position %= playList.size();
        } else {
            position++;
        }
        beforePlayNext();
        if (mService != null) {
            preparePlay(mService, this.position, playList.get(position));
        }
    }

    public final void playLast() {
        if (position <= 0) {
            if (!isLoop) {
                return;
            }
            position--;
            position += playList.size();
        } else {
            position--;
        }
        beforePlayLast();
        if (mService != null) {
            preparePlay(mService, this.position, playList.get(position));
        }
    }

    public final void play(int position) {
        if (position < 0 || position > playList.size() - 1) {
            return;
        }
        this.position = position;
        if (mService != null) {
            preparePlay(mService, this.position, playList.get(position));
        }
    }

    public int getPosition() {
        return position;
    }

    @Override
    public void bindService(MediaService service) {
        mService = service;
    }

    abstract void beforePlayNext();

    abstract void beforePlayLast();

    abstract void preparePlay(MediaService service, int position, T t);
}
