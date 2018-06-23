/*
 * Copyright (c) 2016 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of CamCov.
 *
 * CamCov is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CamCov is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CamCov.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.camcov;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

public class OverlayService extends Service {
    public static final String ACTION_CHANGE_ALPHA = "ryey.camcov.action.CHANGE_ALPHA";

    public static final String EXTRA_ALPHA = "ryey.camcov.extra.ALPHA";
    public static final String EXTRA_SX = "ryey.camcov.extra.ScaleX";
    public static final String EXTRA_SY = "ryey.camcov.extra.ScaleY";

    private View mOverlayView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("OverlayService", "(broadcast received) action:" + intent.getAction());
            switch (intent.getAction()) {
                case ACTION_CHANGE_ALPHA:
                    setOverlayAlpha(intent.getFloatExtra(EXTRA_ALPHA, CamOverlay.DEFAULT_ALPHA));
                    break;
            }
        }
    };

    private static boolean running = false;
    private static CamTileService camTileService;

    public static void start(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        float alpha = Float.parseFloat(sharedPreferences.getString(
                context.getString(R.string.key_pref_alpha), String.valueOf(CamOverlay.DEFAULT_ALPHA)));
        float sx = sharedPreferences.getBoolean(
                context.getString(R.string.key_pref_invert_x), false) ? -1: 1;
        float sy = sharedPreferences.getBoolean(
                context.getString(R.string.key_pref_invert_y), false) ? -1: 1;
        Bundle bundle = new Bundle();
        bundle.putFloat(EXTRA_ALPHA, alpha);
        bundle.putFloat(EXTRA_SX, sx);
        bundle.putFloat(EXTRA_SY, sy);
        start(context, bundle);
    }

    public static void start(Context context, @NonNull Bundle bundle) {
        Intent intent = new Intent(context, OverlayService.class);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, OverlayService.class);
        context.stopService(intent);
    }

    public static void toggle(Context context) {
        if (running)
            stop(context);
        else
            start(context);
    }

    public static boolean isRunning() {
        String str;
        if (running) {
            str = "RUNNING";
        } else {
            str = "NOT running";
        }
        Log.d("OverlayService", "service is " + str);
        return running;
    }

    public static void registerListener(CamTileService service) {
        camTileService = service;
    }

    public static void unregisterListener() {
        camTileService = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("OverlayService", "onCreate");
        super.onCreate();
        running = true;
        updateTile();

        mOverlayView = CamOverlay.show(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHANGE_ALPHA);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("OverlayService", "onStartCommand");
        setOverlayAlpha(intent.getFloatExtra(EXTRA_ALPHA, CamOverlay.DEFAULT_ALPHA));
        float sx = intent.getFloatExtra(EXTRA_SX, 1);
        float sy = intent.getFloatExtra(EXTRA_SY, 1);
        setOverlayScale(sx, sy);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("OverlayService", "onDestroy");
        super.onDestroy();
        running = false;
        updateTile();
        unregisterReceiver(mReceiver);

        CamOverlay.hide();
    }

    protected void setOverlayAlpha(float alpha) {
        if (mOverlayView != null) {
            mOverlayView.setAlpha(alpha);
        }
    }

    void setOverlayScale(float sx, float sy) {
        if (mOverlayView != null) {
            mOverlayView.setScaleX(sx);
            mOverlayView.setScaleY(sy);
        }
    }

    private void updateTile() {
        if (camTileService != null)
            camTileService.updateState();
    }
}

