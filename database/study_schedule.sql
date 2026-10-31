-- Database: study_schedule.db (SQLite)
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS Account(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS Student(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    studentCode TEXT NOT NULL UNIQUE,
    fullname TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    accountId INTEGER NOT NULL,
    FOREIGN KEY(accountId) REFERENCES Account(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS Subject(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subjectCode TEXT NOT NULL UNIQUE,
    subjectName TEXT NOT NULL,
    description TEXT,
    credits INTEGER NOT NULL DEFAULT 3
);
CREATE TABLE IF NOT EXISTS StudySchedule(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    studentId INTEGER NOT NULL,
    subjectId INTEGER NOT NULL,
    studyDate TEXT NOT NULL,      -- yyyy-MM-dd
    startTime TEXT NOT NULL,      -- HH:mm
    endTime TEXT NOT NULL,        -- HH:mm
    location TEXT,
    status TEXT NOT NULL DEFAULT 'Pending',  -- Pending | Confirmed | Cancelled
    FOREIGN KEY(studentId) REFERENCES Student(id) ON DELETE CASCADE,
    FOREIGN KEY(subjectId) REFERENCES Subject(id) ON DELETE CASCADE
);

-- Dữ liệu mẫu
INSERT INTO Account(username,password,role) VALUES
 ('admin','admin123','ADMIN'),
 ('sv001','123456','STUDENT'),
 ('sv002','123456','STUDENT');

INSERT INTO Student(studentCode,fullname,email,phone,accountId) VALUES
 ('sv001','Nguyễn Văn A','a@example.com','0901234567',2),
 ('sv002','Trần Thị B','b@example.com','0912345678',3);

INSERT INTO Subject(subjectCode,subjectName,description,credits) VALUES
 ('MOB101','Lập trình Android','Android cơ bản',3),
 ('DB101','Cơ sở dữ liệu','SQL và SQLite',3),
 ('JAV101','Lập trình Java','Java hướng đối tượng',3),
 ('WEB101','Lập trình Web','HTML, CSS, JS',3),
 ('ENG101','Tiếng Anh chuyên ngành','Anh văn IT',2);

INSERT INTO StudySchedule(studentId,subjectId,studyDate,startTime,endTime,location,status) VALUES
 (1,1,'2026-11-02','08:00','10:00','Phòng A101','Confirmed'),
 (1,2,'2026-11-02','13:00','15:00','Phòng B203','Pending'),
 (1,3,'2026-11-04','09:00','11:00','Phòng A102','Pending'),
 (1,5,'2026-11-05','14:00','16:00','Thư viện','Cancelled'),
 (2,4,'2026-11-03','08:00','10:00','Phòng C301','Confirmed');
