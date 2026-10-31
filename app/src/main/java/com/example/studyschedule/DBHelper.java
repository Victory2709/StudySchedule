package com.example.studyschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studyschedule.Models.Account;
import com.example.studyschedule.Models.Student;
import com.example.studyschedule.Models.StudySchedule;
import com.example.studyschedule.Models.Subject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "study_schedule.db";

    public DBHelper(Context c) {
        super(c, DB_NAME, null, 1);
        copyFromAssetsIfNeeded(c);
    }

    /** Lần chạy đầu: chép study_schedule.db từ thư mục assets thành database của app. */
    private void copyFromAssetsIfNeeded(Context c) {
        File dbFile = c.getDatabasePath(DB_NAME);
        if (dbFile.exists()) return; // đã có thì dùng tiếp, không ghi đè dữ liệu
        File dir = dbFile.getParentFile();
        if (dir != null) dir.mkdirs();
        try (InputStream in = c.getAssets().open(DB_NAME);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        } catch (IOException e) {
            // Không có file trong assets thì onCreate() sẽ tự tạo database như cũ
        }
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        db.disableWriteAheadLogging(); // mọi thay đổi nằm trong 1 file .db duy nhất
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Account(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL, role TEXT NOT NULL)");
        db.execSQL("CREATE TABLE Student(id INTEGER PRIMARY KEY AUTOINCREMENT, studentCode TEXT NOT NULL UNIQUE, fullname TEXT NOT NULL, email TEXT, phone TEXT, accountId INTEGER NOT NULL, FOREIGN KEY(accountId) REFERENCES Account(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE Subject(id INTEGER PRIMARY KEY AUTOINCREMENT, subjectCode TEXT NOT NULL UNIQUE, subjectName TEXT NOT NULL, description TEXT, credits INTEGER NOT NULL DEFAULT 3)");
        db.execSQL("CREATE TABLE StudySchedule(id INTEGER PRIMARY KEY AUTOINCREMENT, studentId INTEGER NOT NULL, subjectId INTEGER NOT NULL, studyDate TEXT NOT NULL, startTime TEXT NOT NULL, endTime TEXT NOT NULL, location TEXT, status TEXT NOT NULL DEFAULT 'Pending', FOREIGN KEY(studentId) REFERENCES Student(id) ON DELETE CASCADE, FOREIGN KEY(subjectId) REFERENCES Subject(id) ON DELETE CASCADE)");
        // Dữ liệu mẫu tối thiểu
        db.execSQL("INSERT INTO Account(username,password,role) VALUES('admin','admin123','ADMIN'),('sv001','123456','STUDENT')");
        db.execSQL("INSERT INTO Student(studentCode,fullname,email,phone,accountId) VALUES('sv001','Nguyen Van A','a@example.com','0901234567',2)");
        db.execSQL("INSERT INTO Subject(subjectCode,subjectName,description,credits) VALUES('MOB101','Lap trinh Android','Android co ban',3),('DB101','Co so du lieu','SQL va SQLite',3),('JAV101','Lap trinh Java','Java OOP',3)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) { }

    // ===================== ACCOUNT =====================
    public Account login(String u, String p) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,username,password,role FROM Account WHERE username=? AND password=?", new String[]{u, p});
        Account a = null;
        if (c.moveToFirst()) {
            a = new Account();
            a.id = c.getInt(0); a.username = c.getString(1); a.password = c.getString(2); a.role = c.getString(3);
        }
        c.close();
        return a;
    }

    // ===================== STUDENT =====================
    private ContentValues studentValues(Student s) {
        ContentValues v = new ContentValues();
        v.put("studentCode", s.studentCode); v.put("fullname", s.fullname);
        v.put("email", s.email); v.put("phone", s.phone);
        return v;
    }

    private Student mapStudent(Cursor c) {
        Student s = new Student();
        s.id = c.getInt(0); s.studentCode = c.getString(1); s.fullname = c.getString(2);
        s.email = c.getString(3); s.phone = c.getString(4); s.accountId = c.getInt(5);
        return s;
    }

    /** Thêm sinh viên + tạo tài khoản (username = mã sinh viên). Trả về -1 nếu lỗi/trùng. */
    public long addStudent(Student s, String password) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues a = new ContentValues();
            a.put("username", s.studentCode); a.put("password", password); a.put("role", "STUDENT");
            long accId = db.insert("Account", null, a);
            if (accId < 0) return -1;
            ContentValues v = studentValues(s);
            v.put("accountId", accId);
            long id = db.insert("Student", null, v);
            if (id < 0) return -1;
            db.setTransactionSuccessful();
            return id;
        } finally { db.endTransaction(); }
    }

    public boolean updateStudent(Student s, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            int n = db.update("Student", studentValues(s), "id=?", new String[]{String.valueOf(s.id)});
            if (n < 1) return false;
            ContentValues a = new ContentValues();
            a.put("username", s.studentCode);
            if (newPassword != null && !newPassword.isEmpty()) a.put("password", newPassword);
            if (db.update("Account", a, "id=?", new String[]{String.valueOf(s.accountId)}) < 1) return false;
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) { return false; } finally { db.endTransaction(); }
    }

    public void deleteStudent(Student s) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("Student", "id=?", new String[]{String.valueOf(s.id)});
        db.delete("Account", "id=?", new String[]{String.valueOf(s.accountId)});
    }

    public List<Student> searchStudents(String q) {
        String like = "%" + q + "%";
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,studentCode,fullname,email,phone,accountId FROM Student " +
                        "WHERE studentCode LIKE ? OR fullname LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY studentCode",
                new String[]{like, like, like, like});
        List<Student> l = new ArrayList<>();
        while (c.moveToNext()) l.add(mapStudent(c));
        c.close();
        return l;
    }

    public Student getStudentByAccount(int accountId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,studentCode,fullname,email,phone,accountId FROM Student WHERE accountId=?",
                new String[]{String.valueOf(accountId)});
        Student s = c.moveToFirst() ? mapStudent(c) : null;
        c.close();
        return s;
    }

    // ===================== SUBJECT =====================
    private ContentValues subjectValues(Subject s) {
        ContentValues v = new ContentValues();
        v.put("subjectCode", s.subjectCode); v.put("subjectName", s.subjectName);
        v.put("description", s.description); v.put("credits", s.credits);
        return v;
    }

    public boolean addSubject(Subject s) { return getWritableDatabase().insert("Subject", null, subjectValues(s)) > 0; }

    public boolean updateSubject(Subject s) {
        return getWritableDatabase().update("Subject", subjectValues(s), "id=?", new String[]{String.valueOf(s.id)}) > 0;
    }

    public void deleteSubject(int id) { getWritableDatabase().delete("Subject", "id=?", new String[]{String.valueOf(id)}); }

    public List<Subject> searchSubjects(String q) {
        String like = "%" + q + "%";
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,subjectCode,subjectName,description,credits FROM Subject " +
                        "WHERE subjectCode LIKE ? OR subjectName LIKE ? OR description LIKE ? ORDER BY subjectCode",
                new String[]{like, like, like});
        List<Subject> l = new ArrayList<>();
        while (c.moveToNext()) {
            Subject s = new Subject();
            s.id = c.getInt(0); s.subjectCode = c.getString(1); s.subjectName = c.getString(2);
            s.description = c.getString(3); s.credits = c.getInt(4);
            l.add(s);
        }
        c.close();
        return l;
    }

    public List<Subject> allSubjects() { return searchSubjects(""); }

    // ===================== STUDY SCHEDULE =====================
    private static final String SCHEDULE_SELECT =
            "SELECT ss.id,ss.studentId,ss.subjectId,ss.studyDate,ss.startTime,ss.endTime,ss.location,ss.status,sj.subjectName " +
                    "FROM StudySchedule ss JOIN Subject sj ON sj.id=ss.subjectId ";

    private StudySchedule mapSchedule(Cursor c) {
        StudySchedule s = new StudySchedule();
        s.id = c.getInt(0); s.studentId = c.getInt(1); s.subjectId = c.getInt(2);
        s.studyDate = c.getString(3); s.startTime = c.getString(4); s.endTime = c.getString(5);
        s.location = c.getString(6); s.status = c.getString(7); s.subjectName = c.getString(8);
        return s;
    }

    private ContentValues scheduleValues(StudySchedule s) {
        ContentValues v = new ContentValues();
        v.put("studentId", s.studentId); v.put("subjectId", s.subjectId);
        v.put("studyDate", s.studyDate); v.put("startTime", s.startTime); v.put("endTime", s.endTime);
        v.put("location", s.location); v.put("status", s.status);
        return v;
    }

    public long addSchedule(StudySchedule s) { return getWritableDatabase().insert("StudySchedule", null, scheduleValues(s)); }

    public boolean updateSchedule(StudySchedule s) {
        return getWritableDatabase().update("StudySchedule", scheduleValues(s), "id=?", new String[]{String.valueOf(s.id)}) > 0;
    }

    public void updateStatus(int id, String status) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        getWritableDatabase().update("StudySchedule", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteSchedule(int id) { getWritableDatabase().delete("StudySchedule", "id=?", new String[]{String.valueOf(id)}); }

    public StudySchedule getSchedule(int id) {
        Cursor c = getReadableDatabase().rawQuery(SCHEDULE_SELECT + "WHERE ss.id=?", new String[]{String.valueOf(id)});
        StudySchedule s = c.moveToFirst() ? mapSchedule(c) : null;
        c.close();
        return s;
    }

    public List<StudySchedule> searchSchedules(int studentId, String q) {
        String like = "%" + q + "%";
        Cursor c = getReadableDatabase().rawQuery(
                SCHEDULE_SELECT + "WHERE ss.studentId=? AND (sj.subjectName LIKE ? OR ss.location LIKE ? OR ss.studyDate LIKE ? OR ss.status LIKE ?) " +
                        "ORDER BY ss.studyDate, ss.startTime",
                new String[]{String.valueOf(studentId), like, like, like, like});
        List<StudySchedule> l = new ArrayList<>();
        while (c.moveToNext()) l.add(mapSchedule(c));
        c.close();
        return l;
    }

    /** Lấy lịch học trong khoảng ngày [from, to] (định dạng yyyy-MM-dd). */
    public List<StudySchedule> schedulesBetween(int studentId, String from, String to) {
        Cursor c = getReadableDatabase().rawQuery(
                SCHEDULE_SELECT + "WHERE ss.studentId=? AND ss.studyDate BETWEEN ? AND ? ORDER BY ss.studyDate, ss.startTime",
                new String[]{String.valueOf(studentId), from, to});
        List<StudySchedule> l = new ArrayList<>();
        while (c.moveToNext()) l.add(mapSchedule(c));
        c.close();
        return l;
    }

    /** Kiểm tra trùng lịch: cùng sinh viên, cùng ngày, khoảng giờ giao nhau (bỏ qua lịch đã hủy). */
    public boolean hasConflict(int studentId, String date, String start, String end, int excludeId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM StudySchedule WHERE studentId=? AND studyDate=? AND status<>'Cancelled' " +
                        "AND id<>? AND startTime<? AND endTime>?",
                new String[]{String.valueOf(studentId), date, String.valueOf(excludeId), end, start});
        c.moveToFirst();
        int n = c.getInt(0);
        c.close();
        return n > 0;
    }
}
