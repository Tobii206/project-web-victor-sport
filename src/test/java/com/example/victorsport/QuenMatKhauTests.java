package com.example.victorsport;

import com.example.victorsport.Service.DatLaiMatKhauService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuenMatKhauTests {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final JavaMailSender mail=mock(JavaMailSender.class);
    private DatLaiMatKhauService service() {
        var beans=new StaticListableBeanFactory();beans.addBean("mail",mail);
        var service=new DatLaiMatKhauService(jdbc,beans.getBeanProvider(JavaMailSender.class));
        ReflectionTestUtils.setField(service,"emailEnabled",true);
        ReflectionTestUtils.setField(service,"emailGui","shop@gmail.com");
        return service;
    }
    @Test void guiVaDatLaiDungBangChoKhachVaNhanVien() {
        for(int loai:List.of(0,1)) {
            reset(jdbc,mail);
            var service=service();
            when(jdbc.queryForList(contains("la_nhan_vien"),eq("user@gmail.com"),eq("user@gmail.com")))
                .thenReturn(List.of(Map.of("id",7,"la_nhan_vien",loai)));
            var hash=new AtomicReference<String>();
            when(jdbc.update(contains("SET ma_xac_minh_hash=?"),anyString(),eq(7))).thenAnswer(call->{hash.set(call.getArgument(1));return 1;});
            service.guiMa("user@gmail.com");
            var message=ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mail).send(message.capture());
            assertArrayEquals(new String[]{"user@gmail.com"},message.getValue().getTo());
            var matcher=java.util.regex.Pattern.compile("[0-9]{6}").matcher(message.getValue().getText());
            assertTrue(matcher.find());String ma=matcher.group();
            when(jdbc.queryForList(contains("FROM bao_mat_tai_khoan WITH"),eq(7)))
                .thenReturn(List.of(Map.of("id",99,"ma_xac_minh_hash",hash.get())));
            assertFalse(service.datLai("user@gmail.com","abcdef","moi12345","moi12345"));
            assertTrue(service.datLai("user@gmail.com",ma,"moi12345","moi12345"));
            String bang=loai==1?"nhan_vien":"khach_hang";
            var matKhau=ArgumentCaptor.forClass(String.class);
            verify(jdbc).update(eq("UPDATE "+bang+" SET mat_khau=?,ngay_cap_nhat=SYSDATETIME() WHERE id=?"),matKhau.capture(),eq(7));
            assertTrue(new BCryptPasswordEncoder().matches("moi12345",matKhau.getValue()));
            String cot=loai==1?"id_nhan_vien":"id_khach_hang";
            verify(jdbc).update(contains("bi_khoa=0,ma_xac_minh_hash=NULL,han_xac_minh=NULL WHERE "+cot),eq(7));
            assertFalse(service.datLai("user@gmail.com",ma.equals("000000")?"000001":"000000","moi12345","moi12345"));
        }
    }
    @Test void emailKhongCoHoacTrungKhongGuiMa() {
        var service=service();
        service.guiMa("unknown@gmail.com");
        when(jdbc.queryForList(contains("la_nhan_vien"),eq("shared@gmail.com"),eq("shared@gmail.com")))
            .thenReturn(List.of(Map.of("id",1,"la_nhan_vien",0),Map.of("id",1,"la_nhan_vien",1)));
        service.guiMa("shared@gmail.com");
        verifyNoInteractions(mail);
        verify(jdbc,never()).update(contains("UPDATE bao_mat_tai_khoan"),anyString(),anyInt());
    }
    @Test void loiDangNhapSmtpHuyMaVaBaoLoiRoRang() {
        var service=service();
        when(jdbc.queryForList(contains("la_nhan_vien"),eq("user@gmail.com"),eq("user@gmail.com")))
            .thenReturn(List.of(Map.of("id",7,"la_nhan_vien",1)));
        when(jdbc.update(contains("SET ma_xac_minh_hash=?"),anyString(),eq(7))).thenReturn(1);
        doThrow(new MailAuthenticationException("failed")).when(mail).send(any(SimpleMailMessage.class));
        var loi=assertThrows(IllegalArgumentException.class,()->service.guiMa("user@gmail.com"));
        assertTrue(loi.getMessage().contains("mật khẩu ứng dụng"));
        verify(jdbc).update(contains("ngay_gui_ma=NULL WHERE id_nhan_vien=?"),eq(7),anyString());
    }
}
