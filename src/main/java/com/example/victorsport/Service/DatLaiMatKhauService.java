package com.example.victorsport.Service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DatLaiMatKhauService {
    private final JdbcTemplate jdbc;
    private final ObjectProvider<JavaMailSender> mail;
    @Value("${taikhoan.email-enabled:false}") private boolean emailEnabled;
    @Value("${spring.mail.username:}") private String emailGui;
    public DatLaiMatKhauService(JdbcTemplate jdbc,ObjectProvider<JavaMailSender> mail) {this.jdbc=jdbc;this.mail=mail;}
    public boolean sanSang() {return emailEnabled && !emailGui.isBlank() && mail.getIfAvailable()!=null;}
    private static String bam(String giaTri) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(giaTri.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    private Map<String,Object> timTaiKhoan(String email) {
        var ds=jdbc.queryForList("""
            SELECT id,1 AS la_nhan_vien FROM nhan_vien
            WHERE LOWER(email)=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0
            UNION ALL
            SELECT id,0 AS la_nhan_vien FROM khach_hang
            WHERE LOWER(email)=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0
            """,email,email);
        // Email phải xác định đúng một tài khoản đang hoạt động.
        return ds.size()==1?ds.get(0):null;
    }
    public void guiMa(String email) {
        if(!sanSang()) throw new IllegalArgumentException("Cửa hàng chưa bật gửi email. Vui lòng liên hệ cửa hàng để đặt lại mật khẩu.");
        if(!KiemTraThongTin.laEmailGmail(email)) throw new IllegalArgumentException("Vui lòng nhập email Gmail hợp lệ.");
        email=email.trim().toLowerCase(Locale.ROOT);
        var taiKhoan=timTaiKhoan(email);
        // Luôn trả cùng thông báo, không tiết lộ email có tài khoản hay không.
        if(taiKhoan==null) return;
        int id=((Number)taiKhoan.get("id")).intValue();
        boolean nhanVien=((Number)taiKhoan.get("la_nhan_vien")).intValue()==1;
        String cot=nhanVien?"id_nhan_vien":"id_khach_hang";
        guiMaTaiKhoan(id, nhanVien, email, cot+":"+id, "Mã đặt lại mật khẩu");
    }

    private void guiMaTaiKhoan(int id, boolean nhanVien, String email, String noiDungBam, String tieuDe) {
        if (!sanSang()) throw new IllegalArgumentException("Cửa hàng chưa bật gửi email. Vui lòng liên hệ cửa hàng.");
        String cot=nhanVien?"id_nhan_vien":"id_khach_hang";
        new BaoMatTaiKhoanService(jdbc).taoNeuChuaCo(id,nhanVien);
        String ma=String.format("%06d",new SecureRandom().nextInt(1000000));
        String hash=bam(noiDungBam+":"+ma);
        int dong=jdbc.update("""
            UPDATE bao_mat_tai_khoan SET ma_xac_minh_hash=?,han_xac_minh=DATEADD(MINUTE,10,SYSDATETIME()),
                ngay_gui_ma=SYSDATETIME(),so_lan_thu_ma=0
            WHERE %s=? AND (ngay_gui_ma IS NULL OR ngay_gui_ma<DATEADD(SECOND,-60,SYSDATETIME()))
            """.formatted(cot),hash,id);
        if(dong==0) throw new IllegalArgumentException("Vui lòng đợi đủ 60 giây trước khi gửi lại mã.");
        var message=new SimpleMailMessage();message.setFrom(emailGui);message.setTo(email);
        message.setSubject("VictorSport - "+tieuDe);
        message.setText("Mã xác minh của bạn: "+ma+"\nMã có hiệu lực trong 10 phút. Không chia sẻ mã này.\nNếu bạn không yêu cầu, vui lòng bỏ qua email.");
        try {mail.getObject().send(message);}
        catch(org.springframework.mail.MailException e) {
            jdbc.update("UPDATE bao_mat_tai_khoan SET ma_xac_minh_hash=NULL,han_xac_minh=NULL,ngay_gui_ma=NULL WHERE "+cot+"=? AND ma_xac_minh_hash=?",id,hash);
            if(e instanceof org.springframework.mail.MailAuthenticationException)
                throw new IllegalArgumentException("Không đăng nhập được máy chủ gửi email. Cửa hàng cần kiểm tra email và mật khẩu ứng dụng Google.");
            throw new IllegalArgumentException("Không kết nối hoặc gửi được email. Vui lòng kiểm tra kết nối mạng và cấu hình SMTP của cửa hàng.");
        }
    }

    public record YeuCauDoiMatKhau(int id, boolean nhanVien, String email, String dauVanTayMatKhauCu, String matKhauMoi)
            implements java.io.Serializable {
        String noiDungBam() {
            return "doi:"+(nhanVien?"id_nhan_vien":"id_khach_hang")+":"+id+":"+dauVanTayMatKhauCu+":"+matKhauMoi;
        }
    }

    public YeuCauDoiMatKhau guiMaDoiMatKhau(int id, boolean nhanVien, String cu, String moi, String nhapLai) {
        TuPhucVuTaiKhoanService.kiemTraMatKhau(moi, nhapLai);
        String bang=nhanVien?"nhan_vien":"khach_hang";
        var ds=jdbc.queryForList("SELECT email,mat_khau FROM "+bang+" WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",id);
        if (ds.size()!=1 || !TuPhucVuTaiKhoanService.dungMatKhau(cu,(String)ds.get(0).get("mat_khau")))
            throw new IllegalArgumentException("Mật khẩu hiện tại chưa đúng hoặc tài khoản không còn hoạt động.");
        if (TuPhucVuTaiKhoanService.dungMatKhau(moi,(String)ds.get(0).get("mat_khau")))
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        String email=Objects.toString(ds.get(0).get("email"),"").trim();
        if (!KiemTraThongTin.laEmailGmail(email)) throw new IllegalArgumentException("Tài khoản cần có email Gmail hợp lệ để xác nhận đổi mật khẩu.");
        var yeuCau=new YeuCauDoiMatKhau(id,nhanVien,email,bam((String)ds.get(0).get("mat_khau")),new BCryptPasswordEncoder().encode(moi));
        guiMaTaiKhoan(id,nhanVien,email,yeuCau.noiDungBam(),"Mã xác nhận đổi mật khẩu");
        return yeuCau;
    }

    @Transactional
    public boolean xacNhanDoiMatKhau(YeuCauDoiMatKhau yeuCau, String ma) {
        if (yeuCau==null || ma==null || !ma.matches("[0-9]{6}")) return false;
        String bang=yeuCau.nhanVien()?"nhan_vien":"khach_hang";
        String cot=yeuCau.nhanVien()?"id_nhan_vien":"id_khach_hang";
        var ds=jdbc.queryForList("SELECT id,ma_xac_minh_hash FROM bao_mat_tai_khoan WITH (UPDLOCK,HOLDLOCK) WHERE "+cot
                +"=? AND bi_khoa=0 AND han_xac_minh>SYSDATETIME() AND so_lan_thu_ma<5",yeuCau.id());
        if (ds.size()!=1) return false;
        var taiKhoan=jdbc.queryForList("SELECT email,mat_khau FROM "+bang+" WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",yeuCau.id());
        if (taiKhoan.size()!=1 || !yeuCau.dauVanTayMatKhauCu().equals(bam((String)taiKhoan.get(0).get("mat_khau")))
                || !yeuCau.email().equalsIgnoreCase(Objects.toString(taiKhoan.get(0).get("email"),"").trim())) return false;
        if (!bam(yeuCau.noiDungBam()+":"+ma).equals(ds.get(0).get("ma_xac_minh_hash"))) {
            jdbc.update("UPDATE bao_mat_tai_khoan SET so_lan_thu_ma=so_lan_thu_ma+1 WHERE id=?",ds.get(0).get("id"));
            return false;
        }
        jdbc.update("UPDATE "+bang+" SET mat_khau=?,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=?",
                yeuCau.matKhauMoi(),yeuCau.id(),yeuCau.id());
        jdbc.update("UPDATE bao_mat_tai_khoan SET ma_xac_minh_hash=NULL,han_xac_minh=NULL,so_lan_thu_ma=0 WHERE id=?",ds.get(0).get("id"));
        return true;
    }
    // Trả false để các lần thử mã sai vẫn được commit vào database.
    @Transactional
    public boolean datLai(String email,String ma,String mk,String nhapLai) {
        TuPhucVuTaiKhoanService.kiemTraMatKhau(mk,nhapLai);
        if(!KiemTraThongTin.laEmailGmail(email) || ma==null || !ma.matches("[0-9]{6}")) return false;
        var taiKhoan=timTaiKhoan(email.trim().toLowerCase(Locale.ROOT));
        if(taiKhoan==null) return false;
        int idTaiKhoan=((Number)taiKhoan.get("id")).intValue();
        boolean nhanVien=((Number)taiKhoan.get("la_nhan_vien")).intValue()==1;
        String cot=nhanVien?"id_nhan_vien":"id_khach_hang";
        var ds=jdbc.queryForList("""
            SELECT id,ma_xac_minh_hash FROM bao_mat_tai_khoan WITH (UPDLOCK,HOLDLOCK)
            WHERE %s=? AND han_xac_minh>SYSDATETIME() AND so_lan_thu_ma<5
            """.formatted(cot),idTaiKhoan);
        if(ds.size()!=1) return false;
        var row=ds.get(0);
        String hash=bam(cot+":"+idTaiKhoan+":"+ma);
        if(!hash.equals(row.get("ma_xac_minh_hash"))) {
            jdbc.update("UPDATE bao_mat_tai_khoan SET so_lan_thu_ma=so_lan_thu_ma+1 WHERE id=?",row.get("id"));return false;
        }
        String bang=nhanVien?"nhan_vien":"khach_hang";
        jdbc.update("UPDATE "+bang+" SET mat_khau=?,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=?",new BCryptPasswordEncoder().encode(mk),idTaiKhoan,idTaiKhoan);
        new BaoMatTaiKhoanService(jdbc).moKhoa(idTaiKhoan,nhanVien);
        return true;
    }
}
