package com.example.studyschedule;

import android.content.Context;
import android.content.SharedPreferences;

/** Lưu thông tin tài khoản đăng nhập bằng SharedPreferences. */
public class SessionManager {
    private final SharedPreferences p;

    public SessionManager(Context c) { p = c.getSharedPreferences("session", Context.MODE_PRIVATE); }

    public void save(int accountId, String username, String role, int studentId, String name) {
        p.edit().putBoolean("logged", true).putInt("accountId", accountId).putString("username", username)
                .putString("lastUsername", username).putString("role", role)
                .putInt("studentId", studentId).putString("name", name).apply();
    }

    public void logout() { p.edit().putBoolean("logged", false).apply(); }
    public boolean isLoggedIn() { return p.getBoolean("logged", false); }
    public String getRole() { return p.getString("role", ""); }
    public int getStudentId() { return p.getInt("studentId", -1); }
    public String getName() { return p.getString("name", ""); }
    public String getUsername() { return p.getString("username", ""); }
    public String getLastUsername() { return p.getString("lastUsername", ""); }
}
