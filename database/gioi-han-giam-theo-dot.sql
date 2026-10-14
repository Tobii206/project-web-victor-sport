USE DATN_VictorSport;
GO

-- Giới hạn chung cho các mẫu giày trong đợt. NULL hoặc 0 nghĩa là không giới hạn.
IF COL_LENGTH('dbo.dot_giam_gia', 'so_tien_giam_toi_da') IS NULL
    ALTER TABLE dbo.dot_giam_gia ADD so_tien_giam_toi_da DECIMAL(18,2) NULL;
GO
