package com.example.studyschedule;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AdminMainActivity extends BaseActivity {
    @Override protected boolean showBack() { return false; }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final SessionManager sm = new SessionManager(this);
        LinearLayout root = Ui.page(this);
        root.addView(Ui.text(this, "Xin chào Admin: " + sm.getUsername(), 20, true));
        root.addView(Ui.button(this, "Quản lý sinh viên", v -> startActivity(new Intent(this, StudentManageActivity.class))), Ui.lp(this));
        root.addView(Ui.button(this, "Quản lý môn học", v -> startActivity(new Intent(this, SubjectManageActivity.class))), Ui.lp(this));
        root.addView(Ui.button(this, "Đăng xuất", v -> logout()), Ui.lp(this));
        setContentView(root);
    }
}
