-- Tạo bảng tài khoản đăng nhập. Chạy trong SSMS (có thể chạy lại, không bị trùng).
USE QuanLyBanHang;
GO

IF OBJECT_ID(N'TaiKhoan', N'U') IS NULL
CREATE TABLE TaiKhoan (
    TenDangNhap VARCHAR(50)   PRIMARY KEY,
    MatKhau     VARCHAR(64)   NOT NULL,   -- mật khẩu đã mã hóa SHA-256 (64 ký tự hex)
    HoTen       NVARCHAR(100) NOT NULL
);
GO

-- Tài khoản mặc định: admin / 123456
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE TenDangNhap = 'admin')
INSERT INTO TaiKhoan (TenDangNhap, MatKhau, HoTen)
VALUES ('admin', LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'), 2)), N'Quản trị viên');
GO

-- Muốn thêm tài khoản khác, sửa rồi chạy mẫu sau:
-- INSERT INTO TaiKhoan (TenDangNhap, MatKhau, HoTen)
-- VALUES ('nv01', LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'matkhau_cua_ban'), 2)), N'Lê Minh Cường');

-- Muốn đổi mật khẩu admin:
-- UPDATE TaiKhoan SET MatKhau = LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'matkhaumoi'), 2))
-- WHERE TenDangNhap = 'admin';

SELECT TenDangNhap, HoTen FROM TaiKhoan;
GO
