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
CREATE UNIQUE INDEX UX_ctdgg_alive ON dbo.chi_tiet_dot_giam_gia(id_dot_giam_gia, id_chi_tiet_san_pham) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_chuc_nang_ma_alive ON dbo.chuc_nang(ma_chuc_nang) WHERE xoa_mem = 0;
CREATE UNIQUE INDEX UX_qhcn_alive ON dbo.quyen_han_chuc_nang(id_quyen_han, id_chuc_nang) WHERE xoa_mem = 0;

CREATE INDEX IX_hd_khach_hang_alive ON dbo.hoa_don(id_khach_hang, ngay_tao DESC) WHERE xoa_mem = 0;
CREATE INDEX IX_hd_nhan_vien_alive ON dbo.hoa_don(id_nhan_vien, ngay_tao DESC) WHERE xoa_mem = 0;
CREATE INDEX IX_hdct_hd_alive ON dbo.hoa_don_chi_tiet(id_hoa_don, id_chi_tiet_san_pham) WHERE xoa_mem = 0;
CREATE INDEX IX_gdtt_hd_alive ON dbo.giao_dich_thanh_toan(id_hoa_don, thoi_gian_tao DESC) WHERE xoa_mem = 0;
CREATE INDEX IX_lshd_hd_alive ON dbo.lich_su_hoa_don(id_hoa_don, thoi_gian DESC) WHERE xoa_mem = 0;
GO


-- =====================================================
-- DU LIEU MAU
-- =====================================================

USE DATN_SevenStrike;
GO

SET NOCOUNT ON;
GO

INSERT INTO dbo.xuat_xu (ten_xuat_xu) VALUES
(N'Má»¹'),
(N'Äá»©c'),
(N'Nháº­t Báº£n'),
(N'Viá»‡t Nam'),
(N'Indonesia');

INSERT INTO dbo.thuong_hieu (ten_thuong_hieu) VALUES
(N'Nike'),
(N'Adidas'),
(N'Puma'),
(N'Mizuno'),
(N'Asics'),
(N'New Balance');

INSERT INTO dbo.mau_sac (ten_mau_sac, ma_mau_hex) VALUES
(N'Tráº¯ng', '#FFFFFF'),
(N'Äen', '#000000'),
(N'Äá»', '#D71920'),
(N'Xanh dÆ°Æ¡ng', '#1D4ED8'),
(N'XÃ¡m', '#808080'),
(N'VÃ ng neon', '#D9F99D');

INSERT INTO dbo.kich_thuoc (ten_kich_thuoc, gia_tri_kich_thuoc) VALUES
(N'Size 38', 38.0),
(N'Size 39', 39.0),
(N'Size 40', 40.0),
(N'Size 41', 41.0),
(N'Size 42', 42.0),
(N'Size 43', 43.0),
(N'Size 44', 44.0);

INSERT INTO dbo.vi_tri_thi_dau (ten_vi_tri) VALUES
(N'Tiá»n Ä‘áº¡o'),
(N'Tiá»n vá»‡'),
(N'Háº­u vá»‡'),
(N'Thá»§ mÃ´n'),
(N'Cháº¡y bá»™'),
(N'Táº­p luyá»‡n Ä‘a nÄƒng');

INSERT INTO dbo.phong_cach_choi (ten_phong_cach) VALUES
(N'Tá»‘c Ä‘á»™'),
(N'Kiá»ƒm soÃ¡t bÃ³ng'),
(N'SÃºt bÃ³ng máº¡nh'),
(N'ÃŠm chÃ¢n Ä‘Æ°á»ng dÃ i'),
(N'Linh hoáº¡t'),
(N'Thá»i trang thá»ƒ thao');

INSERT INTO dbo.co_giay (ten_co_giay) VALUES
(N'Cá»• tháº¥p'),
(N'Cá»• lá»­ng'),
(N'Cá»• cao');

INSERT INTO dbo.form_chan (ten_form_chan) VALUES
(N'Ã”m chÃ¢n'),
(N'TiÃªu chuáº©n'),
(N'Rá»™ng chÃ¢n');

INSERT INTO dbo.chat_lieu (ten_chat_lieu) VALUES
(N'Da tá»•ng há»£p'),
(N'Váº£i knit'),
(N'Váº£i mesh'),
(N'Da kangaroo'),
(N'Canvas'),
(N'Primeknit');

INSERT INTO dbo.quyen_han (ten_quyen_han) VALUES
(N'Quáº£n trá»‹ viÃªn'),
(N'NhÃ¢n viÃªn bÃ¡n hÃ ng'),
(N'NhÃ¢n viÃªn kho');

INSERT INTO dbo.nhan_vien (
    id_quyen_han, ten_nhan_vien, ten_tai_khoan, mat_khau, email, so_dien_thoai,
    ngay_sinh, thanh_pho, quan, phuong, dia_chi_cu_the
) VALUES
(1, N'Nguyá»…n Minh QuÃ¢n', 'admin', '123456', 'admin@sevenstrike.vn', '0900000001', '1998-02-12', N'HÃ  Ná»™i', N'Cáº§u Giáº¥y', N'Dá»‹ch Vá»ng', N'12 XuÃ¢n Thá»§y'),
(2, N'LÃª Thanh Huyá»n', 'banhang01', '123456', 'huyen.lt@sevenstrike.vn', '0900000002', '2000-07-21', N'HÃ  Ná»™i', N'Nam Tá»« LiÃªm', N'Má»¹ ÄÃ¬nh 1', N'25 LÃª Äá»©c Thá»'),
(3, N'Tráº§n Äá»©c Anh', 'kho01', '123456', 'anh.td@sevenstrike.vn', '0900000003', '1999-11-04', N'HÃ  Ná»™i', N'Thanh XuÃ¢n', N'NhÃ¢n ChÃ­nh', N'88 Nguyá»…n TrÃ£i');

INSERT INTO dbo.khach_hang (
    ten_khach_hang, ten_tai_khoan, mat_khau, email, so_dien_thoai,
    gioi_tinh, ngay_sinh, anh_dai_dien
) VALUES
(N'Nguyá»…n VÄƒn An', 'khach01', '123456', 'khach01@gmail.com', '0900000004', 1, '2003-05-10', 'images/customers/khach01.jpg'),
(N'Tráº§n Thá»‹ BÃ¬nh', 'khach02', '123456', 'khach02@gmail.com', '0900000005', 0, '2004-08-15', 'images/customers/khach02.jpg'),
(N'LÃª Minh HoÃ ng', 'khach03', '123456', 'khach03@gmail.com', '0900000006', 1, '2002-11-20', 'images/customers/khach03.jpg'),
(N'Pháº¡m Thu HÃ ', 'khach04', '123456', 'khach04@gmail.com', '0900000007', 0, '2003-03-08', 'images/customers/khach04.jpg'),
(N'Äá»— Quá»‘c Huy', 'khach05', '123456', 'khach05@gmail.com', '0900000008', 1, '2001-12-25', 'images/customers/khach05.jpg');

INSERT INTO dbo.dia_chi_khach_hang (
    id_khach_hang, ten_dia_chi, thanh_pho, quan, phuong, dia_chi_cu_the, mac_dinh
) VALUES
(1, N'NhÃ  riÃªng', N'HÃ  Ná»™i', N'Cáº§u Giáº¥y', N'Dá»‹ch Vá»ng', N'Sá»‘ 10 ngÃµ 68 Tráº§n ThÃ¡i TÃ´ng', 1),
(2, N'KÃ½ tÃºc xÃ¡', N'HÃ  Ná»™i', N'Thanh XuÃ¢n', N'Thanh XuÃ¢n Báº¯c', N'PhÃ²ng 501 nhÃ  B', 1),
(3, N'NhÃ  riÃªng', N'Háº£i PhÃ²ng', N'LÃª ChÃ¢n', N'DÆ° HÃ ng KÃªnh', N'22 Nguyá»…n VÄƒn Linh', 1),
(4, N'CÃ´ng ty', N'ÄÃ  Náºµng', N'Háº£i ChÃ¢u', N'BÃ¬nh HiÃªn', N'15 Nguyá»…n VÄƒn Linh', 1),
(5, N'CÄƒn há»™', N'Há»“ ChÃ­ Minh', N'Quáº­n 1', N'Báº¿n NghÃ©', N'68 LÃª Lá»£i', 1);

INSERT INTO dbo.san_pham (
    id_thuong_hieu, id_xuat_xu, id_vi_tri_thi_dau, id_phong_cach_choi,
    id_co_giay, id_chat_lieu, ten_san_pham, mo_ta_ngan, mo_ta_chi_tiet
) VALUES
(1, 1, 1, 1, 1, 1, N'Nike Mercurial Vapor 15 Academy TF', N'GiÃ y Ä‘Ã¡ bÃ³ng sÃ¢n cá» nhÃ¢n táº¡o thiÃªn vá» tá»‘c Ä‘á»™.', N'Upper tá»•ng há»£p má»ng, Ä‘áº¿ TF bÃ¡m sÃ¢n tá»‘t, phÃ¹ há»£p tiá»n Ä‘áº¡o vÃ  cáº§u thá»§ cháº¡y cÃ¡nh.'),
(2, 2, 2, 2, 1, 6, N'Adidas Predator Accuracy.3 TF', N'GiÃ y kiá»ƒm soÃ¡t bÃ³ng cho sÃ¢n cá» nhÃ¢n táº¡o.', N'Bá» máº·t vÃ¢n ná»•i há»— trá»£ cháº¡m bÃ³ng, form Ã´m chÃ¢n, phÃ¹ há»£p tiá»n vá»‡.'),
(3, 2, 2, 5, 2, 2, N'Puma Future Match TT', N'GiÃ y linh hoáº¡t cho lá»‘i chÆ¡i biáº¿n hÃ³a.', N'Thiáº¿t káº¿ cá»• lá»­ng, cháº¥t liá»‡u knit co giÃ£n, há»— trá»£ xoay trá»Ÿ nhanh.'),
(4, 3, 2, 2, 1, 4, N'Mizuno Morelia Neo IV Pro AS', N'GiÃ y da má»m, cáº£m giÃ¡c bÃ³ng tá»‘t.', N'Da kangaroo cao cáº¥p, trá»ng lÆ°á»£ng nháº¹, phÃ¹ há»£p cáº§u thá»§ thÃ­ch kiá»ƒm soÃ¡t bÃ³ng.'),
(5, 3, 5, 4, 1, 3, N'Asics Gel-Kayano 30', N'GiÃ y cháº¡y bá»™ á»•n Ä‘á»‹nh cho Ä‘Æ°á»ng dÃ i.', N'CÃ´ng nghá»‡ GEL giáº£m cháº¥n, Ä‘á»‡m Ãªm, há»— trá»£ ngÆ°á»i cháº¡y cáº§n Ä‘á»™ á»•n Ä‘á»‹nh cao.'),
(1, 1, 5, 4, 1, 3, N'Nike Air Zoom Pegasus 40', N'GiÃ y cháº¡y bá»™ háº±ng ngÃ y nháº¹ vÃ  bá»n.', N'Äá»‡m Zoom Air Ä‘Ã n há»“i, upper mesh thoÃ¡ng khÃ­, phÃ¹ há»£p cháº¡y road.'),
(6, 5, 6, 6, 1, 5, N'New Balance 550 White Green', N'Sneaker thá»ƒ thao phong cÃ¡ch retro.', N'Thiáº¿t káº¿ láº¥y cáº£m há»©ng bÃ³ng rá»• cá»• Ä‘iá»ƒn, dá»… phá»‘i Ä‘á»“, Ä‘i há»c vÃ  Ä‘i chÆ¡i.'),
(2, 2, 6, 6, 1, 1, N'Adidas Samba OG Black White', N'Sneaker thá»ƒ thao cá»• Ä‘iá»ƒn.', N'Form tháº¥p, Ä‘áº¿ gum, phong cÃ¡ch lifestyle nhÆ°ng váº«n giá»¯ tinh tháº§n thá»ƒ thao.');

INSERT INTO dbo.chi_tiet_san_pham (
    id_san_pham, id_mau_sac, id_kich_thuoc, id_form_chan, so_luong, gia_niem_yet, gia_ban, ghi_chu
) VALUES
(1, 6, 3, 1, 18, 2190000, 1990000, N'VÃ ng neon size 40'),
(1, 2, 4, 1, 15, 2190000, 1990000, N'Äen size 41'),
(2, 2, 4, 2, 12, 2390000, 2190000, N'Äen size 41'),
(2, 1, 5, 2, 10, 2390000, 2190000, N'Tráº¯ng size 42'),
(3, 3, 3, 1, 11, 1990000, 1790000, N'Äá» size 40'),
(3, 4, 4, 1, 9, 1990000, 1790000, N'Xanh size 41'),
(4, 1, 3, 2, 8, 3290000, 2990000, N'Tráº¯ng size 40'),
(4, 2, 5, 2, 6, 3290000, 2990000, N'Äen size 42'),
(5, 5, 4, 3, 14, 4390000, 3990000, N'XÃ¡m size 41'),
(6, 4, 4, 2, 20, 3490000, 3190000, N'Xanh size 41'),
(7, 1, 3, 2, 16, 2890000, 2590000, N'Tráº¯ng size 40'),
(8, 2, 2, 2, 13, 2790000, 2490000, N'Äen size 39');

INSERT INTO dbo.anh_chi_tiet_san_pham (id_chi_tiet_san_pham, duong_dan_anh, la_anh_dai_dien, mo_ta) VALUES
(1, 'images/products/nike-mercurial-vapor-15-tf-neon.jpg', 1, N'Nike Mercurial Vapor 15 Academy TF vÃ ng neon'),
(2, 'images/products/nike-mercurial-vapor-15-tf-black.jpg', 1, N'Nike Mercurial Vapor 15 Academy TF Ä‘en'),
(3, 'images/products/adidas-predator-accuracy-tf-black.jpg', 1, N'Adidas Predator Accuracy.3 TF Ä‘en'),
(4, 'images/products/adidas-predator-accuracy-tf-white.jpg', 1, N'Adidas Predator Accuracy.3 TF tráº¯ng'),
(5, 'images/products/puma-future-match-tt-red.jpg', 1, N'Puma Future Match TT Ä‘á»'),
(6, 'images/products/puma-future-match-tt-blue.jpg', 1, N'Puma Future Match TT xanh'),
(7, 'images/products/mizuno-morelia-neo-iv-white.jpg', 1, N'Mizuno Morelia Neo IV Pro AS tráº¯ng'),
(8, 'images/products/mizuno-morelia-neo-iv-black.jpg', 1, N'Mizuno Morelia Neo IV Pro AS Ä‘en'),
(9, 'images/products/asics-gel-kayano-30-grey.jpg', 1, N'Asics Gel-Kayano 30 xÃ¡m'),
(10, 'images/products/nike-pegasus-40-blue.jpg', 1, N'Nike Air Zoom Pegasus 40 xanh'),
(11, 'images/products/new-balance-550-white-green.jpg', 1, N'New Balance 550 White Green'),
(12, 'images/products/adidas-samba-og-black-white.jpg', 1, N'Adidas Samba OG Black White');

INSERT INTO dbo.dot_giam_gia (
    ten_dot_giam_gia, loai_giam_gia, gia_tri_giam_gia, ngay_bat_dau, ngay_ket_thuc, muc_uu_tien
) VALUES
(N'Sale khai trÆ°Æ¡ng SevenStrike', 0, 10, '2026-01-01', '2026-12-31', 1),
(N'Sale giÃ y cháº¡y bá»™ cuá»‘i tuáº§n', 0, 8, '2026-09-01', '2026-10-31', 2);

INSERT INTO dbo.chi_tiet_dot_giam_gia (
    id_dot_giam_gia, id_chi_tiet_san_pham, so_luong_ap_dung, gia_tri_giam_rieng, ghi_chu
) VALUES
(1, 1, 50, NULL, N'Ãp dá»¥ng Mercurial vÃ ng neon'),
(1, 3, 50, NULL, N'Ãp dá»¥ng Predator Ä‘en'),
(1, 5, 40, NULL, N'Ãp dá»¥ng Puma Ä‘á»'),
(2, 9, 30, NULL, N'Ãp dá»¥ng Asics Gel-Kayano'),
(2, 10, 30, NULL, N'Ãp dá»¥ng Nike Pegasus');

INSERT INTO dbo.phieu_giam_gia (
    ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_tien_giam_toi_da,
    hoa_don_toi_thieu, so_luong_su_dung, ngay_bat_dau, ngay_ket_thuc, mo_ta
) VALUES
(N'Giáº£m 10% Ä‘Æ¡n tá»« 1 triá»‡u', 0, 10, 300000, 1000000, 100, '2026-01-01', '2026-12-31', N'Voucher toÃ n shop'),
(N'Giáº£m 200K Ä‘Æ¡n tá»« 2 triá»‡u', 1, 200000, 200000, 2000000, 50, '2026-01-01', '2026-12-31', N'Voucher toÃ n shop'),
(N'Freeship ná»™i thÃ nh', 1, 30000, 30000, 500000, 200, '2026-01-01', '2026-12-31', N'Há»— trá»£ phÃ­ váº­n chuyá»ƒn');

INSERT INTO dbo.hoa_don (
    id_khach_hang, id_nhan_vien, id_phieu_giam_gia,
    loai_don, phi_van_chuyen, tong_tien, tong_tien_sau_giam,
    ten_khach_hang, dia_chi_khach_hang, so_dien_thoai_khach_hang, email_khach_hang,
    trang_thai_hien_tai, ngay_thanh_toan, ghi_chu
) VALUES
(1, 2, 2, 2, 30000, 3980000, 3780000, N'Nguyá»…n VÄƒn An', N'Sá»‘ 10 ngÃµ 68 Tráº§n ThÃ¡i TÃ´ng, Cáº§u Giáº¥y, HÃ  Ná»™i', '0900000004', 'khach01@gmail.com', 5, '2026-09-20 10:30:00', N'Mua 2 Ä‘Ã´i Ä‘Ã¡ bÃ³ng'),
(2, 2, 3, 2, 30000, 2190000, 2160000, N'Tráº§n Thá»‹ BÃ¬nh', N'PhÃ²ng 501 nhÃ  B, Thanh XuÃ¢n Báº¯c, HÃ  Ná»™i', '0900000005', 'khach02@gmail.com', 4, '2026-09-21 14:05:00', N'Giao giá» hÃ nh chÃ­nh'),
(3, 2, NULL, 0, 0, 2990000, 2990000, N'LÃª Minh HoÃ ng', N'22 Nguyá»…n VÄƒn Linh, LÃª ChÃ¢n, Háº£i PhÃ²ng', '0900000006', 'khach03@gmail.com', 5, '2026-09-22 19:10:00', N'Mua táº¡i quáº§y'),
(4, 2, 2, 2, 30000, 3990000, 3790000, N'Pháº¡m Thu HÃ ', N'15 Nguyá»…n VÄƒn Linh, Háº£i ChÃ¢u, ÄÃ  Náºµng', '0900000007', 'khach04@gmail.com', 2, NULL, N'Chá» xÃ¡c nháº­n thanh toÃ¡n'),
(5, 2, 1, 1, 30000, 2590000, 2331000, N'Äá»— Quá»‘c Huy', N'68 LÃª Lá»£i, Quáº­n 1, Há»“ ChÃ­ Minh', '0900000008', 'khach05@gmail.com', 3, '2026-09-23 08:20:00', N'ÄÆ¡n online sneaker');

INSERT INTO dbo.hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia, ghi_chu) VALUES
(1, 1, 1, 1990000, N'Nike Mercurial Vapor 15 vÃ ng neon size 40'),
(1, 2, 1, 1990000, N'Nike Mercurial Vapor 15 Ä‘en size 41'),
(2, 3, 1, 2190000, N'Adidas Predator Accuracy.3 Ä‘en size 41'),
(3, 7, 1, 2990000, N'Mizuno Morelia Neo IV tráº¯ng size 40'),
(4, 9, 1, 3990000, N'Asics Gel-Kayano 30 xÃ¡m size 41'),
(5, 11, 1, 2590000, N'New Balance 550 White Green size 40');

INSERT INTO dbo.phuong_thuc_thanh_toan (ten_phuong_thuc_thanh_toan, nha_cung_cap) VALUES
(N'Tiá»n máº·t', N'POS'),
(N'Chuyá»ƒn khoáº£n ngÃ¢n hÃ ng', N'VietQR'),
(N'VÃ­ Ä‘iá»‡n tá»­', N'MoMo'),
(N'Thanh toÃ¡n khi nháº­n hÃ ng', N'COD');

INSERT INTO dbo.giao_dich_thanh_toan (
    id_hoa_don, id_phuong_thuc_thanh_toan, so_tien, trang_thai,
    ma_yeu_cau, ma_giao_dich_ngoai, ma_tham_chieu, thoi_gian_tao, nguoi_cap_nhat, ghi_chu
) VALUES
(1, 2, 3780000, N'thanh_cong', 'REQ000001', 'BANK202609200001', 'HD00001', '2026-09-20 10:30:00', 2, N'ÄÃ£ nháº­n chuyá»ƒn khoáº£n'),
(2, 4, 2160000, N'cho_thanh_toan', 'REQ000002', NULL, 'HD00002', '2026-09-21 14:05:00', 2, N'COD'),
(3, 1, 2990000, N'thanh_cong', 'REQ000003', 'CASH202609220001', 'HD00003', '2026-09-22 19:10:00', 2, N'Thanh toÃ¡n táº¡i quáº§y'),
(4, 3, 3790000, N'khoi_tao', 'REQ000004', NULL, 'HD00004', '2026-09-22 21:30:00', 2, N'Chá» vÃ­ Ä‘iá»‡n tá»­ pháº£n há»“i'),
(5, 2, 2331000, N'thanh_cong', 'REQ000005', 'BANK202609230001', 'HD00005', '2026-09-23 08:20:00', 2, N'ÄÃ£ nháº­n chuyá»ƒn khoáº£n');

INSERT INTO dbo.lich_su_hoa_don (
    id_hoa_don, trang_thai, thoi_gian, ghi_chu, nguoi_cap_nhat, nguoi_thuc_hien, loai_nguoi_thuc_hien
) VALUES
(1, 1, '2026-09-20 10:00:00', N'Táº¡o Ä‘Æ¡n online', 2, 1, 'KHACH_HANG'),
(1, 5, '2026-09-21 16:40:00', N'ÄÃ£ giao thÃ nh cÃ´ng', 2, 2, 'NHAN_VIEN'),
(2, 1, '2026-09-21 14:05:00', N'Táº¡o Ä‘Æ¡n COD', 2, 2, 'NHAN_VIEN'),
(2, 4, '2026-09-22 09:15:00', N'Äang giao hÃ ng', 2, 2, 'NHAN_VIEN'),
(3, 5, '2026-09-22 19:15:00', N'HoÃ n táº¥t táº¡i quáº§y', 2, 2, 'NHAN_VIEN'),
(4, 2, '2026-09-22 21:35:00', N'ÄÃ£ xÃ¡c nháº­n Ä‘Æ¡n', 2, 2, 'NHAN_VIEN'),
(5, 3, '2026-09-23 09:00:00', N'Äang chuáº©n bá»‹ hÃ ng', 2, 2, 'NHAN_VIEN');

INSERT INTO dbo.chuc_nang (ma_chuc_nang, ten_chuc_nang, mo_ta) VALUES
('PRODUCT_READ', N'Xem sáº£n pháº©m', N'Cho phÃ©p xem danh sÃ¡ch vÃ  chi tiáº¿t sáº£n pháº©m'),
('PRODUCT_WRITE', N'Quáº£n lÃ½ sáº£n pháº©m', N'ThÃªm, sá»­a, xÃ³a má»m sáº£n pháº©m'),
('ORDER_READ', N'Xem hÃ³a Ä‘Æ¡n', N'Cho phÃ©p xem hÃ³a Ä‘Æ¡n'),
('ORDER_WRITE', N'Quáº£n lÃ½ hÃ³a Ä‘Æ¡n', N'Cáº­p nháº­t tráº¡ng thÃ¡i vÃ  thanh toÃ¡n hÃ³a Ä‘Æ¡n'),
('CUSTOMER_READ', N'Xem khÃ¡ch hÃ ng', N'Cho phÃ©p xem thÃ´ng tin khÃ¡ch hÃ ng'),
('REPORT_READ', N'Xem bÃ¡o cÃ¡o', N'Cho phÃ©p xem thá»‘ng kÃª doanh thu');

INSERT INTO dbo.quyen_han_chuc_nang (id_quyen_han, id_chuc_nang) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(2, 1), (2, 3), (2, 4), (2, 5),
(3, 1), (3, 2), (3, 3);
GO

