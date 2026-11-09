package com.example.victorsport.Config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Bổ sung giá chung mà không cần tạo lại database đang sử dụng. */
@Component
public class GiaChungSanPhamConfig implements InitializingBean {
    private final JdbcTemplate jdbc;

    public GiaChungSanPhamConfig(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void afterPropertiesSet() {
        jdbc.execute("""
            IF COL_LENGTH('san_pham','gia_ban') IS NULL
            BEGIN
                EXEC('ALTER TABLE san_pham ADD gia_ban DECIMAL(18,2) NULL CONSTRAINT CK_san_pham_gia_chung CHECK (gia_ban>=0)');
            END
            EXEC('UPDATE sp SET gia_ban=(SELECT TOP 1 ct.gia_ban FROM chi_tiet_san_pham ct
                WHERE ct.id_san_pham=sp.id AND ISNULL(ct.xoa_mem,0)=0 ORDER BY ct.id)
                FROM san_pham sp WHERE sp.gia_ban IS NULL');
            EXEC('UPDATE ct SET gia_ban=sp.gia_ban,ngay_cap_nhat=SYSDATETIME()
                FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id=ct.id_san_pham
                WHERE ISNULL(ct.xoa_mem,0)=0 AND ISNULL(sp.xoa_mem,0)=0 AND sp.gia_ban IS NOT NULL
                  AND (ct.gia_ban IS NULL OR ct.gia_ban<>sp.gia_ban)');
            """);
    }
}
