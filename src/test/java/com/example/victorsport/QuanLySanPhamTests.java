package com.example.victorsport;

import com.example.victorsport.Controller.QuanLySanPhamController;
import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.QuanLySanPhamService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuanLySanPhamTests {
    private MockHttpSession session() {
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(1,"admin@example.com","Quản lý","Quản trị viên"));return s;
    }
    private MockMvc mvc(QuanLySanPhamService service) {
        var view=new InternalResourceViewResolver("/WEB-INF/views/",".jsp");
        return MockMvcBuilders.standaloneSetup(new QuanLySanPhamController(service)).setViewResolvers(view).build();
    }
    @Test void chuaDangNhapKhongDuocGhi() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        mvc(service).perform(post("/quan-ly/san-pham/luu")).andExpect(redirectedUrl("/login"));verifyNoInteractions(service);
    }
    @Test void loiNhapLieuGiuLaiThongTin() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        when(service.luu(isNull(),anyMap(),eq(1))).thenThrow(new IllegalArgumentException("Chọn thương hiệu."));
        mvc(service).perform(post("/quan-ly/san-pham/luu").session(session()).param("ten_san_pham","Giày mới").param("trang_thai_kinh_doanh","1"))
            .andExpect(redirectedUrl("/quan-ly/san-pham/them")).andExpect(flash().attribute("error","Chọn thương hiệu."))
            .andExpect(flash().attribute("sanPham",Map.of("ten_san_pham","Giày mới","trang_thai_kinh_doanh",true)));
    }
    @Test void themThanhCongMoChiTietDeThemMau() throws Exception {
        var service=mock(QuanLySanPhamService.class);when(service.luu(isNull(),anyMap(),eq(1))).thenReturn(99);
        mvc(service).perform(post("/quan-ly/san-pham/luu").session(session())).andExpect(redirectedUrl("/quan-ly/san-pham/99"));
    }
    @Test void tepKhongPhaiAnhKhongDuocGhiDatabase() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        var tep=new MockMultipartFile("tepAnh","anh.png","image/png","Không phải ảnh".getBytes());
        mvc(service).perform(multipart("/quan-ly/san-pham/1/anh/them").file(tep).param("idMau","1").session(session()))
            .andExpect(redirectedUrl("/quan-ly/san-pham/1#anh-san-pham")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }
    @Test void anhHopLeDuocLuuVaGanVaoDungMau() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        var bytes=new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",bytes);
        var tep=new MockMultipartFile("tepAnh","anh.png","image/png",bytes.toByteArray());
        var duongDan=org.mockito.ArgumentCaptor.forClass(String.class);
        java.nio.file.Path tepDaLuu=null;
        try {
            mvc(service).perform(multipart("/quan-ly/san-pham/8/anh/them").file(tep).param("idMau","12").session(session()))
                .andExpect(redirectedUrl("/quan-ly/san-pham/8#anh-san-pham")).andExpect(flash().attributeExists("success"));
            verify(service).themAnh(eq(8),eq(12),duongDan.capture());
            tepDaLuu=java.nio.file.Path.of(duongDan.getValue().substring(1));
            org.junit.jupiter.api.Assertions.assertTrue(java.nio.file.Files.exists(tepDaLuu));
            org.junit.jupiter.api.Assertions.assertNotNull(javax.imageio.ImageIO.read(tepDaLuu.toFile()));
        } finally {if(tepDaLuu!=null)java.nio.file.Files.deleteIfExists(tepDaLuu);}
    }
}
