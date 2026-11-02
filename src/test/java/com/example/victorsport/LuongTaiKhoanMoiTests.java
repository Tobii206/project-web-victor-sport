package com.example.victorsport;

import com.example.victorsport.Service.*;
import com.example.victorsport.Controller.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LuongTaiKhoanMoiTests {
    @Test void khachTuDangKyVaDoiMatKhauCoGhiNguoiThucHien() {
        var jdbc=mock(JdbcTemplate.class);var service=new TuPhucVuTaiKhoanService(jdbc);
        when(jdbc.queryForObject(contains("INSERT INTO khach_hang"),eq(Integer.class),any(Object[].class))).thenReturn(7);
        assertEquals(7,service.dangKy(new com.example.victorsport.Dto.TaiKhoanKhachForm("Long","long@gmail.com","0325327602",null,null),"123456","123456"));
        verify(jdbc).update("UPDATE khach_hang SET nguoi_tao=id WHERE id=?",7);
        when(jdbc.queryForList(contains("SELECT mat_khau"),eq(7))).thenReturn(List.of(Map.of("mat_khau","123456")));
        service.doiMatKhau(7,"123456","654321","654321");
        verify(jdbc).update(contains("SET mat_khau=?,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=?"),anyString(),eq(7),eq(7));
    }
    @Test void luuNgaySinhVaGioiTinhCuaDungTaiKhoanDangNhap() throws Exception {
        var service=mock(TuPhucVuTaiKhoanService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new TuPhucVuTaiKhoanController(service,mock(DatLaiMatKhauService.class),mock(TaiKhoanService.class))).build();
        var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);s.setAttribute("csrfTaiKhoan","ma-hop-le");
        mvc.perform(post("/tai-khoan/chinh-sua").session(s).param("csrfTaiKhoan","ma-hop-le")
                .param("hoTen","Long").param("email","long@gmail.com").param("sdt","0325327602")
                .param("ngaySinh","2001-06-15").param("gioiTinh","0").param("id","99"))
                .andExpect(redirectedUrl("/tai-khoan"));
        verify(service).capNhat(eq(7),argThat(form -> java.time.LocalDate.of(2001,6,15).equals(form.ngaySinh()) && Integer.valueOf(0).equals(form.gioiTinh())));
        mvc.perform(post("/tai-khoan/chinh-sua").session(s).param("csrfTaiKhoan","ma-hop-le")
                .param("hoTen","Long").param("email","long@gmail.com").param("sdt","0325327602")
                .param("ngaySinh","").param("gioiTinh",""))
                .andExpect(redirectedUrl("/tai-khoan"));
        verify(service).capNhat(eq(7),argThat(form -> form.ngaySinh()==null && form.gioiTinh()==null));
    }
    @Test void khongLuuNgaySinhTuongLaiHoacGioiTinhSai() throws Exception {
        var service=mock(TuPhucVuTaiKhoanService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new TuPhucVuTaiKhoanController(service,mock(DatLaiMatKhauService.class),mock(TaiKhoanService.class))).build();
        var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);s.setAttribute("csrfTaiKhoan","ma-hop-le");
        for(var values:List.of(new String[]{java.time.LocalDate.now().plusDays(1).toString(),"1"},new String[]{"2001-06-15","2"},new String[]{"khong-phai-ngay","0"})) {
            mvc.perform(post("/tai-khoan/chinh-sua").session(s).param("csrfTaiKhoan","ma-hop-le")
                    .param("hoTen","Long").param("email","long@gmail.com").param("sdt","0325327602")
                    .param("ngaySinh",values[0]).param("gioiTinh",values[1]))
                    .andExpect(redirectedUrl("/tai-khoan/chinh-sua")).andExpect(flash().attributeExists("error"));
        }
        verifyNoInteractions(service);
    }
    @Test void capNhatDatabaseBaoGomNgaySinhVaGioiTinh() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("AS hoTen"),eq(7))).thenReturn(List.of(Map.of("id",7)));
        var service=new TuPhucVuTaiKhoanService(jdbc);
        var date=java.time.LocalDate.of(2001,6,15);
        service.capNhat(7,new com.example.victorsport.Dto.TaiKhoanKhachForm("Long","long@gmail.com","0325327602",date,0));
        verify(jdbc).update(contains("ngay_sinh=?,gioi_tinh=?"),eq("Long"),eq("long@gmail.com"),eq("0325327602"),eq(date),eq(0),eq(7),eq(7));
    }
    @Test void guiLaiQuaSomKhongGuiEmail() {
        var jdbc=mock(JdbcTemplate.class);
        var sender=mock(org.springframework.mail.javamail.JavaMailSender.class);
        var beans=new org.springframework.beans.factory.support.StaticListableBeanFactory();
        beans.addBean("mailSender",sender);
        var service=new DatLaiMatKhauService(jdbc,beans.getBeanProvider(org.springframework.mail.javamail.JavaMailSender.class));
        org.springframework.test.util.ReflectionTestUtils.setField(service,"emailEnabled",true);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"emailGui","shop@gmail.com");
        when(jdbc.queryForList(contains("la_nhan_vien"),eq("khach@gmail.com"),eq("khach@gmail.com")))
                .thenReturn(List.of(Map.of("id",7,"la_nhan_vien",0)));
        var error=assertThrows(IllegalArgumentException.class,()->service.guiMa("khach@gmail.com"));
        assertTrue(error.getMessage().contains("60 giây"));
        verifyNoInteractions(sender);
    }
    @Test void khongChoDoiThongTinKhachKhacVaBatBuocMaBaoVeForm() throws Exception {
        var service=mock(TuPhucVuTaiKhoanService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new TuPhucVuTaiKhoanController(service,mock(DatLaiMatKhauService.class),mock(TaiKhoanService.class))).build();
        mvc.perform(post("/tai-khoan/chinh-sua").param("hoTen","Long").param("email","long@gmail.com").param("sdt","0325327602")).andExpect(redirectedUrl("/login"));
        var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);s.setAttribute("csrfTaiKhoan","ma-hop-le");
        mvc.perform(post("/tai-khoan/chinh-sua").session(s).param("hoTen","Long").param("email","long@gmail.com").param("sdt","0325327602"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
        mvc.perform(post("/tai-khoan/chinh-sua").session(s).param("id","99").param("csrfTaiKhoan","ma-hop-le")
                .param("hoTen","Long").param("email","long@gmail.com").param("sdt","0325327602"))
                .andExpect(redirectedUrl("/tai-khoan"));
        verify(service).capNhat(eq(7),any());
    }
    @Test void matKhauKhongKhopVaQua72ByteBiTuChoi() {
        assertThrows(IllegalArgumentException.class,()->TuPhucVuTaiKhoanService.kiemTraMatKhau("123456","654321"));
        String dai="ấ".repeat(30);
        assertThrows(IllegalArgumentException.class,()->TuPhucVuTaiKhoanService.kiemTraMatKhau(dai,dai));
        assertFalse(TuPhucVuTaiKhoanService.dungMatKhau("123456","$2a$12$khong-hop-le"));
    }
    @Test void khoaDatabaseCoHieuLucVoiDoiTuongMoi() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject(contains("bi_khoa=1"),eq(Integer.class),eq(7))).thenReturn(1);
        assertTrue(new BaoMatTaiKhoanService(jdbc).biKhoa(7,false));
        assertTrue(new BaoMatTaiKhoanService(jdbc).biKhoa(7,false));
    }
    @Test void chuaCauHinhEmailKhongTuTaoMaHoacGuiEmail() {
        var jdbc=mock(JdbcTemplate.class);
        var service=new DatLaiMatKhauService(jdbc,new org.springframework.beans.factory.support.StaticListableBeanFactory().getBeanProvider(org.springframework.mail.javamail.JavaMailSender.class));
        assertFalse(service.sanSang());
        assertThrows(IllegalArgumentException.class,()->service.guiMa("khach@gmail.com"));
        verifyNoInteractions(jdbc);
    }
}
