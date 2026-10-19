package com.example.victorsport;

import com.example.victorsport.Dto.DotGiamGiaForm;
import com.example.victorsport.Service.QuanLyDotGiamGiaService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatLaiDotGiamGiaTests {
    private final LocalDate homNay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    private DotGiamGiaForm form(LocalDate dau,LocalDate cuoi,int trangThai) {
        var f=new DotGiamGiaForm();f.setTen_dot_giam_gia("Ưu đãi");f.setLoai_giam_gia(0);
        f.setGia_tri_giam_gia(BigDecimal.TEN);f.setNgay_bat_dau(dau);f.setNgay_ket_thuc(cuoi);
        f.setMuc_uu_tien(1);f.setTrang_thai(trangThai);return f;
    }
    private QuanLyDotGiamGiaService service(JdbcTemplate jdbc,Object trangThaiCu) {
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(8)))
            .thenReturn(List.of(Map.of("id",8,"trang_thai",trangThaiCu)));
        return new QuanLyDotGiamGiaService(jdbc);
    }
    @Test void batLaiTrongNgayBatDauHoacNgayKetThucDeuThongBao() {
        for(LocalDate[] ngay:new LocalDate[][]{{homNay,homNay.plusDays(1)},{homNay.minusDays(1),homNay},{homNay,homNay}}) {
            var jdbc=mock(JdbcTemplate.class);var service=service(jdbc,false);
            var ketQua=service.luuVaKiemTraThongBao(8,form(ngay[0],ngay[1],1));
            assertEquals(8,ketQua.id());assertTrue(ketQua.guiEmail());
        }
    }
    @Test void chuaDenNgayHetHanDangBatHoacVanNgungKhongThongBao() {
        for(int i=0;i<4;i++) {
            var jdbc=mock(JdbcTemplate.class);var service=service(jdbc,i==2?1:0);
            var f=form(i==0?homNay.plusDays(1):homNay.minusDays(2),i==1?homNay.minusDays(1):homNay.plusDays(2),i==3?0:1);
            assertFalse(service.luuVaKiemTraThongBao(8,f).guiEmail());
        }
    }
    @Test void haiLanLuuLienTiepChiLanChuyenTrangThaiDuocThongBao() {
        var jdbc=mock(JdbcTemplate.class);var dangBat=new AtomicBoolean(false);
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(8)))
            .thenAnswer(i->List.of(Map.of("id",8,"trang_thai",dangBat.getAndSet(true))));
        var service=new QuanLyDotGiamGiaService(jdbc);var f=form(homNay,homNay.plusDays(1),1);
        assertTrue(service.luuVaKiemTraThongBao(8,f).guiEmail());
        assertFalse(service.luuVaKiemTraThongBao(8,f).guiEmail());
        verify(jdbc,times(2)).queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(8));
    }
    @Test void luuDatabaseThatBaiKhongTraKetQuaCanGuiEmail() {
        var jdbc=mock(JdbcTemplate.class);var service=service(jdbc,false);
        when(jdbc.update(anyString(),any(Object[].class)))
            .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("Lỗi thử nghiệm"));
        assertThrows(org.springframework.dao.DataAccessException.class,
            ()->service.luuVaKiemTraThongBao(8,form(homNay,homNay,1)));
    }
}
