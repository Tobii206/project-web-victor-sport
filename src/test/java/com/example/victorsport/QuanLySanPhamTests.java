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
    @Test void themLaiCungAnhCungMauKhongTaoDongTrung() {
        var jdbc=mock(org.springframework.jdbc.core.JdbcTemplate.class);
        when(jdbc.queryForList("SELECT id FROM san_pham WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND ISNULL(xoa_mem,0)=0",8))
            .thenReturn(java.util.List.of(Map.of("id",8)));
        when(jdbc.queryForObject("SELECT COUNT(*) FROM chi_tiet_san_pham WHERE id=? AND id_san_pham=? AND ISNULL(xoa_mem,0)=0",Integer.class,12,8)).thenReturn(1);
        when(jdbc.queryForList(contains("SELECT id,duong_dan_anh FROM anh_chi_tiet_san_pham"),eq(12)))
            .thenReturn(java.util.List.of(Map.of("id",31,"duong_dan_anh","/image/anh-cu.png")));
        new QuanLySanPhamService(jdbc).themAnh(8,12,"/image/anh-cu.png");
        verify(jdbc,never()).update(contains("INSERT INTO anh_chi_tiet_san_pham"),any(Object[].class));
        verify(jdbc).update(contains("id<>?"),eq(12),eq(31));
        verify(jdbc).update("UPDATE anh_chi_tiet_san_pham SET la_anh_dai_dien=1 WHERE id=?",31);
    }
    private byte[] anhThu(int kieu) throws Exception {
        var anh=new java.awt.image.BufferedImage(3,2,kieu);
        for(int y=0;y<2;y++)for(int x=0;x<3;x++)anh.setRGB(x,y,0xff000000 | (x+1)*12345+(y+1)*23456);
        var bytes=new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(anh,"png",bytes);return bytes.toByteArray();
    }
    @Test void chonLaiAnhCoSanKhacTenKhongTaoTepMoi() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        var mvc=mvc(service);
        var tepCu=java.nio.file.Path.of("image","anh-goc-"+java.util.UUID.randomUUID()+".png");
        java.nio.file.Files.write(tepCu,anhThu(java.awt.image.BufferedImage.TYPE_INT_RGB));
        try {
            long truoc;
            try(var tep=java.nio.file.Files.list(tepCu.getParent())) {truoc=tep.count();}
            var upload=new MockMultipartFile("tepAnh","ten-khac.png","image/png",anhThu(java.awt.image.BufferedImage.TYPE_INT_ARGB));
            mvc.perform(multipart("/quan-ly/san-pham/8/anh/them").file(upload).param("idMau","12").session(session()))
                .andExpect(flash().attribute("success","Đã dùng lại ảnh có sẵn cho mẫu giày."));
            mvc.perform(multipart("/quan-ly/san-pham/8/anh/them").file(upload).param("idMau","13").session(session()))
                .andExpect(flash().attributeExists("success"));
            verify(service).themAnh(8,12,"/image/"+tepCu.getFileName());
            verify(service).themAnh(8,13,"/image/"+tepCu.getFileName());
            try(var tep=java.nio.file.Files.list(tepCu.getParent())) {org.junit.jupiter.api.Assertions.assertEquals(truoc,tep.count());}
        } finally {java.nio.file.Files.deleteIfExists(tepCu);}
    }
    @Test void loiLuuDatabaseKhongXoaAnhDangDungLai() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        doThrow(new IllegalArgumentException("Không tìm thấy mẫu giày để thêm ảnh.")).when(service).themAnh(anyInt(),anyInt(),anyString());
        var tepCu=java.nio.file.Path.of("image","anh-giu-lai-"+java.util.UUID.randomUUID()+".png");
        byte[] noiDung=anhThu(java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.nio.file.Files.write(tepCu,noiDung);
        try {
            var upload=new MockMultipartFile("tepAnh","chon-lai.png","image/png",noiDung);
            mvc(service).perform(multipart("/quan-ly/san-pham/8/anh/them").file(upload).param("idMau","999").session(session()))
                .andExpect(flash().attributeExists("error"));
            org.junit.jupiter.api.Assertions.assertArrayEquals(noiDung,java.nio.file.Files.readAllBytes(tepCu));
        } finally {java.nio.file.Files.deleteIfExists(tepCu);}
    }
    @Test void loiLuuDatabaseDonTepMoiVuaTao() throws Exception {
        var service=mock(QuanLySanPhamService.class);
        var duongDan=org.mockito.ArgumentCaptor.forClass(String.class);
        doThrow(new IllegalArgumentException("Không tìm thấy mẫu giày để thêm ảnh.")).when(service).themAnh(anyInt(),anyInt(),duongDan.capture());
        var anh=new java.awt.image.BufferedImage(7,3,java.awt.image.BufferedImage.TYPE_INT_RGB);
        anh.setRGB(0,0,java.util.UUID.randomUUID().hashCode());
        var bytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(anh,"png",bytes);
        var upload=new MockMultipartFile("tepAnh","anh-moi.png","image/png",bytes.toByteArray());
        mvc(service).perform(multipart("/quan-ly/san-pham/8/anh/them").file(upload).param("idMau","999").session(session()))
            .andExpect(flash().attributeExists("error"));
        org.junit.jupiter.api.Assertions.assertFalse(java.nio.file.Files.exists(java.nio.file.Path.of(duongDan.getValue().substring(1))));
    }
    private MockHttpSession session() {
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(1,"admin@example.com","Quản lý","Quản trị viên"));return s;
    }
    private MockMvc mvc(QuanLySanPhamService service) {
        var view=new InternalResourceViewResolver("/WEB-INF/view/",".jsp");
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
        mvc(service).perform(post("/quan-ly/san-pham/luu").session(session())
                .param("ten_san_pham","Giày mới").param("id_thuong_hieu","1").param("trang_thai_kinh_doanh","1"))
                .andExpect(redirectedUrl("/quan-ly/san-pham/99"));
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
            org.junit.jupiter.api.Assertions.assertTrue(duongDan.getValue().startsWith("/image/"));
            tepDaLuu=java.nio.file.Path.of(duongDan.getValue().substring(1));
            org.junit.jupiter.api.Assertions.assertTrue(java.nio.file.Files.exists(tepDaLuu));
            org.junit.jupiter.api.Assertions.assertNotNull(javax.imageio.ImageIO.read(tepDaLuu.toFile()));
        } finally {if(tepDaLuu!=null)java.nio.file.Files.deleteIfExists(tepDaLuu);}
    }
}
