USE DATN_VictorSport;
GO
-- Giữ giá bán hiện tại; chỉ dùng giá niêm yết cũ nếu giá bán chưa được nhập.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH('dbo.chi_tiet_san_pham', 'gia_niem_yet') IS NOT NULL
BEGIN
    EXEC(N'UPDATE dbo.chi_tiet_san_pham SET gia_ban=gia_niem_yet WHERE gia_ban IS NULL;
           ALTER TABLE dbo.chi_tiet_san_pham DROP COLUMN gia_niem_yet;');
END;
IF EXISTS (SELECT 1 FROM dbo.chi_tiet_san_pham WHERE gia_ban IS NULL OR gia_ban < 0)
    THROW 50001, N'Sản phẩm còn giá trống hoặc giá âm. Vui lòng kiểm tra lại.', 1;
ALTER TABLE dbo.chi_tiet_san_pham ALTER COLUMN gia_ban DECIMAL(18,2) NOT NULL;
COMMIT TRANSACTION;
GO
