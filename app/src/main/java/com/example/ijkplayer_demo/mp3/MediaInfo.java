package com.example.ijkplayer_demo.mp3;

public class MediaInfo {
    private String url;
    private String type;
    private long duration;
    private int width;
    private int height;
    private float aspectVideo;
    private Object data;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getAspectVideo() {
        return aspectVideo;
    }

    public void setAspectVideo(float aspectVideo) {
        this.aspectVideo = aspectVideo;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void reset() {
        url = "";
        type = "";
        duration = 0;

    }
}
