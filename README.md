# Quản Lý Cây Cảnh

Ứng dụng Android quản lý cửa hàng cây cảnh, xây dựng theo báo cáo phân tích – thiết kế hệ thống
(`docs/BaoCao-PT-TK-HeThong-v3.md`, không đưa lên git).

## Công nghệ

- Java + AndroidX, Material 3 (bảng màu nâu – kem, giao diện sáng cố định).
- SQLite qua `SQLiteOpenHelper`, không dùng Room/Kotlin.
- `minSdk 34`, `targetSdk 37`; xử lý edge-to-edge bằng `WindowInsets` và
  `OnBackPressedDispatcher` cho điều hướng back.

## Chạy thử

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Mở app → màn hình đăng nhập. Dữ liệu mẫu được tạo tự động ở lần chạy đầu
(3 tài khoản, 4 loại cây, 12 sản phẩm, 9 đơn hàng, 1 cuộc trò chuyện).

### Tài khoản demo

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin@caycanh.vn` | `123456` |
| Khách hàng | `khach@caycanh.vn` | `123456` |
| Khách hàng | `vananh@caycanh.vn` | `123456` |

## Chức năng

**Khách hàng** (đăng nhập → `CustomerActivity`, 5 tab)

- Trang chủ: banner, danh mục, sản phẩm nổi bật.
- Sản phẩm: tìm kiếm, lọc theo loại/khoảng giá/sắp xếp, chi tiết sản phẩm, thêm vào giỏ, mua ngay.
- Giỏ hàng: sửa số lượng, xoá dòng, ghi chú, chọn thanh toán (COD/chuyển khoản), đặt hàng.
- Đơn hàng: lọc theo trạng thái, tìm theo mã, chi tiết, huỷ đơn, mua lại.
- Tài khoản: cập nhật thông tin, đổi mật khẩu, mở trò chuyện với cửa hàng, đăng xuất.
- Trò chuyện: gửi/nhận tin nhắn với cửa hàng (tự làm mới khi đang mở).

**Quản trị viên** (đăng nhập → `AdminActivity`, 5 tab)

- Tổng quan: số sản phẩm, số đơn, doanh thu đã giao, tồn kho thấp, đơn gần đây.
- Sản phẩm: thêm/sửa/xoá (sản phẩm đã bán thì chuyển sang *Ngừng bán*), quản lý tồn kho.
- Đơn hàng: lọc/tìm, xem chi tiết, cập nhật trạng thái, tạo đơn tại quầy.
- Thống kê: doanh thu theo ngày, sản phẩm bán chạy, tồn kho thấp, lọc khoảng ngày.
- Trò chuyện: danh sách hội thoại và trả lời khách.

## Cấu trúc mã nguồn

```
app/src/main/java/com/example/quanlycaycanh/
├── LoginActivity, RegisterActivity, Ui, Session
├── customer/   # màn hình khách hàng + adapter
├── admin/      # màn hình quản trị + adapter
└── data/       # 7 POJO, DBHelper (schema + dữ liệu mẫu), Repository (truy vấn/nghiệp vụ)
```

### Cơ sở dữ liệu

7 bảng: `TaiKhoan`, `LoaiCay`, `SanPham`, `DonHang`, `ChiTietDonHang`, `CuocTroChuyen`, `TinNhan`.

Quy ước nghiệp vụ:

- Giỏ hàng là một `DonHang` có `TrangThai = 'GIO'` (mỗi khách tối đa một giỏ đang mở).
- Đơn online cộng phí vận chuyển 30.000 ₫ khi đặt; đơn tại quầy và đơn huỷ không cộng phí.
- Đặt hàng trừ tồn kho, huỷ đơn hoàn lại tồn kho.
- `DonHang.LichSuTrangThai` lưu lịch sử dạng `MA_TRANG_THAI | thời gian | ghi chú`.

## Ghi chú phạm vi demo

- Ảnh sản phẩm là emoji lưu trong `SanPham.HinhAnh` (không cần tài nguyên ảnh).
- "Quên mật khẩu" chỉ hiển thị hướng dẫn vì bản demo chưa gửi email.
- Chưa có chế độ xem sản phẩm cho khách vãng lai (cần đăng nhập để mua và theo dõi đơn).
