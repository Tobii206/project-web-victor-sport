package com.example.victorsport;

import com.example.victorsport.Controller.AnhDaiDienKhachController;
import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Controller.TaiKhoanController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import com.example.victorsport.Service.AnhDaiDienKhachService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnhDaiDienKhachTests {
    @TempDir Path temp;
    MockMultipartFile anh() throws Exception {
        var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(20,10,BufferedImage.TYPE_INT_RGB),"png",out);
        return new MockMultipartFile("tepAnh","avatar.png","image/png",out.toByteArray());
    }
    JdbcTemplate jdbc(String cu) {
        var jdbc=mock(JdbcTemplate.class);
        var row=new HashMap<String,Object>();row.put("anh_dai_dien",cu);
        when(jdbc.queryForList(contains("SELECT anh_dai_dien"),eq(7))).thenReturn(List.of(row));
        when(jdbc.update(contains("SET anh_dai_dien"),anyString(),eq(7),eq(7))).thenReturn(1);
        return jdbc;
    }
    List<Path> files() throws Exception {try(var ds=Files.list(temp)){return ds.filter(Files::isRegularFile).toList();}}
    @Test void anhDuocThuNhoVaLuuDungChuTaiKhoan() throws Exception {
        var jdbc=jdbc(null);new AnhDaiDienKhachService(jdbc,temp.toString()).doiAnh(7,anh());
        assertEquals(1,files().size());var img=ImageIO.read(files().get(0).toFile());assertEquals(256,img.getWidth());assertEquals(256,img.getHeight());
        verify(jdbc).update(contains("nguoi_cap_nhat=?"),startsWith("/image/avatars/7-"),eq(7),eq(7));
    }
    @Test void anhNhanVienLuuDungCotVaKhongXoaAnhKhachCungId() throws Exception {
        var jdbc=mock(JdbcTemplate.class);
        String tenKhach="7-"+UUID.randomUUID()+".png";Path anhKhach=temp.resolve(tenKhach);Files.write(anhKhach,new byte[]{1});
        when(jdbc.queryForList(contains("SELECT anh_nhan_vien AS anh_dai_dien FROM nhan_vien"),eq(7)))
                .thenReturn(List.of(Map.of("anh_dai_dien","/image/avatars/"+tenKhach)));
        when(jdbc.update(contains("UPDATE nhan_vien SET anh_nhan_vien"),anyString(),eq(7),eq(7))).thenReturn(1);
        new AnhDaiDienKhachService(jdbc,temp.toString()).doiAnhNhanVien(7,anh());
        assertTrue(Files.exists(anhKhach));assertEquals(2,files().size());
        verify(jdbc).update(contains("nguoi_cap_nhat=?"),startsWith("/image/avatars/nv-7-"),eq(7),eq(7));
        verify(jdbc,never()).update(contains("UPDATE khach_hang"),any(Object[].class));
    }
    @Test void doiAnhNhanVienXoaDungAnhCuCuaNhanVien() throws Exception {
        String tenCu="nv-7-"+UUID.randomUUID()+".png";Path cu=temp.resolve(tenCu);Files.write(cu,new byte[]{1});
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM nhan_vien WITH"),eq(7))).thenReturn(List.of(Map.of("anh_dai_dien","/image/avatars/"+tenCu)));
        when(jdbc.update(contains("UPDATE nhan_vien"),anyString(),eq(7),eq(7))).thenReturn(1);
        new AnhDaiDienKhachService(jdbc,temp.toString()).doiAnhNhanVien(7,anh());
        assertFalse(Files.exists(cu));assertEquals(1,files().size());
    }
    @Test void nhanVienDaNghiKhongDuocDoiAnh() {
        var jdbc=mock(JdbcTemplate.class);
        assertThrows(IllegalArgumentException.class,()->new AnhDaiDienKhachService(jdbc,temp.toString()).doiAnhNhanVien(7,anh()));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void caNhanVienVaAdminDeuDoiAnhBangIdPhienKhongBangIdGuiLen() throws Exception {
        for(String quyen:List.of("Nhân viên","Quản trị viên")) {
            var service=mock(AnhDaiDienKhachService.class);var mvc=MockMvcBuilders.standaloneSetup(new AnhDaiDienKhachController(service)).build();
            var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(7,"nv@gmail.com","Nhân viên",quyen));
            s.setAttribute("csrfTaiKhoan","token");s.setAttribute("khachHangOnlineDangNhap",99);
            mvc.perform(multipart("/tai-khoan/anh-dai-dien").file(anh()).session(s)).andExpect(status().isForbidden());
            verifyNoInteractions(service);
            mvc.perform(multipart("/tai-khoan/anh-dai-dien").file(anh()).session(s).param("csrfTaiKhoan","token").param("id","99").param("loai","khach"))
                    .andExpect(redirectedUrl("/tai-khoan")).andExpect(flash().attributeExists("success"));
            verify(service).doiAnhNhanVien(eq(7),any());verify(service,never()).doiAnh(anyInt(),any());
        }
    }
    @Test void trangTaiKhoanNhanVienCoTokenDeDoiAnh() throws Exception {
        var service=mock(TaiKhoanService.class);var s=new MockHttpSession();
        s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(7,"admin@gmail.com","Admin","Quản trị viên"));
        var data=Map.<String,Object>of("ho_ten","Admin","anh_dai_dien","/image/avatars/nv-7-test.png","vai_tro","Quản trị viên");
        when(service.layThongTinNhanVien(7)).thenReturn(data);
        MockMvcBuilders.standaloneSetup(new TaiKhoanController(service)).build().perform(get("/tai-khoan").session(s))
                .andExpect(view().name("client/account/show")).andExpect(model().attribute("thongTin",data))
                .andExpect(model().attribute("laNhanVien",true)).andExpect(model().attributeExists("csrfTaiKhoan"));
        assertNotNull(s.getAttribute("csrfTaiKhoan"));
    }
    @Test void doiAnhChiXoaAnhCuSauCommitVaRollbackXoaAnhMoi() throws Exception {
        String tenCu="7-"+UUID.randomUUID()+".png";Path cu=temp.resolve(tenCu);Files.write(cu,new byte[]{1});
        var jdbc=jdbc("/image/avatars/"+tenCu);var service=new AnhDaiDienKhachService(jdbc,temp.toString());
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.doiAnh(7,anh());assertEquals(2,files().size());
            for(var sync:TransactionSynchronizationManager.getSynchronizations()) sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            assertEquals(List.of(cu),files());
        } finally {TransactionSynchronizationManager.clearSynchronization();}
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.doiAnh(7,anh());assertTrue(Files.exists(cu));
            for(var sync:TransactionSynchronizationManager.getSynchronizations()) sync.afterCommit();
            assertFalse(Files.exists(cu));assertEquals(1,files().size());
        } finally {TransactionSynchronizationManager.clearSynchronization();}
    }
    @Test void databaseLoiKhongDeLaiAnhMoiVaKhongXoaAnhCu() throws Exception {
        String tenCu="7-"+UUID.randomUUID()+".png";var cu=temp.resolve(tenCu);Files.write(cu,new byte[]{1});
        var jdbc=jdbc("/image/avatars/"+tenCu);
        when(jdbc.update(contains("SET anh_dai_dien"),anyString(),eq(7),eq(7))).thenThrow(new DataIntegrityViolationException("test"));
        assertThrows(DataIntegrityViolationException.class,()->new AnhDaiDienKhachService(jdbc,temp.toString()).doiAnh(7,anh()));
        assertEquals(List.of(cu),files());
    }
    @Test void anhGiaQuaLonVaTaiKhoanKhongHoatDongBiChan() throws Exception {
        var jdbc=jdbc(null);var service=new AnhDaiDienKhachService(jdbc,temp.toString());
        assertThrows(IllegalArgumentException.class,()->service.doiAnh(7,new MockMultipartFile("tepAnh","x.png","image/png","khong-phai-anh".getBytes())));
        assertThrows(IllegalArgumentException.class,()->service.doiAnh(7,new MockMultipartFile("tepAnh","x.png","image/png",new byte[5*1024*1024+1])));
        when(jdbc.queryForList(contains("SELECT anh_dai_dien"),eq(7))).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class,()->service.doiAnh(7,anh()));
        assertEquals(0,files().size());verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void khongXoaDuongDanAnhNgoaiThuMucHoacCuaKhachKhac() throws Exception {
        String ten="8-"+UUID.randomUUID()+".png";var khac=temp.resolve(ten);Files.write(khac,new byte[]{1});
        new AnhDaiDienKhachService(jdbc("/image/avatars/"+ten),temp.toString()).doiAnh(7,anh());
        assertTrue(Files.exists(khac));
    }
    @Test void uploadBatBuocDangNhapTokenVaChiDungIdTrongPhien() throws Exception {
        var service=mock(AnhDaiDienKhachService.class);var mvc=MockMvcBuilders.standaloneSetup(new AnhDaiDienKhachController(service)).build();
        mvc.perform(multipart("/tai-khoan/anh-dai-dien").file(anh())).andExpect(redirectedUrl("/login"));
        var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);s.setAttribute("csrfTaiKhoan","token");
        mvc.perform(multipart("/tai-khoan/anh-dai-dien").file(anh()).session(s)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
        mvc.perform(multipart("/tai-khoan/anh-dai-dien").file(anh()).session(s).param("csrfTaiKhoan","token").param("id","99"))
                .andExpect(redirectedUrl("/tai-khoan")).andExpect(flash().attributeExists("success"));
        verify(service).doiAnh(eq(7),any());
    }
}
