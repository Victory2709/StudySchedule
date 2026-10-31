package com.example.studyschedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        Notifier.show(c, i.getIntExtra("id", 0) + 500000, i.getStringExtra("title"), i.getStringExtra("text"));
    }
}
