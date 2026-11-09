package com.example.victorsport;

import com.example.victorsport.Service.QuanLyDotGiamGiaService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrangThaiDotGiamGiaTests {
    @Test void hetHanUuTienHonCongTacBatTatVaTinhDenHetNgayKetThuc() {
        var ngay=LocalDate.of(2026,11,2);
        for(boolean bat:new boolean[]{false,true}) {
            assertEquals("Hết hạn",QuanLyDotGiamGiaService.tenTrangThai(Map.of("ngay_ket_thuc",java.sql.Date.valueOf(ngay.minusDays(1)),"trang_thai",bat),ngay));
            assertEquals(bat?"Cho phép áp dụng":"Ngừng áp dụng",QuanLyDotGiamGiaService.tenTrangThai(Map.of("ngay_ket_thuc",ngay,"trang_thai",bat),ngay));
        }
    }
    @Test void locHetHanBangNgayKhongCanDoiTrangThaiLuuTrongDatabase() {
        var jdbc=mock(JdbcTemplate.class);var service=new QuanLyDotGiamGiaService(jdbc);
        service.tim("sale","2");
        verify(jdbc).queryForList(contains("d.ngay_ket_thuc<?"),eq("%sale%"),eq("%sale%"),eq(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void boLocBatTatKhongLayLanDotDaHetHan() {
        for(String value:List.of("0","1")) {
            var jdbc=mock(JdbcTemplate.class);new QuanLyDotGiamGiaService(jdbc).tim("",value);
            if(value.equals("0")) verify(jdbc).queryForList(contains("d.trang_thai=? AND d.ngay_ket_thuc>=?"),eq("%%"),eq("%%"),eq(0),any(LocalDate.class));
            else verify(jdbc).queryForList(contains("d.ngay_bat_dau<=?"),eq("%%"),eq("%%"),eq(1),any(LocalDate.class),any(LocalDate.class));
        }
    }
    @Test void sapMoChiKhiDuocBatVaChuaDenNgayBatDau() {
        var ngay=LocalDate.of(2026,11,3);
        for(int bat:new int[]{0,1}) {
            var row=new HashMap<String,Object>();row.put("trang_thai",bat);row.put("ngay_bat_dau",java.sql.Date.valueOf(ngay.plusDays(1)));row.put("ngay_ket_thuc",ngay.plusDays(5));
            assertEquals(bat==1?"Sắp mở":"Ngừng áp dụng",QuanLyDotGiamGiaService.tenTrangThai(row,ngay));
            row.put("ngay_bat_dau",ngay);
            assertEquals(bat==1?"Cho phép áp dụng":"Ngừng áp dụng",QuanLyDotGiamGiaService.tenTrangThai(row,ngay));
        }
    }
    @Test void locSapMoKhongLayDotBiTatHoacHetHan() {
        var jdbc=mock(JdbcTemplate.class);new QuanLyDotGiamGiaService(jdbc).tim("sale","3");
        var ngay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        verify(jdbc).queryForList(contains("d.trang_thai=1 AND d.ngay_bat_dau>? AND d.ngay_ket_thuc>=?"),eq("%sale%"),eq("%sale%"),eq(ngay),eq(ngay));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void chiTietTraNhanSapMoTheoNgayDaLuu() {
        var jdbc=mock(JdbcTemplate.class);var ngay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        when(jdbc.queryForList(contains("SELECT * FROM dot_giam_gia"),eq(2)))
                .thenReturn(List.of(Map.of("trang_thai",true,"ngay_bat_dau",ngay.plusDays(1),"ngay_ket_thuc",ngay.plusDays(5))));
        var row=new QuanLyDotGiamGiaService(jdbc).lay(2);
        assertEquals("Sắp mở",row.get("ten_trang_thai"));assertEquals(true,row.get("sap_mo"));assertEquals(false,row.get("het_han"));
    }
    @Test void chiTietTraTrangThaiHetHanMaVanGiuCongTacCu() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("SELECT * FROM dot_giam_gia"),eq(2)))
                .thenReturn(List.of(Map.of("trang_thai",true,"ngay_ket_thuc",LocalDate.of(2000,1,1))));
        var row=new QuanLyDotGiamGiaService(jdbc).lay(2);
        assertEquals("Hết hạn",row.get("ten_trang_thai"));assertEquals(true,row.get("het_han"));assertEquals(true,row.get("trang_thai"));
    }
}
