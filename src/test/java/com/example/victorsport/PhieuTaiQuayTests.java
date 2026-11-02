package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.*;
import com.example.victorsport.Service.BanHangTaiQuayService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PhieuTaiQuayTests {
    MockHttpSession session() {
        var session=new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"nv@example.com","Nhân viên","Bán hàng"));
        var gio=new GioHangTaiQuay();gio.themHoacCapNhat(new SanPhamTrongGio(1,"SP1","Giày","Trắng","40",1,5,new BigDecimal("3000000")));
        session.setAttribute("gioHangTaiQuay",gio);return session;
    }
    LuaChonPhieuGiamGia coupon() {return new LuaChonPhieuGiamGia(3,"PGG00003","Giảm 200K",true,new BigDecimal("200000"),new BigDecimal("2000000"));}
    @Test void tuChonHienDungMaPhieuVaKhongGhimLuaChonTuDongVaoSession() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var session=session();
        when(service.layPhieuGiamGiaKhaDung(any())).thenReturn(List.of(coupon()));
        when(service.tinhTongKet(any(),isNull())).thenReturn(new TongKetBanHang(new BigDecimal("3000000"),new BigDecimal("200000"),new BigDecimal("2800000"),"Giảm 200K",3));
        MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build()
                .perform(get("/pos").session(session)).andExpect(model().attribute("selectedDiscountCouponId",3));
        assertNull(session.getAttribute("idPhieuGiamGiaTaiQuay"));
    }
    @Test void tuChonPhieuBangDotVaTonTrongPhieuNhanVienChonDuGiamItHon() throws Exception {
        var jdbc=mock(JdbcTemplate.class);var service=new BanHangTaiQuayService(jdbc);
        var gio=(GioHangTaiQuay)session().getAttribute("gioHangTaiQuay");
        when(jdbc.queryForList(contains("FROM chi_tiet_dot_giam_gia"),eq(1))).thenReturn(List.of(Map.of("loai_giam_gia",true,"gia_tri",new BigDecimal("300000"))));
        org.mockito.stubbing.Answer<Object> phieu=invocation->{
            var rs=mock(java.sql.ResultSet.class);rs.getClass();
            when(rs.getInt("id")).thenReturn(3);when(rs.getString("ten_phieu_giam_gia")).thenReturn("Phiếu thử");
            when(rs.getBoolean("loai_phieu_giam_gia")).thenReturn(true);
            when(rs.getBigDecimal("gia_tri_giam_gia")).thenReturn(invocation.getArguments().length==3?new BigDecimal("300000"):new BigDecimal("200000"));
            return List.of(((RowMapper<?>)invocation.getArgument(1)).mapRow(rs,0));
        };
        doAnswer(phieu).when(jdbc).query(anyString(),any(RowMapper.class),any(BigDecimal.class));
        doAnswer(phieu).when(jdbc).query(anyString(),any(RowMapper.class),eq(3),any(BigDecimal.class));
        assertEquals(3,service.tinhTongKet(gio).getIdPhieuGiamGia());
        var manual=service.tinhTongKet(gio,3);
        assertEquals(3,manual.getIdPhieuGiamGia());assertEquals(new BigDecimal("2800000.00"),manual.getTongThanhToan());
    }
    @Test void khongChoChonPhieuKhongConDuDieuKien() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var session=session();session.setAttribute("idPhieuGiamGiaTaiQuay",3);
        MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build()
                .perform(post("/pos/phieu-giam-gia/chon").session(session).param("discountCouponId","3"))
                .andExpect(redirectedUrl("/pos")).andExpect(flash().attributeExists("error"));
        assertNull(session.getAttribute("idPhieuGiamGiaTaiQuay"));
    }
    @Test void vanChonDuocPhieuDuDieuKienVaThongBaoSoSanhUuDai() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var session=session();
        when(service.layPhieuGiamGiaKhaDung(any())).thenReturn(List.of(coupon()));
        MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build()
                .perform(post("/pos/phieu-giam-gia/chon").session(session).param("discountCouponId","3"))
                .andExpect(redirectedUrl("/pos")).andExpect(flash().attributeExists("success"));
        assertEquals(3,session.getAttribute("idPhieuGiamGiaTaiQuay"));
    }
    @Test void phieuDaChonHetHanSeBoChonKhiTaiLai() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var session=session();session.setAttribute("idPhieuGiamGiaTaiQuay",3);
        MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build()
                .perform(get("/pos").session(session)).andExpect(view().name("admin/pos/show"))
                .andExpect(model().attribute("discountCoupons",List.of()));
        assertNull(session.getAttribute("idPhieuGiamGiaTaiQuay"));
        verify(service).tinhTongKet(any(),isNull());
    }
    @Test void caDanhSachVaTuChonVaChonTrucTiepDeuLoaiFreeship() {
        var jdbc=mock(JdbcTemplate.class);var service=new BanHangTaiQuayService(jdbc);
        var goc=new BigDecimal("3000000");var gio=new GioHangTaiQuay();
        service.layPhieuGiamGiaKhaDung(goc);service.tinhTongKet(gio);service.tinhTongKet(gio,3);
        var sql=org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc,times(2)).query(sql.capture(),any(RowMapper.class),any(BigDecimal.class));
        verify(jdbc).query(sql.capture(),any(RowMapper.class),eq(3),any(BigDecimal.class));
        for(String query:sql.getAllValues()) {
            assertTrue(query.contains("ISNULL(ap_dung_van_chuyen, 0) = 0"));assertFalse(query.contains("NOT LIKE"));
            assertTrue(query.contains("ISNULL(hoa_don_toi_thieu, 0) <= ?"));
        }
    }
}
