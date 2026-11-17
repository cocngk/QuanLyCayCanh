# Biên bản kiểm tra tích hợp Phần 3 & Phần 4

## Đã hoàn tất (kiểm tra tĩnh)

- Đọc hai bộ mã nguồn, nhập 2 Activity mới vào `com.example.btl`.
- Đăng ký trong Manifest; Activity đăng nhập vẫn là launcher duy nhất.
- Nối Trang chủ → Quản lý cây → Thêm/Sửa → Lưu → Quản lý cây.
- Nối các tab tới Trang chủ/Thống kê/Tài khoản đã tồn tại. Hóa đơn vẫn chưa được làm trong dự án gốc.
- Phần cây dùng chung `caycanh.db` với tài khoản; DB version 3 và nâng cấp v2 → v3 không xóa bảng users.
- Đồng bộ icon, bổ sung vector 24dp cho icon còn thiếu; không ghi đè icon cũ.
- Kiểm tra XML, Manifest, `R.id`, `@drawable`, `@color`, các liên kết màn hình và file ảnh mẫu: không phát hiện tham chiếu thiếu.
- Phân tích hợp lệ `libs.versions.toml` và xác nhận RecyclerView/CardView có dependencies.
- Thử các thao tác SQL cơ bản (thêm, tìm theo tên/loại, sửa, xóa) bằng SQLite riêng để kiểm tra ý tưởng schema.

## Chưa xác nhận trong môi trường này

- **Chưa chạy được `gradlew assembleDebug`** vì máy tạo gói không có Android SDK và bộ Gradle đã cài sẵn.
- **Chưa thể xác nhận ứng dụng chạy thực tế** trên Android Emulator/điện thoại; cần mở project trong Android Studio và kiểm tra Logcat.

## Các thao tác nên tự test

1. Đăng nhập, từ Trang chủ bấm Cây cảnh; từ Tài khoản/Thống kê bấm Cây cảnh.
2. Xem 6 sản phẩm mẫu; tìm theo tên; lọc theo loại; sắp xếp giá hoặc tồn kho; chuyển trang.
3. Thêm cây, chọn ảnh từ thư viện, để trống trường bắt buộc thử kiểm tra lỗi, lưu thông tin.
4. Sửa cây, đổi ảnh, kiểm tra danh sách cập nhật; xóa cây với hộp thoại xác nhận.
5. Tắt hẳn ứng dụng, mở lại để kiểm tra cây và ảnh vẫn còn; kiểm tra tài khoản cũ không mất.
