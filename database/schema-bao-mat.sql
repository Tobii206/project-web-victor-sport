IF OBJECT_ID(N'bao_mat_tai_khoan', N'U') IS NULL
BEGIN
    CREATE TABLE bao_mat_tai_khoan (
        id INT IDENTITY PRIMARY KEY,
        id_khach_hang INT NULL REFERENCES khach_hang(id),
        id_nhan_vien INT NULL REFERENCES nhan_vien(id),
        so_lan_sai INT NOT NULL DEFAULT 0,
        bi_khoa BIT NOT NULL DEFAULT 0,
        ma_xac_minh_hash CHAR(64) NULL,
        han_xac_minh DATETIME2 NULL,
        ngay_gui_ma DATETIME2 NULL,
        so_lan_thu_ma INT NOT NULL DEFAULT 0,
        CONSTRAINT CK_bao_mat_chu_tai_khoan CHECK (
            (id_khach_hang IS NOT NULL AND id_nhan_vien IS NULL) OR
            (id_khach_hang IS NULL AND id_nhan_vien IS NOT NULL)
        )
    )
    CREATE UNIQUE INDEX UX_bao_mat_khach ON bao_mat_tai_khoan(id_khach_hang) WHERE id_khach_hang IS NOT NULL
    CREATE UNIQUE INDEX UX_bao_mat_nhan_vien ON bao_mat_tai_khoan(id_nhan_vien) WHERE id_nhan_vien IS NOT NULL
END;

IF EXISTS (SELECT 1 FROM sys.computed_columns WHERE object_id=OBJECT_ID(N'hoa_don') AND name=N'tong_tien_giam' AND definition NOT LIKE N'%phi_van_chuyen%')
BEGIN
    ALTER TABLE hoa_don DROP COLUMN tong_tien_giam
    ALTER TABLE hoa_don ADD tong_tien_giam AS CAST((tong_tien+ISNULL(phi_van_chuyen,0)-tong_tien_sau_giam) AS DECIMAL(18,2)) PERSISTED
END;


IF OBJECT_ID(N'van_don_ghn', N'U') IS NULL
BEGIN
 CREATE TABLE van_don_ghn (
  id_hoa_don INT PRIMARY KEY REFERENCES hoa_don(id),
  ma_don_shop VARCHAR(50) NOT NULL UNIQUE,
  ma_van_don VARCHAR(50) NULL,
  trang_thai VARCHAR(60) NOT NULL DEFAULT 'chua_tao',
  ten_tinh NVARCHAR(100) NOT NULL,
  ten_xa NVARCHAR(150) NOT NULL,
  khoi_luong INT NOT NULL CHECK(khoi_luong>0),
  dai INT NOT NULL, rong INT NOT NULL, cao INT NOT NULL,
  phi_bao_gia DECIMAL(18,0) NOT NULL CHECK(phi_bao_gia>=0),
  phi_thuc_te DECIMAL(18,0) NULL,
  ngay_cap_nhat DATETIME2 NOT NULL DEFAULT SYSDATETIME()
 )
 CREATE UNIQUE INDEX UX_van_don_ghn_ma ON van_don_ghn(ma_van_don) WHERE ma_van_don IS NOT NULL
END;


IF OBJECT_ID(N'thong_bao_da_doc', N'U') IS NULL
BEGIN
 CREATE TABLE thong_bao_da_doc (
  id INT IDENTITY PRIMARY KEY,
  id_lich_su INT NOT NULL REFERENCES lich_su_hoa_don(id),
  id_khach_hang INT NULL REFERENCES khach_hang(id),
  id_nhan_vien INT NULL REFERENCES nhan_vien(id),
  ngay_doc DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT CK_thong_bao_chu CHECK (
   (id_khach_hang IS NOT NULL AND id_nhan_vien IS NULL) OR
   (id_nhan_vien IS NOT NULL AND id_khach_hang IS NULL)
  )
 )
 CREATE UNIQUE INDEX UX_thong_bao_khach ON thong_bao_da_doc(id_khach_hang,id_lich_su) WHERE id_khach_hang IS NOT NULL
 CREATE UNIQUE INDEX UX_thong_bao_admin ON thong_bao_da_doc(id_nhan_vien,id_lich_su) WHERE id_nhan_vien IS NOT NULL
END;
