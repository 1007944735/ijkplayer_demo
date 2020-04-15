package com.example.ijkplayer_demo.mp3;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MediaServiceManager {
    private static boolean isStartService = false;
    private static Intent startIntent;
    private Context context;
    private IServiceBind serviceBind;
    private MediaService mediaService;


    public MediaServiceManager(Context context, IServiceBind serviceBind) {
        this.context = context;
        this.serviceBind = serviceBind;
        if (!isStartService) {
            startService();
            isStartService = true;
        }
        bindService();
    }

    private void startService() {
        startIntent = new Intent(context, MediaService.class);
        context.startService(startIntent);
    }

    private void bindService() {
        Intent bindService = new Intent(context, MediaService.class);
        context.bindService(bindService, mServiceConnection, Activity.BIND_AUTO_CREATE);
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaService = ((MediaService.MediaBinder) service).getService();
            if (mediaService != null) {
                if (serviceBind != null) {
                    serviceBind.onConnected(mediaService);
                }
            } else {
                if (serviceBind != null) {
                    serviceBind.onDisconnected();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (serviceBind != null) {
                serviceBind.onDisconnected();
            }
        }
    };

    public void destroy() {
        context.unbindService(mServiceConnection);
    }

}
