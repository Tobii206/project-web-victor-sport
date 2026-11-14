package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DoiMatKhauEmailTests {
    private String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    @Test void caKhachVaAdminChiDoiSauKhiXacNhanEmailVaMaKhongDungLaiDuoc() throws Exception {
        for(boolean staff:List.of(false,true)) {
            var jdbc=mock(JdbcTemplate.class);var sender=mock(JavaMailSender.class);
            var beans=new StaticListableBeanFactory();beans.addBean("mail",sender);
            var service=new DatLaiMatKhauService(jdbc,beans.getBeanProvider(JavaMailSender.class));
            ReflectionTestUtils.setField(service,"emailEnabled",true);
            ReflectionTestUtils.setField(service,"emailGui","shop@gmail.com");
            String table=staff?"nhan_vien":"khach_hang";
            var account=Map.<String,Object>of("email","user@gmail.com","mat_khau","123456");
            when(jdbc.queryForList(startsWith("SELECT email,mat_khau FROM "+table),eq(7))).thenReturn(List.of(account));
            when(jdbc.update(contains("SET ma_xac_minh_hash=?"),anyString(),eq(7))).thenReturn(1);
            var pending=service.guiMaDoiMatKhau(7,staff,"123456","654321","654321");
            verify(jdbc,never()).update(startsWith("UPDATE "+table+" SET mat_khau"),anyString(),anyInt(),anyInt());
            var mail=org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);verify(sender).send(mail.capture());
            assertArrayEquals(new String[]{"user@gmail.com"},mail.getValue().getTo());
            var code=java.util.regex.Pattern.compile("[0-9]{6}").matcher(mail.getValue().getText());assertTrue(code.find());
            String prefix="doi:"+(staff?"id_nhan_vien":"id_khach_hang")+":7:"+hash("123456")+":"+pending.matKhauMoi();
            when(jdbc.queryForList(startsWith("SELECT id,ma_xac_minh_hash FROM bao_mat_tai_khoan"),eq(7)))
                    .thenReturn(List.of(Map.of("id",11,"ma_xac_minh_hash",hash(prefix+":"+code.group()))),List.of());
            assertTrue(service.xacNhanDoiMatKhau(pending,code.group()));
            assertTrue(new BCryptPasswordEncoder().matches("654321",pending.matKhauMoi()));
            verify(jdbc).update(startsWith("UPDATE "+table+" SET mat_khau"),eq(pending.matKhauMoi()),eq(7),eq(7));
            verify(jdbc).update(contains("SET ma_xac_minh_hash=NULL,han_xac_minh=NULL,so_lan_thu_ma=0"),eq(11));
            assertFalse(service.xacNhanDoiMatKhau(pending,code.group()));
        }
    }
    @Test void maSaiHoacMaDatLaiMatKhauKhongDungChoDoiMatKhau() throws Exception {
        var jdbc=mock(JdbcTemplate.class);
        var service=new DatLaiMatKhauService(jdbc,new StaticListableBeanFactory().getBeanProvider(JavaMailSender.class));
        var pending=new DatLaiMatKhauService.YeuCauDoiMatKhau(7,false,"user@gmail.com",hash("123456"),"new-hash");
        when(jdbc.queryForList(startsWith("SELECT id,ma_xac_minh_hash"),eq(7)))
                .thenReturn(List.of(Map.of("id",11,"ma_xac_minh_hash",hash("id_khach_hang:7:123456"))));
        when(jdbc.queryForList(startsWith("SELECT email,mat_khau"),eq(7)))
                .thenReturn(List.of(Map.of("email","user@gmail.com","mat_khau","123456")));
        assertFalse(service.xacNhanDoiMatKhau(pending,"123456"));
        verify(jdbc).update(contains("so_lan_thu_ma=so_lan_thu_ma+1"),eq(11));
        verify(jdbc,never()).update(startsWith("UPDATE khach_hang SET mat_khau"),anyString(),anyInt(),anyInt());
        clearInvocations(jdbc);
        assertFalse(service.xacNhanDoiMatKhau(pending,"abc"));verifyNoInteractions(jdbc);
    }
    @Test void emailHoacMatKhauDaThayDoiThiYeuCauCuKhongConHieuLuc() throws Exception {
        var jdbc=mock(JdbcTemplate.class);
        var service=new DatLaiMatKhauService(jdbc,new StaticListableBeanFactory().getBeanProvider(JavaMailSender.class));
        var pending=new DatLaiMatKhauService.YeuCauDoiMatKhau(7,true,"user@gmail.com",hash("123456"),"new-hash");
        when(jdbc.queryForList(startsWith("SELECT id,ma_xac_minh_hash"),eq(7))).thenReturn(List.of(Map.of("id",11)));
        for(var account:List.of(Map.of("email","other@gmail.com","mat_khau","123456"),Map.of("email","user@gmail.com","mat_khau","other-password"))) {
            when(jdbc.queryForList(startsWith("SELECT email,mat_khau"),eq(7))).thenReturn(List.of(new HashMap<>(account)));
            assertFalse(service.xacNhanDoiMatKhau(pending,"123456"));
        }
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void webGuiMaChoDungVaiTroVaChiDangXuatSauXacNhan() throws Exception {
        for(boolean staff:List.of(false,true)) {
            var reset=mock(DatLaiMatKhauService.class);var profile=mock(TuPhucVuTaiKhoanService.class);
            var mvc=MockMvcBuilders.standaloneSetup(new TuPhucVuTaiKhoanController(profile,reset,mock(TaiKhoanService.class))).build();
            var session=new MockHttpSession();session.setAttribute("csrfTaiKhoan","token");
            if(staff)session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(7,"admin@gmail.com","Admin","Quản trị viên"));
            else session.setAttribute("khachHangOnlineDangNhap",7);
            var pending=new DatLaiMatKhauService.YeuCauDoiMatKhau(7,staff,"user@gmail.com","old","new");
            when(reset.guiMaDoiMatKhau(7,staff,"123456","654321","654321")).thenReturn(pending);
            mvc.perform(post("/tai-khoan/doi-mat-khau").session(session).param("csrfTaiKhoan","token")
                    .param("matKhauCu","123456").param("matKhau","654321").param("nhapLai","654321"))
                    .andExpect(redirectedUrl("/tai-khoan/doi-mat-khau"));
            assertSame(pending,session.getAttribute("doiMatKhauCho"));
            mvc.perform(get("/tai-khoan/doi-mat-khau").session(session)).andExpect(model().attribute("dangXacNhanDoiMatKhau",true));
            mvc.perform(post("/tai-khoan/doi-mat-khau/xac-nhan").session(session).param("ma","111111")).andExpect(status().isForbidden());
            mvc.perform(post("/tai-khoan/doi-mat-khau/xac-nhan").session(session).param("csrfTaiKhoan","token").param("ma","111111"))
                    .andExpect(redirectedUrl("/tai-khoan/doi-mat-khau")).andExpect(flash().attributeExists("error"));
            assertFalse(session.isInvalid());
            when(reset.xacNhanDoiMatKhau(pending,"123456")).thenReturn(true);
            mvc.perform(post("/tai-khoan/doi-mat-khau/xac-nhan").session(session).param("csrfTaiKhoan","token").param("ma","123456"))
                    .andExpect(redirectedUrl("/login"));
            assertTrue(session.isInvalid());verifyNoInteractions(profile);
        }
    }
}
