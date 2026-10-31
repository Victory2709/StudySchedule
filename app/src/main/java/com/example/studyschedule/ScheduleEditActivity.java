package com.example.studyschedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.StudySchedule;
import com.example.studyschedule.Models.Subject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Sinh viên: đặt lịch mới / sửa lịch (kiểm tra trùng lịch). */
public class ScheduleEditActivity extends BaseActivity {
    private DBHelper db;
    private SessionManager sm;
    private Spinner spSubject, spStatus;
    private Button btDate, btStart, btEnd;
    private EditText etLoc;
    private List<Subject> subjects;
    private int scheduleId = -1;
    private String date, start = "08:00", end = "10:00";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DBHelper(this);
        sm = new SessionManager(this);
        scheduleId = getIntent().getIntExtra("id", -1);
        setTitle(scheduleId > 0 ? "Sửa lịch học" : "Đặt lịch học");
        subjects = db.allSubjects();
        if (subjects.isEmpty()) { Ui.toast(this, "Chưa có môn học nào, hãy nhờ Admin thêm"); finish(); return; }
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        LinearLayout l = Ui.page(this);
        spSubject = new Spinner(this);
        spSubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects));
        spStatus = new Spinner(this);
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ScheduleListActivity.STATUSES));
        btDate = Ui.button(this, "", v -> pickDate());
        btStart = Ui.button(this, "", v -> pickTime(true));
        btEnd = Ui.button(this, "", v -> pickTime(false));
        etLoc = Ui.edit(this, "Địa điểm học", "");

        if (scheduleId > 0) {
            StudySchedule s = db.getSchedule(scheduleId);
            if (s != null) {
                for (int i = 0; i < subjects.size(); i++) if (subjects.get(i).id == s.subjectId) spSubject.setSelection(i);
                for (int i = 0; i < ScheduleListActivity.STATUSES.length; i++)
                    if (ScheduleListActivity.STATUSES[i].equals(s.status)) spStatus.setSelection(i);
                date = s.studyDate; start = s.startTime; end = s.endTime;
                etLoc.setText(s.location);
            }
        }
        refresh();
        l.addView(Ui.text(this, "Môn học", 14, true));
        l.addView(spSubject);
        l.addView(Ui.text(this, "Ngày học / Giờ bắt đầu / Giờ kết thúc", 14, true));
        l.addView(btDate, Ui.lp(this));
        l.addView(btStart, Ui.lp(this));
        l.addView(btEnd, Ui.lp(this));
        l.addView(etLoc, Ui.lp(this));
        l.addView(Ui.text(this, "Trạng thái", 14, true));
        l.addView(spStatus);
        l.addView(Ui.button(this, "Lưu", v -> save()), Ui.lp(this));
        ScrollView sv = new ScrollView(this);
        sv.addView(l);
        setContentView(sv);
    }

    private void refresh() {
        btDate.setText("Ngày: " + date);
        btStart.setText("Bắt đầu: " + start);
        btEnd.setText("Kết thúc: " + end);
    }

    private void pickDate() {
        String[] p = date.split("-");
        new DatePickerDialog(this, (v, y, m, d) -> {
            date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            refresh();
        }, Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2])).show();
    }

    private void pickTime(final boolean isStart) {
        String[] p = (isStart ? start : end).split(":");
        new TimePickerDialog(this, (v, h, m) -> {
            String t = String.format(Locale.US, "%02d:%02d", h, m);
            if (isStart) start = t; else end = t;
            refresh();
        }, Integer.parseInt(p[0]), Integer.parseInt(p[1]), true).show();
    }

    private void save() {
        String loc = etLoc.getText().toString().trim();
        if (loc.isEmpty()) { Ui.toast(this, "Nhập địa điểm học"); return; }
        if (end.compareTo(start) <= 0) { Ui.toast(this, "Giờ kết thúc phải sau giờ bắt đầu"); return; }
        Subject sj = (Subject) spSubject.getSelectedItem();
        String st = (String) spStatus.getSelectedItem();
        int studentId = sm.getStudentId();
        if (!st.equals("Cancelled") && db.hasConflict(studentId, date, start, end, scheduleId > 0 ? scheduleId : 0)) {
            Ui.toast(this, "Trùng lịch: đã có buổi học khác trong khoảng thời gian này!");
            return;
        }
        StudySchedule s = new StudySchedule();
        s.studentId = studentId; s.subjectId = sj.id; s.subjectName = sj.subjectName;
        s.studyDate = date; s.startTime = start; s.endTime = end; s.location = loc; s.status = st;
        boolean isNew = scheduleId <= 0;
        if (isNew) s.id = (int) db.addSchedule(s);
        else { s.id = scheduleId; db.updateSchedule(s); }
        Notifier.schedule(this, s);
        Notifier.show(this, s.id, isNew ? "Đặt lịch học thành công" : "Lịch học đã thay đổi", Notifier.describe(s));
        Ui.toast(this, "Đã lưu lịch học");
        finish();
    }
}
