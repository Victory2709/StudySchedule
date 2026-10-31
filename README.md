# Student Study Schedule Management (Android - Java - SQLite)

Mở thư mục này bằng Android Studio (Giraffe trở lên) -> Sync Gradle -> Run.

## Tài khoản mẫu
| Vai trò | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Sinh viên | sv001 | 123456 |
| Sinh viên | sv002 | 123456 |

## Database
- App tự tạo `study_schedule.db` (4 bảng + dữ liệu mẫu) ở lần chạy đầu.
- Nếu muốn dùng file `study_schedule.db` đi kèm: Run app 1 lần -> Android Studio > Device Explorer
  -> `/data/data/com.example.studyschedule/databases/` -> Upload đè file `study_schedule.db`
  (chạy lại app / Force stop trước khi upload). Hoặc dùng `study_schedule.sql` để import bằng DB Browser for SQLite.

## Chức năng
Đăng nhập 2 vai trò (SharedPreferences) • CRUD + tìm kiếm sinh viên/môn học (Admin) • CRUD + tìm kiếm lịch học,
đặt lịch, chống trùng lịch, trạng thái Pending/Confirmed/Cancelled, xem lịch ngày/tuần/tháng, thông báo (Student).

## Điều hướng
- Mọi màn hình con có nút ← (góc trái thanh tiêu đề) để quay lại.
- Mọi màn hình sau đăng nhập có menu "Đăng xuất" ở góc phải thanh tiêu đề.

## Database từ file
- File `app/src/main/assets/study_schedule.db` được chép thành database của app ở lần chạy đầu (xem `DBHelper.copyFromAssetsIfNeeded`).
- Bản sao để nộp/xem: thư mục `database/` (`study_schedule.db`, `study_schedule.sql`).
- Muốn nạp lại dữ liệu từ file: gỡ app (hoặc Wipe Data máy ảo) rồi Run lại.
