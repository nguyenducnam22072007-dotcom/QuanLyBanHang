# Phần mềm quản lý bán hàng (Java Swing + SQL Server)

Đồ án Java: ứng dụng desktop quản lý bán hàng, kết nối SQL Server qua JDBC.

## Chức năng
| Tab | Chức năng |
|---|---|
| Bán hàng | Chọn khách hàng, nhân viên, thêm sản phẩm vào giỏ, thanh toán (lưu hóa đơn, tự trừ tồn kho) |
| Hóa đơn | Xem danh sách hóa đơn + chi tiết, tìm kiếm, xóa hóa đơn (hoàn lại tồn kho) |
| Sản phẩm | Thêm, sửa, xóa, tìm kiếm |
| Loại sản phẩm | Thêm, sửa, xóa, tìm kiếm |
| Khách hàng | Thêm, sửa, xóa, tìm kiếm |
| Nhân viên | Thêm, sửa, xóa, tìm kiếm |

## Cách chạy
1. **Tạo CSDL:** mở SSMS, mở file `database/QuanLyBanHang.sql`, nhấn Execute (chỉ chạy 1 lần).
2. **Cấu hình kết nối:** mở `src/main/java/qlbh/Db.java`, sửa `USER`, `PASSWORD` (và `URL` nếu cần).
   - SQL Server phải bật chế độ Mixed Mode (có tài khoản `sa`) và bật **TCP/IP, cổng 1433** trong SQL Server Configuration Manager (sau đó restart dịch vụ SQL Server).
   - Nếu dùng instance `SQLEXPRESS`: đổi URL thành `jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=QuanLyBanHang;encrypt=true;trustServerCertificate=true;`
3. **Chạy chương trình** (cần JDK 17 trở lên), chọn một trong các cách:
   - **IDE (NetBeans / IntelliJ / Eclipse):** mở thư mục này như một *Maven project*, chuột phải `Main.java` → Run.
   - **Dòng lệnh:** `mvn compile exec:java -Dexec.mainClass=qlbh.Main`
   - **Đóng gói file jar:** `mvn package` rồi chạy `java -jar target/QuanLyBanHang-1.0-jar-with-dependencies.jar`
   - **Không dùng Maven:** tải `mssql-jdbc-12.8.1.jre11.jar` từ trang Microsoft/GitHub, thêm vào Libraries của project rồi chạy `Main.java`.

## Cấu trúc mã nguồn (`src/main/java/qlbh`)
- `Main`, `MainFrame`: khởi động và cửa sổ chính (các tab).
- `Db`: kết nối SQL Server, hàm `query` / `update`.
- `Ui`, `Item`, `Refreshable`: tiện ích giao diện, kiểm tra dữ liệu nhập.
- `CrudPanel`: khung chung Thêm / Sửa / Xóa / Tìm kiếm / Làm mới.
- `SanPhamPanel`, `LoaiSanPhamPanel`, `KhachHangPanel`, `NhanVienPanel`: các form quản lý (kế thừa `CrudPanel`).
- `BanHangPanel`: lập hóa đơn (dùng transaction).
- `HoaDonPanel`: xem / xóa hóa đơn.

## Đưa code lên GitHub
```
git init
git add .
git commit -m "Phan mem quan ly ban hang"
git branch -M main
git remote add origin https://github.com/<ten-ban>/QuanLyBanHang.git
git push -u origin main
```
