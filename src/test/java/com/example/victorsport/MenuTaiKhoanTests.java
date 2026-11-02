package com.example.victorsport;

import com.example.victorsport.Config.MenuTaiKhoanConfig;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuTaiKhoanTests {
    @Test void khachChiLayAnhCuaTaiKhoanTrongSession() {
        var service=mock(TaiKhoanService.class);var req=new MockHttpServletRequest("GET","/");
        req.getSession().setAttribute("khachHangOnlineDangNhap",7);req.setParameter("id","99");
        var profile=Map.<String,Object>of("ho_ten","Khách","anh_dai_dien","/image/avatars/7.png");
        when(service.layThongTinKhachHang(7)).thenReturn(profile);
        var view=new ModelAndView("client/homepage/show");
        new MenuTaiKhoanConfig(service).postHandle(req,new MockHttpServletResponse(),null,view);
        assertSame(profile,view.getModel().get("hoSoMenuTaiKhoan"));verify(service).layThongTinKhachHang(7);verifyNoMoreInteractions(service);
    }
    @Test void adminVaNhanVienDungAnhNhanVienKeCaCoSessionKhach() {
        for(String role:new String[]{"Quản trị viên","Nhân viên"}) {
            var service=mock(TaiKhoanService.class);var req=new MockHttpServletRequest("GET","/");
            req.getSession().setAttribute("nguoiDungDangNhap",new NguoiDungDangNhap(3,"a@gmail.com","An",role));
            req.getSession().setAttribute("khachHangOnlineDangNhap",7);
            var profile=Map.<String,Object>of("ho_ten","An","vai_tro",role);
            when(service.layThongTinNhanVien(3)).thenReturn(profile);
            var view=new ModelAndView("client/homepage/show");
            new MenuTaiKhoanConfig(service).postHandle(req,new MockHttpServletResponse(),null,view);
            assertSame(profile,view.getModel().get("hoSoMenuTaiKhoan"));verify(service).layThongTinNhanVien(3);verifyNoMoreInteractions(service);
        }
    }
    @Test void trangTaiKhoanTaiSuDungHoSoDaTai() {
        var service=mock(TaiKhoanService.class);var req=new MockHttpServletRequest("GET","/tai-khoan");
        req.getSession().setAttribute("khachHangOnlineDangNhap",7);
        var profile=Map.of("ho_ten","An");var view=new ModelAndView("client/account/show","thongTin",profile);
        new MenuTaiKhoanConfig(service).postHandle(req,new MockHttpServletResponse(),null,view);
        assertSame(profile,view.getModel().get("hoSoMenuTaiKhoan"));verifyNoInteractions(service);
    }
    @Test void khongTruyVanVoiKhachVangLaiPostRedirectVaTaiNguyen() {
        var service=mock(TaiKhoanService.class);var interceptor=new MenuTaiKhoanConfig(service);
        interceptor.postHandle(new MockHttpServletRequest("GET","/"),new MockHttpServletResponse(),null,new ModelAndView("client/homepage/show"));
        var req=new MockHttpServletRequest("POST","/tai-khoan/anh-dai-dien");req.getSession().setAttribute("khachHangOnlineDangNhap",7);
        interceptor.postHandle(req,new MockHttpServletResponse(),null,new ModelAndView("client/account/show"));
        req.setMethod("GET");
        interceptor.postHandle(req,new MockHttpServletResponse(),null,new ModelAndView("redirect:/tai-khoan"));
        interceptor.postHandle(req,new MockHttpServletResponse(),null,null);
        verifyNoInteractions(service);
    }
}
