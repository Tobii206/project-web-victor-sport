package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class QuanLyKhachHangService {
    private final JdbcTemplate jdbc;
    public QuanLyKhachHangService(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    public List<Map<String,Object>> tim(Map<String,String> loc) {
        String sql="""
                SELECT id,ma_khach_hang,ten_khach_hang,ten_tai_khoan,email,so_dien_thoai,gioi_tinh,trang_thai,
                    CAST(CASE WHEN EXISTS (SELECT 1 FROM bao_mat_tai_khoan b WHERE b.id_khach_hang=khach_hang.id AND b.bi_khoa=1) THEN 1 ELSE 0 END AS BIT) AS bi_khoa
                FROM khach_hang WHERE ISNULL(xoa_mem,0)=0
                """;
        List<Object> thamSo=new ArrayList<>();
        String tuKhoa=loc.getOrDefault("tuKhoa","").trim();
        if(!tuKhoa.isEmpty()) {
            sql+=" AND (ten_khach_hang LIKE ? OR ma_khach_hang LIKE ? OR ten_tai_khoan LIKE ? OR email LIKE ? OR so_dien_thoai LIKE ?)";
            for(int i=0;i<5;i++) thamSo.add("%"+tuKhoa+"%");
        }
        String trangThai=loc.get("trangThai");
        if("0".equals(trangThai)||"1".equals(trangThai)) {sql+=" AND trang_thai=?";thamSo.add(Integer.parseInt(trangThai));}
        sql+="ten".equals(loc.get("sapXep"))?" ORDER BY ten_khach_hang,id":" ORDER BY id DESC";
        return jdbc.queryForList(sql,thamSo.toArray());
    }
    public Map<String,Object> lay(int id) {
        var ds=jdbc.queryForList("""
                SELECT kh.id,kh.ma_khach_hang,kh.ten_khach_hang,kh.ten_tai_khoan,kh.email,kh.so_dien_thoai,
                       kh.gioi_tinh,kh.ngay_sinh,kh.trang_thai,dc.ten_dia_chi,dc.thanh_pho,dc.quan,dc.phuong,dc.dia_chi_cu_the
                FROM khach_hang kh OUTER APPLY (
                    SELECT TOP 1 ten_dia_chi,thanh_pho,quan,phuong,dia_chi_cu_the
                    FROM dia_chi_khach_hang WHERE id_khach_hang=kh.id AND ISNULL(xoa_mem,0)=0
                    ORDER BY mac_dinh DESC,id
                ) dc WHERE kh.id=? AND ISNULL(kh.xoa_mem,0)=0
                """,id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy khách hàng.");
        return ds.get(0);
    }
    @Transactional
    public int luu(Integer id,Map<String,String> data,int nguoiSua) {
        String ten=chuoi(data,"ten_khach_hang",100,true);
        String taiKhoan=chuoi(data,"ten_tai_khoan",50,true).toLowerCase(Locale.ROOT);
        if(!taiKhoan.matches("[a-z0-9._@-]+")) throw new IllegalArgumentException("Tên tài khoản chỉ dùng chữ không dấu, số và các ký tự . _ @ -");
        if(taiKhoan.contains("@")&&!KiemTraThongTin.laEmailGmail(taiKhoan)) throw new IllegalArgumentException("Nếu dùng email làm tên tài khoản, email phải có dạng ten@gmail.com.");
        String email=chuoi(data,"email",100,false).toLowerCase(Locale.ROOT);
        if(!email.isEmpty()&&!KiemTraThongTin.laEmailGmail(email)) throw new IllegalArgumentException("Email phải đúng định dạng, ví dụ: ten@gmail.com.");
        String sdt=chuoi(data,"so_dien_thoai",10,false);
        if(!sdt.isEmpty()&&!sdt.matches("0[0-9]{9}")) throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        String matKhau=data.getOrDefault("mat_khau","");
        if((id==null||!matKhau.isEmpty())&&(matKhau.isBlank()||matKhau.length()<6||matKhau.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72))
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự và không quá 72 byte.");
        LocalDate ngaySinh=null;
        String ngay=data.getOrDefault("ngay_sinh","").trim();
        if(!ngay.isEmpty()) {
            try {ngaySinh=LocalDate.parse(ngay);} catch(java.time.format.DateTimeParseException e) {throw new IllegalArgumentException("Ngày sinh không hợp lệ.");}
            if(ngaySinh.isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        }
        String gioi=data.getOrDefault("gioi_tinh","");
        if(!gioi.isEmpty()&&!"0".equals(gioi)&&!"1".equals(gioi)) throw new IllegalArgumentException("Chọn giới tính hợp lệ.");
        Boolean gioiTinh=gioi.isEmpty()?null:"1".equals(gioi);
        String trangThai=data.get("trang_thai");
        if(!"0".equals(trangThai)&&!"1".equals(trangThai)) throw new IllegalArgumentException("Chọn trạng thái khách hàng.");
        String tenDiaChi=chuoi(data,"ten_dia_chi",100,false);
        String thanhPho=chuoi(data,"thanh_pho",100,false),quan=chuoi(data,"quan",100,false),phuong=chuoi(data,"phuong",100,false);
        String diaChi=chuoi(data,"dia_chi_cu_the",255,false);
        // Khóa khi kiểm tra trùng tài khoản để tránh hai lần thêm đồng thời.
        jdbc.queryForList("SELECT id FROM khach_hang WITH (UPDLOCK,HOLDLOCK)");
        if(id!=null) lay(id);
        Integer trung=jdbc.queryForObject("SELECT COUNT(*) FROM khach_hang WHERE LOWER(LTRIM(RTRIM(ten_tai_khoan)))=? AND (? IS NULL OR id<>?)",Integer.class,taiKhoan,id,id);
        if(trung!=null&&trung>0) throw new IllegalArgumentException("Tên tài khoản đã được sử dụng.");
        Integer trungNhanVien=jdbc.queryForObject("SELECT COUNT(*) FROM nhan_vien WHERE LOWER(LTRIM(RTRIM(email)))=?",Integer.class,taiKhoan);
        if(trungNhanVien!=null&&trungNhanVien>0) throw new IllegalArgumentException("Tên tài khoản trùng email đăng nhập nhân viên. Vui lòng chọn tên khác.");
        List<Map<String,Object>> diaChiCu=id==null?List.of():jdbc.queryForList("SELECT TOP 1 id FROM dia_chi_khach_hang WHERE id_khach_hang=? AND ISNULL(xoa_mem,0)=0 ORDER BY mac_dinh DESC,id",id);
        boolean coDiaChi=!diaChi.isEmpty()||!thanhPho.isEmpty()||!quan.isEmpty()||!phuong.isEmpty()||!tenDiaChi.isEmpty()||!diaChiCu.isEmpty();
        if(coDiaChi&&diaChi.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập địa chỉ cụ thể.");
        boolean hoatDong="1".equals(trangThai);
        String matKhauLuu=matKhau.isEmpty()?null:new BCryptPasswordEncoder().encode(matKhau);
        if(id==null) {
            id=jdbc.queryForObject("""
                    INSERT INTO khach_hang(ten_khach_hang,ten_tai_khoan,mat_khau,email,so_dien_thoai,gioi_tinh,ngay_sinh,trang_thai,nguoi_tao)
                    OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?,?,?)
                    """,Integer.class,ten,taiKhoan,matKhauLuu,email,sdt,gioiTinh,ngaySinh,hoatDong,nguoiSua);
        } else {
            jdbc.update("""
                    UPDATE khach_hang SET ten_khach_hang=?,ten_tai_khoan=?,email=?,so_dien_thoai=?,gioi_tinh=?,ngay_sinh=?,trang_thai=?,
                        ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND ISNULL(xoa_mem,0)=0
                    """,ten,taiKhoan,email,sdt,gioiTinh,ngaySinh,hoatDong,nguoiSua,id);
            if(matKhauLuu!=null) jdbc.update("UPDATE khach_hang SET mat_khau=? WHERE id=?",matKhauLuu,id);
        }
        if(coDiaChi) {
            if(tenDiaChi.isEmpty()) tenDiaChi="Địa chỉ mặc định";
            if(diaChiCu.isEmpty()) jdbc.update("INSERT INTO dia_chi_khach_hang(id_khach_hang,ten_dia_chi,thanh_pho,quan,phuong,dia_chi_cu_the,mac_dinh) VALUES (?,?,?,?,?,?,1)",id,tenDiaChi,thanhPho,quan,phuong,diaChi);
            else jdbc.update("UPDATE dia_chi_khach_hang SET ten_dia_chi=?,thanh_pho=?,quan=?,phuong=?,dia_chi_cu_the=? WHERE id=? AND id_khach_hang=?",tenDiaChi,thanhPho,quan,phuong,diaChi,diaChiCu.get(0).get("id"),id);
        }
        return id;
    }
    @Transactional
    public void ngungHoatDong(int id,int nguoiSua) {
        lay(id);
        jdbc.update("UPDATE khach_hang SET trang_thai=0,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND ISNULL(xoa_mem,0)=0",nguoiSua,id);
    }
    private String chuoi(Map<String,String> data,String ten,int toiDa,boolean batBuoc) {
        String value=data.getOrDefault(ten,"").trim();
        if(batBuoc&&value.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập họ tên và tên tài khoản khách hàng.");
        if(value.length()>toiDa) throw new IllegalArgumentException("Thông tin nhập quá dài. Vui lòng kiểm tra lại.");
        return value;
    }
}
