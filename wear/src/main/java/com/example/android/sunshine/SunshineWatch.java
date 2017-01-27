/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.TimeZone;

public class SunshineWatch extends CanvasWatchFaceService {


    Resources resources;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }


    public class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient
            .ConnectionCallbacks {

        boolean isAmbient;
        boolean mRegisteredTimeZoneReceiver = false;
        boolean isLowBitAmbient;
        boolean weatherAvailable;

        Paint maxWeatherPaint;
        Paint minWeatherPaint;

        Paint datePaint;
        Paint timePaint;

        Paint bgPaint;


        Calendar calendar;

        final BroadcastReceiver currentTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        float timeYOffset;
        float dateYOffset;
        float dividerYOffset;
        float weatherYOffset;

        float timeXOffset;
        float dateXOffset;
        float weatherXOffset;

        String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT",
                "NOV", "DEC"};
        String[] weekNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        int wId;
        String maxTemp;
        String minTemp;

        private GoogleApiClient mGoogleApiClient;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatch.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .setShowSystemUiTime(false)
                    .build());

            calendar = Calendar.getInstance();

            resources = SunshineWatch.this.getResources();

            bgPaint = new Paint();
            bgPaint.setColor(resources.getColor(R.color.blu));

            Paint paint = new Paint();
            paint.setColor(resources.getColor(R.color.white));
            paint.setAntiAlias(true);
            timePaint = paint;

            paint = new Paint();
            paint.setColor(resources.getColor(R.color.lite_blu));
            paint.setAntiAlias(true);
            datePaint = paint;

            paint = new Paint();
            paint.setColor(resources.getColor(R.color.white));
            paint.setAntiAlias(true);
            maxWeatherPaint = paint;

            paint = new Paint();
            paint.setColor(resources.getColor(R.color.lite_blu));
            paint.setAntiAlias(true);
            minWeatherPaint = paint;

            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
            mGoogleApiClient.connect();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }


        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineWatch.this.unregisterReceiver(currentTimeZoneReceiver);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatch.this.registerReceiver(currentTimeZoneReceiver, intentFilter);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (!visible) {
                unregisterReceiver();
                EventBus.getDefault().unregister(this);

            } else {
                registerReceiver();
                EventBus.getDefault().register(this);
                calendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            boolean isRound = insets.isRound();
            float weatherTxtSize;

            if (isRound) {
                timeXOffset = resources.getDimension(R.dimen.rnd_time_x_offset);
                dateXOffset = resources.getDimension(R.dimen.rnd_date_x_offset);
                weatherXOffset = resources.getDimension(R.dimen.rnd_weather_x_offset);
                timeYOffset = resources.getDimension(R.dimen.rnd_time_y_offset);
                dateYOffset = resources.getDimension(R.dimen.rnd_date_y_offset);
                dividerYOffset = resources.getDimension(R.dimen.rnd_y_offset_dvd);
                weatherYOffset = resources.getDimension(R.dimen.rnd_y_offset_weather);

                timePaint.setTextSize(resources.getDimension(R.dimen.rnd_time_size));

                weatherTxtSize = resources.getDimension(R.dimen.rnd_weather_size);
                maxWeatherPaint.setTextSize(weatherTxtSize);
                minWeatherPaint.setTextSize(weatherTxtSize);

                datePaint.setTextSize(resources.getDimension(R.dimen.rnd_date_size));

            } else {

                timeXOffset = resources.getDimension(R.dimen.sq_time_x_offset);
                dateXOffset = resources.getDimension(R.dimen.sq_date_x_offset);
                weatherXOffset = resources.getDimension(R.dimen.sq_weather_x_offset);
                timeYOffset = resources.getDimension(R.dimen.sq_time_y_offset);
                dateYOffset = resources.getDimension(R.dimen.sq_date_y_offset);
                dividerYOffset = resources.getDimension(R.dimen.sq_y_offset_dvd);
                weatherYOffset = resources.getDimension(R.dimen.sq_y_offset_weather);

                timePaint.setTextSize(resources.getDimension(R.dimen.sq_time_size));

                weatherTxtSize = resources.getDimension(R.dimen.sq_weather_size);
                maxWeatherPaint.setTextSize(weatherTxtSize);
                minWeatherPaint.setTextSize(weatherTxtSize);

                datePaint.setTextSize(resources.getDimension(R.dimen.sq_date_size));
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            isLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (inAmbientMode) {
                if (isLowBitAmbient) {
                    timePaint.setAntiAlias(false);
                    datePaint.setAntiAlias(false);
                }
                datePaint.setColor(resources.getColor(R.color.white));
            } else {
                if (isLowBitAmbient) {
                    timePaint.setAntiAlias(true);
                    datePaint.setAntiAlias(true);
                }
                datePaint.setColor(resources.getColor(R.color.lite_blu));
            }

            isAmbient = inAmbientMode;

            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long currentTime = System.currentTimeMillis();
            calendar.setTimeInMillis(currentTime);

            String timeString = String.format("%d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));

            String dateString = String.format("%s, %s %d %d",
                    weekNames[calendar.get(Calendar.DAY_OF_WEEK)],
                    monthNames[calendar.get(Calendar.MONTH)],
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.YEAR));

            if (isAmbient) {
                canvas.drawColor(Color.BLACK);
                canvas.drawText(timeString, timeXOffset, timeYOffset, timePaint);
                canvas.drawText(dateString, dateXOffset, dateYOffset, datePaint);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), bgPaint);

                canvas.drawText(timeString, timeXOffset, timeYOffset, timePaint);
                canvas.drawText(dateString, dateXOffset, dateYOffset, datePaint);

                canvas.drawLine((3 * bounds.width()) / 8, dividerYOffset, (5 * bounds.width()) / 8,
                        dividerYOffset,
                        datePaint);


                if (weatherAvailable) {

                    float xOffset = weatherXOffset;

                    Bitmap bitmap = BitmapFactory.decodeResource
                            (resources, getArtResourceForWeatherCondition(wId));

                    if(bitmap == null)
                        Log.d("xxx", "bitmap null, wid " + wId + " resource " + resources.toString());

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);

                    canvas.drawBitmap(scaledBitmap, xOffset, weatherYOffset - 45, maxWeatherPaint);

                    xOffset = xOffset + 70;

                    canvas.drawText(maxTemp + "", xOffset, weatherYOffset, maxWeatherPaint);

                    xOffset = xOffset + maxWeatherPaint.measureText(maxTemp);

                    canvas.drawText(minTemp + "", xOffset, weatherYOffset, minWeatherPaint);
                }
            }

        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND)
        public void setWeatherData(ChangeWeatherService.Weather w) {

            weatherAvailable = true;
            wId = w.wId;
            maxTemp = w.highTemp;
            minTemp = w.lowTemp;

            invalidate();
        }

        private int getArtResourceForWeatherCondition(int wID) {

            if (wID >= 802 && wID <= 804) {
                return R.drawable.art_clouds;
            } else if (wID == 801) {
                return R.drawable.art_light_clouds;
            } else if (wID == 800) {
                return R.drawable.art_clear;
            } else if (wID == 761 || wID == 781) {
                return R.drawable.art_storm;
            } else if (wID >= 701 && wID <= 761) {
                return R.drawable.art_fog;
            } else if (wID >= 600 && wID <= 622) {
                return R.drawable.art_snow;
            } else if (wID >= 520 && wID <= 531) {
                return R.drawable.art_rain;
            } else if (wID == 511) {
                return R.drawable.art_snow;
            } else if (wID >= 500 && wID <= 504) {
                return R.drawable.art_rain;
            } else if (wID >= 300 && wID <= 321) {
                return R.drawable.art_light_rain;
            } else if (wID >= 200 && wID <= 232) {
                return R.drawable.art_storm;
            }

            return -1;

        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult connectedNodes = Wearable.NodeApi.getConnectedNodes(
                            mGoogleApiClient).await();

                    for (Node node : connectedNodes.getNodes()) {
                        Wearable.MessageApi.sendMessage(
                                mGoogleApiClient, node.getId(), "/get-weather",
                                new byte[0]
                        ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            }
                        });
                    }
                }
            }).start();
        }

        @Override
        public void onConnectionSuspended(int i) {
        }
    }
}
