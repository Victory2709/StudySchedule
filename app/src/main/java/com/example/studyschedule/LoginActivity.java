package com.example.studyschedule;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.Models.Account;
import com.example.studyschedule.Models.Student;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final SessionManager sm = new SessionManager(this);
        if (sm.isLoggedIn()) { goHome(sm.getRole()); return; }
        final DBHelper db = new DBHelper(this);

        LinearLayout root = Ui.page(this);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(Ui.text(this, "QUẢN LÝ LỊCH HỌC", 24, true));
        final EditText user = Ui.edit(this, "Tên đăng nhập", sm.getLastUsername());
        final EditText pass = Ui.edit(this, "Mật khẩu", "");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.HORIZONTAL);
        final RadioButton rbStudent = new RadioButton(this);
        rbStudent.setText("Sinh viên");
        rbStudent.setId(1001);
        RadioButton rbAdmin = new RadioButton(this);
        rbAdmin.setText("Admin");
        rbAdmin.setId(1002);
        rg.addView(rbStudent);
        rg.addView(rbAdmin);
        rg.check(1001);
        root.addView(user, Ui.lp(this));
        root.addView(pass, Ui.lp(this));
        root.addView(rg, Ui.lp(this));
        root.addView(Ui.button(this, "Đăng nhập", v -> {
            String u = user.getText().toString().trim(), p = pass.getText().toString();
            if (u.isEmpty() || p.isEmpty()) { Ui.toast(this, "Nhập đầy đủ thông tin"); return; }
            Account a = db.login(u, p);
            if (a == null) { Ui.toast(this, "Sai tên đăng nhập hoặc mật khẩu"); return; }
            String expected = rg.getCheckedRadioButtonId() == 1002 ? "ADMIN" : "STUDENT";
            if (!a.role.equals(expected)) { Ui.toast(this, "Sai vai trò đăng nhập"); return; }
            int sid = -1;
            String name = a.username;
            if (a.role.equals("STUDENT")) {
                Student s = db.getStudentByAccount(a.id);
                if (s == null) { Ui.toast(this, "Tài khoản chưa gắn sinh viên"); return; }
                sid = s.id; name = s.fullname;
            }
            sm.save(a.id, a.username, a.role, sid, name);
            goHome(a.role);
        }), Ui.lp(this));
        setContentView(root);
    }

    private void goHome(String role) {
        startActivity(new Intent(this, role.equals("ADMIN") ? AdminMainActivity.class : StudentMainActivity.class));
        finish();
    }
}
