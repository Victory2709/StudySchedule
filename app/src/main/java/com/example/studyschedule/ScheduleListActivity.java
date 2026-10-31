package com.example.studyschedule;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.StudySchedule;

import java.util.ArrayList;
import java.util.List;

/** Sinh viên: xem / tìm kiếm / sửa / xóa / đổi trạng thái lịch học. */
public class ScheduleListActivity extends BaseActivity {
    static final String[] STATUSES = {"Pending", "Confirmed", "Cancelled"};
    private DBHelper db;
    private SessionManager sm;
    private ListView lv;
    private EditText etSearch;
    private List<StudySchedule> data = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Lịch học của tôi");
        db = new DBHelper(this);
        sm = new SessionManager(this);
        LinearLayout root = Ui.page(this);
        etSearch = Ui.edit(this, "Tìm theo môn, địa điểm, ngày, trạng thái", "");
        lv = new ListView(this);
        root.addView(etSearch, Ui.lp(this));
        root.addView(Ui.button(this, "+ Đặt lịch học mới", v -> startActivity(new Intent(this, ScheduleEditActivity.class))), Ui.lp(this));
        root.addView(lv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        Ui.onTextChange(etSearch, this::load);
        lv.setOnItemClickListener((p, v, pos, id) -> actions(data.get(pos)));
    }

    @Override protected void onResume() { super.onResume(); load(); }

    private void load() {
        data = db.searchSchedules(sm.getStudentId(), etSearch.getText().toString().trim());
        List<String> rows = new ArrayList<>();
        for (StudySchedule s : data)
            rows.add(s.subjectName + "\n" + s.studyDate + " | " + s.startTime + " - " + s.endTime +
                    "\nĐịa điểm: " + s.location + " | " + s.status);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }

    private void actions(final StudySchedule s) {
        new AlertDialog.Builder(this).setTitle(s.subjectName)
                .setItems(new String[]{"Sửa", "Đổi trạng thái", "Xóa"}, (d, w) -> {
                    if (w == 0) startActivity(new Intent(this, ScheduleEditActivity.class).putExtra("id", s.id));
                    else if (w == 1) new AlertDialog.Builder(this).setTitle("Chọn trạng thái")
                            .setItems(STATUSES, (x, i) -> changeStatus(s, STATUSES[i])).show();
                    else new AlertDialog.Builder(this).setTitle("Xóa lịch học?")
                            .setPositiveButton("Xóa", (x, y) -> {
                                db.deleteSchedule(s.id);
                                Notifier.cancel(this, s.id);
                                Notifier.show(this, s.id, "Lịch học đã bị xóa", Notifier.describe(s));
                                load();
                            }).setNegativeButton("Hủy", null).show();
                }).show();
    }

    private void changeStatus(StudySchedule s, String st) {
        if (!st.equals("Cancelled") && db.hasConflict(s.studentId, s.studyDate, s.startTime, s.endTime, s.id)) {
            Ui.toast(this, "Trùng với lịch học khác, không thể chuyển trạng thái");
            return;
        }
        db.updateStatus(s.id, st);
        s.status = st;
        Notifier.schedule(this, s);
        Notifier.show(this, s.id, "Trạng thái lịch học thay đổi", Notifier.describe(s));
        load();
    }
}
