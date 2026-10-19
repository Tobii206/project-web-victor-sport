package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmailDotGiamGiaTests {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final JavaMailSender sender = mock(JavaMailSender.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);

    private EmailDotGiamGiaService service(Executor executor) {
        when(provider.getIfAvailable()).thenReturn(sender);
        when(sender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        var service = new EmailDotGiamGiaService(jdbc,provider,executor);
        ReflectionTestUtils.setField(service,"emailEnabled",true);
        ReflectionTestUtils.setField(service,"emailGui","cuahang@gmail.com");
        return service;
    }
    private void duLieu(Object loai) {
        when(jdbc.queryForList(contains("FROM dot_giam_gia"),eq(8))).thenReturn(List.of(Map.of(
            "ten_dot_giam_gia","Ưu đãi cuối tuần","loai_giam_gia",loai,"gia_tri_giam_gia",new BigDecimal("15.00"),
            "so_tien_giam_toi_da",new BigDecimal("100000"),"ngay_bat_dau",java.sql.Date.valueOf("2026-10-20"),
            "ngay_ket_thuc",java.sql.Date.valueOf("2026-10-25"))));
        when(jdbc.queryForList(contains("FROM khach_hang"),eq(String.class)))
            .thenReturn(Arrays.asList(" KHACH1@gmail.com ","khach2@gmail.com","khach1@gmail.com",null,"khong-hop-le"));
    }
    @Test void guiNenRiengTungKhachKhongTrungVaDungNoiDung() throws Exception {
        var hangDoi = new ArrayList<Runnable>();
        var service = service(hangDoi::add);duLieu(false);
        assertTrue(service.thongBaoDotMoi(8));verifyNoInteractions(jdbc,sender);
        assertEquals(1,hangDoi.size());hangDoi.get(0).run();
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender,times(2)).send(captor.capture());
        var mails = captor.getAllValues();
        assertEquals("khach1@gmail.com",((InternetAddress)mails.get(0).getRecipients(Message.RecipientType.TO)[0]).getAddress());
        assertEquals("khach2@gmail.com",((InternetAddress)mails.get(1).getRecipients(Message.RecipientType.TO)[0]).getAddress());
        for (var mail:mails) {
            assertNull(mail.getRecipients(Message.RecipientType.CC));assertNull(mail.getRecipients(Message.RecipientType.BCC));
            String text = noiDung(mail,"text/plain");
            assertTrue(text.contains("15%"));assertTrue(text.contains("100.000 đ"));
            assertTrue(text.contains("20/10/2026 đến hết 25/10/2026"));
            assertTrue(text.contains("các mẫu giày tham gia"));
            String html = noiDung(mail,"text/html");
            assertTrue(html.contains("15%"));assertTrue(html.contains("Ưu đãi cuối tuần"));
            assertTrue(html.contains("cid:victorsport-logo"));
            assertTrue(coLogo(mail));
            assertEquals("VictorSport",((InternetAddress)mail.getFrom()[0]).getPersonal());
            java.nio.file.Files.writeString(java.nio.file.Path.of("target/email-giam-gia-preview.html"),html);
        }
        verify(jdbc).queryForList(argThat(sql -> sql.contains("FROM khach_hang") && sql.contains("trang_thai=1")
            && sql.contains("xoa_mem,0)=0") && sql.contains("ten_tai_khoan") && sql.contains("mat_khau")),eq(String.class));
    }
    @Test void giamTienDungDonViDongVaMotKhachLoiVanGuiKhachSau() throws Exception {
        var service = service(Runnable::run);duLieu(true);
        doThrow(new MailSendException("Lỗi thử nghiệm")).doNothing().when(sender).send(any(MimeMessage.class));
        assertTrue(service.thongBaoDotMoi(8));
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender,times(2)).send(captor.capture());
        assertTrue(noiDung(captor.getValue(),"text/plain").contains("Mức giảm: 15 đ"));
        assertFalse(noiDung(captor.getValue(),"text/html").contains("15%"));
    }
    private String noiDung(Part part,String type) throws Exception {
        if (part instanceof MimeMessage message) message.saveChanges();
        if (part.isMimeType(type)) return part.getContent().toString();
        if (part.getContent() instanceof Multipart multi) for (int i=0;i<multi.getCount();i++) {
            String text=noiDung(multi.getBodyPart(i),type);if(!text.isEmpty()) return text;
        }
        return "";
    }
    private boolean coLogo(Part part) throws Exception {
        if (part.isMimeType("image/png") && Arrays.asList(part.getHeader("Content-ID")).contains("<victorsport-logo>")) return true;
        if (part.getContent() instanceof Multipart multi) for (int i=0;i<multi.getCount();i++) if(coLogo(multi.getBodyPart(i))) return true;
        return false;
    }
    @Test void tenChuongTrinhDuocEscapeKhongChenHtml() throws Exception {
        var service=service(Runnable::run);duLieu(false);
        when(jdbc.queryForList(contains("FROM dot_giam_gia"),eq(8))).thenReturn(List.of(Map.of(
            "ten_dot_giam_gia","Sale <img src=x onerror=alert(1)> & quà tặng","loai_giam_gia",false,
            "gia_tri_giam_gia",new BigDecimal("10"),"ngay_bat_dau",LocalDate.now(),"ngay_ket_thuc",LocalDate.now().plusDays(1))));
        assertTrue(service.thongBaoDotMoi(8));
        var captor=org.mockito.ArgumentCaptor.forClass(MimeMessage.class);verify(sender,times(2)).send(captor.capture());
        String html=noiDung(captor.getValue(),"text/html");
        assertTrue(html.contains("&lt;img"));assertFalse(html.contains("<img src=x"));
        assertFalse(html.contains("Tối đa được giảm"));assertFalse(html.contains("{{"));
    }
    @Test void chuaBatMailKhongDocKhachHayXepLich() {
        var executor = mock(Executor.class);var service = service(executor);
        ReflectionTestUtils.setField(service,"emailEnabled",false);
        assertFalse(service.thongBaoDotMoi(8));verifyNoInteractions(jdbc,sender,executor);
    }
    @Test void hangDoiDayHoacDatabaseLoiKhongNemLoiVeLuuDot() {
        var executor = mock(Executor.class);var service = service(executor);
        doThrow(new java.util.concurrent.RejectedExecutionException()).when(executor).execute(any());
        assertFalse(service.thongBaoDotMoi(8));verifyNoInteractions(sender);
        var serviceChayNgay = service(Runnable::run);
        when(jdbc.queryForList(contains("FROM dot_giam_gia"),eq(8)))
            .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB test"));
        assertDoesNotThrow(() -> serviceChayNgay.thongBaoDotMoi(8));verifyNoInteractions(sender);
    }
    @Test void dotDaNgungHoacHetHanKhongGui() {
        var service = service(Runnable::run);
        when(jdbc.queryForList(contains("FROM dot_giam_gia"),eq(8))).thenReturn(List.of());
        assertTrue(service.thongBaoDotMoi(8));verifyNoInteractions(sender);
        verify(jdbc,never()).queryForList(contains("FROM khach_hang"),eq(String.class));
    }
    private MockHttpSession admin() {
        var session = new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(1,"admin@gmail.com","Admin","Quản trị viên"));
        return session;
    }
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder luu(String path,String trangThai) {
        return post(path).session(admin()).param("ten_dot_giam_gia","Đợt mới").param("loai_giam_gia","0")
            .param("gia_tri_giam_gia","15").param("ngay_bat_dau",LocalDate.now().toString())
            .param("ngay_ket_thuc",LocalDate.now().plusDays(5).toString()).param("muc_uu_tien","1").param("trang_thai",trangThai);
    }
    @Test void chiGuiSauKhiTaoThanhCongKhongGuiLaiKhiSua() throws Exception {
        var dot = mock(QuanLyDotGiamGiaService.class);var email = mock(EmailDotGiamGiaService.class);
        when(dot.luuVaKiemTraThongBao(isNull(),any())).thenAnswer(i ->
            new QuanLyDotGiamGiaService.KetQuaLuu(8,Integer.valueOf(1).equals(((com.example.victorsport.Dto.DotGiamGiaForm)i.getArgument(1)).getTrang_thai())));
        when(dot.luuVaKiemTraThongBao(eq(8),any())).thenReturn(new QuanLyDotGiamGiaService.KetQuaLuu(8,false));
        when(email.thongBaoDotMoi(8)).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyDotGiamGiaController(dot,email)).build();
        mvc.perform(luu("/quan-ly/dot-giam-gia/luu","1")).andExpect(redirectedUrl("/quan-ly/dot-giam-gia/8"));
        var order = inOrder(dot,email);order.verify(dot).luuVaKiemTraThongBao(isNull(),any());order.verify(email).thongBaoDotMoi(8);
        mvc.perform(luu("/quan-ly/dot-giam-gia/8/luu","1")).andExpect(redirectedUrl("/quan-ly/dot-giam-gia/8"));
        mvc.perform(luu("/quan-ly/dot-giam-gia/luu","0")).andExpect(redirectedUrl("/quan-ly/dot-giam-gia/8"));
        verify(email,times(1)).thongBaoDotMoi(8);
    }
    @Test void luuLoiHoacFormSaiKhongGuiMail() throws Exception {
        var dot = mock(QuanLyDotGiamGiaService.class);var email = mock(EmailDotGiamGiaService.class);
        when(dot.luuVaKiemTraThongBao(isNull(),any())).thenThrow(new IllegalArgumentException("Không lưu được"));
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyDotGiamGiaController(dot,email)).build();
        mvc.perform(luu("/quan-ly/dot-giam-gia/luu","1")).andExpect(flash().attributeExists("error"));
        mvc.perform(luu("/quan-ly/dot-giam-gia/luu","1").param("gia_tri_giam_gia","abc")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(email);
    }
    @Test void batLaiGuiEmailSauKhiLuuThanhCong() throws Exception {
        var dot=mock(QuanLyDotGiamGiaService.class);var email=mock(EmailDotGiamGiaService.class);
        when(dot.luuVaKiemTraThongBao(eq(8),any())).thenReturn(new QuanLyDotGiamGiaService.KetQuaLuu(8,true));
        when(email.thongBaoBatLai(8)).thenReturn(true);
        var mvc=MockMvcBuilders.standaloneSetup(new QuanLyDotGiamGiaController(dot,email)).build();
        mvc.perform(luu("/quan-ly/dot-giam-gia/8/luu","1")).andExpect(redirectedUrl("/quan-ly/dot-giam-gia/8"));
        var order=inOrder(dot,email);order.verify(dot).luuVaKiemTraThongBao(eq(8),any());order.verify(email).thongBaoBatLai(8);
        verify(email,never()).thongBaoDotMoi(anyInt());
    }
    @Test void emailBatLaiDungTieuDeVaKiemTraLaiThoiGianTruocKhiGui() throws Exception {
        var service=service(Runnable::run);duLieu(false);
        assertTrue(service.thongBaoBatLai(8));
        var captor=org.mockito.ArgumentCaptor.forClass(MimeMessage.class);verify(sender,times(2)).send(captor.capture());
        assertTrue(captor.getValue().getSubject().contains("áp dụng trở lại"));
        assertTrue(noiDung(captor.getValue(),"text/html").contains("ƯU ĐÃI ĐÃ TRỞ LẠI"));
        assertTrue(noiDung(captor.getValue(),"text/plain").contains("áp dụng trở lại"));
        verify(jdbc).queryForList(argThat(sql->sql.contains("FROM dot_giam_gia")
            &&sql.contains("ngay_bat_dau<=CONVERT(date,SYSDATETIME())")&&sql.contains("ngay_ket_thuc>=")),eq(8));
    }
}
