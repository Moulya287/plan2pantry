package com.plan2pantry.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.plan2pantry.R;
import com.plan2pantry.activities.MainActivity;

import java.util.Calendar;

public class NotificationHelper {

    public static final String CHANNEL_WATER = "channel_water";
    public static final String CHANNEL_REMINDER = "channel_reminder";
    public static final String CHANNEL_DAILY_TIP = "daily_tip";

    private static final int NOTIF_WATER_GOAL = 202;
    private static final int NOTIF_WATER_FINAL = 203;
    private static final int NOTIF_DAILY_TIP = 999;

    /**
     * Create notification channels
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);

            // Water tracking channel
            NotificationChannel waterChannel = new NotificationChannel(
                    CHANNEL_WATER,
                    "Water Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            waterChannel.setDescription("Reminders to drink water");
            waterChannel.enableVibration(true);
            waterChannel.setVibrationPattern(new long[]{0, 500, 200, 500});

            // General reminders channel
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "General Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Meal and report reminders");

            // Daily tip channel
            NotificationChannel dailyTipChannel = new NotificationChannel(
                    CHANNEL_DAILY_TIP,
                    "Daily Tips",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dailyTipChannel.setDescription("Daily reminder notifications");

            if (nm != null) {
                nm.createNotificationChannel(waterChannel);
                nm.createNotificationChannel(reminderChannel);
                nm.createNotificationChannel(dailyTipChannel);
            }
        }
    }

    /**
     * Send notification when water goal is achieved
     */
    public static void sendWaterGoalAchievedNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_WATER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Hydration Goal Achieved!")
                .setContentText("Congratulations! You've reached your daily water target!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500});

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_WATER_GOAL, builder.build());
        }
    }

    /**
     * Send final reminder at 9 PM
     */
    public static void sendFinalWaterReminder(Context context, int currentMl, int targetMl) {
        if (currentMl >= targetMl) return;

        int remaining = targetMl - currentMl;
        float remainingL = remaining / 1000f;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_WATER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time to Hydrate!")
                .setContentText(String.format("You're %.1fL away from your daily water goal. Drink up!", remainingL))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500});

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_WATER_FINAL, builder.build());
        }
    }

    /**
     * Send daily tip notification
     */
    public static void sendDailyTipNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DAILY_TIP)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Daily Reminder")
                .setContentText("Time to check your progress for today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_DAILY_TIP, builder.build());
        }
    }

    /**
     * Schedule water reminders throughout the day
     */
    public static void scheduleWaterReminders(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int[] reminderHours = {8, 10, 12, 14, 16, 18, 20};

        for (int i = 0; i < reminderHours.length; i++) {
            scheduleReminder(context, reminderHours[i], 0, 200 + i);
        }

        scheduleReminder(context, 21, 0, 207);
    }

    private static void scheduleReminder(Context context, int hour, int minute, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, WaterReminderReceiver.class);
        intent.putExtra("hour", hour);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}