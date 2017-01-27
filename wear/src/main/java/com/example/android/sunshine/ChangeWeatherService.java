package com.example.android.sunshine;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Rishabh on 26/01/17.
 */

public class ChangeWeatherService extends WearableListenerService {

    public static class Weather {
        public static int wId;
        public static String highTemp;
        public static String lowTemp;

        public Weather(int wId, String highTemp, String lowTemp) {
            this.wId = wId;
            this.highTemp = highTemp;
            this.lowTemp = lowTemp;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent dataEvent : dataEventBuffer) {

            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {

                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String uriPath = dataEvent.getDataItem().getUri().getPath();
                if (uriPath.equals("/fresh-weather")) {
                    EventBus.getDefault().post(new Weather(
                            dataMap.getInt("wId"),
                            dataMap.getString("highTemp"),
                            dataMap.getString("lowTemp")));
                }

            }

        }
    }
}
