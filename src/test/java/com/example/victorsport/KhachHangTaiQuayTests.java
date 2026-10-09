package com.example.victorsport;

import com.example.victorsport.Controller.BanHangTaiQuayController;
import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.BanHangTaiQuayService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KhachHangTaiQuayTests {
    @Test
    void tenRongKhongDuocLuu() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BanHangTaiQuayService service = new BanHangTaiQuayService(jdbc);
        assertThrows(IllegalArgumentException.class, () -> service.themKhachHangMoi("  ", "", "", 2));
        verifyNoInteractions(jdbc);
    }

    @Test
    void soDienThoaiVaEmailSaiKhongDuocLuu() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BanHangTaiQuayService service = new BanHangTaiQuayService(jdbc);
        assertThrows(IllegalArgumentException.class, () -> service.themKhachHangMoi("An", "abc", "", 2));
        assertThrows(IllegalArgumentException.class, () -> service.themKhachHangMoi("An", "", "email-sai", 2));
        verifyNoInteractions(jdbc);
    }

    @Test
    void choPhepBoTrongLienHeVaTuTaoTaiKhoan() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BanHangTaiQuayService service = new BanHangTaiQuayService(jdbc);
        when(jdbc.queryForObject(contains("INSERT INTO khach_hang"), eq(Integer.class),
                eq("Nguyễn An"), anyString(), anyString(), eq(""), eq(""), eq(2))).thenReturn(99);

        assertEquals(99, service.themKhachHangMoi(" Nguyễn An ", null, "  ", 2).intValue());
        ArgumentCaptor<String> taiKhoan = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> matKhau = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(contains("INSERT INTO khach_hang"), eq(Integer.class), eq("Nguyễn An"),
                taiKhoan.capture(), matKhau.capture(), eq(""), eq(""), eq(2));
        assertTrue(taiKhoan.getValue().startsWith("quay_"));
        assertTrue(matKhau.getValue().startsWith("$2a$"));
    }

    @Test
    void themThanhCongTuChonKhachChoDonHienTai() {
        BanHangTaiQuayService service = mock(BanHangTaiQuayService.class);
        BanHangTaiQuayController controller = new BanHangTaiQuayController(service);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,
                new NguoiDungDangNhap(2, "nv@example.com", "Nhân viên", "Bán hàng"));
        when(service.themKhachHangMoi("An", "", "", 2)).thenReturn(99);

        assertEquals("redirect:/pos", controller.themKhachHang("An", "", "", session,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals(99, session.getAttribute("idKhachHangTaiQuay"));
    }

    @Test
    void chonKhachVangLaiBoLuaChonKhachCu() {
        BanHangTaiQuayService service = mock(BanHangTaiQuayService.class);
        BanHangTaiQuayController controller = new BanHangTaiQuayController(service);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,
                new NguoiDungDangNhap(2, "nv@example.com", "Nhân viên", "Bán hàng"));
        session.setAttribute("idKhachHangTaiQuay", 99);

        controller.chonKhachHang(null, session, new RedirectAttributesModelMap());
        assertNull(session.getAttribute("idKhachHangTaiQuay"));
        verifyNoInteractions(service);
    }
}
