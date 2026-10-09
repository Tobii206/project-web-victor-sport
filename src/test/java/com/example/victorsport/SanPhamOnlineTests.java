package com.example.victorsport;

import com.example.victorsport.Controller.SanPhamOnlineController;
import com.example.victorsport.Service.SanPhamOnlineService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SanPhamOnlineTests {
    @Test
    void khongDuocChonPhienBanCuaSanPhamKhac() throws Exception {
        SanPhamOnlineService service = mock(SanPhamOnlineService.class);
        when(service.layPhienBanTheoId(9)).thenReturn(Map.of("id_san_pham", 2));
        MockHttpSession session = new MockHttpSession();
        trang(service).perform(post("/san-pham/1/chon").session(session).param("idPhienBan", "9").param("soLuong", "1"))
                .andExpect(redirectedUrl("/san-pham/1")).andExpect(flash().attributeExists("error"));
        assertNull(session.getAttribute("gioHangOnline"));
    }

    @Test
    void themVaoGioKiemTraTongSoLuongVaKhongDungGioTaiQuay() throws Exception {
        SanPhamOnlineService service = mock(SanPhamOnlineService.class);
        Map<String, Object> pb = Map.of("id_san_pham", 1, "so_luong", 3);
        when(service.layPhienBanTheoId(9)).thenReturn(pb);
        doCallRealMethod().when(service).kiemTraSoLuong(anyMap(), anyInt());
        MockHttpSession session = new MockHttpSession();
        MockMvc mvc = trang(service);
        mvc.perform(post("/san-pham/1/chon").session(session).param("idPhienBan", "9").param("soLuong", "2"))
                .andExpect(flash().attributeExists("success"));
        mvc.perform(post("/san-pham/1/chon").session(session).param("idPhienBan", "9").param("soLuong", "2"))
                .andExpect(flash().attributeExists("error"));
        assertEquals(Map.of(9, 2), session.getAttribute("gioHangOnline"));
        assertNull(session.getAttribute("gioHangTaiQuay"));
        verify(service, never()).datHang(anyMap(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void muaNgayChiLaySanPhamDaChonVaGiuGioCu() throws Exception {
        SanPhamOnlineService service = mock(SanPhamOnlineService.class);
        when(service.layPhienBanTheoId(9)).thenReturn(Map.of("id_san_pham", 1));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gioHangOnline", new HashMap<>(Map.of(20, 3)));
        trang(service).perform(post("/san-pham/1/chon").session(session).param("idPhienBan", "9")
                        .param("soLuong", "1").param("thaoTac", "mua"))
                .andExpect(redirectedUrl("/thanh-toan?loai=mua"));
        assertEquals(Map.of(9, 1), session.getAttribute("muaNgayOnline"));
        assertEquals(Map.of(20, 3), session.getAttribute("gioHangOnline"));
    }

    @Test
    void xoaMauDaNgungBanKhongCanDocDatabase() throws Exception {
        SanPhamOnlineService service = mock(SanPhamOnlineService.class);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("gioHangOnline", new HashMap<>(Map.of(9, 2)));
        trang(service).perform(post("/gio-hang/sua").session(session).param("idPhienBan", "9").param("soLuong", "0"))
                .andExpect(redirectedUrl("/gio-hang"));
        assertEquals(Map.of(), session.getAttribute("gioHangOnline"));
        verifyNoInteractions(service);
    }

    @Test
    void khongTaoHoaDonKhiTonKhoDaBiNguoiKhacMua() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        SanPhamOnlineService service = spy(new SanPhamOnlineService(jdbc));
        Map<String, Object> pb = new HashMap<>();
        pb.put("id", 9); pb.put("so_luong", 2); pb.put("so_luong_mua", 1);
        pb.put("gia_ban", new BigDecimal("500000")); pb.put("thanh_tien", new BigDecimal("500000"));
        doReturn(List.of(pb)).when(service).layGioHang(anyMap());
        assertThrows(IllegalArgumentException.class, () -> service.datHang(Map.of(9, 1), null,
                "Nguyễn An", "0900000000", "123 Hà Nội"));
        verify(jdbc, never()).queryForObject(anyString(), eq(Integer.class), any(), any(), any(), any(), any(), any());
    }

    @Test
    void chanSoLuongAmVaDonTrong() {
        SanPhamOnlineService service = new SanPhamOnlineService(mock(JdbcTemplate.class));
        assertThrows(IllegalArgumentException.class, () -> service.kiemTraSoLuong(Map.of("so_luong", 5), -1));
        assertThrows(IllegalArgumentException.class, () -> service.datHang(Map.of(), null,
                "Nguyễn An", "0900000000", "123 Hà Nội"));
    }

    private MockMvc trang(SanPhamOnlineService service) {
        return MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service)).build();
    }
}
