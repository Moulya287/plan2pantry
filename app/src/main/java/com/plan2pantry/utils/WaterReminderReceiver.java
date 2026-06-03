package com.plan2pantry.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.plan2pantry.activities.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WaterReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int hour = intent.getIntExtra("hour", 12);

        // Send regular water reminder (not final, just reminder)
        sendWaterReminder(context, hour);

        // If it's 9 PM, also check and send final reminder
        if (hour == 21) {
            checkAndSendFinalReminder(context);
        }
    }

    private void sendWaterReminder(Context context, int hour) {
        String timeMessage;
        if (hour < 12) {
            timeMessage = "Morning";
        } else if (hour < 17) {
            timeMessage = "Afternoon";
        } else {
            timeMessage = "Evening";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_WATER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💧 Drink Water!")
                .setContentText("It's " + timeMessage + "! Time to hydrate and stay healthy. 💪")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void checkAndSendFinalReminder(Context context) {
        String userId = SessionManager.getInstance(context).getFirebaseUid();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseFirestore.getInstance().collection("waterIntake").document(userId)
                .collection(todayDate).document("data")
                .get()
                .addOnSuccessListener(document -> {
                    int currentMl = 0;
                    float targetWater = SessionManager.getInstance(context).getTargetWater();
                    int targetMl = (int) (targetWater * 1000);

                    if (document.exists()) {
                        currentMl = document.getLong("totalMl") != null ?
                                document.getLong("totalMl").intValue() : 0;
                    }

                    if (currentMl < targetMl) {
                        NotificationHelper.sendFinalWaterReminder(context, currentMl, targetMl);
                    }
                });
    }
}