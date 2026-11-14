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
    @Test void themNhanhLuuTonKhoRiengChoTungSizeDaChon() {
        var service=spy(new QuanLySanPhamService(mock(JdbcTemplate.class)));
        doReturn(11).when(service).luuMau(anyInt(),isNull(),anyMap(),anyInt());
        var data=Map.of("doi_tuong","NU","ton_size_1","5","ton_size_2","12","ton_size_3","99","so_luong","200");
        assertEquals(2,service.themNhanhMau(8,List.of(1,2,1),data,2));
        verify(service).luuMau(eq(8),isNull(),argThat(m -> "1".equals(m.get("id_kich_thuoc")) && "5".equals(m.get("so_luong")) && "NU".equals(m.get("doi_tuong"))),eq(2));
        verify(service).luuMau(eq(8),isNull(),argThat(m -> "2".equals(m.get("id_kich_thuoc")) && "12".equals(m.get("so_luong"))),eq(2));
        verify(service,times(2)).luuMau(anyInt(),isNull(),anyMap(),anyInt());
    }
    @Test void tonKhoSizeSaiHoacThieuThiKhongLuuBatKySizeNao() {
        for(String invalid:List.of("","-1","1.5","2147483648","abc")) {
            var service=spy(new QuanLySanPhamService(mock(JdbcTemplate.class)));
            assertThrows(IllegalArgumentException.class,()->service.themNhanhMau(8,List.of(1,2),Map.of("ton_size_1","5","ton_size_2",invalid),2));
            verify(service,never()).luuMau(anyInt(),isNull(),anyMap(),anyInt());
        }
        var service=spy(new QuanLySanPhamService(mock(JdbcTemplate.class)));
        assertThrows(IllegalArgumentException.class,()->service.themNhanhMau(8,List.of(1,2),Map.of("ton_size_1","5"),2));
        verify(service,never()).luuMau(anyInt(),isNull(),anyMap(),anyInt());
    }
    @Test void khoiDongDongBoGiaMauLechTheoSanPhamVaKhongDoiGiaHoaDon() {
        var jdbc=mock(JdbcTemplate.class);
        new com.example.victorsport.Config.GiaChungSanPhamConfig(jdbc).afterPropertiesSet();
        var sql=org.mockito.ArgumentCaptor.forClass(String.class);verify(jdbc).execute(sql.capture());
        assertTrue(sql.getValue().contains("UPDATE ct SET gia_ban=sp.gia_ban"));
        assertTrue(sql.getValue().contains("ct.gia_ban<>sp.gia_ban"));
        assertTrue(sql.getValue().contains("ISNULL(ct.xoa_mem,0)=0"));
        assertFalse(sql.getValue().contains("hoa_don"));
    }
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
        verify(jdbc).queryForObject(contains("INSERT INTO chi_tiet_san_pham"),eq(Integer.class),eq(8),eq(1),eq(1),eq(1),eq(5),eq(new BigDecimal("2490000")),eq(1),eq(""),eq(2),eq("UNISEX"));
    }

    @Test void chuaCoGiaChungThiKhongDuocThemMau() {
        var jdbc=jdbc();
        assertThrows(IllegalArgumentException.class,()->new QuanLySanPhamService(jdbc).luuMau(8,null,Map.of("id_mau_sac","1","id_kich_thuoc","1","id_form_chan","1","so_luong","5","trang_thai","1"),2));
        verify(jdbc,never()).queryForObject(contains("INSERT INTO chi_tiet_san_pham"),eq(Integer.class),any(Object[].class));
    }

    @Test void themMauTrungCongTonKhoVaoMaMauCu() {
        var jdbc = jdbc();
        when(jdbc.queryForObject(contains("SELECT gia_ban FROM san_pham"), eq(BigDecimal.class), eq(8)))
                .thenReturn(new BigDecimal("2490000"));
        when(jdbc.queryForObject(startsWith("SELECT COUNT(*) FROM chi_tiet_san_pham"), eq(Integer.class), eq(8), eq(1), eq(2), eq(3), eq("UNISEX"), eq(0)))
                .thenReturn(1);
        when(jdbc.queryForObject(contains("SELECT id FROM chi_tiet_san_pham"), eq(Integer.class), eq(8), eq(1), eq(2), eq(3), eq("UNISEX")))
                .thenReturn(15);
        when(jdbc.update(contains("SET so_luong=so_luong+?"), eq(5), eq(2), eq(15), eq(5))).thenReturn(1);
        var data = Map.of("id_mau_sac", "1", "id_kich_thuoc", "2", "id_form_chan", "3", "so_luong", "5", "trang_thai", "1");
        assertEquals(15, new QuanLySanPhamService(jdbc).luuMau(8, null, data, 2));
        verify(jdbc).update(contains("SET so_luong=so_luong+?"), eq(5), eq(2), eq(15), eq(5));
        verify(jdbc, never()).queryForObject(contains("INSERT INTO chi_tiet_san_pham"), eq(Integer.class), any(Object[].class));
    }
}
