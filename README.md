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

Đăng nhập bằng tài khoản quản trị:

| Tài khoản | Mật khẩu |
|---|---|---|
| `admin@caycanh.vn` | `123456` |

Dữ liệu danh mục và sản phẩm mẫu được tạo ở lần chạy đầu. Không tạo sẵn khách hàng hay hóa đơn mẫu.

## Chức năng

- **Đăng nhập quản trị:** ghi nhớ phiên đăng nhập nội bộ.
- **Trang chủ:** số sản phẩm, tồn kho, hóa đơn, doanh thu; tìm nhanh và lối tắt đến các chức năng.
- **Quản lý cây cảnh:** tìm kiếm, lọc theo loại, thêm/sửa/ngừng bán, cập nhật giá và tồn kho; chọn ảnh từ thiết bị (hỗ trợ emoji làm ảnh dự phòng).
- **Tạo hóa đơn tại quầy:** nhập tên và số điện thoại khách, tìm/lọc cây, chọn số lượng và phương thức thanh toán, lưu thông tin liên hệ trực tiếp trên hóa đơn.
- **Quản lý hóa đơn:** tìm theo mã/tên/số điện thoại trên hóa đơn, lọc trạng thái và xem chi tiết.
- **Thống kê:** doanh thu theo ngày, sản phẩm bán chạy và sản phẩm tồn kho thấp; lọc theo khoảng ngày.
- **Tài khoản quản trị:** xem thông tin, đổi mật khẩu và đăng xuất.

## Cấu trúc

```text
app/src/main/java/com/example/quanlycaycanh/
├── LoginActivity.java
├── admin/   # màn hình và nghiệp vụ quản trị
└── data/    # SQLite, mô hình và Repository
```

## Ghi chú

- Dữ liệu lưu cục bộ trên thiết bị; chưa có backend hoặc đồng bộ nhiều máy.
- Ảnh đã chọn được tham chiếu bằng URI do Android cung cấp; nếu tệp gốc bị xóa hoặc quyền truy cập bị thu hồi, sản phẩm sẽ dùng emoji dự phòng.
