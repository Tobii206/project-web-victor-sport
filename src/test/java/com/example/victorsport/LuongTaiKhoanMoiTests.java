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
