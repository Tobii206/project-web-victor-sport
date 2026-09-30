IF DB_ID(N'DATN_SevenStrike') IS NULL
BEGIN
    CREATE DATABASE DATN_SevenStrike;
END
GO

USE DATN_SevenStrike;
GO

SET NOCOUNT ON;
GO

DROP TABLE IF EXISTS dbo.quyen_han_chuc_nang;
DROP TABLE IF EXISTS dbo.chuc_nang;
DROP TABLE IF EXISTS dbo.lich_su_hoa_don;
DROP TABLE IF EXISTS dbo.giao_dich_thanh_toan;
DROP TABLE IF EXISTS dbo.phuong_thuc_thanh_toan;
DROP TABLE IF EXISTS dbo.hoa_don_chi_tiet;
DROP TABLE IF EXISTS dbo.hoa_don;
DROP TABLE IF EXISTS dbo.phieu_giam_gia;
DROP TABLE IF EXISTS dbo.chi_tiet_dot_giam_gia;
DROP TABLE IF EXISTS dbo.dot_giam_gia;
DROP TABLE IF EXISTS dbo.chi_tiet_gio_hang;
DROP TABLE IF EXISTS dbo.gio_hang;
DROP TABLE IF EXISTS dbo.anh_chi_tiet_san_pham;
DROP TABLE IF EXISTS dbo.chi_tiet_san_pham;
DROP TABLE IF EXISTS dbo.san_pham;
DROP TABLE IF EXISTS dbo.dia_chi_khach_hang;
DROP TABLE IF EXISTS dbo.khach_hang;
DROP TABLE IF EXISTS dbo.nhan_vien;
DROP TABLE IF EXISTS dbo.quyen_han;
DROP TABLE IF EXISTS dbo.chat_lieu;
DROP TABLE IF EXISTS dbo.form_chan;
DROP TABLE IF EXISTS dbo.co_giay;
DROP TABLE IF EXISTS dbo.phong_cach_choi;
DROP TABLE IF EXISTS dbo.vi_tri_thi_dau;
DROP TABLE IF EXISTS dbo.kich_thuoc;
DROP TABLE IF EXISTS dbo.mau_sac;
DROP TABLE IF EXISTS dbo.thuong_hieu;
DROP TABLE IF EXISTS dbo.xuat_xu;
GO

CREATE TABLE dbo.xuat_xu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_xuat_xu AS 'XX' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_xuat_xu NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.thuong_hieu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_thuong_hieu AS 'TH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_thuong_hieu NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.mau_sac (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_mau_sac AS 'MS' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_mau_sac NVARCHAR(255) NOT NULL,
    ma_mau_hex VARCHAR(7) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT CK_mau_sac_hex CHECK (ma_mau_hex IS NULL OR (LEN(ma_mau_hex) = 7 AND LEFT(ma_mau_hex, 1) = '#'))
);

CREATE TABLE dbo.kich_thuoc (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_kich_thuoc AS 'KT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_kich_thuoc NVARCHAR(50) NOT NULL,
    gia_tri_kich_thuoc DECIMAL(4,1) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT CK_kich_thuoc_range CHECK (gia_tri_kich_thuoc IS NULL OR gia_tri_kich_thuoc BETWEEN 35.0 AND 48.0)
);

CREATE TABLE dbo.vi_tri_thi_dau (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_vi_tri AS 'VT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_vi_tri NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.phong_cach_choi (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phong_cach AS 'PC' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_phong_cach NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.co_giay (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_co_giay AS 'CG' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_co_giay NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.form_chan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_form_chan AS 'FC' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_form_chan NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.chat_lieu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_chat_lieu AS 'CL' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_chat_lieu NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.quyen_han (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_quyen_han AS 'QH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_quyen_han NVARCHAR(255) NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.nhan_vien (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_quyen_han INT NOT NULL,
    ma_nhan_vien AS 'NV' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_nhan_vien NVARCHAR(100) NOT NULL,
    ten_tai_khoan VARCHAR(50) NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    email VARCHAR(100) NULL,
    so_dien_thoai VARCHAR(10) NULL,
    anh_nhan_vien NVARCHAR(MAX) NULL,
    ngay_sinh DATE NULL,
    ghi_chu NVARCHAR(255) NULL,
    thanh_pho NVARCHAR(100) NULL,
    quan NVARCHAR(100) NULL,
    phuong NVARCHAR(100) NULL,
    dia_chi_cu_the NVARCHAR(255) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    nguoi_tao INT NULL,
    ngay_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    CONSTRAINT FK_nv_qh FOREIGN KEY (id_quyen_han) REFERENCES dbo.quyen_han(id),
    CONSTRAINT CK_nv_sdt CHECK (so_dien_thoai IS NULL OR (LEN(so_dien_thoai) = 10 AND so_dien_thoai NOT LIKE '%[^0-9]%'))
);

CREATE TABLE dbo.khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang AS 'KH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_khach_hang NVARCHAR(100) NOT NULL,
    ten_tai_khoan VARCHAR(50) NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    email VARCHAR(100) NULL,
    so_dien_thoai VARCHAR(10) NULL,
    gioi_tinh BIT NULL,
    ngay_sinh DATE NULL,
    anh_dai_dien VARCHAR(255) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    nguoi_tao INT NULL,
    ngay_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    CONSTRAINT CK_kh_sdt CHECK (so_dien_thoai IS NULL OR (LEN(so_dien_thoai) = 10 AND so_dien_thoai NOT LIKE '%[^0-9]%'))
);

CREATE TABLE dbo.dia_chi_khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    ma_dia_chi AS 'DC' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_dia_chi NVARCHAR(100) NOT NULL,
    thanh_pho NVARCHAR(100) NULL,
    quan NVARCHAR(100) NULL,
    phuong NVARCHAR(100) NULL,
    dia_chi_cu_the NVARCHAR(255) NULL,
    mac_dinh BIT NOT NULL DEFAULT 0,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_dckh_kh FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id)
);

CREATE TABLE dbo.san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_thuong_hieu INT NOT NULL,
    id_xuat_xu INT NULL,
    id_vi_tri_thi_dau INT NULL,
    id_phong_cach_choi INT NULL,
    id_co_giay INT NULL,
    id_chat_lieu INT NULL,
    ma_san_pham AS 'SP' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_san_pham NVARCHAR(255) NOT NULL,
    mo_ta_ngan NVARCHAR(500) NULL,
    mo_ta_chi_tiet NVARCHAR(MAX) NULL,
    trang_thai_kinh_doanh BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    nguoi_tao INT NULL,
    ngay_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    CONSTRAINT FK_sp_th FOREIGN KEY (id_thuong_hieu) REFERENCES dbo.thuong_hieu(id),
    CONSTRAINT FK_sp_xx FOREIGN KEY (id_xuat_xu) REFERENCES dbo.xuat_xu(id),
    CONSTRAINT FK_sp_vt FOREIGN KEY (id_vi_tri_thi_dau) REFERENCES dbo.vi_tri_thi_dau(id),
    CONSTRAINT FK_sp_pc FOREIGN KEY (id_phong_cach_choi) REFERENCES dbo.phong_cach_choi(id),
    CONSTRAINT FK_sp_cg FOREIGN KEY (id_co_giay) REFERENCES dbo.co_giay(id),
    CONSTRAINT FK_sp_cl FOREIGN KEY (id_chat_lieu) REFERENCES dbo.chat_lieu(id)
);

CREATE TABLE dbo.chi_tiet_san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham INT NOT NULL,
    id_mau_sac INT NOT NULL,
    id_kich_thuoc INT NOT NULL,
    id_form_chan INT NOT NULL,
    ma_chi_tiet_san_pham AS 'CTSP' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    so_luong INT NOT NULL DEFAULT 0,
    gia_niem_yet DECIMAL(18,2) NOT NULL,
    gia_ban DECIMAL(18,2) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    ghi_chu NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    nguoi_tao INT NULL,
    ngay_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    CONSTRAINT FK_ctsp_sp FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id),
    CONSTRAINT FK_ctsp_ms FOREIGN KEY (id_mau_sac) REFERENCES dbo.mau_sac(id),
    CONSTRAINT FK_ctsp_kt FOREIGN KEY (id_kich_thuoc) REFERENCES dbo.kich_thuoc(id),
    CONSTRAINT FK_ctsp_fc FOREIGN KEY (id_form_chan) REFERENCES dbo.form_chan(id),
    CONSTRAINT CK_ctsp_so_luong CHECK (so_luong >= 0),
    CONSTRAINT CK_ctsp_gia CHECK (gia_niem_yet >= 0 AND (gia_ban IS NULL OR gia_ban >= 0))
);

CREATE TABLE dbo.anh_chi_tiet_san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_chi_tiet_san_pham INT NOT NULL,
    duong_dan_anh VARCHAR(255) NOT NULL,
    la_anh_dai_dien BIT NOT NULL DEFAULT 0,
    mo_ta NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_anh_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES dbo.chi_tiet_san_pham(id)
);

CREATE TABLE dbo.gio_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NOT NULL,
    ma_gio_hang AS 'GH' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_gh_kh FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id)
);

CREATE TABLE dbo.chi_tiet_gio_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_gio_hang INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    so_luong INT NOT NULL,
    don_gia DECIMAL(18,2) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_ctgh_gh FOREIGN KEY (id_gio_hang) REFERENCES dbo.gio_hang(id),
    CONSTRAINT FK_ctgh_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES dbo.chi_tiet_san_pham(id),
    CONSTRAINT CK_ctgh_so_luong CHECK (so_luong > 0),
    CONSTRAINT CK_ctgh_don_gia CHECK (don_gia IS NULL OR don_gia >= 0)
);

CREATE TABLE dbo.dot_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_dot_giam_gia AS 'DGG' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_dot_giam_gia NVARCHAR(255) NOT NULL,
    loai_giam_gia BIT NOT NULL DEFAULT 0,
    gia_tri_giam_gia DECIMAL(18,2) NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    muc_uu_tien INT NOT NULL DEFAULT 0,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT CK_dgg_ngay CHECK (ngay_ket_thuc >= ngay_bat_dau),
    CONSTRAINT CK_dgg_gia_tri CHECK ((loai_giam_gia = 0 AND gia_tri_giam_gia BETWEEN 0 AND 100) OR (loai_giam_gia = 1 AND gia_tri_giam_gia >= 0))
);

CREATE TABLE dbo.chi_tiet_dot_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_dot_giam_gia INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    so_luong_ap_dung INT NULL,
    gia_tri_giam_rieng DECIMAL(18,2) NULL,
    so_tien_giam_toi_da_rieng DECIMAL(18,2) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    ghi_chu NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    nguoi_tao INT NULL,
    ngay_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    CONSTRAINT FK_ctdgg_dgg FOREIGN KEY (id_dot_giam_gia) REFERENCES dbo.dot_giam_gia(id),
    CONSTRAINT FK_ctdgg_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES dbo.chi_tiet_san_pham(id)
);

CREATE TABLE dbo.phieu_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_giam_gia AS 'PGG' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_phieu_giam_gia NVARCHAR(255) NOT NULL,
    loai_phieu_giam_gia BIT NOT NULL DEFAULT 0,
    gia_tri_giam_gia DECIMAL(18,2) NOT NULL,
    so_tien_giam_toi_da DECIMAL(18,2) NULL,
    hoa_don_toi_thieu DECIMAL(18,2) NULL,
    so_luong_su_dung INT NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    mo_ta NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT CK_pgg_ngay CHECK (ngay_ket_thuc >= ngay_bat_dau),
    CONSTRAINT CK_pgg_gia_tri CHECK ((loai_phieu_giam_gia = 0 AND gia_tri_giam_gia BETWEEN 0 AND 100) OR (loai_phieu_giam_gia = 1 AND gia_tri_giam_gia >= 0))
);

CREATE TABLE dbo.hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang INT NULL,
    id_nhan_vien INT NULL,
    id_phieu_giam_gia INT NULL,
    ma_hoa_don AS 'HD' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    loai_don TINYINT NOT NULL DEFAULT 0,
    phi_van_chuyen DECIMAL(18,2) NOT NULL DEFAULT 0,
    tong_tien DECIMAL(18,2) NOT NULL,
    tong_tien_sau_giam DECIMAL(18,2) NOT NULL,
    tong_tien_giam AS CAST((tong_tien - tong_tien_sau_giam) AS DECIMAL(18,2)) PERSISTED,
    ten_khach_hang NVARCHAR(100) NOT NULL,
    dia_chi_khach_hang NVARCHAR(255) NOT NULL,
    so_dien_thoai_khach_hang VARCHAR(10) NOT NULL,
    email_khach_hang VARCHAR(100) NULL,
    trang_thai_hien_tai INT NOT NULL DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngay_thanh_toan DATETIME2 NULL,
    ghi_chu NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    nguoi_tao INT NULL,
    ngay_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    da_hoan_phi BIT NULL,
    CONSTRAINT FK_hd_kh FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id),
    CONSTRAINT FK_hd_nv FOREIGN KEY (id_nhan_vien) REFERENCES dbo.nhan_vien(id),
    CONSTRAINT FK_hd_pgg FOREIGN KEY (id_phieu_giam_gia) REFERENCES dbo.phieu_giam_gia(id),
    CONSTRAINT CK_hd_loai_don CHECK (loai_don IN (0,1,2)),
    CONSTRAINT CK_hd_trang_thai CHECK (trang_thai_hien_tai IN (1,2,3,4,5,6,7)),
    CONSTRAINT CK_hd_tien CHECK (phi_van_chuyen >= 0 AND tong_tien >= 0 AND tong_tien_sau_giam >= 0 AND tong_tien_sau_giam <= tong_tien),
    CONSTRAINT CK_hd_sdt CHECK (LEN(so_dien_thoai_khach_hang) = 10 AND so_dien_thoai_khach_hang NOT LIKE '%[^0-9]%')
);

CREATE TABLE dbo.hoa_don_chi_tiet (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT NOT NULL,
    id_chi_tiet_san_pham INT NOT NULL,
    ma_hoa_don_chi_tiet AS 'HDCT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    so_luong INT NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    thanh_tien AS CAST((so_luong * don_gia) AS DECIMAL(18,2)) PERSISTED,
    ghi_chu NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_hdct_hd FOREIGN KEY (id_hoa_don) REFERENCES dbo.hoa_don(id),
    CONSTRAINT FK_hdct_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES dbo.chi_tiet_san_pham(id),
    CONSTRAINT CK_hdct_so_luong CHECK (so_luong > 0),
    CONSTRAINT CK_hdct_don_gia CHECK (don_gia >= 0)
);

CREATE TABLE dbo.phuong_thuc_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phuong_thuc_thanh_toan AS 'PTTT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    ten_phuong_thuc_thanh_toan NVARCHAR(255) NOT NULL,
    nha_cung_cap NVARCHAR(50) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.giao_dich_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT NOT NULL,
    id_phuong_thuc_thanh_toan INT NOT NULL,
    ma_giao_dich_thanh_toan AS 'GDTT' + RIGHT('00000' + CAST(id AS VARCHAR(5)), 5) PERSISTED,
    so_tien DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) NOT NULL DEFAULT N'khoi_tao',
    ma_yeu_cau NVARCHAR(100) NULL,
    ma_giao_dich_ngoai NVARCHAR(100) NULL,
    ma_tham_chieu NVARCHAR(100) NULL,
    duong_dan_thanh_toan NVARCHAR(MAX) NULL,
    du_lieu_qr NVARCHAR(MAX) NULL,
    thoi_gian_het_han DATETIME2 NULL,
    du_lieu_phan_hoi NVARCHAR(MAX) NULL,
    thoi_gian_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    thoi_gian_cap_nhat DATETIME2 NULL,
    nguoi_cap_nhat INT NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    ghi_chu NVARCHAR(255) NULL,
    CONSTRAINT FK_gdtt_hd FOREIGN KEY (id_hoa_don) REFERENCES dbo.hoa_don(id),
    CONSTRAINT FK_gdtt_pttt FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES dbo.phuong_thuc_thanh_toan(id),
    CONSTRAINT FK_gdtt_nv FOREIGN KEY (nguoi_cap_nhat) REFERENCES dbo.nhan_vien(id),
    CONSTRAINT CK_gdtt_so_tien CHECK (so_tien > 0)
);

CREATE TABLE dbo.lich_su_hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT NOT NULL,
    trang_thai INT NOT NULL,
    thoi_gian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ghi_chu NVARCHAR(255) NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    nguoi_cap_nhat INT NULL,
    nguoi_thuc_hien INT NULL,
    loai_nguoi_thuc_hien VARCHAR(20) NULL,
    CONSTRAINT FK_lshd_hd FOREIGN KEY (id_hoa_don) REFERENCES dbo.hoa_don(id),
    CONSTRAINT FK_lshd_nv FOREIGN KEY (nguoi_cap_nhat) REFERENCES dbo.nhan_vien(id),
    CONSTRAINT CK_lshd_trang_thai CHECK (trang_thai IN (1,2,3,4,5,6,7)),
    CONSTRAINT CK_lshd_loai_nguoi CHECK (loai_nguoi_thuc_hien IS NULL OR loai_nguoi_thuc_hien IN ('KHACH_HANG', 'NHAN_VIEN', 'HE_THONG'))
);

CREATE TABLE dbo.chuc_nang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_chuc_nang VARCHAR(50) NOT NULL,
    ten_chuc_nang NVARCHAR(255) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    trang_thai BIT NOT NULL DEFAULT 1,
    xoa_mem BIT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.quyen_han_chuc_nang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_quyen_han INT NOT NULL,
    id_chuc_nang INT NOT NULL,
    xoa_mem BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_qhcn_qh FOREIGN KEY (id_quyen_han) REFERENCES dbo.quyen_han(id),
    CONSTRAINT FK_qhcn_cn FOREIGN KEY (id_chuc_nang) REFERENCES dbo.chuc_nang(id)
);
GO

CREATE UNIQUE INDEX UX_nv_ten_tai_khoan_alive ON dbo.nhan_vien(ten_tai_khoan) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_nv_email_alive ON dbo.nhan_vien(email) WHERE xoa_mem = 0 AND email IS NOT NULL;
CREATE UNIQUE INDEX UX_kh_ten_tai_khoan_alive ON dbo.khach_hang(ten_tai_khoan) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_kh_email_alive ON dbo.khach_hang(email) WHERE xoa_mem = 0 AND email IS NOT NULL;
CREATE UNIQUE INDEX UX_dckh_mac_dinh_alive ON dbo.dia_chi_khach_hang(id_khach_hang) WHERE mac_dinh = 1 AND xoa_mem = 0;
CREATE UNIQUE INDEX UX_ctsp_variant_alive ON dbo.chi_tiet_san_pham(id_san_pham, id_mau_sac, id_kich_thuoc, id_form_chan) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_anh_ctsp_dai_dien_alive ON dbo.anh_chi_tiet_san_pham(id_chi_tiet_san_pham) WHERE la_anh_dai_dien = 1 AND xoa_mem = 0;
CREATE UNIQUE INDEX UX_gh_kh_alive ON dbo.gio_hang(id_khach_hang) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_ctgh_item_alive ON dbo.chi_tiet_gio_hang(id_gio_hang, id_chi_tiet_san_pham) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_ctdgg_alive ON dbo.chi_tiet_dot_giam_gia(id_dot_giam_gia, id_chi_tiet_san_pham) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_chuc_nang_ma_alive ON dbo.chuc_nang(ma_chuc_nang) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_qhcn_alive ON dbo.quyen_han_chuc_nang(id_quyen_han, id_chuc_nang) WHERE xoa_mem = 0;

CREATE INDEX IX_hd_khach_hang_alive ON dbo.hoa_don(id_khach_hang, ngay_tao DESC) WHERE xoa_mem = 0;
CREATE INDEX IX_hd_nhan_vien_alive ON dbo.hoa_don(id_nhan_vien, ngay_tao DESC) WHERE xoa_mem = 0;
CREATE INDEX IX_hdct_hd_alive ON dbo.hoa_don_chi_tiet(id_hoa_don, id_chi_tiet_san_pham) WHERE xoa_mem = 0;
CREATE INDEX IX_gdtt_hd_alive ON dbo.giao_dich_thanh_toan(id_hoa_don, thoi_gian_tao DESC) WHERE xoa_mem = 0;
CREATE INDEX IX_lshd_hd_alive ON dbo.lich_su_hoa_don(id_hoa_don, thoi_gian DESC) WHERE xoa_mem = 0;
GO
