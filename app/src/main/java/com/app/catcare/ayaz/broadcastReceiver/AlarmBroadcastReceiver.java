package com.app.catcare.ayaz.broadcastReceiver;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.activities.AlarmActivity;
import com.app.catcare.ayaz.models.Task;
import com.app.catcare.ayaz.service.NotificationService;


public class AlarmBroadcastReceiver extends BroadcastReceiver {
    String title, desc, date, time;



    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service1 = new Intent(context, NotificationService.class);


        title = intent.getStringExtra("TITLE");
        desc = intent.getStringExtra("DESC");
        date = intent.getStringExtra("DATE");
        time = intent.getStringExtra("TIME");

        service1.putExtra("TITLE", title);
        service1.putExtra("DESC", desc);
        service1.putExtra("DATE", date);
        service1.putExtra("TIME", time);
        service1.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        ContextCompat.startForegroundService(context, service1 );
    }

}
