package com.example.victorsport.Security;

import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class PhanQuyenInterceptor implements HandlerInterceptor {
    private final com.example.victorsport.Service.PhanQuyenService phanQuyenService;
    public PhanQuyenInterceptor(com.example.victorsport.Service.PhanQuyenService phanQuyenService) {
        this.phanQuyenService = phanQuyenService;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object taiKhoan = null;
        if (session != null) {
            taiKhoan = session.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP);
        }
        if (!(taiKhoan instanceof NguoiDungDangNhap)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        NguoiDungDangNhap nguoiDung = (NguoiDungDangNhap) taiKhoan;
        // Đọc lại quyền để việc đổi quyền trong database có hiệu lực ngay.
        nguoiDung = phanQuyenService.layQuyen(nguoiDung.getId());
        if (nguoiDung == null) {
            session.removeAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP, nguoiDung);

        String duongDan = request.getRequestURI().substring(request.getContextPath().length());
        boolean chiXem = request.getMethod().equals("GET") || request.getMethod().equals("HEAD");
        boolean duocTruyCap = false;
        if (thuocMuc(duongDan, "/pos")) duocTruyCap = nguoiDung.isBanHangTaiQuay();
        else if (thuocMuc(duongDan, "/quan-ly/san-pham")) {
            duocTruyCap = chiXem ? nguoiDung.isXemSanPham() : nguoiDung.isSuaSanPham();
            if (duongDan.equals("/quan-ly/san-pham/them")) duocTruyCap = nguoiDung.isSuaSanPham();
        }
        else if (thuocMuc(duongDan, "/quan-ly/hoa-don")) {
            duocTruyCap = nguoiDung.isQuanLy() || (chiXem ? nguoiDung.isXemHoaDon() : nguoiDung.isSuaHoaDon());
            if (duongDan.endsWith("/xac-nhan-thanh-toan") || duongDan.endsWith("/huy")) duocTruyCap = nguoiDung.isQuanLy();
        }
        else if (thuocMuc(duongDan, "/quan-ly/khach-hang")) {
            duocTruyCap = nguoiDung.isQuanLy() || (chiXem && nguoiDung.isXemKhachHang());
            if (duongDan.equals("/quan-ly/khach-hang/them")) duocTruyCap = nguoiDung.isQuanLy();
        }
        else if (thuocMuc(duongDan, "/quan-ly/dot-giam-gia") || thuocMuc(duongDan, "/quan-ly/phieu-giam-gia")
                || thuocMuc(duongDan, "/quan-ly/nhan-vien") || thuocMuc(duongDan, "/quan-ly/gio-hang")) duocTruyCap = nguoiDung.isQuanLy();
        if (duocTruyCap) return true;
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/WEB-INF/views/Admin/khong-co-quyen.jsp").forward(request, response);
        return false;
    }

    private boolean thuocMuc(String duongDan, String muc) {
        return duongDan.equals(muc) || duongDan.startsWith(muc + "/");
    }
}
