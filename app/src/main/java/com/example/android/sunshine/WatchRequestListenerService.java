package com.example.android.sunshine;

import com.example.android.sunshine.sync.SunshineSyncTask;
import com.example.android.sunshine.utilities.NotificationUtils;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Rishabh on 26/01/17.
 */

public class WatchRequestListenerService extends WearableListenerService {
    public void register(NotificationUtils.Callback callback) {
        callback.setupGoogleClient();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("/get-weather")) {
            SunshineSyncTask.syncWeather(this);
            WatchRequestListenerService watchRequestListenerService = new WatchRequestListenerService();
            NotificationUtils.Callback callBack = new SunshineSyncTask();
            watchRequestListenerService.register(callBack);
        }
    }
}
