package com.example.victorsport;

import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Security.PhanQuyenConfig;
import com.example.victorsport.Service.PhanQuyenService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PhanQuyenTests {
    @Test
    void quyenDatabaseQuyetDinhTruyCapVaThayDoiCoHieuLucNgay() throws Exception {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(CauHinhKiemTra.class, PhanQuyenConfig.class);
            context.refresh();
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            PhanQuyenService service = context.getBean(PhanQuyenService.class);
            NguoiDungDangNhap nv = taoNguoiDung("Nhân viên", "PRODUCT_READ", "PRODUCT_WRITE", "ORDER_READ", "ORDER_WRITE", "CUSTOMER_READ");
            when(service.layQuyen(2)).thenReturn(nv);
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP, nv);
            for (String duongDan : new String[]{"/pos", "/pos/thanh-toan", "/quan-ly/hoa-don/1"}) {
                mvc.perform(get(duongDan).session(session)).andExpect(status().isOk());
                mvc.perform(post(duongDan).session(session)).andExpect(status().isOk());
            }
            mvc.perform(get("/quan-ly/san-pham").session(session)).andExpect(status().isOk());
            mvc.perform(post("/quan-ly/hoa-don/1/xac-nhan-thanh-toan").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/quan-ly/san-pham/8").session(session)).andExpect(status().isOk());
            // Nhân viên bị chặn quyền sửa kể cả database cũ còn PRODUCT_WRITE.
            mvc.perform(get("/quan-ly/san-pham/them").session(session)).andExpect(status().isForbidden());
            for (String duongDan : new String[]{"/quan-ly/san-pham/luu", "/quan-ly/san-pham/8/luu", "/quan-ly/san-pham/8/xoa",
                    "/quan-ly/san-pham/8/mau/luu", "/quan-ly/san-pham/8/mau/12/xoa", "/quan-ly/san-pham/8/anh/them", "/quan-ly/san-pham/8/anh/1/dai-dien", "/quan-ly/san-pham/8/anh/1/xoa"}) {
                mvc.perform(post(duongDan).session(session)).andExpect(status().isForbidden());
            }
            mvc.perform(get("/quan-ly/khach-hang").session(session)).andExpect(status().isOk());
            mvc.perform(post("/quan-ly/khach-hang").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/quan-ly/khach-hang/them").session(session)).andExpect(status().isForbidden());
            for (String duongDan : new String[]{"/quan-ly/nhan-vien", "/quan-ly/dot-giam-gia", "/quan-ly/phieu-giam-gia", "/quan-ly/gio-hang", "/quan-ly/hoa-don-khac"}) {
                mvc.perform(get(duongDan).session(session)).andExpect(status().isForbidden())
                        .andExpect(forwardedUrl("/WEB-INF/views/Admin/khong-co-quyen.jsp"));
                mvc.perform(post(duongDan).session(session)).andExpect(status().isForbidden());
            }
            // Cùng phiên đăng nhập, thu hồi quyền ghi có hiệu lực ngay.
            when(service.layQuyen(2)).thenReturn(taoNguoiDung("Nhân viên", "PRODUCT_READ", "ORDER_READ"));
            mvc.perform(get("/quan-ly/san-pham").session(session)).andExpect(status().isOk());
            mvc.perform(post("/quan-ly/san-pham").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/quan-ly/hoa-don").session(session)).andExpect(status().isOk());
            mvc.perform(post("/quan-ly/hoa-don").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/pos").session(session)).andExpect(status().isForbidden());
            when(service.layQuyen(2)).thenReturn(taoNguoiDung("Quản trị viên"));
            mvc.perform(post("/quan-ly/hoa-don/1/xac-nhan-thanh-toan").session(session)).andExpect(status().isOk());
            mvc.perform(get("/quan-ly/san-pham").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/quan-ly/nhan-vien").session(session)).andExpect(status().isOk());
            mvc.perform(get("/quan-ly/khach-hang/them").session(session)).andExpect(status().isOk());
            mvc.perform(post("/quan-ly/khach-hang/1/luu").session(session)).andExpect(status().isOk());
            when(service.layQuyen(2)).thenReturn(taoNguoiDung("Quản trị viên", "PRODUCT_READ", "PRODUCT_WRITE"));
            mvc.perform(get("/quan-ly/san-pham/them").session(session)).andExpect(status().isOk());
            mvc.perform(post("/quan-ly/san-pham/8/mau/luu").session(session)).andExpect(status().isOk());
            when(service.layQuyen(2)).thenReturn(null);
            mvc.perform(get("/quan-ly/hoa-don").session(session)).andExpect(redirectedUrl("/login"));
            MockHttpSession kh = new MockHttpSession();
            kh.setAttribute("khachHangOnlineDangNhap", 1);
            mvc.perform(get("/pos").session(kh)).andExpect(redirectedUrl("/login"));
            mvc.perform(get("/quan-ly/hoa-don")).andExpect(redirectedUrl("/login"));
            mvc.perform(get("/").session(kh)).andExpect(status().isOk());
        }
    }
    static NguoiDungDangNhap taoNguoiDung(String tenQuyen, String... cacMa) {
        NguoiDungDangNhap nv = new NguoiDungDangNhap(2, "nv@example.com", "Nhân viên", tenQuyen);
        for (String ma : cacMa) nv.themChucNang(ma);
        return nv;
    }
    @Configuration
    @EnableWebMvc
    static class CauHinhKiemTra {
        @Bean PhanQuyenService phanQuyenService() { return mock(PhanQuyenService.class); }
        @Bean TrangKiemTra trangKiemTra() { return new TrangKiemTra(); }
    }
    @RestController
    static class TrangKiemTra {
        @RequestMapping(value = {"/", "/pos", "/pos/**", "/quan-ly/**"}, produces = "text/plain;charset=UTF-8")
        String truyCap() { return "Đã truy cập"; }
    }
}
