package com.example.victorsport.Config;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class MenuTaiKhoanConfig implements WebMvcConfigurer, HandlerInterceptor {
    private final TaiKhoanService taiKhoan;
    public MenuTaiKhoanConfig(TaiKhoanService taiKhoan) { this.taiKhoan = taiKhoan; }

    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(this); }

    @Override public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView view) {
        if (!"GET".equals(request.getMethod()) || view == null || view.getViewName() == null
                || !view.getViewName().startsWith("client/")) return;
        var session = request.getSession(false);
        if (session == null) return;
        try {
            Object profile = "client/account/show".equals(view.getViewName()) ? view.getModel().get("thongTin") : null;
            if (profile == null) {
                if (session.getAttribute("nguoiDungDangNhap") instanceof NguoiDungDangNhap staff)
                    profile = taiKhoan.layThongTinNhanVien(staff.getId());
                else if (session.getAttribute("khachHangOnlineDangNhap") instanceof Integer id)
                    profile = taiKhoan.layThongTinKhachHang(id);
            }
            if (profile != null) view.addObject("hoSoMenuTaiKhoan", profile);
        } catch (DataAccessException | IllegalArgumentException ex) {
            // Menu vẫn dùng được khi thông tin tài khoản tạm thời không tải được.
        }
    }
}
