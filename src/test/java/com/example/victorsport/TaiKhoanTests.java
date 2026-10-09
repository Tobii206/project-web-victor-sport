package com.example.victorsport;

import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Controller.TaiKhoanController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaiKhoanTests {
    @Test
    void khachChuaDangNhapKhongDocHoSoTuDatabase() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        taoTrang(service).perform(get("/tai-khoan")).andExpect(view().name("tai-khoan"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attributeDoesNotExist("thongTin"));
        verifyNoInteractions(service);
    }

    @Test
    void hoSoKhachHangLayIdTuPhienDangNhap() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        Map<String, Object> hoSo = Map.of("ho_ten", "Nguyễn An");
        when(service.layThongTinKhachHang(7)).thenReturn(hoSo);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("khachHangOnlineDangNhap", 7);
        taoTrang(service).perform(get("/tai-khoan").session(session).param("id", "99"))
                .andExpect(model().attribute("thongTin", hoSo))
                .andExpect(model().attributeDoesNotExist("laNhanVien"));
        verify(service).layThongTinKhachHang(7);
        verify(service, never()).layThongTinKhachHang(99);
        assertNull(DangNhapController.layNguoiDungDangNhap(session, new org.springframework.ui.ExtendedModelMap()));
    }

    @Test
    void hoSoNhanVienDungPhienNhanVien() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        Map<String, Object> hoSo = Map.of("ho_ten", "Trần Bình");
        when(service.layThongTinNhanVien(2)).thenReturn(hoSo);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,
                new NguoiDungDangNhap(2, "nv@example.com", "Trần Bình", "Nhân viên"));
        taoTrang(service).perform(get("/tai-khoan").session(session))
                .andExpect(model().attribute("thongTin", hoSo)).andExpect(model().attribute("laNhanVien", true));
        verify(service, never()).layThongTinKhachHang(anyInt());
    }

    @Test
    void dangNhapKhachHangVaDangXuat() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        when(service.dangNhapKhachHang("nguyenan", "123456")).thenReturn(7);
        MockHttpSession session = new MockHttpSession();
        String idCu = session.getId();
        MockMvc mvc = taoTrang(service);
        mvc.perform(post("/tai-khoan/dang-nhap").session(session)
                        .param("taiKhoan", "nguyenan").param("matKhau", "123456"))
                .andExpect(redirectedUrl("/tai-khoan"));
        assertEquals(7, session.getAttribute("khachHangOnlineDangNhap"));
        assertNotEquals(idCu, session.getId());
        assertNull(session.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP));
        mvc.perform(post("/tai-khoan/dang-xuat").session(session)).andExpect(redirectedUrl("/tai-khoan"));
        assertTrue(session.isInvalid());
    }

    @Test
    void matKhauBcryptVaDuLieuMauCuDeuDangNhapDuoc() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq("nguyenan"))).thenReturn(List.of(
                Map.of("id", 7, "mat_khau", new BCryptPasswordEncoder().encode("123456"))));
        TaiKhoanService service = new TaiKhoanService(jdbc);
        assertEquals(7, service.dangNhapKhachHang(" NguyenAn ", "123456"));
        when(jdbc.queryForList(anyString(), eq("nguyenan"))).thenReturn(List.of(Map.of("id", 7, "mat_khau", "123456")));
        assertEquals(7, service.dangNhapKhachHang("nguyenan", "123456"));
    }

    @Test
    void saiNamLanThiKhoaDangNhap() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq("nguyenan"))).thenReturn(List.of(Map.of("id", 7, "mat_khau", "123456")));
        TaiKhoanService service = new TaiKhoanService(jdbc);
        for (int lan = 1; lan <= 5; lan++) {
            assertThrows(IllegalArgumentException.class, () -> service.dangNhapKhachHang("nguyenan", "sai"));
        }
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.dangNhapKhachHang("nguyenan", "123456")).getMessage().contains("bị khóa"));
        verify(jdbc, times(5)).queryForList(anyString(), eq("nguyenan"));
    }

    private MockMvc taoTrang(TaiKhoanService service) {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return MockMvcBuilders.standaloneSetup(new TaiKhoanController(service)).setViewResolvers(resolver).build();
    }
}
