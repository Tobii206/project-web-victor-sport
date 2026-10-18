package com.example.victorsport.Service;

import com.example.victorsport.Dto.TaiKhoanKhachForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TuPhucVuTaiKhoanService {
    private final JdbcTemplate jdbc;
    public TuPhucVuTaiKhoanService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public static void kiemTraMatKhau(String mk,String nhapLai) {
        KiemTraThongTin.kiemTraMatKhau(mk);
        if(mk.getBytes(StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("Mật khẩu tối đa 72 byte.");
        if(!mk.equals(nhapLai)) throw new IllegalArgumentException("Mật khẩu nhập lại không khớp.");
    }
    public static boolean dungMatKhau(String mk,String daLuu) {
        if(mk==null || daLuu==null) return false;
        if(daLuu.matches("^\\$2[aby]\\$.*")) return new BCryptPasswordEncoder().matches(mk,daLuu);
        return mk.equals(daLuu);
    }
    private String emailHopLe(TaiKhoanKhachForm form) {
        if(form.hoTen()==null || form.hoTen().isBlank() || form.hoTen().trim().length()>100
            || !KiemTraThongTin.laEmailGmail(form.email()) || form.sdt()==null || !form.sdt().matches("0[0-9]{9}"))
            throw new IllegalArgumentException("Vui lòng nhập họ tên, email Gmail và số điện thoại hợp lệ.");
        return form.email().trim().toLowerCase(Locale.ROOT);
    }
    private void kiemTraEmailTrung(String email,Integer id) {
        if(!jdbc.queryForList("SELECT id FROM nhan_vien WITH (UPDLOCK,HOLDLOCK) WHERE LOWER(email)=?",email).isEmpty()
            || !jdbc.queryForList("SELECT id FROM khach_hang WITH (UPDLOCK,HOLDLOCK) WHERE (LOWER(email)=? OR LOWER(ten_tai_khoan)=?) AND (? IS NULL OR id<>?)",email,email,id,id).isEmpty())
            throw new IllegalArgumentException("Email đã được sử dụng. Vui lòng chọn email khác.");
    }
    @Transactional
    public int dangKy(TaiKhoanKhachForm form,String mk,String nhapLai) {
        String email=emailHopLe(form); kiemTraMatKhau(mk,nhapLai); kiemTraEmailTrung(email,null);
        // Tên tài khoản tối đa 50 ký tự trong database. Khách đăng nhập bằng email.
        String tenTaiKhoan=email.length()<=50?email:"kh_"+UUID.randomUUID().toString().replace("-","");
        return jdbc.queryForObject("""
            INSERT INTO khach_hang(ten_khach_hang,ten_tai_khoan,email,so_dien_thoai,mat_khau,trang_thai,xoa_mem)
            OUTPUT INSERTED.id VALUES (?,?,?,?,?,1,0)
            """,Integer.class,form.hoTen().trim(),tenTaiKhoan,email,form.sdt(),new BCryptPasswordEncoder().encode(mk));
    }
    public Map<String,Object> thongTin(int id) {
        var ds=jdbc.queryForList("SELECT id,ten_khach_hang AS hoTen,email,so_dien_thoai AS sdt FROM khach_hang WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Tài khoản không còn hoạt động.");
        return ds.get(0);
    }
    @Transactional
    public void capNhat(int id,TaiKhoanKhachForm form) {
        thongTin(id); String email=emailHopLe(form); kiemTraEmailTrung(email,id);
        jdbc.update("UPDATE khach_hang SET ten_khach_hang=?,email=?,so_dien_thoai=?,ngay_cap_nhat=SYSDATETIME() WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",form.hoTen().trim(),email,form.sdt(),id);
    }
    @Transactional
    public void doiMatKhau(int id,String cu,String moi,String nhapLai) {
        kiemTraMatKhau(moi,nhapLai);
        var ds=jdbc.queryForList("SELECT mat_khau FROM khach_hang WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty() || !dungMatKhau(cu,(String)ds.get(0).get("mat_khau"))) throw new IllegalArgumentException("Mật khẩu hiện tại chưa đúng.");
        if(cu.equals(moi)) throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        jdbc.update("UPDATE khach_hang SET mat_khau=?,ngay_cap_nhat=SYSDATETIME() WHERE id=?",new BCryptPasswordEncoder().encode(moi),id);
    }
}
