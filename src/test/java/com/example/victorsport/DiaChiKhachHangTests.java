package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DiaChiKhachHangTests {
    private MockHttpSession khach(){var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);return s;}

    @Test void tuDienThongTinKhachVaDiaChiMacDinhKhiThanhToan() throws Exception {
        var online=mock(SanPhamOnlineService.class);var dc=mock(DiaChiKhachHangService.class);
        when(online.layThongTinNguoiNhan(7)).thenReturn(Map.of("ten_khach_hang","Nguyễn Văn An","so_dien_thoai","0325327602"));
        when(dc.danhSach(7)).thenReturn(List.of(Map.of("id",10,"dia_chi_day_du","Số 12, Cầu Giấy, Hà Nội")));
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(online,dc)).build()
                .perform(get("/thanh-toan").session(khach()))
                .andExpect(model().attribute("ten","Nguyễn Văn An"))
                .andExpect(model().attribute("sdt","0325327602"))
                .andExpect(model().attribute("diaChi","Số 12, Cầu Giấy, Hà Nội"))
                .andExpect(model().attribute("idDiaChiDaChon",10))
                .andExpect(header().string("Cache-Control","no-store"));
    }

    @Test void giuThongTinDaSuaSauKhiDatHangLoi() throws Exception {
        var online=mock(SanPhamOnlineService.class);var dc=mock(DiaChiKhachHangService.class);
        when(online.layThongTinNguoiNhan(7)).thenReturn(Map.of("ten_khach_hang","Tên tài khoản","so_dien_thoai","0325327602"));
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(online,dc)).build()
                .perform(get("/thanh-toan").session(khach()).flashAttr("ten","Người nhận khác")
                        .flashAttr("sdt","0901234567").flashAttr("diaChi","Địa chỉ khác"))
                .andExpect(model().attribute("ten","Người nhận khác"))
                .andExpect(model().attribute("sdt","0901234567"))
                .andExpect(model().attribute("diaChi","Địa chỉ khác"));
    }

    @Test void khachChuaDangNhapVanTuNhapThongTin() throws Exception {
        var online=mock(SanPhamOnlineService.class);var dc=mock(DiaChiKhachHangService.class);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(online,dc)).build()
                .perform(get("/thanh-toan"))
                .andExpect(model().attributeDoesNotExist("ten","sdt","diaChiDaLuu"));
        verify(online,never()).layThongTinNguoiNhan(anyInt());
        verifyNoInteractions(dc);
    }

    @Test void phaiDangNhapKhachHangMoiQuanLyDiaChi() throws Exception {
        var service=mock(DiaChiKhachHangService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new DiaChiKhachHangController(service)).build();
        mvc.perform(get("/tai-khoan/dia-chi")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/tai-khoan/dia-chi/10/xoa")).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
        mvc.perform(post("/tai-khoan/dia-chi/10/mac-dinh").session(khach()).param("idKhachHang","99"))
                .andExpect(redirectedUrl("/tai-khoan/dia-chi")).andExpect(flash().attributeExists("success"));
        verify(service).datMacDinh(7,10);
    }

    @Test void khongLuuDiaChiThieuThongTin() throws Exception {
        var service=mock(DiaChiKhachHangService.class);
        MockMvcBuilders.standaloneSetup(new DiaChiKhachHangController(service)).build()
                .perform(post("/tai-khoan/dia-chi/luu").session(khach()).param("tenDiaChi"," "))
                .andExpect(flash().attributeExists("error","formCu"));
        verifyNoInteractions(service);
    }

    @Test void khongDuocXoaHayDatMacDinhDiaChiNguoiKhac() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM khach_hang"),eq(7))).thenReturn(List.of(Map.of("id",7)));
        var service=new DiaChiKhachHangService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.xoa(7,99));
        assertThrows(IllegalArgumentException.class,()->service.datMacDinh(7,99));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }

    @Test void thanhToanLayDiaChiTuDatabaseKhongTinDiaChiGuiLen() throws Exception {
        var online=mock(SanPhamOnlineService.class);var dc=mock(DiaChiKhachHangService.class);
        when(online.layGioDaLuu(7)).thenReturn(new LinkedHashMap<>(Map.of(9,1)));
        when(dc.chiTiet(7,10)).thenReturn(Map.of("dia_chi_day_du","Số 1, Phường Cầu Giấy, Thành phố Hà Nội"));
        when(online.datHang(anyMap(),eq(7),eq("Long"),eq("0325327602"),anyString(),eq(false))).thenReturn(20);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(online,dc)).build()
                .perform(post("/thanh-toan").session(khach()).param("idDiaChi","10").param("ten","Long")
                        .param("sdt","0325327602").param("diaChi","Địa chỉ giả"))
                .andExpect(redirectedUrl("/don-hang/20")).andExpect(flash().attributeExists("success"));
        verify(online).datHang(anyMap(),eq(7),eq("Long"),eq("0325327602"),eq("Số 1, Phường Cầu Giấy, Thành phố Hà Nội"),eq(false));
    }

    @Test void chonDiaChiKhacKhachThiKhongTaoDon() throws Exception {
        var online=mock(SanPhamOnlineService.class);var dc=mock(DiaChiKhachHangService.class);
        when(dc.chiTiet(7,99)).thenThrow(new IllegalArgumentException("Không tìm thấy địa chỉ của bạn."));
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(online,dc)).build()
                .perform(post("/thanh-toan").session(khach()).param("idDiaChi","99").param("ten","Long")
                        .param("sdt","0325327602").param("diaChi","Hà Nội"))
                .andExpect(flash().attributeExists("error")).andExpect(redirectedUrl("/thanh-toan?loai=gio"));
        verify(online,never()).datHang(anyMap(),any(),anyString(),anyString(),anyString(),anyBoolean());
    }
}
