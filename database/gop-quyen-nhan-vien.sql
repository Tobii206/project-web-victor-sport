USE DATN_VictorSport;
GO
-- Dùng cho database đã có dữ liệu. Không chạy lại insertGiay.sql.
-- Chạy lại tệp này vẫn giữ nguyên tài khoản và các quyền đã cấp.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @idNhanVien INT;
SELECT TOP 1 @idNhanVien = id FROM quyen_han
WHERE ten_quyen_han = N'Nhân viên' AND ISNULL(xoa_mem, 0) = 0 ORDER BY id;

IF @idNhanVien IS NULL
BEGIN
    INSERT INTO quyen_han (ten_quyen_han) VALUES (N'Nhân viên');
    SET @idNhanVien = SCOPE_IDENTITY();
END;

INSERT INTO quyen_han_chuc_nang (id_quyen_han, id_chuc_nang)
SELECT DISTINCT @idNhanVien, qcn.id_chuc_nang
FROM quyen_han_chuc_nang qcn JOIN quyen_han qh ON qh.id = qcn.id_quyen_han
WHERE qh.ten_quyen_han IN (N'Nhân viên bán hàng', N'Nhân viên kho')
    AND ISNULL(qh.xoa_mem, 0) = 0 AND ISNULL(qh.trang_thai, 1) = 1 AND ISNULL(qcn.xoa_mem, 0) = 0
    AND NOT EXISTS (SELECT 1 FROM quyen_han_chuc_nang moi
                    WHERE moi.id_quyen_han = @idNhanVien AND moi.id_chuc_nang = qcn.id_chuc_nang);

UPDATE nhan_vien SET id_quyen_han = @idNhanVien
WHERE id_quyen_han IN (SELECT id FROM quyen_han
                      WHERE ten_quyen_han IN (N'Nhân viên bán hàng', N'Nhân viên kho'));

UPDATE quyen_han SET trang_thai = 0, xoa_mem = 1
WHERE ten_quyen_han IN (N'Nhân viên bán hàng', N'Nhân viên kho');

COMMIT TRANSACTION;
GO
