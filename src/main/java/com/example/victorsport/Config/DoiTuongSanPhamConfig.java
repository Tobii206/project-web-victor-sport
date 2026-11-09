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
    }
}
