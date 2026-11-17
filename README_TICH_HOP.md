# BTL di động — Tích hợp màn hình 3 và 4

## Phạm vi

- Giữ các màn hình đã có của dự án `btldidong` (Java + XML, package `com.example.btl`).
- Màn hình 3: `PlantManagementActivity.java` / `activity_plant_management.xml`: tìm kiếm, lọc loại cây, sắp xếp, phân trang 5 dòng, chỉnh sửa và xóa có xác nhận.
- Màn hình 4: `AddEditPlantActivity.java` / `activity_add_edit_plant.xml`: chọn ảnh thư viện, nhập/chỉnh sửa, kiểm tra dữ liệu, lưu SQLite.
- Cây cảnh dùng `Plant.java`, `PlantAdapter.java`, `PlantDatabase.java`, `ImageStorage.java`, `BottomNavHelper.java`.
- Thêm khai báo 2 Activity trong `AndroidManifest.xml` (không sửa launcher là `Login`).

## Dữ liệu

**Chỉ dùng một SQLite**: `caycanh.db` do `DBHelper.java` quản lý. Tăng DB version từ 2 lên 3, thêm bảng `plants` và 6 cây mẫu. Nâng cấp từ v2 lên v3 không xóa bảng tài khoản; dữ liệu đăng nhập vẫn giữ nguyên. CRUD của cây đi qua `PlantDatabase` và kết nối từ `DBHelper`.

## Giao diện

- Cây cảnh sử dụng màu chủ đạo `#087A35` khớp header Trang chủ, nền nhạt `#F5F5F5`, badge `#E8F5E9`.
- Dùng lại `ic_tree`, `ic_search`, `ic_edit`, `ic_delete`, `ic_add`, `ic_home`, `ic_bill`, `ic_chart`, `ic_user` của dự án gốc.
- Các icon chưa có chuẩn 24dp được thêm mới (`ic_plant_back`, `ic_plant_save`, `ic_plant_image`, `ic_filter`, `ic_box`, ...). Các vector gốc của màn hình khác không bị ghi đè.

## Chạy thử trong Android Studio trên Windows 11

1. Giải nén ZIP rồi chọn **Open** tại thư mục `btldidong` (có `settings.gradle.kts`).
2. Đợi **Gradle Sync**. Dự án gốc yêu cầu Gradle 9.4.1, AGP 9.2.1, JDK 21, `compileSdk` 36.1; cần Android SDK tương ứng và internet nếu thư viện chưa tải.
3. Chạy **app**; đăng ký hoặc đăng nhập, vào **Trang chủ** -> **Cây cảnh** hoặc nút **Quản lý cây cảnh**.
4. Từ danh sách bấm **+** để thêm; nhập tên, chọn loại, giá > 0, tồn kho >= 0; có thể chọn ảnh từ thư viện; bấm **Lưu**.
5. Thử tìm kiếm theo tên, chọn chip loại cây, bộ lọc sắp xếp, chuyển trang, sửa và xóa.
6. Bấm tab **Trang chủ**, **Thống kê**, **Tài khoản** để kiểm tra điều hướng. Tab **Hóa đơn** hiện vẫn là màn hình chưa triển khai ở dự án tổng.
7. Từ Trang chủ, có thể nhập từ khóa trong ô tìm kiếm rồi nhấn **Search** trên bàn phím để vào danh sách đã lọc.

## Lưu ý kiểm chứng

Đã kiểm tra tĩnh: cú pháp XML, tham chiếu resource/ID, tên package, khai báo Activity, cấu hình Gradle. Môi trường tạo ZIP không có Android SDK và bộ Gradle có sẵn nên **chưa thể chạy `assembleDebug` hoặc Emulator** tại đây. Để xác nhận build thành công cần Sync/Run trên Android Studio thật.
