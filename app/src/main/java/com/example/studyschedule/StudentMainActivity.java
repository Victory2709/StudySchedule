package com.example.studyschedule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.StudySchedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentMainActivity extends BaseActivity {
    @Override protected boolean showBack() { return false; }

    private SessionManager sm;
    private DBHelper db;
    private TextView upcoming;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        sm = new SessionManager(this);
        db = new DBHelper(this);
        LinearLayout root = Ui.page(this);
        root.addView(Ui.text(this, "Xin chào, " + sm.getName(), 20, true));
        root.addView(Ui.button(this, "Lịch học của tôi (đặt / sửa / xóa)", v -> startActivity(new Intent(this, ScheduleListActivity.class))), Ui.lp(this));
        root.addView(Ui.button(this, "Xem lịch (ngày / tuần / tháng)", v -> startActivity(new Intent(this, CalendarActivity.class))), Ui.lp(this));
        root.addView(Ui.button(this, "Đăng xuất", v -> logout()), Ui.lp(this));
        root.addView(Ui.text(this, "\nBuổi học sắp tới:", 16, true));
        upcoming = Ui.text(this, "", 15, false);
        root.addView(upcoming);
        setContentView(root);

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        Notifier.rescheduleAll(this, sm.getStudentId());
    }

    @Override protected void onResume() {
        super.onResume();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        List<StudySchedule> l = db.schedulesBetween(sm.getStudentId(), today, "9999-12-31");
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (StudySchedule s : l) {
            if ("Cancelled".equals(s.status)) continue;
            sb.append("• ").append(Notifier.describe(s)).append("\n");
            if (++n == 3) break;
        }
        upcoming.setText(n == 0 ? "Không có buổi học nào." : sb.toString());
    }
}
