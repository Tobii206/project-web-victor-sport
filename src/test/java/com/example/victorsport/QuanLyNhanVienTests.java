package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.QuanLyNhanVienService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuanLyNhanVienTests {
    private MockHttpSession session(String quyen) {
        var session=new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(1,"admin@example.com","Quản lý",quyen));
        return session;
    }
    private MockMvc mvc(QuanLyNhanVienService service) {
        return MockMvcBuilders.standaloneSetup(new QuanLyNhanVienController(service))
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/",".jsp")).build();
    }
    @Test void chuaDangNhapKhongDuocGhi() throws Exception {
        var service=mock(QuanLyNhanVienService.class);
        mvc(service).perform(post("/quan-ly/nhan-vien/luu")).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
    }
    @Test void nhanVienKhongDuocThemSuaVaChoNghiViec() throws Exception {
        var service=mock(QuanLyNhanVienService.class);var mvc=mvc(service);var session=session("Nhân viên");
        mvc.perform(get("/quan-ly/nhan-vien").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/quan-ly/nhan-vien/luu").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/quan-ly/nhan-vien/2/nghi-viec").session(session)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void loiNhapLieuKhongLuuMatKhauVaoFlash() throws Exception {
        var service=mock(QuanLyNhanVienService.class);
        when(service.luu(isNull(),anyMap(),eq(1))).thenThrow(new IllegalArgumentException("Email không hợp lệ."));
        mvc(service).perform(post("/quan-ly/nhan-vien/luu").session(session("Quản trị viên"))
                .param("ten_nhan_vien","Nhân viên mới").param("email","sai").param("trang_thai","1").param("mat_khau","khong-duoc-luu"))
                .andExpect(redirectedUrl("/quan-ly/nhan-vien/them"))
                .andExpect(flash().attribute("nhanVien",Map.of("ten_nhan_vien","Nhân viên mới","email","sai","trang_thai",true)));
    }
    @Test void thanhCongVeDanhSach() throws Exception {
        var service=mock(QuanLyNhanVienService.class);
        mvc(service).perform(post("/quan-ly/nhan-vien/2/nghi-viec").session(session("Quản trị viên")))
                .andExpect(redirectedUrl("/quan-ly/nhan-vien")).andExpect(flash().attributeExists("success"));
        verify(service).nghiViec(2,1);
    }
    @Test void khongDuocTuChoNghiViec() {
        var jdbc=mock(JdbcTemplate.class);var service=new QuanLyNhanVienService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.nghiViec(1,1));verifyNoInteractions(jdbc);
    }
    @Test void thongTinSaiKhongDuocGhiDatabase() {
        var jdbc=mock(JdbcTemplate.class);var service=new QuanLyNhanVienService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.luu(null,Map.of("ten_nhan_vien","Mới","email","email sai"),1));
        assertThrows(IllegalArgumentException.class,()->service.luu(null,Map.of("ten_nhan_vien","Mới","email","nv@example.com","so_dien_thoai","123"),1));
        assertThrows(IllegalArgumentException.class,()->service.luu(null,Map.of("ten_nhan_vien","Mới","email","nv@example.com","mat_khau","12"),1));
        verifyNoInteractions(jdbc);
    }
}
