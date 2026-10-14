package com.example.victorsport;

import com.example.victorsport.Service.GiaGiamSanPham;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GiaGiamSanPhamTests {
    private Map<String,Object> dot(boolean tien, String gia, int uuTien, String toiDa) {
        Map<String,Object> d = new HashMap<>();
        d.put("loai_giam_gia", tien); d.put("gia_tri", new BigDecimal(gia)); d.put("muc_uu_tien", uuTien);
        d.put("so_tien_giam_toi_da", toiDa == null ? null : new BigDecimal(toiDa));
        return d;
    }
    private void gia(String mongDoi, List<Map<String,Object>> dots) {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(dots);
        assertEquals(new BigDecimal(mongDoi), GiaGiamSanPham.giaSauGiam(jdbc,9,new BigDecimal("1000000")));
    }
    @Test void khongCoDotGiuGiaGoc() { gia("1000000.00",List.of()); }
    @Test void giamPhanTramVaTien() {
        gia("800000.00",List.of(dot(false,"20",0,null)));
        gia("700000.00",List.of(dot(true,"300000",0,null)));
    }
    @Test void gioiHanGiamVaKhongAm() {
        gia("950000.00",List.of(dot(false,"20",0,"50000")));
        gia("950000.00",List.of(dot(true,"100000",0,"50000")));
        gia("800000.00",List.of(dot(false,"20",0,"0")));
        gia("700000.00",List.of(dot(true,"300000",0,null)));
        gia("0.00",List.of(dot(true,"2000000",0,null)));
    }
    @Test void cungUuTienChonGiaTotNhatVaKhongCongDon() {
        gia("700000.00",List.of(dot(false,"20",2,null),dot(true,"300000",2,null),dot(false,"90",1,null)));
    }
    @Test void cauLenhChiLayDotDangHoatDongVaKhongDungGiamRieng() {
        var jdbc = mock(JdbcTemplate.class);
        GiaGiamSanPham.giaSauGiam(jdbc,9,BigDecimal.TEN);
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForList(sql.capture(),eq(9));
        assertTrue(sql.getValue().contains("BETWEEN d.ngay_bat_dau AND d.ngay_ket_thuc"));
        assertTrue(sql.getValue().contains("c.trang_thai=1 AND d.trang_thai=1"));
        assertFalse(sql.getValue().contains("gia_tri_giam_rieng"));
    }
}
