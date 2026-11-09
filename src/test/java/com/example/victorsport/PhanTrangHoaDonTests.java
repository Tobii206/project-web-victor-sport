package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.HoaDonService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PhanTrangHoaDonTests {
    private JdbcTemplate jdbc(int tong) {
        var j=mock(JdbcTemplate.class);
        when(j.queryForObject(contains("SELECT COUNT(*) FROM hoa_don"),eq(Integer.class),any(Object[].class))).thenReturn(tong);
        return j;
    }
    @Test void moiTrangLayDungMuoiDonVaGiuThuTuMoiNhat() {
        var j=jdbc(25);var page=new HoaDonService(j).layTrangHoaDon("",null,"","",null,null,2);
        assertEquals(2,page.get("trang"));assertEquals(3,page.get("soTrang"));assertEquals(25,page.get("tongHoaDon"));
        assertEquals(11,page.get("tuDong"));assertEquals(20L,page.get("denDong"));
        verify(j).queryForList(contains("ORDER BY COALESCE(ngay_thanh_toan,ngay_tao) DESC,id DESC OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY"),
                eq("%%"),eq("%%"),eq("%%"),eq(10));
    }
    @Test void trangCuoiVaTrangVuotPhamViKhongHienBangTrongGia() {
        for(int requested:List.of(3,999,Integer.MAX_VALUE)) {
            var j=jdbc(21);var page=new HoaDonService(j).layTrangHoaDon("",null,"","",null,null,requested);
            assertEquals(3,page.get("trang"));assertEquals(21,page.get("tuDong"));assertEquals(21L,page.get("denDong"));
            verify(j).queryForList(contains("OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY"),eq("%%"),eq("%%"),eq("%%"),eq(20));
        }
    }
    @Test void khongCoKetQuaVaTrangAmVanAnToan() {
        for(int requested:List.of(-3,0,8)) {
            var j=jdbc(0);var page=new HoaDonService(j).layTrangHoaDon("",null,"","",null,null,requested);
            assertEquals(1,page.get("trang"));assertEquals(1,page.get("soTrang"));assertEquals(0,page.get("tuDong"));assertEquals(0L,page.get("denDong"));
            verify(j).queryForList(contains("FETCH NEXT 10 ROWS ONLY"),eq("%%"),eq("%%"),eq("%%"),eq(0));
        }
    }
    @Test void demVaLayDuLieuDungChungBoLocVaThamSo() {
        var j=jdbc(15);var tu=LocalDate.of(2026,11,1);var den=LocalDate.of(2026,11,7);
        new HoaDonService(j).layTrangHoaDon(" HD ",4,"tai-quay","da-thanh-toan",tu,den,2);
        var sql=org.mockito.ArgumentCaptor.forClass(String.class);var args=org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(j).queryForObject(sql.capture(),eq(Integer.class),args.capture());
        assertTrue(sql.getValue().contains("loai_don IN (0,2)"));assertTrue(sql.getValue().contains(")>=ISNULL(tong_tien_sau_giam,0)"));
        assertArrayEquals(new Object[]{"%HD%","%HD%","%HD%",4,tu.atStartOfDay(),den.plusDays(1).atStartOfDay()},args.getValue());
        var dataSql=org.mockito.ArgumentCaptor.forClass(String.class);var dataArgs=org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(j).queryForList(dataSql.capture(),dataArgs.capture());
        assertTrue(dataSql.getValue().contains(sql.getValue().substring("SELECT COUNT(*) FROM hoa_don".length())));
        assertArrayEquals(new Object[]{"%HD%","%HD%","%HD%",4,tu.atStartOfDay(),den.plusDays(1).atStartOfDay(),10},dataArgs.getValue());
    }
    @Test void soTrangTronChucVaCuaSoChiHienToiDaNamNutSo() {
        var j=jdbc(100);var page=new HoaDonService(j).layTrangHoaDon("",null,"","",null,null,10);
        assertEquals(10,page.get("soTrang"));assertEquals(6,page.get("trangBatDau"));assertEquals(10,page.get("trangKetThuc"));
    }
    @Test void dieuKienLocSaiKhongDuocTruyVanDemHayDuLieu() {
        var j=mock(JdbcTemplate.class);var s=new HoaDonService(j);
        assertThrows(IllegalArgumentException.class,()->s.layTrangHoaDon("",9,"","",null,null,1));
        verifyNoInteractions(j);
    }
    @Test void controllerNhanSoTrangVaTraThongTinPhanTrang() throws Exception {
        var service=mock(HoaDonService.class);var session=new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"nv@example.com","Nhân viên","Bán hàng"));
        when(service.layTrangHoaDon(eq("HD"),isNull(),eq("online"),eq(""),isNull(),isNull(),eq(2)))
                .thenReturn(Map.of("danhSachHoaDon",List.of(),"trang",2,"soTrang",3,"tongHoaDon",25));
        MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build()
                .perform(get("/quan-ly/hoa-don").session(session).param("trang","2").param("tuKhoa","HD").param("loaiDon","online"))
                .andExpect(model().attribute("trang",2)).andExpect(model().attribute("soTrang",3)).andExpect(model().attribute("tongHoaDon",25))
                .andExpect(model().attribute("tuKhoa","HD")).andExpect(model().attribute("loaiDon","online"));
    }
}
