package com.example.victorsport;

import com.example.victorsport.Service.QuanLySanPhamService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnhTheoMauTests {
    private JdbcTemplate jdbc() {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("FROM san_pham WITH"),eq(8))).thenReturn(List.of(Map.of("id",8)));
        when(j.queryForList(startsWith("SELECT id_mau_sac"),eq(12),eq(8))).thenReturn(List.of(Map.of("id_mau_sac",3)));
        return j;
    }
    @Test void gomSizeCungMauMotAnhKhongGomMauKhac() {
        var a=Map.<String,Object>of("id",12,"id_mau_sac",3,"ten_mau_sac","Đen");
        var b=Map.<String,Object>of("id",15,"id_mau_sac",3,"ten_mau_sac","Đen");
        var c=Map.<String,Object>of("id",16,"id_mau_sac",4,"ten_mau_sac","Trắng");
        var photo=Map.<String,Object>of("id",30,"id_mau_sac",3,"id_chi_tiet_san_pham",10);
        var groups=QuanLySanPhamService.nhomTheoMau(List.of(b,c,a),List.of(photo));
        assertEquals(2,groups.size());assertEquals(List.of(b,a),groups.get(0).get("cacMau"));
        assertEquals(12,groups.get(0).get("idMau"));assertEquals(photo,groups.get(0).get("anh"));
        assertNull(groups.get(1).get("anh"));
    }
    @Test void themAnhThuHaiGiuLaiAnhBiaCuaMau() {
        var j = jdbc();
        when(j.queryForList(startsWith("SELECT id,duong_dan_anh"), eq(8), eq(3)))
                .thenReturn(List.of(Map.of("id", 30, "duong_dan_anh", "/image/anh-bia.png")));
        new QuanLySanPhamService(j).themAnh(8, 12, "/image/den.png");
        verify(j).update(startsWith("INSERT INTO anh_chi_tiet_san_pham"), eq(12), eq("/image/den.png"), eq(0));
        verify(j, never()).update(startsWith("UPDATE anh_chi_tiet_san_pham"), any(Object[].class));
    }
    @Test void xoaMotAnhGiuLaiCacAnhConLai() {
        var j=jdbc();
        when(j.queryForList(startsWith("SELECT a.*,ct.id_mau_sac"),eq(30),eq(8)))
            .thenReturn(List.of(Map.of("id_mau_sac",3)));
        new QuanLySanPhamService(j).suaAnh(8,30,true);
        verify(j).update("UPDATE anh_chi_tiet_san_pham SET xoa_mem=1,la_anh_dai_dien=0 WHERE id=?",30);
    }
    @Test void khongChoDungMauSanPhamKhacDeTaiAnh() {
        var j=jdbc();assertThrows(IllegalArgumentException.class,()->new QuanLySanPhamService(j).themAnh(8,99,"/image/x.png"));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    private void suaMau(JdbcTemplate j) {
        when(j.queryForObject(startsWith("SELECT COUNT(*) FROM"),eq(Integer.class),any(Object[].class))).thenReturn(1);
        when(j.queryForObject(contains("WHERE id_san_pham=? AND id_mau_sac=?"),eq(Integer.class),any(Object[].class))).thenReturn(0);
        when(j.queryForObject(startsWith("SELECT gia_ban"),eq(BigDecimal.class),eq(8))).thenReturn(new BigDecimal("1000000"));
        when(j.update(startsWith("UPDATE chi_tiet_san_pham SET id_mau_sac"),any(Object[].class))).thenReturn(1);
        new QuanLySanPhamService(j).luuMau(8,12,Map.of("id_mau_sac","4","id_kich_thuoc","1","id_form_chan","1","so_luong","5","so_luong_cu","5","trang_thai","1"),2);
    }
    @Test void doiMauSizeDangGiuAnhThiGiuAnhChoNhomMauCu() {
        var j=jdbc();when(j.queryForList(startsWith("SELECT TOP 1 id FROM chi_tiet_san_pham"),eq(8),eq(3),eq(12))).thenReturn(List.of(Map.of("id",15)));
        suaMau(j);
        verify(j).update(startsWith("UPDATE anh_chi_tiet_san_pham SET id_chi_tiet_san_pham"),eq(15),eq(12));
    }
    @Test void doiMauSizeCuoiKhongMangAnhSaiSangMauMoi() {
        var j=jdbc();suaMau(j);
        verify(j).update(contains("WHERE id_chi_tiet_san_pham=? AND ISNULL(xoa_mem,0)=0"),eq(12));
    }
}
