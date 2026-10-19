package com.example.victorsport.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.util.HtmlUtils;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;

@Service
public class EmailDotGiamGiaService {
    private static final Logger log = LoggerFactory.getLogger(EmailDotGiamGiaService.class);
    private final JdbcTemplate jdbc;
    private final ObjectProvider<JavaMailSender> mail;
    private final Executor executor;
    @Value("${taikhoan.email-enabled:false}") private boolean emailEnabled;
    @Value("${spring.mail.username:}") private String emailGui;

    public EmailDotGiamGiaService(JdbcTemplate jdbc, ObjectProvider<JavaMailSender> mail,
                                 @Qualifier("emailDotGiamGiaExecutor") Executor executor) {
        this.jdbc = jdbc;this.mail = mail;this.executor = executor;
    }

    // Controller gọi sau khi giao dịch lưu đợt giảm giá đã hoàn tất.
    public boolean thongBaoDotMoi(int id) {
        return thongBao(id, false);
    }

    public boolean thongBaoBatLai(int id) {
        return thongBao(id, true);
    }

    private boolean thongBao(int id, boolean batLai) {
        if (!emailEnabled || emailGui == null || emailGui.isBlank()) return false;
        try {
            if (mail.getIfAvailable() == null) return false;
            executor.execute(() -> gui(id, batLai));
            return true;
        } catch (RuntimeException e) {
            log.warn("Chưa xếp lịch gửi email đợt giảm giá {} ({}).", id, e.getClass().getSimpleName());
            return false;
        }
    }

    private void gui(int id, boolean batLai) {
        try {
            var ds = jdbc.queryForList("""
                SELECT ten_dot_giam_gia,loai_giam_gia,gia_tri_giam_gia,so_tien_giam_toi_da,ngay_bat_dau,ngay_ket_thuc
                FROM dot_giam_gia WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0
                  AND ngay_ket_thuc>=CONVERT(date,SYSDATETIME())
                """ + (batLai ? " AND ngay_bat_dau<=CONVERT(date,SYSDATETIME())" : ""), id);
            if (ds.isEmpty()) return;
            var dot = ds.get(0);
            var emailKhach = jdbc.queryForList("""
                SELECT DISTINCT LOWER(LTRIM(RTRIM(email))) AS email FROM khach_hang
                WHERE trang_thai=1 AND ISNULL(xoa_mem,0)=0
                  AND NULLIF(LTRIM(RTRIM(email)),'') IS NOT NULL
                  AND NULLIF(LTRIM(RTRIM(ten_tai_khoan)),'') IS NOT NULL
                  AND NULLIF(LTRIM(RTRIM(mat_khau)),'') IS NOT NULL
                """, String.class);
            var sender = mail.getIfAvailable();
            if (sender == null) return;
            String ten = Objects.toString(dot.get("ten_dot_giam_gia"), "").replaceAll("[\\r\\n]+", " ");
            String noiDung = noiDung(dot, ten, batLai);
            Path logo = Path.of("logo", "logo-nobackgroud.png");
            boolean coLogo = Files.isRegularFile(logo);
            String html = noiDungHtml(dot, ten, coLogo, batLai);
            int thanhCong = 0, thatBai = 0;
            // Mỗi người nhận một email riêng; không lộ danh sách email khách khác.
            Set<String> daGui = new HashSet<>();
            for (String diaChi : emailKhach) {
                if (diaChi == null) continue;
                diaChi = diaChi.trim().toLowerCase(Locale.ROOT);
                if (!KiemTraThongTin.laEmailGmail(diaChi) || !daGui.add(diaChi)) continue;
                try {
                    var message = sender.createMimeMessage();
                    var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                    helper.setFrom(emailGui.trim(), "VictorSport");helper.setTo(diaChi);
                    helper.setSubject("VictorSport - " + (batLai ? "Ưu đãi được áp dụng trở lại: " : "Đợt giảm giá mới: ") + ten);
                    helper.setText(noiDung, html);
                    if (coLogo) helper.addInline("victorsport-logo", new FileSystemResource(logo), "image/png");
                    sender.send(message);thanhCong++;
                } catch (MailException | MessagingException | IOException e) {thatBai++;}
            }
            log.info("Email đợt giảm giá {}: gửi thành công {}, thất bại {}.", id, thanhCong, thatBai);
        } catch (RuntimeException | IOException e) {
            log.warn("Chưa gửi xong email đợt giảm giá {} ({}).", id, e.getClass().getSimpleName());
        }
    }

    private String noiDung(Map<String,Object> dot, String ten, boolean batLai) {
        var tien = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        BigDecimal giaTri = new BigDecimal(dot.get("gia_tri_giam_gia").toString());
        Object loai = dot.get("loai_giam_gia");
        boolean phanTram = Boolean.FALSE.equals(loai) || loai instanceof Number n && n.intValue() == 0;
        String mucGiam = phanTram ? giaTri.stripTrailingZeros().toPlainString() + "%" : tien.format(giaTri) + " đ";
        String gioiHan = "";
        if (dot.get("so_tien_giam_toi_da") != null) {
            BigDecimal toiDa = new BigDecimal(dot.get("so_tien_giam_toi_da").toString());
            if (toiDa.signum() > 0) gioiHan = "Tối đa được giảm: " + tien.format(toiDa) + " đ / đôi.\n";
        }
        return "Chào bạn,\n\n" + (batLai ? "Đợt giảm giá tại VictorSport đã được áp dụng trở lại!\n\n" : "VictorSport vừa có đợt giảm giá mới!\n\n")
            + "Tên chương trình: " + ten + "\nMức giảm: " + mucGiam + "\n" + gioiHan
            + "Thời gian: " + ngay(dot.get("ngay_bat_dau")) + " đến hết " + ngay(dot.get("ngay_ket_thuc")) + "\n\n"
            + "Ưu đãi áp dụng cho các mẫu giày tham gia chương trình. "
            + "Bạn có thể xem sản phẩm và giá ưu đãi trên website VictorSport.\n\n"
            + "Cảm ơn bạn đã đăng ký tài khoản và đồng hành cùng VictorSport!";
    }

    private String ngay(Object value) {
        LocalDate date = value instanceof java.sql.Date d ? d.toLocalDate() : LocalDate.parse(value.toString());
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String noiDungHtml(Map<String,Object> dot, String ten, boolean coLogo, boolean batLai) throws IOException {
        String template;
        try (var input = new ClassPathResource("templates/email-dot-giam-gia.html").getInputStream()) {
            template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        var tien = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        var giaTri = new BigDecimal(dot.get("gia_tri_giam_gia").toString());
        Object loai = dot.get("loai_giam_gia");
        boolean phanTram = Boolean.FALSE.equals(loai) || loai instanceof Number n && n.intValue() == 0;
        String mucGiam = phanTram ? giaTri.stripTrailingZeros().toPlainString() + "%" : tien.format(giaTri) + " đ";
        String gioiHan = "";
        if (dot.get("so_tien_giam_toi_da") != null) {
            var toiDa = new BigDecimal(dot.get("so_tien_giam_toi_da").toString());
            if (toiDa.signum() > 0) gioiHan = "<p style=\"margin:8px 0 0;font-size:13px;line-height:22px;color:#8a4a23;\">Tối đa được giảm <strong>"
                + tien.format(toiDa) + " đ</strong> / đôi</p>";
        }
        return template.replace("{{LOGO}}", coLogo
                ? "<img src=\"cid:victorsport-logo\" alt=\"VictorSport\" width=\"190\" style=\"display:block;width:190px;max-width:100%;height:auto;border:0;\">"
                : "<strong style=\"font-size:22px;color:#142b52;\">VICTOR<span style=\"color:#b94e13;\">SPORT</span></strong>")
            .replace("{{NHAN}}", batLai ? "ƯU ĐÃI ĐÃ TRỞ LẠI" : "ƯU ĐÃI MỚI TỪ VICTORSPORT")
            .replace("{{LOI_MO}}", batLai
                ? "Chương trình giảm giá đã được áp dụng trở lại và vẫn đang trong thời gian ưu đãi. Cùng khám phá các mẫu giày tham gia nhé!"
                : "VictorSport gửi bạn thông tin chương trình giảm giá mới. Cùng khám phá ưu đãi dành cho các mẫu giày tham gia nhé!")
            .replace("{{MUC_GIAM}}", HtmlUtils.htmlEscape(mucGiam, StandardCharsets.UTF_8.name()))
            .replace("{{THOI_GIAN}}", ngay(dot.get("ngay_bat_dau")) + " đến hết " + ngay(dot.get("ngay_ket_thuc")))
            .replace("{{GIOI_HAN}}", gioiHan)
            .replace("{{TEN}}", HtmlUtils.htmlEscape(ten, StandardCharsets.UTF_8.name()));
    }
}
