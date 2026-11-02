package com.example.victorsport.Config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Chuyển phiếu cũ sang loại tường minh một lần, không tạo lại database. */
@Component
public class LoaiPhieuGiamGiaConfig implements InitializingBean {
    private final JdbcTemplate jdbc;
    public LoaiPhieuGiamGiaConfig(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override public void afterPropertiesSet() {
        jdbc.execute("""
            SET XACT_ABORT ON;
            BEGIN TRY
                BEGIN TRANSACTION;
                IF COL_LENGTH('phieu_giam_gia','ap_dung_van_chuyen') IS NULL
                BEGIN
                    EXEC('ALTER TABLE phieu_giam_gia ADD ap_dung_van_chuyen BIT NULL');
                    EXEC(N'UPDATE phieu_giam_gia SET ap_dung_van_chuyen=CASE
                        WHEN LOWER(LTRIM(ten_phieu_giam_gia)) LIKE N''freeship%''
                          OR LOWER(LTRIM(ten_phieu_giam_gia)) LIKE N''free ship%''
                          OR LOWER(LTRIM(ten_phieu_giam_gia)) LIKE N''miễn phí vận chuyển%'' THEN 1 ELSE 0 END');
                    EXEC('ALTER TABLE phieu_giam_gia ALTER COLUMN ap_dung_van_chuyen BIT NOT NULL');
                    EXEC('ALTER TABLE phieu_giam_gia ADD CONSTRAINT DF_phieu_ap_dung_van_chuyen DEFAULT 0 FOR ap_dung_van_chuyen');
                END;
                IF COL_LENGTH('phieu_giam_gia','chi_ap_dung_ha_noi') IS NULL
                BEGIN
                    EXEC('ALTER TABLE phieu_giam_gia ADD chi_ap_dung_ha_noi BIT NULL');
                    EXEC(N'UPDATE phieu_giam_gia SET chi_ap_dung_ha_noi=CASE
                        WHEN ap_dung_van_chuyen=1 AND LOWER(ten_phieu_giam_gia) COLLATE Latin1_General_CI_AI LIKE N''%noi thanh%'' THEN 1 ELSE 0 END');
                    EXEC('ALTER TABLE phieu_giam_gia ALTER COLUMN chi_ap_dung_ha_noi BIT NOT NULL');
                    EXEC('ALTER TABLE phieu_giam_gia ADD CONSTRAINT DF_phieu_chi_ha_noi DEFAULT 0 FOR chi_ap_dung_ha_noi');
                END;
                COMMIT TRANSACTION;
            END TRY
            BEGIN CATCH
                IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
                THROW;
            END CATCH;
            """);
    }
}
