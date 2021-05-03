package com.jandraid.charmcaster.Additional;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.jandraid.charmcaster.MainActivity;
import com.jandraid.charmcaster.PlayerActivity;
import com.jandraid.charmcaster.R;

public class NotificationService2 extends Service {
    Notification status;
    String songname;
    RemoteViews views,bigViews;
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PlayerActivity lp = new PlayerActivity();
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            songname = intent.getStringExtra("Song Title");
            showNotification();
        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            //Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
            lp.pauseplayclicked();
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            //Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            lp.close();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void showNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(), R.layout.status_bar1);
        bigViews = new RemoteViews(getPackageName(), R.layout.status_bar_expanded1);
        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Intent previousIntent = new Intent(this, NotificationService2.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, NotificationService2.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, NotificationService2.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        Intent closeIntent = new Intent(this, NotificationService2.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        views.setImageViewResource(R.id.status_bar_play,
                R.drawable.ic_baseline_pause_24);
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.drawable.ic_baseline_pause_24);
        views.setTextViewText(R.id.status_bar_track_name, songname);
        bigViews.setTextViewText(R.id.status_bar_track_name, songname);
        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_baseline_music_note_24;
        status.contentIntent = pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String NOTIFICATION_CHANNEL_ID = getPackageName();
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                    .setCustomContentView(views)
                    .setCustomBigContentView(bigViews)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        }
        else
        {
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        }
    }



}
