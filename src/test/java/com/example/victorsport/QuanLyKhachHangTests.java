package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.QuanLyKhachHangService;
import com.example.victorsport.Service.TaiKhoanService;
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

class QuanLyKhachHangTests {
    private MockHttpSession session(String quyen) {
        var s=new MockHttpSession();var user=new NguoiDungDangNhap(2,"nv@example.com","Nhân viên",quyen);
        user.themChucNang("CUSTOMER_READ");s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,user);return s;
    }
    private MockMvc mvc(QuanLyKhachHangService service) {
        return MockMvcBuilders.standaloneSetup(new QuanLyKhachHangController(service,mock(TaiKhoanService.class)))
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/view/",".jsp")).build();
    }
    @Test void chuaDangNhapKhongDuocGhi() throws Exception {
        var service=mock(QuanLyKhachHangService.class);
        mvc(service).perform(post("/quan-ly/khach-hang/luu")).andExpect(redirectedUrl("/login"));verifyNoInteractions(service);
    }
    @Test void nhanVienChiDuocXem() throws Exception {
        var service=mock(QuanLyKhachHangService.class);var mvc=mvc(service);var session=session("Nhân viên");
        mvc.perform(get("/quan-ly/khach-hang").session(session)).andExpect(view().name("admin/customer/show"));
        mvc.perform(get("/quan-ly/khach-hang/them").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/quan-ly/khach-hang/3/luu").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/quan-ly/khach-hang/3/ngung-hoat-dong").session(session)).andExpect(status().isForbidden());
        verify(service,never()).luu(any(),anyMap(),anyInt());verify(service,never()).ngungHoatDong(anyInt(),anyInt());
    }
    @Test void loiNhapLieuKhongGiuMatKhau() throws Exception {
        var service=mock(QuanLyKhachHangService.class);
        when(service.luu(isNull(),anyMap(),eq(2))).thenThrow(new IllegalArgumentException("Tên tài khoản đã được sử dụng."));
        Map<String,Object> form=new java.util.HashMap<>();
        form.put("ten_khach_hang","Khách mới");form.put("trang_thai",true);form.put("gioi_tinh",null);
        mvc(service).perform(post("/quan-ly/khach-hang/luu").session(session("Quản trị viên"))
                .param("ten_khach_hang","Khách mới").param("trang_thai","1").param("mat_khau","khong-duoc-luu"))
                .andExpect(redirectedUrl("/quan-ly/khach-hang/them"))
                .andExpect(flash().attribute("khachHang",form));
    }
    @Test void adminDuocNgungHoatDong() throws Exception {
        var service=mock(QuanLyKhachHangService.class);
        mvc(service).perform(post("/quan-ly/khach-hang/3/ngung-hoat-dong").session(session("Quản trị viên")))
                .andExpect(redirectedUrl("/quan-ly/khach-hang")).andExpect(flash().attributeExists("success"));
        verify(service).ngungHoatDong(3,2);
    }
    @Test void thongTinSaiKhongDuocGhi() {
        var jdbc=mock(JdbcTemplate.class);var service=new QuanLyKhachHangService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.luu(null,Map.of("ten_khach_hang","Mới","ten_tai_khoan","có dấu"),2));
        assertThrows(IllegalArgumentException.class,()->service.luu(null,Map.of("ten_khach_hang","Mới","ten_tai_khoan","khachmoi","email","email sai"),2));
        assertThrows(IllegalArgumentException.class,()->service.luu(null,Map.of("ten_khach_hang","Mới","ten_tai_khoan","khachmoi","mat_khau","12"),2));
        verifyNoInteractions(jdbc);
    }
    @Test void chiDoiMatKhauThanhCongMoiMoKhoa() throws Exception {
        var service=mock(QuanLyKhachHangService.class);var taiKhoan=mock(TaiKhoanService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new QuanLyKhachHangController(service,taiKhoan)).build();
        mvc.perform(post("/quan-ly/khach-hang/1/luu").session(session("Quản trị viên"))
                .param("ten_khach_hang","Khách").param("trang_thai","1")
                .param("ten_tai_khoan","khach01").param("mat_khau","matkhaumoi"))
                .andExpect(redirectedUrl("/quan-ly/khach-hang"));
        verify(taiKhoan).moKhoaSauDoiMatKhau("khach01");
        clearInvocations(taiKhoan);
        mvc.perform(post("/quan-ly/khach-hang/1/luu").session(session("Quản trị viên"))
                .param("ten_khach_hang","Khách").param("trang_thai","1")
                .param("ten_tai_khoan","khach01").param("mat_khau",""));
        verifyNoInteractions(taiKhoan);
        when(service.luu(eq(1),anyMap(),eq(2))).thenThrow(new IllegalArgumentException("Không lưu được."));
        mvc.perform(post("/quan-ly/khach-hang/1/luu").session(session("Quản trị viên"))
                .param("ten_khach_hang","Khách").param("trang_thai","1")
                .param("ten_tai_khoan","khach01").param("mat_khau","matkhaumoi"))
                .andExpect(flash().attributeExists("error"));
        verifyNoInteractions(taiKhoan);
    }
}
