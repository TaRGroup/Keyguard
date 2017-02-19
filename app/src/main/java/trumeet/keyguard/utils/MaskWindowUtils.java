/*
 * Copyright (C) 2017 TaRGroup
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package trumeet.keyguard.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telecom.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.math.BigDecimal;

import trumeet.keyguard.activities.KeyguardLiveActivity;
import trumeet.keyguard.R;

/**
 * Created by Administrator on 2017/2/17.
 * @author Trumeet
 */

public class MaskWindowUtils {
    private View mKeyguardView;
    private long mTotalTime = 5;
    private long mUsedTime = 0;
    private int mScreenOn = 0;
    private WindowManager mManager;
    private Context mContext;
    private Thread mUiThread;
    private Handler mHandler;
    private MaskWindowUtils.TimerPrefsUtil mPrefsUtil;

    @MainThread
    public MaskWindowUtils (Context context) {
        mUiThread = Thread.currentThread();
        mHandler = new Handler();
        mContext = context;
        mPrefsUtil = new TimerPrefsUtil(context);
        mManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void tryStart () {
        tryStart(true);
    }

    public void tryStart (boolean startSettings) {
        mUsedTime = mPrefsUtil.getUsed();
        mTotalTime = mPrefsUtil.getTotal();
        mScreenOn = 0;
        mKeyguardView = showKeyguardView(startSettings);
        if (mKeyguardView != null) {
            startTimerThread();
        }
    }

    @Nullable
    private View showKeyguardView (boolean showSettings) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(mContext)) {
                if (!showSettings)
                    return null;
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                mContext.startActivity(i);
                return null;
            }
        }
        View keyguardView = LayoutInflater.from(mContext).inflate(R.layout.layout_keyguard, null);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.format = PixelFormat.TRANSPARENT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mManager.addView(keyguardView, wmParams);
        mContext.startActivity(new Intent(mContext, KeyguardLiveActivity.class));
        return keyguardView;
    }

    private void startTimerThread () {
        final TextView timer = (TextView) mKeyguardView.findViewById(R.id.text_lock_left);
        new Thread(new Runnable() {
            private static final String TAG_TIMER = "Timer";
            @Override
            public void run() {
                Log.w(TAG_TIMER, "-> run -> Timer Started");
                Log.i(TAG_TIMER, "UsedTime#" + mUsedTime + ";Total#" + mTotalTime);
                Log.i(TAG_TIMER, "Register Screen On Receiver..");
                ReceiverUtil receiverUtil = new ReceiverUtil(mContext, new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                            Log.w(TAG_TIMER, "-> onReceive -> screenOn");
                            mScreenOn++;
                            Log.i(TAG_TIMER, "screenOn#" + mScreenOn);
                        }
                    }
                });
                receiverUtil.subscribe(new IntentFilter(Intent.ACTION_SCREEN_ON));
                if (mUsedTime == mTotalTime) {
                    Log.w(TAG_TIMER, "Time is Up, Resetting");
                    mUsedTime = 0;
                }
                for (; mUsedTime <= mTotalTime; mUsedTime++ ) {
                    mPrefsUtil.updateUsed(mUsedTime);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timer.setText(
                                    mContext.getString(R.string.text_time_left
                                    , Utils.formatSecToStr(
                                                    BigDecimal.valueOf(mTotalTime - mUsedTime)
                                            ))
                            );
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignore) {
                    }
                }
                Log.i(TAG_TIMER, "Time is Up");
                dismissKeyguardView();
                receiverUtil.unsubscribe();
                mPrefsUtil.onFinish(mScreenOn);
            }
        }).start();
    }

    private void dismissKeyguardView () {
        try {
            mManager.removeView(mKeyguardView);
            mContext.sendBroadcast(new Intent(KeyguardLiveActivity.ACTION_DISMISS));
            Notification finishNotification = new NotificationCompat.Builder(mContext)
                    .setContentTitle(mContext.getString(R.string.notification_finish_title))
                    .setContentText(mContext.getString(R.string.notification_finish_text,
                            Utils.formatSecToStr(BigDecimal.valueOf(mUsedTime)),
                                    String.valueOf(mScreenOn)))
                    .setSmallIcon(R.drawable.ic_stat_lock_open)
                    .setVibrate(new long[]{300})
                    .build();
            ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(0, finishNotification);
        } catch (Exception e) {
            e.printStackTrace();
            android.os.Process.killProcess(
                    android.os.Process.myPid()
            );
        }
    }

    private void runOnUiThread (Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }

    public static class TimerPrefsUtil {
        private static final String PREFS_HISTORY_TIME = "history_time";
        private static final String PREFS_HISTORY_SCREEN_ON = "history_screen_on";
        private static final String PREFS_NAME = "timer";
        private static final String PREFS_USED = "used";
        private static final String PREFS_TOTAL = "total";
        private static final int DEFAULT_USED = 0;
        private static final int DEFAULT_TOTAL = 5;
        private SharedPreferences preferences;
        public TimerPrefsUtil (Context context) {
            preferences = context.getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        public void updateUsed (long used) {
            preferences.edit().putLong(PREFS_USED, used).apply();
        }

        public void updateTotal (long total) {
            preferences.edit().putLong(PREFS_TOTAL, total).apply();
        }

        public long getUsed () {
            return preferences.getLong(PREFS_USED, DEFAULT_USED);
        }

        public long getTotal () {
            return preferences.getLong(PREFS_TOTAL, DEFAULT_TOTAL);
        }

        public String getTotalStr () {
            int[] times = Utils.splitToComponentTimes(BigDecimal.valueOf(getTotal()));
            return Utils.convertIntArrayToString(times);
        }

        public long getHistoryTime () {
            return preferences.getLong(PREFS_HISTORY_TIME, 0);
        }

        void setHistoryTime (long time) {
            preferences.edit().putLong(PREFS_HISTORY_TIME, time).apply();
        }

        public int getHistoryScreenOn () {
            return preferences.getInt(PREFS_HISTORY_SCREEN_ON, 0);
        }

        void setHistoryScreenOn (int screenOn) {
            preferences.edit().putInt(PREFS_HISTORY_SCREEN_ON, screenOn).apply();
        }

        void onFinish (int screenOn) {
            setHistoryScreenOn(getHistoryScreenOn() + screenOn);
            setHistoryTime(getHistoryTime() + getUsed());
        }
    }
}
