package com.example.victorsport.Config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DoiTuongSanPhamConfig implements InitializingBean {
    private final JdbcTemplate jdbc;

    public DoiTuongSanPhamConfig(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void afterPropertiesSet() {
        jdbc.execute("""
            IF COL_LENGTH('san_pham', 'doi_tuong') IS NULL
                ALTER TABLE san_pham ADD doi_tuong VARCHAR(6) NULL
                    CONSTRAINT CK_san_pham_doi_tuong CHECK (doi_tuong IN ('NAM', 'NU', 'UNISEX'));
            """);
        jdbc.execute("""
            IF COL_LENGTH('chi_tiet_san_pham','doi_tuong') IS NULL
                EXEC('ALTER TABLE chi_tiet_san_pham ADD doi_tuong VARCHAR(6) NULL');
            EXEC('UPDATE ct SET doi_tuong=COALESCE(sp.doi_tuong,''UNISEX'')
                  FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id=ct.id_san_pham
                  WHERE ct.doi_tuong IS NULL');
            IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('chi_tiet_san_pham') AND name='doi_tuong' AND is_nullable=1)
                EXEC('ALTER TABLE chi_tiet_san_pham ALTER COLUMN doi_tuong VARCHAR(6) NOT NULL');
            IF NOT EXISTS (SELECT 1 FROM sys.default_constraints dc JOIN sys.columns c ON c.object_id=dc.parent_object_id AND c.column_id=dc.parent_column_id
                           WHERE dc.parent_object_id=OBJECT_ID('chi_tiet_san_pham') AND c.name='doi_tuong')
                EXEC('ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT DF_ctsp_doi_tuong DEFAULT ''UNISEX'' FOR doi_tuong');
            IF OBJECT_ID('CK_ctsp_doi_tuong','C') IS NULL
                EXEC('ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT CK_ctsp_doi_tuong CHECK (doi_tuong IN (''NAM'',''NU'',''UNISEX''))');
            """);
    }
}
