package com.app.catcare.ayaz.utils;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.app.catcare.ayaz.broadcastReceiver.AlarmBroadcastReceiver;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 20/08/2023
 */
public class Alarm {
    public static void setAlarm(int i, Long timestamp, Context ctx,String TaskTitle,String TaskDescription, String Date, String Time) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmBroadcastReceiver.class);
        alarmIntent.putExtra("TITLE", TaskTitle);
        alarmIntent.putExtra("DESC", TaskDescription);
        alarmIntent.putExtra("DATE", Date);
        alarmIntent.putExtra("TIME", Time);


        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, i, alarmIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }
}
