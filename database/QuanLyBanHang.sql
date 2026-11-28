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
