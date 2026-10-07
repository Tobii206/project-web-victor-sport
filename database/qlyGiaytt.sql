CREATE DATABASE DATN_VictorSport;
GO

USE DATN_VictorSport;
GO

CREATE TABLE xuat_xu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_xuat_xu AS 'XX' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_xuat_xu NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE thuong_hieu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_thuong_hieu AS 'TH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_thuong_hieu NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE mau_sac (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_mau_sac AS 'MS' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_mau_sac NVARCHAR(255) NOT NULL,
    ma_mau_hex VARCHAR(7),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE kich_thuoc (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_kich_thuoc AS 'KT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_kich_thuoc NVARCHAR(50) NOT NULL,
    gia_tri_kich_thuoc DECIMAL(4,1),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE vi_tri_thi_dau (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_vi_tri AS 'VT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_vi_tri NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE phong_cach_choi (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phong_cach AS 'PC' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_phong_cach NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE co_giay (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_co_giay AS 'CG' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_co_giay NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE form_chan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_form_chan AS 'FC' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_form_chan NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE chat_lieu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_chat_lieu AS 'CL' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_chat_lieu NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE quyen_han (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_quyen_han AS 'QH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_quyen_han NVARCHAR(255) NOT NULL,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE nhan_vien (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_quyen_han INT NOT NULL,
    ma_nhan_vien AS 'NV' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_nhan_vien NVARCHAR(100) NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    so_dien_thoai VARCHAR(10),
    anh_nhan_vien NVARCHAR(MAX),
    ngay_sinh DATE,
    ghi_chu NVARCHAR(255),
    thanh_pho NVARCHAR(100),
    quan NVARCHAR(100),
    phuong NVARCHAR(100),
    dia_chi_cu_the NVARCHAR(255),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    nguoi_tao INT,
    ngay_cap_nhat DATETIME2,
    nguoi_cap_nhat INT,
    FOREIGN KEY (id_quyen_han) REFERENCES quyen_han(id)
);

CREATE TABLE khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang AS 'KH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_khach_hang NVARCHAR(100) NOT NULL,
    ten_tai_khoan VARCHAR(50) NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    so_dien_thoai VARCHAR(10),
    gioi_tinh BIT,
    ngay_sinh DATE,
    anh_dai_dien VARCHAR(255),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    nguoi_tao INT,
    ngay_cap_nhat DATETIME2,
    nguoi_cap_nhat INT
);

CREATE TABLE dia_chi_khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    ma_dia_chi AS 'DC' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_dia_chi NVARCHAR(100) NOT NULL,
    thanh_pho NVARCHAR(100),
    quan NVARCHAR(100),
    phuong NVARCHAR(100),
    dia_chi_cu_the NVARCHAR(255),
    mac_dinh BIT DEFAULT 0,
    xoa_mem BIT DEFAULT 0,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);

CREATE TABLE san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_thuong_hieu INT NOT NULL,
    id_xuat_xu INT,
    id_vi_tri_thi_dau INT,
    id_phong_cach_choi INT,
    id_co_giay INT,
    id_chat_lieu INT,
    ma_san_pham AS 'SP' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_san_pham NVARCHAR(255) NOT NULL,
    mo_ta_ngan NVARCHAR(500),
    mo_ta_chi_tiet NVARCHAR(MAX),
    trang_thai_kinh_doanh BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    nguoi_tao INT,
    ngay_cap_nhat DATETIME2,
    nguoi_cap_nhat INT,
    FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id),
    FOREIGN KEY (id_xuat_xu) REFERENCES xuat_xu(id),
    FOREIGN KEY (id_vi_tri_thi_dau) REFERENCES vi_tri_thi_dau(id),
    FOREIGN KEY (id_phong_cach_choi) REFERENCES phong_cach_choi(id),
    FOREIGN KEY (id_co_giay) REFERENCES co_giay(id),
    FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id)
);

CREATE TABLE chi_tiet_san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham INT NOT NULL,
    id_mau_sac INT NOT NULL,
    id_kich_thuoc INT NOT NULL,
    id_form_chan INT NOT NULL,
    ma_chi_tiet_san_pham AS 'CTSP' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    so_luong INT DEFAULT 0,
    gia_niem_yet DECIMAL(18,2) NOT NULL,
    gia_ban DECIMAL(18,2),
    trang_thai BIT DEFAULT 1,
    ghi_chu NVARCHAR(255),
    xoa_mem BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    nguoi_tao INT,
    ngay_cap_nhat DATETIME2,
    nguoi_cap_nhat INT,
    FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id),
    FOREIGN KEY (id_kich_thuoc) REFERENCES kich_thuoc(id),
    FOREIGN KEY (id_form_chan) REFERENCES form_chan(id)
);

CREATE TABLE anh_chi_tiet_san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_chi_tiet_san_pham INT NOT NULL,
    duong_dan_anh VARCHAR(255) NOT NULL,
    la_anh_dai_dien BIT DEFAULT 0,
    mo_ta NVARCHAR(255),
    xoa_mem BIT DEFAULT 0,
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);

CREATE TABLE gio_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    ma_gio_hang AS 'GH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    UNIQUE (id_khach_hang)
);

CREATE TABLE chi_tiet_gio_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_gio_hang INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    so_luong INT NOT NULL,
    don_gia DECIMAL(18,2),
    xoa_mem BIT DEFAULT 0,
    FOREIGN KEY (id_gio_hang) REFERENCES gio_hang(id),
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    UNIQUE (id_gio_hang, id_chi_tiet_san_pham)
);

CREATE TABLE dot_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_dot_giam_gia AS 'DGG' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_dot_giam_gia NVARCHAR(255) NOT NULL,
    loai_giam_gia BIT DEFAULT 0,
    gia_tri_giam_gia DECIMAL(18,2) NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    muc_uu_tien INT DEFAULT 0,
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE chi_tiet_dot_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_dot_giam_gia INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    so_luong_ap_dung INT,
    gia_tri_giam_rieng DECIMAL(18,2),
    so_tien_giam_toi_da_rieng DECIMAL(18,2),
    trang_thai BIT DEFAULT 1,
    ghi_chu NVARCHAR(255),
    xoa_mem BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    nguoi_tao INT,
    ngay_cap_nhat DATETIME2,
    nguoi_cap_nhat INT,
    FOREIGN KEY (id_dot_giam_gia) REFERENCES dot_giam_gia(id),
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);

CREATE TABLE phieu_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_giam_gia AS 'PGG' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_phieu_giam_gia NVARCHAR(255) NOT NULL,
    loai_phieu_giam_gia BIT DEFAULT 0,
    gia_tri_giam_gia DECIMAL(18,2) NOT NULL,
    so_tien_giam_toi_da DECIMAL(18,2),
    hoa_don_toi_thieu DECIMAL(18,2),
    so_luong_su_dung INT NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    trang_thai BIT DEFAULT 1,
    mo_ta NVARCHAR(255),
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT,
    id_nhan_vien INT,
    id_phieu_giam_gia INT,
    ma_hoa_don AS 'HD' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    loai_don TINYINT DEFAULT 0,
    phi_van_chuyen DECIMAL(18,2) DEFAULT 0,
    tong_tien DECIMAL(18,2) NOT NULL,
    tong_tien_sau_giam DECIMAL(18,2) NOT NULL,
    tong_tien_giam AS CAST((tong_tien - tong_tien_sau_giam) AS DECIMAL(18,2)) PERSISTED,
    ten_khach_hang NVARCHAR(100) NOT NULL,
    dia_chi_khach_hang NVARCHAR(255) NOT NULL,
    so_dien_thoai_khach_hang VARCHAR(10) NOT NULL,
    email_khach_hang VARCHAR(100),
    trang_thai_hien_tai INT DEFAULT 1,
    ngay_tao DATETIME2 DEFAULT SYSDATETIME(),
    ngay_thanh_toan DATETIME2,
    ghi_chu NVARCHAR(255),
    xoa_mem BIT DEFAULT 0,
    nguoi_tao INT,
    ngay_cap_nhat DATETIME2,
    nguoi_cap_nhat INT,
    da_hoan_phi BIT,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id),
    FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id)
);

CREATE TABLE hoa_don_chi_tiet (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    ma_hoa_don_chi_tiet AS 'HDCT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    so_luong INT NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    thanh_tien AS CAST((so_luong * don_gia) AS DECIMAL(18,2)) PERSISTED,
    ghi_chu NVARCHAR(255),
    xoa_mem BIT DEFAULT 0,
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);

CREATE TABLE phuong_thuc_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phuong_thuc_thanh_toan AS 'PTTT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_phuong_thuc_thanh_toan NVARCHAR(255) NOT NULL,
    nha_cung_cap NVARCHAR(50),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE giao_dich_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT NOT NULL,
    id_phuong_thuc_thanh_toan INT NOT NULL,
    ma_giao_dich_thanh_toan AS 'GDTT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    so_tien DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'khoi_tao',
    ma_yeu_cau NVARCHAR(100),
    ma_giao_dich_ngoai NVARCHAR(100),
    ma_tham_chieu NVARCHAR(100),
    duong_dan_thanh_toan NVARCHAR(MAX),
    du_lieu_qr NVARCHAR(MAX),
    thoi_gian_het_han DATETIME2,
    du_lieu_phan_hoi NVARCHAR(MAX),
    thoi_gian_tao DATETIME2 DEFAULT SYSDATETIME(),
    thoi_gian_cap_nhat DATETIME2,
    nguoi_cap_nhat INT,
    xoa_mem BIT DEFAULT 0,
    ghi_chu NVARCHAR(255),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES phuong_thuc_thanh_toan(id),
    FOREIGN KEY (nguoi_cap_nhat) REFERENCES nhan_vien(id)
);

CREATE TABLE lich_su_hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT NOT NULL,
    trang_thai INT NOT NULL,
    thoi_gian DATETIME2 DEFAULT SYSDATETIME(),
    ghi_chu NVARCHAR(255),
    xoa_mem BIT DEFAULT 0,
    nguoi_cap_nhat INT,
    nguoi_thuc_hien INT,
    loai_nguoi_thuc_hien VARCHAR(20),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    FOREIGN KEY (nguoi_cap_nhat) REFERENCES nhan_vien(id)
);

CREATE TABLE chuc_nang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_chuc_nang VARCHAR(50) NOT NULL,
    ten_chuc_nang NVARCHAR(255) NOT NULL,
    mo_ta NVARCHAR(255),
    trang_thai BIT DEFAULT 1,
    xoa_mem BIT DEFAULT 0
);

CREATE TABLE quyen_han_chuc_nang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_quyen_han INT NOT NULL,
    id_chuc_nang INT NOT NULL,
    xoa_mem BIT DEFAULT 0,
    FOREIGN KEY (id_quyen_han) REFERENCES quyen_han(id),
    FOREIGN KEY (id_chuc_nang) REFERENCES chuc_nang(id),
    UNIQUE (id_quyen_han, id_chuc_nang)
);
