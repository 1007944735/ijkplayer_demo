package com.example.ijkplayer_demo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ijkplayer_demo.mp3.IServiceBind;
import com.example.ijkplayer_demo.mp3.MediaPlayView;
import com.example.ijkplayer_demo.mp3.MediaService;
import com.example.ijkplayer_demo.mp3.MediaServiceManager;

public class SecondActivity extends AppCompatActivity {
    private MediaServiceManager mediaServiceManager;
    private MediaService service;
    private MediaPlayView display;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        display = findViewById(R.id.display);
        mediaServiceManager = new MediaServiceManager(this, new IServiceBind() {

            @Override
            public void onConnected(MediaService control) {
                service = control;
            }

            @Override
            public void onDisconnected() {

            }
        });
    }

    public void play1(View view) {
        if (service != null) {
            display.bindMediaService(service);
            service.playWithUrl("http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4", true,"1");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            display.enterFullPlay();
        } else {
            display.exitFullPlay();
        }
    }

    @Override
    protected void onDestroy() {
        mediaServiceManager.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (display.isFullScreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }

    public void stop(View view) {
    }
}
