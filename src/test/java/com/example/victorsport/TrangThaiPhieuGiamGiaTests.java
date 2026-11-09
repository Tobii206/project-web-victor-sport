package com.example.victorsport;

import com.example.victorsport.Service.QuanLyPhieuGiamGiaService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrangThaiPhieuGiamGiaTests {
    private final LocalDate ngay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Test void caHaiLoaiPhieuCoBonTrangThaiVaTinhDenHetNgayKetThuc() {
        for(int loai:List.of(0,1)) {
            var jdbc=mock(JdbcTemplate.class);
            var row=new HashMap<String,Object>();
            row.put("ap_dung_van_chuyen",loai);row.put("trang_thai",true);
            row.put("ngay_bat_dau",java.sql.Date.valueOf(ngay.plusDays(1)));
            row.put("ngay_ket_thuc",java.sql.Date.valueOf(ngay.plusDays(2)));
            when(jdbc.queryForList(anyString(),eq(7))).thenAnswer(i->List.of(new HashMap<>(row)));
            var service=new QuanLyPhieuGiamGiaService(jdbc);
            assertEquals("Sắp mở",service.lay(7).get("ten_trang_thai"));
            assertEquals(true,service.lay(7).get("sap_mo"));
            row.put("ngay_bat_dau",ngay);row.put("ngay_ket_thuc",ngay);
            assertEquals("Cho phép áp dụng",service.lay(7).get("ten_trang_thai"));
            row.put("trang_thai",false);
            assertEquals("Ngừng áp dụng",service.lay(7).get("ten_trang_thai"));
            row.put("ngay_ket_thuc",ngay.minusDays(1));
            assertEquals("Hết hạn",service.lay(7).get("ten_trang_thai"));
            assertEquals(true,service.lay(7).get("het_han"));
            assertEquals(false,service.lay(7).get("trang_thai"));
            verify(jdbc,never()).update(anyString(),any(Object[].class));
        }
    }

    @Test void locTrangThaiDocLapLoaiPhieuVaKhongTrungCacNhom() {
        for(String loai:List.of("0","1")) {
            var jdbc=mock(JdbcTemplate.class);var service=new QuanLyPhieuGiamGiaService(jdbc);
            service.tim("a","2",loai);
            verify(jdbc).queryForList(contains("ngay_ket_thuc<?"),eq("%a%"),eq("%a%"),eq(ngay),eq(Integer.valueOf(loai)));
            service.tim("a","3",loai);
            verify(jdbc).queryForList(contains("trang_thai=1 AND ngay_bat_dau>? AND ngay_ket_thuc>=?"),eq("%a%"),eq("%a%"),eq(ngay),eq(ngay),eq(Integer.valueOf(loai)));
            service.tim("a","1",loai);
            verify(jdbc).queryForList(contains("trang_thai=? AND ngay_ket_thuc>=? AND ngay_bat_dau<=?"),eq("%a%"),eq("%a%"),eq(1),eq(ngay),eq(ngay),eq(Integer.valueOf(loai)));
            service.tim("a","0",loai);
            verify(jdbc).queryForList(contains("trang_thai=? AND ngay_ket_thuc>=?"),eq("%a%"),eq("%a%"),eq(0),eq(ngay),eq(Integer.valueOf(loai)));
        }
    }

    @Test void danhSachCungTraNhanTrangThaiNhuChiTiet() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(
                Map.of("trang_thai",1,"ngay_bat_dau",ngay.plusDays(1),"ngay_ket_thuc",ngay.plusDays(2)),
                Map.of("trang_thai",1,"ngay_ket_thuc",ngay.minusDays(1))));
        var rows=new QuanLyPhieuGiamGiaService(jdbc).tim("","", "1");
        assertEquals("Sắp mở",rows.get(0).get("ten_trang_thai"));
        assertEquals("Hết hạn",rows.get(1).get("ten_trang_thai"));
    }
}
