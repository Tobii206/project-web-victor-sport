package com.example.victorsport;

import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Security.SecurityConfig;
import com.example.victorsport.Service.DangNhapService;
import com.example.victorsport.Service.TaiKhoanService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DangNhapSecurityTests {
    @Test
    void boLocSecurityKhongChanTrangVaXuLyDangNhapChung() throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(CauHinhKiemTra.class, SecurityConfig.class);
            context.refresh();
            FilterChainProxy boLoc = context.getBean(FilterChainProxy.class);
            for (jakarta.servlet.Filter filter : boLoc.getFilterChains().get(0).getFilters()) {
                assertFalse(filter.getClass().getSimpleName().equals("DefaultLoginPageGeneratingFilter"));
                assertFalse(filter.getClass().getSimpleName().equals("UsernamePasswordAuthenticationFilter"));
            }
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(boLoc).build();
            mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("client/auth/login"));
            DangNhapService nv = context.getBean(DangNhapService.class);
            when(nv.laTaiKhoanNhanVien("nv@gmail.com")).thenReturn(true);
            when(nv.dangNhap("nv@gmail.com", "123456")).thenReturn(DangNhapService.KetQuaDangNhap.thanhCong(
                    PhanQuyenTests.taoNguoiDung("Nhân viên", "ORDER_WRITE")));
            mvc.perform(post("/login").param("taiKhoan", "nv@gmail.com").param("matKhau", "123456"))
                    .andExpect(redirectedUrl("/pos"));
            verify(nv).dangNhap("nv@gmail.com", "123456");

            TaiKhoanService kh = context.getBean(TaiKhoanService.class);
            when(kh.dangNhapKhachHang("nguyenan", "123456")).thenReturn(7);
            mvc.perform(post("/login").param("taiKhoan", "nguyenan").param("matKhau", "123456"))
                    .andExpect(redirectedUrl("/"));
            verify(kh).dangNhapKhachHang("nguyenan", "123456");
        }
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    static class CauHinhKiemTra {
        @Bean DangNhapService dangNhapService() { return mock(DangNhapService.class); }
        @Bean TaiKhoanService taiKhoanService() { return mock(TaiKhoanService.class); }
        @Bean DangNhapController dangNhapController(DangNhapService nv, TaiKhoanService kh) {
            return new DangNhapController(nv, kh, mock(com.example.victorsport.Service.SanPhamOnlineService.class));
        }
        @Bean InternalResourceViewResolver viewResolver() {
            return new InternalResourceViewResolver("/WEB-INF/view/", ".jsp");
        }
    }
}

