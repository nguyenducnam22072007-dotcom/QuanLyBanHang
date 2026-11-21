-- Chạy file này trong SSMS (New Query -> Execute) MỘT LẦN để tạo CSDL + dữ liệu mẫu
IF DB_ID(N'QuanLyBanHang') IS NULL CREATE DATABASE QuanLyBanHang;
GO
USE QuanLyBanHang;
GO

CREATE TABLE LoaiSanPham (
    MaLoai     INT IDENTITY(1,1) PRIMARY KEY,
    TenLoai    NVARCHAR(100) NOT NULL UNIQUE,
    MoTa       NVARCHAR(255)
);

CREATE TABLE SanPham (
    MaSP       INT IDENTITY(1,1) PRIMARY KEY,
    TenSP      NVARCHAR(150) NOT NULL,
    MaLoai     INT NOT NULL,
    DonGia     DECIMAL(18,0) NOT NULL CHECK (DonGia >= 0),
    SoLuongTon INT NOT NULL DEFAULT 0 CHECK (SoLuongTon >= 0),
    DonViTinh  NVARCHAR(30),
    CONSTRAINT FK_SanPham_Loai FOREIGN KEY (MaLoai) REFERENCES LoaiSanPham(MaLoai)
);

CREATE TABLE KhachHang (
    MaKH        INT IDENTITY(1,1) PRIMARY KEY,
    HoTen       NVARCHAR(100) NOT NULL,
    GioiTinh    NVARCHAR(5) CHECK (GioiTinh IN (N'Nam', N'Nữ')),
    SoDienThoai VARCHAR(15) UNIQUE,
    Email       VARCHAR(100),
    DiaChi      NVARCHAR(255)
);

CREATE TABLE NhanVien (
    MaNV        INT IDENTITY(1,1) PRIMARY KEY,
    HoTen       NVARCHAR(100) NOT NULL,
    NgaySinh    DATE,
    SoDienThoai VARCHAR(15) UNIQUE,
    ChucVu      NVARCHAR(50),
    NgayVaoLam  DATE DEFAULT GETDATE()
);

CREATE TABLE HoaDon (
    MaHD      INT IDENTITY(1,1) PRIMARY KEY,
    NgayLap   DATETIME NOT NULL DEFAULT GETDATE(),
    MaKH      INT NOT NULL,
    MaNV      INT NOT NULL,
    TrangThai NVARCHAR(30) DEFAULT N'Đã thanh toán',
    CONSTRAINT FK_HoaDon_KH FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_HoaDon_NV FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
);

CREATE TABLE ChiTietHoaDon (
    MaHD      INT NOT NULL,
    MaSP      INT NOT NULL,
    SoLuong   INT NOT NULL CHECK (SoLuong > 0),
    DonGiaBan DECIMAL(18,0) NOT NULL CHECK (DonGiaBan >= 0),
    CONSTRAINT PK_CTHD PRIMARY KEY (MaHD, MaSP),
    CONSTRAINT FK_CTHD_HD FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_SP FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)
);
GO

-- Dữ liệu mẫu
INSERT INTO LoaiSanPham (TenLoai, MoTa) VALUES
(N'Điện thoại', N'Điện thoại di động'),
(N'Laptop', N'Máy tính xách tay'),
(N'Phụ kiện', N'Tai nghe, sạc, ốp lưng');

INSERT INTO SanPham (TenSP, MaLoai, DonGia, SoLuongTon, DonViTinh) VALUES
(N'iPhone 15', 1, 20000000, 30, N'Cái'),
(N'Samsung Galaxy S24', 1, 18000000, 25, N'Cái'),
(N'Laptop Dell XPS 13', 2, 28000000, 10, N'Cái'),
(N'Tai nghe Bluetooth', 3, 500000, 100, N'Cái');

INSERT INTO KhachHang (HoTen, GioiTinh, SoDienThoai, Email, DiaChi) VALUES
(N'Nguyễn Văn An', N'Nam', '0901234567', 'an@gmail.com', N'Hà Nội'),
(N'Trần Thị Bình', N'Nữ', '0912345678', 'binh@gmail.com', N'Hà Nội');

INSERT INTO NhanVien (HoTen, NgaySinh, SoDienThoai, ChucVu) VALUES
(N'Lê Minh Cường', '2000-05-10', '0987654321', N'Nhân viên bán hàng');

INSERT INTO HoaDon (MaKH, MaNV) VALUES (1, 1), (2, 1);

INSERT INTO ChiTietHoaDon (MaHD, MaSP, SoLuong, DonGiaBan) VALUES
(1, 1, 1, 20000000),
(1, 4, 2, 500000),
(2, 3, 1, 28000000);
GO

ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = '123456', CHECK_POLICY = OFF;


USE QuanLyBanHang;
GO

INSERT INTO LoaiSanPham (TenLoai, MoTa)
SELECT v.TenLoai, v.MoTa
FROM (VALUES
    (N'Đồ gia dụng',        N'Dụng cụ và thiết bị dùng trong gia đình'),
    (N'Đồ điện nhà bếp',    N'Thiết bị điện dùng để nấu nướng'),
    (N'Thực phẩm',          N'Lương thực, gia vị, đồ khô'),
    (N'Đồ uống',            N'Nước giải khát, sữa, cà phê'),
    (N'Bánh kẹo & snack',   N'Bánh, kẹo, đồ ăn vặt')
) v(TenLoai, MoTa)
WHERE NOT EXISTS (SELECT 1 FROM LoaiSanPham l WHERE l.TenLoai = v.TenLoai);
GO

INSERT INTO SanPham (TenSP, MaLoai, DonGia, SoLuongTon, DonViTinh)
SELECT v.TenSP, l.MaLoai, v.DonGia, v.SoLuongTon, v.DonViTinh
FROM (VALUES
    (N'Quạt đứng Senko 5 cánh',            N'Đồ gia dụng',      1150000, 20,  N'Cái'),
    (N'Bàn ủi hơi nước Philips',           N'Đồ gia dụng',       520000, 25,  N'Cái'),
    (N'Máy hút bụi cầm tay',               N'Đồ gia dụng',      1990000, 12,  N'Cái'),
    (N'Máy lọc không khí Xiaomi',          N'Đồ gia dụng',      3200000,  8,  N'Cái'),
    (N'Bộ nồi inox 3 chiếc',               N'Đồ gia dụng',       650000, 30,  N'Bộ'),
    (N'Bộ dao nhà bếp 5 món',              N'Đồ gia dụng',       320000, 40,  N'Bộ'),
    (N'Cây lau nhà xoay 360 độ',           N'Đồ gia dụng',       180000, 35,  N'Cái'),
    (N'Hộp đựng thực phẩm thủy tinh',      N'Đồ gia dụng',       120000, 60,  N'Cái'),

    (N'Nồi cơm điện Sharp 1.8L',           N'Đồ điện nhà bếp',  1290000, 22,  N'Cái'),
    (N'Ấm đun siêu tốc 1.7L',              N'Đồ điện nhà bếp',   350000, 45,  N'Cái'),
    (N'Máy xay sinh tố Panasonic',         N'Đồ điện nhà bếp',   790000, 18,  N'Cái'),
    (N'Lò vi sóng Samsung 23L',            N'Đồ điện nhà bếp',  2450000, 10,  N'Cái'),
    (N'Nồi chiên không dầu 4L',            N'Đồ điện nhà bếp',  1850000, 15,  N'Cái'),
    (N'Bếp từ đơn Sunhouse',               N'Đồ điện nhà bếp',   890000, 20,  N'Cái'),

    (N'Gạo ST25 túi 5kg',                  N'Thực phẩm',         135000, 80,  N'Túi'),
    (N'Mì gói Hảo Hảo thùng 30 gói',       N'Thực phẩm',         115000, 50,  N'Thùng'),
    (N'Dầu ăn Simply 1 lít',               N'Thực phẩm',          62000, 70,  N'Chai'),
    (N'Nước mắm Nam Ngư 500ml',            N'Thực phẩm',          38000, 90,  N'Chai'),
    (N'Đường tinh luyện 1kg',              N'Thực phẩm',          27000, 100, N'Gói'),
    (N'Trứng gà hộp 10 quả',               N'Thực phẩm',          35000, 60,  N'Hộp'),
    (N'Muối i-ốt 500g',                    N'Thực phẩm',           8000, 120, N'Gói'),

    (N'Nước suối Aquafina thùng 24 chai',  N'Đồ uống',            95000, 40,  N'Thùng'),
    (N'Coca-Cola thùng 24 lon',            N'Đồ uống',           210000, 35,  N'Thùng'),
    (N'Sữa tươi Vinamilk 1 lít',           N'Đồ uống',            34000, 75,  N'Hộp'),
    (N'Cà phê G7 3in1 hộp 21 gói',         N'Đồ uống',            55000, 65,  N'Hộp'),
    (N'Trà xanh không độ chai 455ml',      N'Đồ uống',            10000, 150, N'Chai'),
    (N'Nước cam ép Twister 1 lít',         N'Đồ uống',            28000, 55,  N'Chai'),

    (N'Bánh Oreo 133g',                    N'Bánh kẹo & snack',   18000, 100, N'Gói'),
    (N'Snack khoai tây Lay''s 54g',        N'Bánh kẹo & snack',   12000, 130, N'Gói'),
    (N'Kẹo Alpenliebe 120g',               N'Bánh kẹo & snack',   25000, 90,  N'Gói'),
    (N'Bánh quy bơ Cosy hộp thiếc',        N'Bánh kẹo & snack',   89000, 30,  N'Hộp'),
    (N'Socola KitKat 4 thanh',             N'Bánh kẹo & snack',   32000, 70,  N'Gói')
) v(TenSP, TenLoai, DonGia, SoLuongTon, DonViTinh)
JOIN LoaiSanPham l ON l.TenLoai = v.TenLoai
WHERE NOT EXISTS (SELECT 1 FROM SanPham s WHERE s.TenSP = v.TenSP);
GO

SELECT l.TenLoai, COUNT(*) AS SoSanPham
FROM SanPham sp JOIN LoaiSanPham l ON sp.MaLoai = l.MaLoai
GROUP BY l.TenLoai;
GO


USE QuanLyBanHang;
GO

IF OBJECT_ID(N'TaiKhoan', N'U') IS NULL
CREATE TABLE TaiKhoan (
    TenDangNhap VARCHAR(50)   PRIMARY KEY,
    MatKhau     VARCHAR(64)   NOT NULL,  
    HoTen       NVARCHAR(100) NOT NULL
);
GO

IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE TenDangNhap = 'nguyenducnam')
INSERT INTO TaiKhoan (TenDangNhap, MatKhau, HoTen)
VALUES ('nguyenducnam', LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'), 2)), N'Quản trị viên');
GO


SELECT TenDangNhap, HoTen FROM TaiKhoan;
GO