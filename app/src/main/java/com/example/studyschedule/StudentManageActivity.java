package com.example.studyschedule;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.Student;

import java.util.ArrayList;
import java.util.List;

/** Admin: thêm / sửa / xóa / tìm kiếm / xem sinh viên. */
public class StudentManageActivity extends BaseActivity {
    private DBHelper db;
    private ListView lv;
    private EditText etSearch;
    private List<Student> data = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Quản lý sinh viên");
        db = new DBHelper(this);
        LinearLayout root = Ui.page(this);
        etSearch = Ui.edit(this, "Tìm theo mã, tên, email, SĐT", "");
        Button add = Ui.button(this, "+ Thêm sinh viên", v -> form(null));
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
        data = db.searchStudents(etSearch.getText().toString().trim());
        List<String> rows = new ArrayList<>();
        for (Student s : data) rows.add(s.studentCode + " - " + s.fullname + "\n" + s.email + " | " + s.phone);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }

    private void actions(final Student s) {
        new AlertDialog.Builder(this).setTitle(s.fullname)
                .setItems(new String[]{"Xem chi tiết", "Sửa", "Xóa"}, (d, w) -> {
                    if (w == 0) new AlertDialog.Builder(this).setTitle("Thông tin sinh viên")
                            .setMessage("Mã SV: " + s.studentCode + "\nHọ tên: " + s.fullname + "\nEmail: " + s.email +
                                    "\nSĐT: " + s.phone + "\nTài khoản: " + s.studentCode)
                            .setPositiveButton("Đóng", null).show();
                    else if (w == 1) form(s);
                    else new AlertDialog.Builder(this).setTitle("Xóa sinh viên?")
                            .setMessage("Tài khoản và toàn bộ lịch học của sinh viên cũng sẽ bị xóa.")
                            .setPositiveButton("Xóa", (x, y) -> { db.deleteStudent(s); load(); })
                            .setNegativeButton("Hủy", null).show();
                }).show();
    }

    private void form(final Student old) {
        LinearLayout l = Ui.page(this);
        final EditText code = Ui.edit(this, "Mã sinh viên", old == null ? "" : old.studentCode);
        final EditText name = Ui.edit(this, "Họ tên", old == null ? "" : old.fullname);
        final EditText email = Ui.edit(this, "Email", old == null ? "" : old.email);
        final EditText phone = Ui.edit(this, "Số điện thoại", old == null ? "" : old.phone);
        final EditText pass = Ui.edit(this, old == null ? "Mật khẩu" : "Mật khẩu mới (bỏ trống nếu giữ nguyên)", "");
        l.addView(code); l.addView(name); l.addView(email); l.addView(phone); l.addView(pass);
        ScrollView sv = new ScrollView(this);
        sv.addView(l);
        final AlertDialog d = new AlertDialog.Builder(this).setTitle(old == null ? "Thêm sinh viên" : "Sửa sinh viên")
                .setView(sv).setPositiveButton("Lưu", null).setNegativeButton("Hủy", null).create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Student s = old == null ? new Student() : old;
            s.studentCode = code.getText().toString().trim();
            s.fullname = name.getText().toString().trim();
            s.email = email.getText().toString().trim();
            s.phone = phone.getText().toString().trim();
            String pw = pass.getText().toString();
            if (s.studentCode.isEmpty() || s.fullname.isEmpty()) { Ui.toast(this, "Nhập mã và họ tên"); return; }
            if (!s.email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) { Ui.toast(this, "Email không hợp lệ"); return; }
            if (!s.phone.isEmpty() && !s.phone.matches("\\d{9,11}")) { Ui.toast(this, "SĐT phải gồm 9-11 chữ số"); return; }
            if (old == null) {
                if (pw.length() < 4) { Ui.toast(this, "Mật khẩu tối thiểu 4 ký tự"); return; }
                if (db.addStudent(s, pw) < 0) { Ui.toast(this, "Mã sinh viên đã tồn tại"); return; }
            } else if (!db.updateStudent(s, pw)) { Ui.toast(this, "Cập nhật thất bại (trùng mã?)"); return; }
            d.dismiss();
            load();
        }));
        d.show();
    }
}
