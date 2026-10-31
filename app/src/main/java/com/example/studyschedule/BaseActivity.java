package com.example.studyschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

/** Activity cha: có nút quay lại (←) trên thanh tiêu đề và menu Đăng xuất. */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int MENU_LOGOUT = 1001;

    /** Màn hình chính override trả về false để ẩn nút quay lại. */
    protected boolean showBack() { return true; }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (showBack() && getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_LOGOUT, 0, "Đăng xuất")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        if (item.getItemId() == MENU_LOGOUT) { logout(); return true; }
        return super.onOptionsItemSelected(item);
    }

    protected void logout() {
        new SessionManager(this).logout();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
