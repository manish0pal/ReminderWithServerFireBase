package com.learnado.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.widget.RemoteViews;


import androidx.core.app.NotificationCompat;

import com.learnado.MainActivity;
import com.learnado.R;

import java.util.Date;
import java.util.Locale;

public class NotifierAlarm extends BroadcastReceiver {

    private AppDatabase appDatabase;

    //TextToSpeech toSpeech1;
    @Override
    public void onReceive(Context context, Intent intent) {

        appDatabase = AppDatabase.geAppdatabase(context.getApplicationContext());
        RoomDAO roomDAO = appDatabase.getRoomDAO();
        Reminders reminder = new Reminders();
        reminder.setMessage(intent.getStringExtra("Message"));
        reminder.setRemindDate(new Date(intent.getStringExtra("RemindDate")));
        reminder.setId(intent.getIntExtra("id", 0));
        roomDAO.Delete(reminder);
        AppDatabase.destroyInstance();

        Uri alarmsound = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.smssound);
        //Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent intent1 = new Intent(context, MainPage.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainPage.class);
        taskStackBuilder.addNextIntent(intent1);

        //texttospeech

    /*    toSpeech1=new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    toSpeech1.setLanguage(Locale.UK);
                }
            }
        });
*/
        //tts


     PendingIntent intent2 = taskStackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        NotificationChannel channel = null;
        //call show


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("my_channel_01","hello", NotificationManager.IMPORTANCE_HIGH);
        }

        Notification notification = builder.setContentTitle("Reminder")
                .setContentText(intent.getStringExtra("Message")).setAutoCancel(true)
                .setSound(alarmsound).setSmallIcon(R.drawable.ntficon)
                .setContentIntent(intent2)
                .setChannelId("my_channel_01")
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notification);

    }
}
