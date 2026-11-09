package com.example.victorsport;

import com.example.victorsport.Controller.SanPhamOnlineController;
import com.example.victorsport.Service.*;
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

class ChonMuaGioHangTests {
    MockHttpSession session() {
        var s=new MockHttpSession();s.setAttribute("csrfChonGio","token");
        s.setAttribute("gioHangOnline",new LinkedHashMap<>(Map.of(9,2,10,1)));return s;
    }
    @Test void chonTungMauHoacTatCaVaBoBaoGiaCu() throws Exception {
        var sp=mock(SanPhamOnlineService.class);var s=session();
        var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp,mock(DiaChiKhachHangService.class))).build();
        for(String[] ids:new String[][]{{"9"},{"9","10"}}) {
            s.setAttribute("baoGiaGhn_gioHangOnline","cũ");
            mvc.perform(post("/gio-hang/chon-mua").session(s).param("csrfChonGio","token").param("idMau",ids)).andExpect(redirectedUrl("/thanh-toan"));
            assertEquals(ids.length,((Set<?>)s.getAttribute("mauChonMuaOnline")).size());assertNull(s.getAttribute("baoGiaGhn_gioHangOnline"));
            assertEquals(Map.of(9,2,10,1),s.getAttribute("gioHangOnline"));
        }
    }
    @Test void chonRongMauNgoaiGioVaSaiTokenBiChan() throws Exception {
        var sp=mock(SanPhamOnlineService.class);var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp,mock(DiaChiKhachHangService.class))).build();
        mvc.perform(post("/gio-hang/chon-mua").session(session()).param("idMau","9")).andExpect(status().isForbidden());
        mvc.perform(post("/gio-hang/chon-mua").session(session()).param("csrfChonGio","token")).andExpect(redirectedUrl("/gio-hang")).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/gio-hang/chon-mua").session(session()).param("csrfChonGio","token").param("idMau","999")).andExpect(redirectedUrl("/gio-hang")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(sp);
    }
    @Test void checkoutChiDocMauDaChonChoCaKhachVaKhachVangLai() throws Exception {
        for(boolean login:new boolean[]{false,true}) {
            var sp=mock(SanPhamOnlineService.class);var s=session();s.setAttribute("mauChonMuaOnline",Set.of(9));
            if(login) {s.setAttribute("khachHangOnlineDangNhap",7);when(sp.layGioDaLuu(7)).thenReturn(new LinkedHashMap<>(Map.of(9,2,10,1)));}
            MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp,mock(DiaChiKhachHangService.class))).build()
                    .perform(get("/thanh-toan").session(s)).andExpect(model().attribute("gioDaChon",Map.of(9,2)));
            verify(sp).layGioHang(Map.of(9,2));
            assertEquals(Map.of(9,2,10,1),s.getAttribute("gioHangOnline"));
        }
    }
    @Test void datThanhCongChiXoaMauDaMuaDatLoiGiuNguyenGio() throws Exception {
        for(boolean login:new boolean[]{false,true}) {
            var sp=mock(SanPhamOnlineService.class);var s=session();s.setAttribute("mauChonMuaOnline",Set.of(9));
            Integer kh=login?7:null;
            if(login) {s.setAttribute("khachHangOnlineDangNhap",7);when(sp.layGioDaLuu(7)).thenReturn(new LinkedHashMap<>(Map.of(9,2,10,1)));}
            when(sp.datHang(eq(Map.of(9,2)),eq(kh),eq("An"),eq("0900000001"),eq("Hà Nội"),eq(false)))
                    .thenThrow(new IllegalArgumentException("Hết hàng")).thenReturn(10);
            var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp,mock(DiaChiKhachHangService.class))).build();
            mvc.perform(post("/thanh-toan").session(s).param("ten","An").param("sdt","0900000001").param("diaChi","Hà Nội")).andExpect(flash().attributeExists("error"));
            assertEquals(Map.of(9,2,10,1),s.getAttribute("gioHangOnline"));assertEquals(Set.of(9),s.getAttribute("mauChonMuaOnline"));
            mvc.perform(post("/thanh-toan").session(s).param("ten","An").param("sdt","0900000001").param("diaChi","Hà Nội")).andExpect(flash().attributeExists("success"));
            assertEquals(Map.of(10,1),s.getAttribute("gioHangOnline"));assertNull(s.getAttribute("mauChonMuaOnline"));
        }
    }
    @Test void khongTuMuaTatCaKhiMauDaChonBiXoa() throws Exception {
        var sp=mock(SanPhamOnlineService.class);var s=session();s.setAttribute("mauChonMuaOnline",Set.of(999));
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(sp,mock(DiaChiKhachHangService.class))).build()
                .perform(post("/thanh-toan").session(s).param("ten","An").param("sdt","0900000001").param("diaChi","Hà Nội")).andExpect(flash().attributeExists("error"));
        verify(sp,never()).datHang(anyMap(),any(),anyString(),anyString(),anyString(),anyBoolean());
    }
    SanPhamOnlineService service(JdbcTemplate jdbc) {
        var sp=spy(new SanPhamOnlineService(jdbc));
        when(jdbc.queryForList(contains("FROM khach_hang"),eq(7))).thenReturn(List.of(Map.of("id",7)));
        when(jdbc.queryForList(contains("SELECT id FROM gio_hang"),eq(7))).thenReturn(List.of(Map.of("id",5)));
        doReturn(new LinkedHashMap<>(Map.of(9,2,10,1))).when(sp).layGioDaLuu(7);
        var dong=Map.<String,Object>of("id",9,"so_luong_mua",2,"so_luong",5,"gia_ban",new BigDecimal("1000000"),"thanh_tien_goc",new BigDecimal("2000000"),"thanh_tien",new BigDecimal("2000000"));
        doReturn(List.of(dong)).when(sp).layGioHang(Map.of(9,2));
        when(jdbc.update(contains("UPDATE chi_tiet_san_pham"),any(),any(),any(),any())).thenReturn(1);
        when(jdbc.queryForObject(contains("INSERT INTO hoa_don"),eq(Integer.class),any(Object[].class))).thenReturn(10);
        when(jdbc.queryForList(contains("nha_cung_cap=N'COD'"))).thenReturn(List.of(Map.of("id",4)));
        return sp;
    }
    @Test void databaseChiTruTonVaXoaDongGioDaChon() {
        var jdbc=mock(JdbcTemplate.class);var sp=service(jdbc);
        assertEquals(10,sp.datHang(Map.of(9,2),7,"An","0900000001","Hà Nội",false,"COD",null,new BigDecimal("2000000")));
        verify(sp).layGioHang(Map.of(9,2));
        verify(jdbc).update(contains("UPDATE chi_tiet_gio_hang SET xoa_mem=1 WHERE id_gio_hang=? AND id_chi_tiet_san_pham=?"),eq(5),eq(9));
        verify(jdbc,never()).update(contains("UPDATE chi_tiet_gio_hang SET xoa_mem=1"),eq(5),eq(10));
    }
    @Test void databaseChanMauNgoaiGioVaSoLuongDaThayDoi() {
        for(Map<Integer,Integer> chon:List.of(Map.of(999,1),Map.of(9,3))) {
            var jdbc=mock(JdbcTemplate.class);var sp=service(jdbc);
            assertThrows(IllegalArgumentException.class,()->sp.datHang(chon,7,"An","0900000001","Hà Nội",false,"COD",null,null));
            verify(jdbc,never()).update(contains("UPDATE chi_tiet_san_pham"),any(Object[].class));
        }
    }
}
