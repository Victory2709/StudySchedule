package com.example.studyschedule;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.StudySchedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Xem lịch học theo ngày / tuần / tháng. */
public class CalendarActivity extends BaseActivity {
    private DBHelper db;
    private SessionManager sm;
    private Spinner mode;
    private TextView header;
    private ListView lv;
    private long selected = System.currentTimeMillis();
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Lịch học");
        db = new DBHelper(this);
        sm = new SessionManager(this);
        LinearLayout root = Ui.page(this);
        mode = new Spinner(this);
        mode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Theo ngày", "Theo tuần", "Theo tháng"}));
        CalendarView cal = new CalendarView(this);
        header = Ui.text(this, "", 15, true);
        lv = new ListView(this);
        root.addView(mode);
        root.addView(cal, new LinearLayout.LayoutParams(-1, Ui.dp(this, 300)));
        root.addView(header, Ui.lp(this));
        root.addView(lv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        cal.setOnDateChangeListener((v, y, m, d) -> {
            Calendar c = Calendar.getInstance();
            c.set(y, m, d);
            selected = c.getTimeInMillis();
            load();
        });
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { load(); }
            public void onNothingSelected(AdapterView<?> p) { }
        });
    }

    private void load() {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTimeInMillis(selected);
        String from, to;
        int m = mode.getSelectedItemPosition();
        if (m == 0) { from = to = fmt.format(c.getTime()); }
        else if (m == 1) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            from = fmt.format(c.getTime());
            c.add(Calendar.DAY_OF_YEAR, 6);
            to = fmt.format(c.getTime());
        } else {
            c.set(Calendar.DAY_OF_MONTH, 1);
            from = fmt.format(c.getTime());
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            to = fmt.format(c.getTime());
        }
        header.setText(from.equals(to) ? "Ngày " + from : "Từ " + from + " đến " + to);
        List<StudySchedule> data = db.schedulesBetween(sm.getStudentId(), from, to);
        List<String> rows = new ArrayList<>();
        for (StudySchedule s : data)
            rows.add(s.studyDate + " | " + s.startTime + "-" + s.endTime + "\n" + s.subjectName + " @ " + s.location + " [" + s.status + "]");
        if (rows.isEmpty()) rows.add("Không có lịch học");
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }
}
