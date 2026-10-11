USE DATN_VictorSport;
GO
-- Gán ảnh theo tên sản phẩm. Chỉ thay ảnh mẫu JPG cũ, giữ các ảnh khác đã thêm.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @anh TABLE (ten_san_pham NVARCHAR(100), ten_tep VARCHAR(100), anh_mau_cu VARCHAR(100));
INSERT INTO @anh VALUES
(N'Nike Mercurial Vapor 15 Academy TF', 'nike-mercurial-vapor-15', 'nike-mercurial-vapor-15-tf-'),
(N'Adidas Predator Accuracy.3 TF', 'adidas-predator-accuracy-3', 'adidas-predator-accuracy-tf-'),
(N'Puma Future Match TT', 'puma-future-match-tt', 'puma-future-match-tt-'),
(N'Mizuno Morelia Neo IV Pro AS', 'mizuno-morelia-neo-iv', 'mizuno-morelia-neo-iv-'),
(N'Asics Gel-Kayano 30', 'asics-gel-kayano-30', 'asics-gel-kayano-30-'),
(N'Nike Air Zoom Pegasus 40', 'nike-air-zoom-pegasus', 'nike-pegasus-40-'),
(N'New Balance 550 White Green', 'new-balance-550', 'new-balance-550-white-green'),
(N'Adidas Samba OG Black White', 'adidas-samba-og', 'adidas-samba-og-black-white');
UPDATE a SET duong_dan_anh = '/images/products/' + m.ten_tep + '-1.png'
FROM anh_chi_tiet_san_pham a
JOIN chi_tiet_san_pham ct ON ct.id = a.id_chi_tiet_san_pham
JOIN san_pham sp ON sp.id = ct.id_san_pham
JOIN @anh m ON m.ten_san_pham = sp.ten_san_pham
WHERE (a.duong_dan_anh LIKE 'images/products/' + m.anh_mau_cu + '%.jpg'
    OR a.duong_dan_anh LIKE '/images/products/' + m.anh_mau_cu + '%.jpg')
    AND ISNULL(a.xoa_mem, 0) = 0;
INSERT INTO anh_chi_tiet_san_pham (id_chi_tiet_san_pham, duong_dan_anh, la_anh_dai_dien, mo_ta)
SELECT ct.id, '/images/products/' + m.ten_tep + '-' + CAST(so.stt AS VARCHAR(1)) + '.png',
    CASE WHEN so.stt = 1 AND NOT EXISTS (
        SELECT 1 FROM anh_chi_tiet_san_pham dai_dien
        WHERE dai_dien.id_chi_tiet_san_pham = ct.id AND dai_dien.la_anh_dai_dien = 1
            AND ISNULL(dai_dien.xoa_mem, 0) = 0) THEN 1 ELSE 0 END,
    N'Ảnh sản phẩm ' + sp.ten_san_pham
FROM chi_tiet_san_pham ct
JOIN san_pham sp ON sp.id = ct.id_san_pham
JOIN @anh m ON m.ten_san_pham = sp.ten_san_pham
CROSS JOIN (VALUES (1), (2), (3), (4)) so(stt)
WHERE ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(sp.xoa_mem, 0) = 0
    AND NOT EXISTS (SELECT 1 FROM anh_chi_tiet_san_pham cu
        WHERE cu.id_chi_tiet_san_pham = ct.id AND ISNULL(cu.xoa_mem, 0) = 0
            AND cu.duong_dan_anh = '/images/products/' + m.ten_tep + '-' + CAST(so.stt AS VARCHAR(1)) + '.png');
COMMIT TRANSACTION;
GO
