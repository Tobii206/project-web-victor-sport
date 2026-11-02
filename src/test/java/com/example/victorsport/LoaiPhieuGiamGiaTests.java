package com.example.victorsport;

import com.example.victorsport.Config.LoaiPhieuGiamGiaConfig;
import com.example.victorsport.Dto.PhieuGiamGiaForm;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoaiPhieuGiamGiaTests {
    @Test void loaiPhieuDocLapVoiTenVaPhamViHaNoiTuongMinh() {
        var service=new SanPhamOnlineService(mock(JdbcTemplate.class));
        assertTrue(service.laPhieuFreeship(Map.of("ten_phieu_giam_gia","Ưu đãi tháng này","ap_dung_van_chuyen",true)));
        assertFalse(service.laPhieuFreeship(Map.of("ten_phieu_giam_gia","Freeship nhưng là tên tùy đặt","ap_dung_van_chuyen",false)));
        assertFalse(service.laFreeshipNoiThanh(Map.of("ten_phieu_giam_gia","Freeship nội thành","ap_dung_van_chuyen",true,"chi_ap_dung_ha_noi",false)));
        assertTrue(service.laFreeshipNoiThanh(Map.of("ten_phieu_giam_gia","Phiếu A","ap_dung_van_chuyen",1,"chi_ap_dung_ha_noi",1)));
    }
    PhieuGiamGiaForm form(int ship,int haNoi) {
        var f=new PhieuGiamGiaForm();f.setTen_phieu_giam_gia("Tên tùy chọn");f.setLoai_phieu_giam_gia(1);
        f.setGia_tri_giam_gia(new BigDecimal("30000"));f.setNgay_bat_dau(LocalDate.now());f.setNgay_ket_thuc(LocalDate.now().plusDays(1));
        f.setAp_dung_van_chuyen(ship);f.setChi_ap_dung_ha_noi(haNoi);f.setSo_luong_cu(10);f.setSo_luong_su_dung(10);
        return f;
    }
    @Test void luuLoaiVaPhamViKhongDuaVaoTen() {
        for(int ship:new int[]{0,1}) {
            var jdbc=mock(JdbcTemplate.class);var service=new QuanLyPhieuGiamGiaService(jdbc);
            when(jdbc.update(anyString(),any(Object[].class))).thenReturn(1);
            service.luu(3,form(ship,1));
            var sql=org.mockito.ArgumentCaptor.forClass(String.class);
            var args=org.mockito.ArgumentCaptor.forClass(Object[].class);
            verify(jdbc).update(sql.capture(),args.capture());
            assertTrue(sql.getValue().contains("ap_dung_van_chuyen=?"));
            Object[] values=args.getValue();assertEquals(ship,values[values.length-4]);assertEquals(ship==1?1:0,values[values.length-3]);
        }
    }
    @Test void locLoaiPhieuBangGiaTriRieng() {
        var jdbc=mock(JdbcTemplate.class);new QuanLyPhieuGiamGiaService(jdbc).tim("a","1","1");
        verify(jdbc).queryForList(contains("ap_dung_van_chuyen=?"),eq("%a%"),eq("%a%"),eq(1),eq(1));
    }
    @Test void chanLoaiKhongHopLeTruocKhiGhi() {
        var jdbc=mock(JdbcTemplate.class);var service=new QuanLyPhieuGiamGiaService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.luu(3,form(2,0)));
        var f=form(1,0);f.setAp_dung_van_chuyen(null);
        assertThrows(IllegalArgumentException.class,()->service.luu(3,f));verifyNoInteractions(jdbc);
    }
    @Test void nangCapKhongXoaDuLieuVaChiPhanLoaiTenKhiChuaCoCot() {
        var jdbc=mock(JdbcTemplate.class);new LoaiPhieuGiamGiaConfig(jdbc).afterPropertiesSet();
        var sql=org.mockito.ArgumentCaptor.forClass(String.class);verify(jdbc).execute(sql.capture());
        assertTrue(sql.getValue().contains("IF COL_LENGTH('phieu_giam_gia','ap_dung_van_chuyen') IS NULL"));
        assertTrue(sql.getValue().contains("ROLLBACK TRANSACTION"));
        assertFalse(sql.getValue().contains("DROP TABLE"));assertFalse(sql.getValue().contains("DELETE FROM"));
    }
}
