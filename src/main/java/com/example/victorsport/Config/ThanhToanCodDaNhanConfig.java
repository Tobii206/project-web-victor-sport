package com.example.victorsport.Config;

import com.example.victorsport.Service.HoaDonService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Đồng bộ các đơn COD đã nhận theo luồng cũ nhưng chưa ghi nhận đủ thanh toán. */
@Component
public class ThanhToanCodDaNhanConfig implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final HoaDonService hoaDon;
    public ThanhToanCodDaNhanConfig(JdbcTemplate jdbc,HoaDonService hoaDon) {this.jdbc=jdbc;this.hoaDon=hoaDon;}
    public int dongBo() {
        var ids=jdbc.queryForList("""
            SELECT hd.id FROM hoa_don hd
            WHERE hd.loai_don IN (1,2) AND hd.trang_thai_hien_tai=5 AND ISNULL(hd.xoa_mem,0)=0
              AND (SELECT TOP 1 pt.nha_cung_cap FROM giao_dich_thanh_toan gd
                   JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
                   WHERE gd.id_hoa_don=hd.id AND gd.trang_thai<>N'da_huy' AND ISNULL(gd.xoa_mem,0)=0 ORDER BY gd.id DESC)=N'COD'
              AND (SELECT ISNULL(SUM(gd.so_tien),0) FROM giao_dich_thanh_toan gd
                   WHERE gd.id_hoa_don=hd.id AND gd.trang_thai=N'thanh_cong' AND ISNULL(gd.xoa_mem,0)=0)<hd.tong_tien_sau_giam
            ORDER BY hd.id
            """,Integer.class);
        int soDon=0;
        for(int id:ids) if(hoaDon.dongBoCodDonDaNhan(id)) soDon++;
        return soDon;
    }
    @Override public void run(ApplicationArguments args) {dongBo();}
}
