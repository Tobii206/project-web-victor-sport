package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.*;
import com.example.victorsport.Service.BanHangTaiQuayService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TienKhachThanhToanTaiQuayTests {
    private NguoiDungDangNhap nhanVien() {return new NguoiDungDangNhap(2,"nv@example.com","Nhân viên","Bán hàng");}
    private GioHangTaiQuay gio() {
        var gio=new GioHangTaiQuay();gio.themHoacCapNhat(new SanPhamTrongGio(1,"SP1","Giày","Trắng","40",1,5,new BigDecimal("1000000")));return gio;
    }
    private BanHangTaiQuayService service(JdbcTemplate jdbc) {
        var service=spy(new BanHangTaiQuayService(jdbc));
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"),eq(10),eq(2))).thenReturn(List.of(Map.of("id",10)));
        when(jdbc.queryForList(contains("FROM chi_tiet_san_pham"),eq(Integer.class),eq(1))).thenReturn(List.of(5));
        doReturn(new TongKetBanHang(new BigDecimal("1000000"),new BigDecimal("200000"),new BigDecimal("800000.00"),"Ưu đãi",null))
                .when(service).tinhTongKet(any(),nullable(Integer.class));
        return service;
    }
    private BanHangTaiQuayService.YeuCauThanhToan yeuCau(BigDecimal tien) {
        return new BanHangTaiQuayService.YeuCauThanhToan(null,null,null,null,null,1,null,null,tien);
    }
    @Test void dungSoTienSauUuDaiMoiHoanTat() {
        var jdbc=mock(JdbcTemplate.class);var service=service(jdbc);
        assertEquals(10,service.thanhToan(gio(),yeuCau(new BigDecimal("800000")),nhanVien(),10));
        verify(jdbc).update(contains("trang_thai_hien_tai = 5"),eq(10));
        verify(jdbc,times(1)).update(contains("UPDATE chi_tiet_san_pham"),eq(1),eq(2),eq(1));
    }
    @Test void thieuThuaBoTrongVaSoLeSaiKhongTaoHoaDonHayTruKho() {
        for(var tien:Arrays.asList(null,new BigDecimal("700000"),new BigDecimal("900000"),new BigDecimal("-1"),new BigDecimal("800000.001"))) {
            var jdbc=mock(JdbcTemplate.class);var service=service(jdbc);
            var error=assertThrows(IllegalArgumentException.class,()->service.thanhToan(gio(),yeuCau(tien),nhanVien(),10));
            assertTrue(error.getMessage().contains("đúng bằng"));
            verify(jdbc,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void soTienCuKhongDuocDungKhiUuDaiDaThayDoi() {
        var jdbc=mock(JdbcTemplate.class);var service=service(jdbc);
        doReturn(new TongKetBanHang(new BigDecimal("1000000"),BigDecimal.ZERO,new BigDecimal("1000000"),"Không áp dụng",null))
                .when(service).tinhTongKet(any(),nullable(Integer.class));
        assertThrows(IllegalArgumentException.class,()->service.thanhToan(gio(),yeuCau(new BigDecimal("800000")),nhanVien(),10));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void formGuiDungTienVaDonChoKhongBatNhapTien() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var session=new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,nhanVien());
        var mvc=MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build();
        mvc.perform(post("/pos/checkout").session(session).param("paidAmount","800000.00"))
                .andExpect(redirectedUrl("/pos")).andExpect(flash().attributeExists("success"));
        verify(service).thanhToan(any(),argThat(y -> new BigDecimal("800000.00").equals(y.getTienKhachDaThanhToan())),any(),isNull());
        mvc.perform(post("/pos/checkout").session(session).param("hanhDong","luu-don-cho"))
                .andExpect(redirectedUrl("/pos")).andExpect(flash().attributeExists("success"));
        verify(service).luuDonCho(any(),argThat(y -> y.getTienKhachDaThanhToan()==null),any(),isNull());
    }
    @Test void soTienSaiDinhDangKhongDuocGuiVaoService() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var session=new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,nhanVien());
        var mvc=MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build();
        for(String tien:List.of("","abc","-1","1e10","800000.001"))
            mvc.perform(post("/pos/checkout").session(session).param("paidAmount",tien))
                    .andExpect(redirectedUrl("/pos")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }
}
