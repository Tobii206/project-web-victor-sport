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
    @Test void chonGiaTotNhatKhongPhuThuocUuTienVaKhongCongDon() {
        gia("100000.00",List.of(dot(false,"20",2,null),dot(true,"300000",2,null),dot(false,"90",1,null)));
    }
    private Map<String,Object> coId(int id,boolean tien,String gia,String toiDa) {
        var d=dot(tien,gia,0,toiDa);d.put("id_dot_giam_gia",id);return d;
    }
    @Test void bangTienThucGiamChonDotMoiNhatKeCaKhacLoaiGiam() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(List.of(coId(2,false,"20",null),coId(8,true,"200000",null)));
        var kq=GiaGiamSanPham.tinhGia(jdbc,9,new BigDecimal("1000000"));
        assertEquals(8,kq.idDotGiamGia());assertEquals(new BigDecimal("800000.00"),kq.giaSauGiam());
    }
    @Test void dotMoiGiamItHonKhongThayTheDotCuTotHon() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(List.of(coId(8,true,"100000",null),coId(2,false,"30",null)));
        var kq=GiaGiamSanPham.tinhGia(jdbc,9,new BigDecimal("1000000"));
        assertEquals(2,kq.idDotGiamGia());assertEquals(new BigDecimal("700000.00"),kq.giaSauGiam());
    }
    @Test void soSanhSauKhiApDungMucTranVaGioiHanGiaGoc() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(List.of(coId(8,false,"50","50000"),coId(2,true,"100000",null)));
        assertEquals(2,GiaGiamSanPham.tinhGia(jdbc,9,new BigDecimal("1000000")).idDotGiamGia());
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(List.of(coId(2,true,"2000000",null),coId(8,true,"1500000",null)));
        var kq=GiaGiamSanPham.tinhGia(jdbc,9,new BigDecimal("1000000"));
        assertEquals(8,kq.idDotGiamGia());assertEquals(new BigDecimal("0.00"),kq.giaSauGiam());
    }
    @Test void moiMauTuChonUuDaiTotNhatTheoGiaCuaMau() {
        var jdbc=mock(JdbcTemplate.class);
        var ds=List.of(coId(2,false,"20",null),coId(8,true,"150000",null));
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(ds);
        when(jdbc.queryForList(anyString(),eq(10))).thenReturn(ds);
        assertEquals(2,GiaGiamSanPham.tinhGia(jdbc,9,new BigDecimal("1000000")).idDotGiamGia());
        assertEquals(8,GiaGiamSanPham.tinhGia(jdbc,10,new BigDecimal("500000")).idDotGiamGia());
        assertEquals(new BigDecimal("500000.00"),GiaGiamSanPham.giaSauGiam(jdbc,11,new BigDecimal("500000")));
    }
    @Test void cauLenhChiLayDotDangHoatDongVaKhongDungGiamRieng() {
        var jdbc = mock(JdbcTemplate.class);
        GiaGiamSanPham.giaSauGiam(jdbc,9,BigDecimal.TEN);
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForList(sql.capture(),eq(9));
        assertTrue(sql.getValue().contains("BETWEEN d.ngay_bat_dau AND d.ngay_ket_thuc"));
        assertTrue(sql.getValue().contains("c.trang_thai=1 AND d.trang_thai=1"));
        assertTrue(sql.getValue().contains("c.id_chi_tiet_san_pham=?"));
        assertTrue(sql.getValue().contains("ISNULL(c.xoa_mem,0)=0 AND ISNULL(d.xoa_mem,0)=0"));
        assertTrue(sql.getValue().contains("ORDER BY d.id DESC"));
        assertFalse(sql.getValue().contains("muc_uu_tien"));
        assertFalse(sql.getValue().contains("gia_tri_giam_rieng"));
    }
}
