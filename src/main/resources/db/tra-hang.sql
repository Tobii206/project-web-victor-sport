IF COL_LENGTH('hoa_don','tong_tien_da_hoan') IS NULL
    ALTER TABLE hoa_don ADD tong_tien_da_hoan DECIMAL(18,2) NOT NULL CONSTRAINT DF_hd_da_hoan DEFAULT 0;
GO
IF COL_LENGTH('hoa_don_chi_tiet','so_luong_da_tra') IS NULL
    ALTER TABLE hoa_don_chi_tiet ADD so_luong_da_tra INT NOT NULL CONSTRAINT DF_hdct_da_tra DEFAULT 0;
GO
IF OBJECT_ID('yeu_cau_tra_hang','U') IS NULL
BEGIN
    CREATE TABLE yeu_cau_tra_hang (
        id INT IDENTITY PRIMARY KEY,
        ma_yeu_cau AS 'TH'+RIGHT('000000'+CAST(id AS VARCHAR(10)),6) PERSISTED,
        ma_tao VARCHAR(36) NOT NULL UNIQUE,
        id_hoa_don INT NOT NULL REFERENCES hoa_don(id),
        trang_thai VARCHAR(30) NOT NULL DEFAULT 'CHO_XAC_NHAN',
        ly_do NVARCHAR(100) NOT NULL,ghi_chu NVARCHAR(1000),
        hinh_thuc_tra VARCHAR(20) NOT NULL,
        phuong_thuc_hoan VARCHAR(10) NOT NULL,
        ngan_hang NVARCHAR(100),so_tai_khoan VARCHAR(34),chu_tai_khoan NVARCHAR(100),
        hang_van_chuyen NVARCHAR(100),ma_van_don VARCHAR(100),
        so_tien_hoan DECIMAL(18,2) NOT NULL CHECK(so_tien_hoan>=0),
        ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ngay_cap_nhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ngay_hoan_tien DATETIME2,
        nguoi_tao_khach INT REFERENCES khach_hang(id),nguoi_tao_nv INT REFERENCES nhan_vien(id),
        CONSTRAINT CK_th_trang_thai CHECK(trang_thai IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_GUI_HANG','DA_NHAN_HANG','DANG_KIEM_TRA','DA_DUYET_HOAN_TRA','DA_HOAN_TIEN','TU_CHOI','DA_HUY')),
        CONSTRAINT CK_th_hinh_thuc CHECK(hinh_thuc_tra IN ('GUI_HANG','TAI_CUA_HANG')),
        CONSTRAINT CK_th_hoan CHECK(phuong_thuc_hoan IN ('CASH','BANK'))
    );
    CREATE INDEX IX_th_hoa_don ON yeu_cau_tra_hang(id_hoa_don,id);
    CREATE INDEX IX_th_trang_thai ON yeu_cau_tra_hang(trang_thai,ngay_tao);
END;
GO
IF OBJECT_ID('chi_tiet_tra_hang','U') IS NULL
BEGIN
    CREATE TABLE chi_tiet_tra_hang (
        id INT IDENTITY PRIMARY KEY,id_yeu_cau INT NOT NULL REFERENCES yeu_cau_tra_hang(id),
        id_hoa_don_chi_tiet INT NOT NULL REFERENCES hoa_don_chi_tiet(id),
        so_luong INT NOT NULL CHECK(so_luong>0),
        so_tien_hoan DECIMAL(18,2) NOT NULL CHECK(so_tien_hoan>=0),
        so_luong_nhap INT NOT NULL DEFAULT 0,so_luong_loi INT NOT NULL DEFAULT 0,
        CONSTRAINT UQ_ctth_dong UNIQUE(id_yeu_cau,id_hoa_don_chi_tiet),
        CONSTRAINT CK_ctth_kiem_tra CHECK(so_luong_nhap>=0 AND so_luong_loi>=0 AND so_luong_nhap+so_luong_loi<=so_luong)
    );
END;
GO
IF OBJECT_ID('anh_minh_chung_tra_hang','U') IS NULL
BEGIN
    CREATE TABLE anh_minh_chung_tra_hang (
        id INT IDENTITY PRIMARY KEY,id_yeu_cau INT NOT NULL REFERENCES yeu_cau_tra_hang(id),
        du_lieu VARBINARY(MAX) NOT NULL CHECK(DATALENGTH(du_lieu)<=5242880)
    );
END;
GO
IF OBJECT_ID('lich_su_tra_hang','U') IS NULL
BEGIN
    CREATE TABLE lich_su_tra_hang (
        id INT IDENTITY PRIMARY KEY,id_yeu_cau INT NOT NULL REFERENCES yeu_cau_tra_hang(id),
        trang_thai VARCHAR(30) NOT NULL,noi_dung NVARCHAR(1000) NOT NULL,
        id_khach INT REFERENCES khach_hang(id),id_nhan_vien INT REFERENCES nhan_vien(id),
        ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END;
GO
IF OBJECT_ID('giao_dich_hoan_tien','U') IS NULL
BEGIN
    CREATE TABLE giao_dich_hoan_tien (
        id INT IDENTITY PRIMARY KEY,id_yeu_cau INT NOT NULL UNIQUE REFERENCES yeu_cau_tra_hang(id),
        id_hoa_don INT NOT NULL REFERENCES hoa_don(id),so_tien DECIMAL(18,2) NOT NULL CHECK(so_tien>=0),
        phuong_thuc VARCHAR(10) NOT NULL,ma_giao_dich NVARCHAR(100) NOT NULL UNIQUE,
        id_nhan_vien INT NOT NULL REFERENCES nhan_vien(id),ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END;
GO
IF OBJECT_ID('san_pham_hoan_tra','U') IS NULL
BEGIN
    CREATE TABLE san_pham_hoan_tra (
        id INT IDENTITY PRIMARY KEY,id_chi_tiet_tra INT NOT NULL UNIQUE REFERENCES chi_tiet_tra_hang(id),
        id_chi_tiet_san_pham INT NOT NULL REFERENCES chi_tiet_san_pham(id),
        so_luong INT NOT NULL CHECK(so_luong>0),ghi_chu NVARCHAR(1000),
        ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END;
GO

IF COL_LENGTH('chi_tiet_tra_hang','duoc_duyet') IS NULL
    ALTER TABLE chi_tiet_tra_hang ADD duoc_duyet BIT NOT NULL CONSTRAINT DF_ctth_duyet DEFAULT 1;
GO
