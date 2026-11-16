# Quản Lý Cửa Hàng Cây Cảnh

Ứng dụng Android nội bộ dành cho **quản trị viên/nhân viên cửa hàng**. Người dùng đăng nhập để quản lý cây cảnh, tạo hóa đơn tại quầy, tra cứu đơn và xem báo cáo. Không có luồng đăng ký hoặc mua hàng dành cho khách trong giao diện ứng dụng.

## Công nghệ

- Java, AndroidX và Material 3.
- SQLite cục bộ qua `SQLiteOpenHelper`.
- `minSdk 34`, `targetSdk 37`, Java 11.
- Giao diện sáng tông xanh lá – trắng; điều hướng 5 mục: Trang chủ, Cây cảnh, Hóa đơn, Thống kê, Tài khoản.

## Chạy thử

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Đăng nhập bằng tài khoản quản trị demo:

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin@caycanh.vn` | `123456` |

Dữ liệu sản phẩm và hóa đơn mẫu được tạo ở lần chạy đầu. Các tài khoản khách mẫu chỉ phục vụ dữ liệu lịch sử hóa đơn; ứng dụng không cho đăng nhập bằng tài khoản khách.

## Chức năng

- **Đăng nhập quản trị:** ghi nhớ phiên; từ chối tài khoản không phải quản trị viên.
- **Trang chủ:** số sản phẩm, tồn kho, hóa đơn, doanh thu; tìm nhanh và lối tắt đến các chức năng.
- **Quản lý cây cảnh:** tìm kiếm, lọc theo loại, thêm/sửa/ngừng bán, cập nhật giá và tồn kho; chọn ảnh từ thiết bị (hỗ trợ emoji làm ảnh dự phòng).
- **Tạo hóa đơn tại quầy:** nhập tên và số điện thoại khách, tìm/lọc cây, chọn số lượng và phương thức thanh toán, lưu hóa đơn. Khách tại quầy được lưu thành hồ sơ liên hệ để tra cứu về sau.
- **Quản lý hóa đơn:** tìm theo mã/tên/số điện thoại khách, lọc trạng thái, xem chi tiết và cập nhật trạng thái.
- **Thống kê:** doanh thu theo ngày, sản phẩm bán chạy và sản phẩm tồn kho thấp; lọc theo khoảng ngày.
- **Tài khoản quản trị:** xem thông tin, đổi mật khẩu và đăng xuất.

## Cấu trúc

```text
app/src/main/java/com/example/quanlycaycanh/
├── LoginActivity.java
├── admin/   # màn hình và nghiệp vụ quản trị
└── data/    # SQLite, mô hình và Repository
```

## Ghi chú demo

- Dữ liệu lưu cục bộ trên thiết bị; chưa có backend hoặc đồng bộ nhiều máy.
- Ảnh đã chọn được tham chiếu bằng URI do Android cung cấp; nếu tệp gốc bị xóa hoặc quyền truy cập bị thu hồi, sản phẩm sẽ dùng emoji dự phòng.
- Báo cáo phân tích trong `docs/` mô tả phạm vi cũ có luồng khách hàng; cần được nhóm cập nhật riêng để thống nhất với phiên bản admin-only này.
