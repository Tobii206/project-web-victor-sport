package com.example.victorsport;

import com.example.victorsport.Service.QuanLySanPhamService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GiaChungSanPhamTests {
    private JdbcTemplate jdbc() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM san_pham WITH"),eq(8))).thenReturn(List.of(Map.of("id",8)));
        when(jdbc.queryForObject(startsWith("SELECT COUNT(*) FROM"),eq(Integer.class),any(Object[].class))).thenReturn(1);
        when(jdbc.queryForObject(contains("WHERE id_san_pham=? AND id_mau_sac=?"),eq(Integer.class),any(Object[].class))).thenReturn(0);
        return jdbc;
    }

    @Test void luuGiaSanPhamDongBoTatCaMauKhongDoiHoaDon() {
        var jdbc=jdbc();
        new QuanLySanPhamService(jdbc).luu(8,Map.of("ten_san_pham","Giày Victor","id_thuong_hieu","1","trang_thai_kinh_doanh","1","gia_ban","2490000"),2);
        verify(jdbc).update(contains("UPDATE chi_tiet_san_pham SET gia_ban="),eq(new BigDecimal("2490000")),eq(2),eq(8));
        verify(jdbc,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
    }

    @Test void mauMoiLayGiaDaLuuKhongLayGiaGuiTuTrinhDuyet() {
        var jdbc=jdbc();
        when(jdbc.queryForObject(contains("SELECT gia_ban FROM san_pham"),eq(BigDecimal.class),eq(8))).thenReturn(new BigDecimal("2490000"));
        when(jdbc.queryForObject(contains("INSERT INTO chi_tiet_san_pham"),eq(Integer.class),any(Object[].class))).thenReturn(15);
        var d=Map.of("id_mau_sac","1","id_kich_thuoc","1","id_form_chan","1","so_luong","5","trang_thai","1","gia_ban","1");
        assertEquals(15,new QuanLySanPhamService(jdbc).luuMau(8,null,d,2));
        verify(jdbc).queryForObject(contains("INSERT INTO chi_tiet_san_pham"),eq(Integer.class),eq(8),eq(1),eq(1),eq(1),eq(5),eq(new BigDecimal("2490000")),eq(1),eq(""),eq(2));
    }

    @Test void chuaCoGiaChungThiKhongDuocThemMau() {
        var jdbc=jdbc();
        assertThrows(IllegalArgumentException.class,()->new QuanLySanPhamService(jdbc).luuMau(8,null,Map.of("id_mau_sac","1","id_kich_thuoc","1","id_form_chan","1","so_luong","5","trang_thai","1"),2));
        verify(jdbc,never()).queryForObject(contains("INSERT INTO chi_tiet_san_pham"),eq(Integer.class),any(Object[].class));
    }
}
