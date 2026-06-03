package com.plan2pantry.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyTipReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SessionManager session = SessionManager.getInstance(context);

        // Check if daily tips are enabled
        if (!session.isDailyTipsEnabled()) {
            return;
        }

        // Use NotificationHelper to send the notification
        NotificationHelper.sendDailyTipNotification(context);
    }
}