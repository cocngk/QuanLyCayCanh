# Báo cáo rà soát và sửa lỗi — `code/final`

**Phạm vi:** rà soát bốn snapshot `code/pham-thi-huyen-trang`,
`code/tieu-thi-van-anh`, `code/ngo-duc-duy` và `code/nguyen-huu-khanh`;
nhánh `code/final` được tạo từ snapshot Người 4 (`a20ad9b`).

## Người 1 — Phạm Thị Huyền Trang

| Phát hiện | Ảnh hưởng | Xử lý |
|---|---|---|
| Màn đăng ký vẫn mở cho mọi người sau khi tài khoản đầu tiên đã được tạo; ứng dụng chưa có phân quyền người dùng cuối/quản trị viên. | Không đúng phạm vi ứng dụng chỉ dành cho người quản lý; người khác có thể tự tạo tài khoản đăng nhập. | Giới hạn đăng ký cho bước khởi tạo tài khoản quản trị đầu tiên; ẩn liên kết đăng ký sau đó và kiểm tra lại trong `Register` lẫn giao dịch `DBHelper.addUser()` để tránh đăng ký đồng thời. |

## Người 2 — Tiêu Thị Vân Anh

| Phát hiện | Ảnh hưởng | Xử lý |
|---|---|---|
| Trang chủ và màn thống kê hiển thị số liệu mẫu cố định; biểu đồ cũng dùng dữ liệu giả. Ngày lọc mặc định cố định và nút lọc chỉ hiện Toast. | Số liệu không phản ánh cây, tồn kho hay hóa đơn thực tế; chọn ngày không làm thay đổi báo cáo. | Nối thẻ tổng quan với SQLite; thống kê doanh thu, hóa đơn, số cây bán, doanh số theo danh mục, top cây và tồn kho theo khoảng ngày được chọn. Trạng thái không có dữ liệu được hiển thị riêng. |
| Điều hướng Hóa đơn ở màn thống kê chưa mở màn quản lý hóa đơn. | Luồng quản lý bị đứt dù màn hóa đơn đã được bổ sung ở nhánh sau. | Nối mục điều hướng sang `InvoiceManagementActivity`. |

## Người 3 — Ngô Đức Duy

| Phát hiện | Ảnh hưởng | Xử lý |
|---|---|---|
| Schema `plants` giữa các phiên bản không tương thích: bảng cũ có thể thiếu `description`/`image_uri`, nhưng phiên bản DB không tăng hoặc không có migration tương ứng. | Cài cập nhật trên DB cũ có thể lỗi khi đọc hoặc lưu cây. | Bổ sung migration bảo toàn dữ liệu cũ, thêm cột còn thiếu và chuyển tên drawable cũ sang URI tài nguyên; tạo bảng hóa đơn sau bước tương thích schema. |
| Seed cây mẫu không kiểm tra dữ liệu đã có. | Có thể chèn trùng khi seed được gọi lại. | Thêm guard theo số bản ghi trước khi seed. |
| Bộ lọc có danh mục cố định, gồm “Cây khác” dù danh mục không tồn tại; form sửa cũng chỉ hiểu một danh sách danh mục cứng. | Chọn danh mục có thể trả về danh sách rỗng; sửa cây thuộc danh mục bổ sung có thể không chọn đúng loại. | Lấy danh mục từ DB cho bộ lọc và hợp nhất danh mục hiện có với các lựa chọn mặc định trong form. |
| Form dùng `numberDecimal` cho giá tiền và không xử lý nhập theo dấu phân cách hàng nghìn. | Giá kiểu `120.000` có thể bị hiểu sai hoặc báo không hợp lệ. | Chuyển sang nhập số nguyên đồng nhất với cách lưu hóa đơn; chuẩn hóa dấu phân cách trước khi kiểm tra. |
| Form có thể bị bàn phím che nút Lưu; mục Hóa đơn ở thanh điều hướng tài khoản chưa mở màn tương ứng. | Khó thao tác trên màn hình nhỏ và luồng điều hướng không nhất quán. | Bật `adjustResize` cho form; liên kết mục này đến danh sách hóa đơn và đổi nhãn “Đơn hàng” thành “Hóa đơn”. |

## Người 4 — Nguyễn Hữu Khanh

| Phát hiện | Ảnh hưởng | Xử lý |
|---|---|---|
| `invoice_items` không lưu snapshot danh mục cây. | Nếu cây bị đổi danh mục hoặc xóa sau khi bán, báo cáo lịch sử theo danh mục không thể khôi phục chính xác. | Bổ sung `plant_category` vào dòng hóa đơn; hóa đơn mới lưu danh mục tại thời điểm bán. Migration v4→v5 điền dữ liệu có thể suy ra từ cây còn tồn tại. Với dữ liệu lịch sử đã đổi/xóa cây, danh mục cũ không thể khôi phục chắc chắn. |

## Kiểm tra

- `./gradlew test assembleDebug` — **thành công**.
- APK debug: `app/build/outputs/apk/debug/app-debug.apk`.
- Bộ unit test hiện có chỉ gồm một test mẫu phép cộng; chưa có kiểm thử tự động cho migration, CRUD cây, tạo/hủy hóa đơn hoặc thống kê.

### Kiểm thử thực tế trên Waydroid

- Cài và mở APK debug trên Waydroid; đăng nhập bằng tài khoản quản trị kiểm thử `QATester`.
- Mở danh sách cây, form thêm và form sửa. Xác nhận 6 cây seed sẵn có; form sửa nạp đúng cây lưỡi hổ, danh mục “Cây nội thất”, giá `120000` và tồn kho `35`. Thoát form, không lưu thay đổi.
- Tạo một hóa đơn kiểm thử cho 1 cây lưỡi hổ (`120.000 đ`), sau đó hủy hóa đơn. Sau khi hủy: tồn kho cây lưỡi hổ trở lại `35`, tổng tồn kho là `172`, còn 6 cây; danh sách ghi nhận 1 hóa đơn đã hủy, 0 hoàn thành và doanh thu `0 đ`.
- Màn thống kê với khoảng ngày 01–17/11/2026 hiển thị doanh thu, hóa đơn và sản phẩm bán đều bằng 0; tồn kho 172 và các biểu đồ bán hàng ở trạng thái chưa có dữ liệu.
- Mở màn tài khoản; tài khoản kiểm thử hiển thị vai trò “Người quản lý cửa hàng”. Logcat kiểm tra sau các luồng trên không ghi nhận `FATAL EXCEPTION` của ứng dụng.
- Dữ liệu kiểm thử còn lại có chủ đích: tài khoản `QATester` và một hóa đơn `QAInvoiceTest` ở trạng thái **đã hủy**. Không thêm/sửa/xóa cây; lượt kiểm thử ban đầu không sửa mã nguồn.
- Ảnh chụp được lưu ngoài repository tại `/tmp/opencode/`: `waydroid-login.png`, `waydroid-register.png`, `waydroid-home.png`, `waydroid-plants.png`, `waydroid-plant-form.png`, `waydroid-invoice-list.png`, `waydroid-create-invoice.png`, `waydroid-statistics.png`, `waydroid-account.png`.

### Đối chiếu giao diện lại trên thiết bị — 18/11/2026

- Chụp lại trực tiếp từ Waydroid các màn Trang chủ, Cây cảnh, Hóa đơn, Thống kê, Tài khoản, Thông tin cá nhân và Tạo hóa đơn; phiên Waydroid đang chạy, ADB thiết bị online.
- Ảnh xác nhận có lỗi hiển thị icon: `ic_back.xml` khai báo kích thước nội tại `800dp` dù viewport chỉ `72×72`; nút quay lại trên Hóa đơn/Tạo hóa đơn bị phóng và cắt. Nút “Xem” ở dòng hóa đơn cũng quá hẹp nên nhãn bị xuống dòng/cắt cạnh icon mắt.
- Các icon thanh điều hướng và danh sách cây vẫn hiển thị trong ảnh mới; logcat không có `FATAL EXCEPTION`.
- Ảnh mới: `/tmp/opencode/waydroid-recheck-*.png`.

### Sửa và xác minh lại icon

- Sau khi được người dùng cho phép, chuẩn hóa `ic_back.xml` thành vector 24dp với chevron đơn; nới nút “Xem” lên 92dp, giảm icon mắt còn 18dp và khóa nhãn một dòng.
- `./gradlew test assembleDebug` — **thành công**; cài lại bằng `adb install -r` để giữ DB kiểm thử.
- Chụp lại trên Waydroid: `/tmp/opencode/waydroid-invoices-afterfix.png` và `/tmp/opencode/waydroid-create-afterfix.png`. Nút quay lại và icon mắt hiển thị đúng; nhãn “Xem” không còn bị cắt.
- Retest không tạo thêm dữ liệu: vẫn còn 1 hóa đơn đã hủy, 0 hóa đơn hoàn thành, doanh thu `0 đ`; logcat không có `FATAL EXCEPTION`.
