package com.example.studyschedule;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.studyschedule.Models.StudySchedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Thông báo: hiển thị ngay khi lịch thay đổi + nhắc trước 15 phút khi sắp học. */
public class Notifier {
    private static final String CHANNEL = "study_channel";

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Lịch học", NotificationManager.IMPORTANCE_HIGH);
            c.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    static void show(Context c, int id, String title, String text) {
        ensureChannel(c);
        if (Build.VERSION.SDK_INT >= 33 &&
                c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, LoginActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title).setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(open).setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        c.getSystemService(NotificationManager.class).notify(id, b.build());
    }

    static String describe(StudySchedule s) {
        return s.subjectName + " | " + s.studyDate + " " + s.startTime + "-" + s.endTime + " | " + s.location + " | " + s.status;
    }

    /** Đặt báo thức nhắc trước 15 phút (hủy báo thức nếu lịch đã Cancelled / đã qua). */
    static void schedule(Context c, StudySchedule s) {
        cancel(c, s.id);
        if ("Cancelled".equals(s.status)) return;
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(s.studyDate + " " + s.startTime);
            long now = System.currentTimeMillis();
            if (d == null || d.getTime() < now) return;
            long t = Math.max(d.getTime() - 15 * 60 * 1000L, now + 3000);
            Intent i = new Intent(c, ReminderReceiver.class);
            i.putExtra("id", s.id);
            i.putExtra("title", "Sắp đến giờ học");
            i.putExtra("text", describe(s));
            PendingIntent pi = PendingIntent.getBroadcast(c, s.id, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
        } catch (Exception ignored) { }
    }

    static void cancel(Context c, int scheduleId) {
        PendingIntent pi = PendingIntent.getBroadcast(c, scheduleId, new Intent(c, ReminderReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) {
            c.getSystemService(AlarmManager.class).cancel(pi);
            pi.cancel();
        }
    }

    static void rescheduleAll(Context c, int studentId) {
        List<StudySchedule> l = new DBHelper(c).schedulesBetween(studentId, "0000-01-01", "9999-12-31");
        for (StudySchedule s : l) schedule(c, s);
    }
}
