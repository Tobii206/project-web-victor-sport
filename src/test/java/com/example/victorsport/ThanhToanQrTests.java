package com.example.victorsport;

import com.example.victorsport.Controller.SanPhamOnlineController;
import com.example.victorsport.Service.SanPhamOnlineService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ThanhToanQrTests {
    @Test void chonQrTaoDonVaChuyenDenMaQrChoKhachVangLai() throws Exception {
        var service=mock(SanPhamOnlineService.class);
        var session=new MockHttpSession();
        var gio=new LinkedHashMap<Integer,Integer>(); gio.put(9,1);
        session.setAttribute("gioHangOnline",gio);
        when(service.datHang(gio,null,"Long","0325327602","Cầu Giấy",false,"QR")).thenReturn(10);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build()
                .perform(post("/thanh-toan").session(session).param("ten","Long").param("sdt","0325327602")
                        .param("diaChi","Cầu Giấy").param("phuongThuc","QR"))
                .andExpect(redirectedUrl("/thanh-toan/qr/10"));
        assertEquals(true,session.getAttribute("donQr_10"));
        assertTrue(gio.isEmpty());
    }

    @Test void datQrLoiGiuGioVaLuaChon() throws Exception {
        var service=mock(SanPhamOnlineService.class);
        var session=new MockHttpSession(); var gio=new LinkedHashMap<>(Map.of(9,1));
        session.setAttribute("gioHangOnline",gio);
        when(service.datHang(anyMap(),isNull(),anyString(),anyString(),anyString(),eq(false),eq("QR")))
                .thenThrow(new IllegalArgumentException("Chưa có tài khoản QR."));
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build()
                .perform(post("/thanh-toan").session(session).param("ten","Long").param("sdt","0325327602")
                        .param("diaChi","Cầu Giấy").param("phuongThuc","QR"))
                .andExpect(redirectedUrl("/thanh-toan?loai=gio"))
                .andExpect(flash().attribute("phuongThucDaChon","QR"))
                .andExpect(flash().attributeExists("error"));
        assertEquals(Map.of(9,1),gio);
        assertNull(session.getAttribute("donQr_10"));
    }

    @Test void xemQrCanDungKhachHoacDungPhienTaoDon() throws Exception {
        var service=mock(SanPhamOnlineService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build();
        mvc.perform(get("/thanh-toan/qr/10")).andExpect(status().isNotFound());
        verify(service).layThanhToanQr(10,null,false);
        var session=new MockHttpSession();session.setAttribute("khachHangOnlineDangNhap",7);
        when(service.layThanhToanQr(10,7,false)).thenReturn(Map.of("ma_hoa_don","HD00010"));
        mvc.perform(get("/thanh-toan/qr/10").session(session)).andExpect(status().isOk())
                .andExpect(view().name("KhachHang/thanh-toan-qr"))
                .andExpect(header().string("Cache-Control","no-store"));
    }

    @Test void chuaCauHinhVaPhuongThucGiaKhongTaoHoaDon() {
        var jdbc=mock(JdbcTemplate.class); var service=new SanPhamOnlineService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.datHang(Map.of(9,1),null,"Long","0325327602","Hà Nội",false,"QR"));
        assertThrows(IllegalArgumentException.class,()->service.datHang(Map.of(9,1),null,"Long","0325327602","Hà Nội",false,"FAKE"));
        assertNull(service.layThanhToanQr(10,null,false));
        verifyNoInteractions(jdbc);
    }
}
