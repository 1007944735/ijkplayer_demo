package com.example.ijkplayer_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.ijkplayer_demo.mp3.IServiceBind;
import com.example.ijkplayer_demo.mp3.MediaServiceManager;
import com.example.ijkplayer_demo.mp3.MediaService;

public class MainActivity extends AppCompatActivity {
    private MediaServiceManager controller;
    private MediaService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = new MediaServiceManager(this, new IServiceBind() {

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
        service.playWithUrl("http://test10.jy365.net/lessionnew/mp4/wksz180709212.mp4" ,true,"1");
    }

    public void play2(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    @Override
    protected void onDestroy() {
        controller.destroy();
        super.onDestroy();
    }
}
