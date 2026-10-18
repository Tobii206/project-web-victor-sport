package com.example.victorsport.Config;

import com.example.victorsport.Service.ChuongTrucTuyenService;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;

@Configuration
public class DongBoDuLieuConfig implements WebMvcConfigurer {
    private final ChuongTrucTuyenService trucTuyen;
    public DongBoDuLieuConfig(ChuongTrucTuyenService trucTuyen) {this.trucTuyen=trucTuyen;}
    public static String nhom(String path) {
        if(path.startsWith("/quan-ly/san-pham")) return "san-pham,ton-kho";
        if(path.startsWith("/quan-ly/dot-giam-gia")) return "uu-dai,san-pham";
        if(path.startsWith("/quan-ly/phieu-giam-gia")) return "uu-dai";
        if(path.startsWith("/quan-ly/nhan-vien")) return "nhan-vien";
        if(path.equals("/dang-ky") || path.startsWith("/quan-ly/khach-hang") || path.startsWith("/tai-khoan/chinh-sua")) return "khach-hang";
        if(path.startsWith("/tai-khoan/dia-chi")) return "dia-chi";
        if(path.startsWith("/gio-hang") || path.matches("/san-pham/[0-9]+/chon")) return "gio-hang";
        if(path.startsWith("/quan-ly/van-chuyen")) return "van-chuyen,hoa-don";
        if(path.startsWith("/quan-ly/hoa-don") || path.startsWith("/don-hang/") || path.startsWith("/don-vang-lai/")
                || path.startsWith("/pos") || path.equals("/thanh-toan") || path.matches("/thanh-toan/qr/[0-9]+/doi-cod"))
            return "hoa-don,ton-kho,uu-dai,gio-hang,thong-ke,van-chuyen";
        return "";
    }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public void postHandle(HttpServletRequest req,HttpServletResponse res,Object handler,ModelAndView model) {
                if(!"POST".equals(req.getMethod())) return;
                boolean loi=RequestContextUtils.getOutputFlashMap(req).containsKey("error");
                if(model!=null) loi|=model.getModel().containsKey("error") || model.getModel().values().stream()
                    .anyMatch(v->v instanceof BindingResult r && r.hasErrors());
                req.setAttribute("dongBoThanhCong",!loi);
            }
            @Override public void afterCompletion(HttpServletRequest req,HttpServletResponse res,Object handler,Exception ex) {
                if(!Boolean.TRUE.equals(req.getAttribute("dongBoThanhCong")) || ex!=null || res.getStatus()>=400) return;
                String nhom=nhom(req.getRequestURI().substring(req.getContextPath().length()));
                if(!nhom.isEmpty()) trucTuyen.duLieuThayDoi(nhom);
            }
        });
    }
}
