package com.example.victorsport.Config;

import com.example.victorsport.Service.GhnService;
import com.example.victorsport.Service.GiaoHangGhnService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableScheduling
public class DongBoGhnConfig {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(DongBoGhnConfig.class);
    private final JdbcTemplate jdbc;
    private final GhnService ghn;
    private final GiaoHangGhnService giaoHang;
    public DongBoGhnConfig(JdbcTemplate jdbc,GhnService ghn,GiaoHangGhnService giaoHang) {
        this.jdbc=jdbc;this.ghn=ghn;this.giaoHang=giaoHang;
    }
    @Scheduled(initialDelay=60000,fixedDelay=60000)
    public void dongBo() {
        if(!ghn.sanSang()) return;
        try {
            var ds=jdbc.queryForList("""
                SELECT TOP 20 vd.id_hoa_don FROM van_don_ghn vd JOIN hoa_don hd ON hd.id=vd.id_hoa_don
                WHERE vd.ma_van_don IS NOT NULL AND hd.loai_don IN (1,2)
                  AND hd.trang_thai_hien_tai IN (4,5) AND ISNULL(hd.xoa_mem,0)=0
                  AND ISNULL(vd.trang_thai,'') NOT IN ('delivered','cancel','returned')
                ORDER BY vd.ngay_cap_nhat,vd.id_hoa_don
                """);
            for(var dong:ds) {
                int id=((Number)dong.get("id_hoa_don")).intValue();
                try {giaoHang.dongBo(id);}
                catch(RuntimeException e) {log.warn("Chưa đồng bộ GHN cho đơn {}. Hệ thống sẽ thử lại ở lượt sau.",id);}
            }
        } catch(RuntimeException e) {log.warn("Chưa đọc được danh sách đồng bộ GHN. Hệ thống sẽ thử lại ở lượt sau.");}
    }
}
