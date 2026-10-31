package com.example.studyschedule;

public class Models {
    public static class Account { public int id; public String username, password, role; }

    public static class Student {
        public int id, accountId;
        public String studentCode, fullname, email, phone;
    }

    public static class Subject {
        public int id, credits;
        public String subjectCode, subjectName, description;
        @Override public String toString() { return subjectCode + " - " + subjectName; }
    }

    public static class StudySchedule {
        public int id, studentId, subjectId;
        public String studyDate, startTime, endTime, location, status;
        public String subjectName; // lấy từ JOIN
    }
}
