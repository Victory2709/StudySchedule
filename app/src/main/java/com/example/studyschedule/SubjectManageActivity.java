package com.example.studyschedule;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.Subject;

import java.util.ArrayList;
import java.util.List;

/** Admin: thêm / sửa / xóa / tìm kiếm / xem môn học. */
public class SubjectManageActivity extends BaseActivity {
    private DBHelper db;
    private ListView lv;
    private EditText etSearch;
    private List<Subject> data = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Quản lý môn học");
        db = new DBHelper(this);
        LinearLayout root = Ui.page(this);
        etSearch = Ui.edit(this, "Tìm theo mã, tên, mô tả", "");
        Button add = Ui.button(this, "+ Thêm môn học", v -> form(null));
        lv = new ListView(this);
        root.addView(etSearch, Ui.lp(this));
        root.addView(add, Ui.lp(this));
        root.addView(lv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        Ui.onTextChange(etSearch, this::load);
        lv.setOnItemClickListener((p, v, pos, id) -> actions(data.get(pos)));
        load();
    }

    private void load() {
        data = db.searchSubjects(etSearch.getText().toString().trim());
        List<String> rows = new ArrayList<>();
        for (Subject s : data) rows.add(s.subjectCode + " - " + s.subjectName + " (" + s.credits + " tín chỉ)");
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }

    private void actions(final Subject s) {
        new AlertDialog.Builder(this).setTitle(s.subjectName)
                .setItems(new String[]{"Xem chi tiết", "Sửa", "Xóa"}, (d, w) -> {
                    if (w == 0) new AlertDialog.Builder(this).setTitle("Thông tin môn học")
                            .setMessage("Mã: " + s.subjectCode + "\nTên: " + s.subjectName + "\nSố tín chỉ: " + s.credits +
                                    "\nMô tả: " + s.description)
                            .setPositiveButton("Đóng", null).show();
                    else if (w == 1) form(s);
                    else new AlertDialog.Builder(this).setTitle("Xóa môn học?")
                            .setMessage("Các lịch học thuộc môn này cũng sẽ bị xóa.")
                            .setPositiveButton("Xóa", (x, y) -> { db.deleteSubject(s.id); load(); })
                            .setNegativeButton("Hủy", null).show();
                }).show();
    }

    private void form(final Subject old) {
        LinearLayout l = Ui.page(this);
        final EditText code = Ui.edit(this, "Mã môn", old == null ? "" : old.subjectCode);
        final EditText name = Ui.edit(this, "Tên môn", old == null ? "" : old.subjectName);
        final EditText desc = Ui.edit(this, "Mô tả", old == null ? "" : old.description);
        final EditText credits = Ui.edit(this, "Số tín chỉ", old == null ? "3" : String.valueOf(old.credits));
        credits.setInputType(InputType.TYPE_CLASS_NUMBER);
        l.addView(code); l.addView(name); l.addView(desc); l.addView(credits);
        ScrollView sv = new ScrollView(this);
        sv.addView(l);
        final AlertDialog d = new AlertDialog.Builder(this).setTitle(old == null ? "Thêm môn học" : "Sửa môn học")
                .setView(sv).setPositiveButton("Lưu", null).setNegativeButton("Hủy", null).create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Subject s = old == null ? new Subject() : old;
            s.subjectCode = code.getText().toString().trim();
            s.subjectName = name.getText().toString().trim();
            s.description = desc.getText().toString().trim();
            String cr = credits.getText().toString().trim();
            if (s.subjectCode.isEmpty() || s.subjectName.isEmpty() || cr.isEmpty()) { Ui.toast(this, "Nhập mã, tên và số tín chỉ"); return; }
            s.credits = Integer.parseInt(cr);
            if (s.credits < 1 || s.credits > 10) { Ui.toast(this, "Số tín chỉ từ 1 đến 10"); return; }
            boolean ok = old == null ? db.addSubject(s) : db.updateSubject(s);
            if (!ok) { Ui.toast(this, "Lưu thất bại (trùng mã môn?)"); return; }
            d.dismiss();
            load();
        }));
        d.show();
    }
}
