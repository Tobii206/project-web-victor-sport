package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GioHangDatabaseTests {
    private MockHttpSession khach(int id) {
        var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",id);return s;
    }
    @Test void dangNhapKhoiPhucGioDatabaseVaBoGioKhachVangLai() throws Exception {
        var nv=mock(DangNhapService.class);var kh=mock(TaiKhoanService.class);var sp=mock(SanPhamOnlineService.class);
        when(kh.dangNhapKhachHang("khachmoi","123456")).thenReturn(7);
        when(sp.layGioDaLuu(7)).thenReturn(new LinkedHashMap<>(Map.of(9,2)));
        var session=new MockHttpSession();session.setAttribute("gioHangOnline",Map.of(99,5));session.setAttribute("muaNgayOnline",Map.of(99,1));
        MockMvcBuilders.standaloneSetup(new DangNhapController(nv,kh,sp)).build()
                .perform(post("/login").session(session).param("taiKhoan","khachmoi").param("matKhau","123456"))
                .andExpect(redirectedUrl("/"));
        assertEquals(Map.of(9,2),session.getAttribute("gioHangOnline"));assertNull(session.getAttribute("muaNgayOnline"));
    }
    @Test void themVaSuaGhiDungKhachDaDangNhap() throws Exception {
        var sp=mock(SanPhamOnlineService.class);var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build();
        when(sp.layPhienBanTheoId(9)).thenReturn(Map.of("id_san_pham",1));
        when(sp.themVaoGioDaLuu(7,9,2)).thenReturn(Map.of(9,2));
        when(sp.suaGioDaLuu(7,9,0)).thenReturn(Map.of());var s=khach(7);
        mvc.perform(post("/san-pham/1/chon").session(s).param("idPhienBan","9").param("soLuong","2"))
                .andExpect(flash().attributeExists("success"));
        assertEquals(Map.of(9,2),s.getAttribute("gioHangOnline"));verify(sp).themVaoGioDaLuu(7,9,2);
        mvc.perform(post("/gio-hang/sua").session(s).param("idPhienBan","9").param("soLuong","0"));
        assertEquals(Map.of(),s.getAttribute("gioHangOnline"));verify(sp).suaGioDaLuu(7,9,0);
    }
    @Test void datHangThanhCongMoiXoaBanGioTrongPhien() throws Exception {
        var sp=mock(SanPhamOnlineService.class);var s=khach(7);s.setAttribute("gioHangOnline",Map.of(99,5));
        var gio=new LinkedHashMap<>(Map.of(9,2));when(sp.layGioDaLuu(7)).thenReturn(gio);
        when(sp.datHang(gio,7,"Khách","0325327602","Cầu Giấy",false)).thenThrow(new IllegalArgumentException("Hết hàng"));
        var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build();
        mvc.perform(post("/thanh-toan").session(s).param("ten","Khách").param("sdt","0325327602").param("diaChi","Cầu Giấy"))
                .andExpect(flash().attribute("error","Hết hàng"));
        assertEquals(Map.of(9,2),s.getAttribute("gioHangOnline"));
        doReturn(10).when(sp).datHang(gio,7,"Khách","0325327602","Cầu Giấy",false);
        mvc.perform(post("/thanh-toan").session(s).param("ten","Khách").param("sdt","0325327602").param("diaChi","Cầu Giấy"))
                .andExpect(redirectedUrl("/don-hang/10"));
        assertTrue(gio.isEmpty());
    }
    @Test void muaNgayKhongGhiHoacXoaGioDaLuu() throws Exception {
        var sp=mock(SanPhamOnlineService.class);var s=khach(7);
        s.setAttribute("gioHangOnline",Map.of(9,2));s.setAttribute("muaNgayOnline",new LinkedHashMap<>(Map.of(20,1)));
        when(sp.datHang(Map.of(20,1),7,"Khách","0325327602","Cầu Giấy",true)).thenReturn(10);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build()
                .perform(post("/thanh-toan").session(s).param("loai","mua").param("ten","Khách").param("sdt","0325327602").param("diaChi","Cầu Giấy"));
        assertEquals(Map.of(9,2),s.getAttribute("gioHangOnline"));verify(sp,never()).layGioDaLuu(anyInt());
    }
}
