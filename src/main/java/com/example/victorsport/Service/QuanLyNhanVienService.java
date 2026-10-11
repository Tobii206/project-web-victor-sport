package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class QuanLyNhanVienService {
    private final JdbcTemplate jdbc;
    public QuanLyNhanVienService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> cacQuyen() {
        return jdbc.queryForList("SELECT id, ten_quyen_han FROM quyen_han WHERE ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1 ORDER BY id");
    }

    public List<Map<String, Object>> tim(Map<String, String> loc) {
        String sql = """
                SELECT nv.id, nv.ma_nhan_vien, nv.ten_nhan_vien, nv.email, nv.so_dien_thoai,
                       nv.trang_thai, nv.anh_nhan_vien, q.ten_quyen_han
                FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
                WHERE ISNULL(nv.xoa_mem,0)=0
                """;
        List<Object> thamSo = new ArrayList<>();
        String tuKhoa = loc.getOrDefault("tuKhoa", "").trim();
        if (!tuKhoa.isEmpty()) {
            sql += " AND (nv.ten_nhan_vien LIKE ? OR nv.ma_nhan_vien LIKE ? OR nv.email LIKE ? OR nv.so_dien_thoai LIKE ?)";
            for (int i=0; i<4; i++) thamSo.add("%" + tuKhoa + "%");
        }
        String trangThai = loc.get("trangThai");
        if ("0".equals(trangThai) || "1".equals(trangThai)) { sql += " AND nv.trang_thai=?"; thamSo.add(Integer.parseInt(trangThai)); }
        String quyen = loc.getOrDefault("quyen", "");
        if (!quyen.isBlank()) { sql += " AND nv.id_quyen_han=?"; thamSo.add(soNguyen(quyen)); }
        sql += "ten".equals(loc.get("sapXep")) ? " ORDER BY nv.ten_nhan_vien, nv.id" : " ORDER BY nv.id DESC";
        return jdbc.queryForList(sql, thamSo.toArray());
    }

    public Map<String, Object> lay(int id) {
        // Không đưa mật khẩu vào trang thông tin hoặc dữ liệu form.
        var ds = jdbc.queryForList("""
                SELECT id, ma_nhan_vien, ten_nhan_vien, email, so_dien_thoai, id_quyen_han,
                       ngay_sinh, thanh_pho, quan, phuong, dia_chi_cu_the, ghi_chu, trang_thai
                FROM nhan_vien WHERE id=? AND ISNULL(xoa_mem,0)=0
                """, id);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy nhân viên.");
        return ds.get(0);
    }

    @Transactional
    public int luu(Integer id, Map<String, String> data, int nguoiSua) {
        String ten = chuoi(data,"ten_nhan_vien",100,true);
        String email = chuoi(data,"email",100,true).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Email không hợp lệ.");
        String sdt = chuoi(data,"so_dien_thoai",10,false);
        if (!sdt.isEmpty() && !sdt.matches("0[0-9]{9}")) throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        String matKhau = data.getOrDefault("mat_khau", "");
        if ((id == null || !matKhau.isEmpty()) && (matKhau.isBlank() || matKhau.length()<6 || matKhau.length()>255))
            throw new IllegalArgumentException("Mật khẩu phải có từ 6 đến 255 ký tự.");
        LocalDate ngaySinh = null;
        String ngay = data.getOrDefault("ngay_sinh", "").trim();
        if (!ngay.isEmpty()) {
            try { ngaySinh = LocalDate.parse(ngay); }
            catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("Ngày sinh không hợp lệ."); }
            if (ngaySinh.isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        }
        String thanhPho = chuoi(data,"thanh_pho",100,false);
        String quan = chuoi(data,"quan",100,false);
        String phuong = chuoi(data,"phuong",100,false);
        String diaChi = chuoi(data,"dia_chi_cu_the",255,false);
        String ghiChu = chuoi(data,"ghi_chu",255,false);
        int idQuyen = soNguyen(data.get("id_quyen_han"));
        String trangThai = data.get("trang_thai");
        if (!"0".equals(trangThai) && !"1".equals(trangThai)) throw new IllegalArgumentException("Chọn trạng thái nhân viên.");
        boolean dangLam = "1".equals(trangThai);
        var quyen = khoaQuyen();
        String tenQuyen = null;
        for (var q : quyen) if (((Number)q.get("id")).intValue()==idQuyen) tenQuyen=q.get("ten_quyen_han").toString();
        if (tenQuyen==null) throw new IllegalArgumentException("Quyền đã ngừng hoạt động hoặc không tồn tại.");
        if (id!=null) {
            var cu = lay(id);
            boolean doiQuyen = ((Number)cu.get("id_quyen_han")).intValue()!=idQuyen;
            if (id==nguoiSua && (!dangLam || doiQuyen)) throw new IllegalArgumentException("Không thể đổi quyền hoặc cho nghỉ việc tài khoản đang đăng nhập.");
            if (!dangLam || !tenQuyen.trim().equalsIgnoreCase("Quản trị viên")) kiemTraQuanTriCuoi(id);
        }
        Integer trung = jdbc.queryForObject("SELECT COUNT(*) FROM nhan_vien WHERE LOWER(LTRIM(RTRIM(email)))=? AND (? IS NULL OR id<>?)",Integer.class,email,id,id);
        if (trung!=null && trung>0) throw new IllegalArgumentException("Email đã được sử dụng bởi nhân viên khác.");
        if (id==null) {
            return jdbc.queryForObject("""
                    INSERT INTO nhan_vien (ten_nhan_vien,email,so_dien_thoai,id_quyen_han,mat_khau,ngay_sinh,
                        thanh_pho,quan,phuong,dia_chi_cu_the,ghi_chu,trang_thai,nguoi_tao)
                    OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """,Integer.class,ten,email,sdt,idQuyen,matKhau,ngaySinh,thanhPho,quan,phuong,diaChi,ghiChu,dangLam,nguoiSua);
        }
        jdbc.update("""
                UPDATE nhan_vien SET ten_nhan_vien=?,email=?,so_dien_thoai=?,id_quyen_han=?,
                    ngay_sinh=?,thanh_pho=?,quan=?,phuong=?,dia_chi_cu_the=?,ghi_chu=?,trang_thai=?,
                    ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND ISNULL(xoa_mem,0)=0
                """,ten,email,sdt,idQuyen,ngaySinh,thanhPho,quan,phuong,diaChi,ghiChu,dangLam,nguoiSua,id);
        if (!matKhau.isEmpty()) jdbc.update("UPDATE nhan_vien SET mat_khau=? WHERE id=?",matKhau,id);
        return id;
    }

    @Transactional
    public void nghiViec(int id, int nguoiSua) {
        if (id==nguoiSua) throw new IllegalArgumentException("Không thể cho nghỉ việc tài khoản đang đăng nhập.");
        khoaQuyen();
        lay(id);
        kiemTraQuanTriCuoi(id);
        jdbc.update("UPDATE nhan_vien SET trang_thai=0, ngay_cap_nhat=SYSDATETIME(), nguoi_cap_nhat=? WHERE id=? AND ISNULL(xoa_mem,0)=0",nguoiSua,id);
    }

    private List<Map<String,Object>> khoaQuyen() {
        return jdbc.queryForList("SELECT id,ten_quyen_han FROM quyen_han WITH (UPDLOCK,HOLDLOCK) WHERE ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1 ORDER BY id");
    }
    private void kiemTraQuanTriCuoi(int id) {
        var ds = jdbc.queryForList("""
                SELECT nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
                WHERE ISNULL(nv.xoa_mem,0)=0 AND nv.trang_thai=1 AND ISNULL(q.xoa_mem,0)=0
                    AND ISNULL(q.trang_thai,1)=1 AND LTRIM(RTRIM(q.ten_quyen_han))=N'Quản trị viên'
                """);
        if (ds.size()==1 && ((Number)ds.get(0).get("id")).intValue()==id)
            throw new IllegalArgumentException("Phải giữ lại ít nhất một quản trị viên đang làm việc.");
    }
    private int soNguyen(String value) {
        try { int so=Integer.parseInt(value); if (so>0) return so; }
        catch (NumberFormatException e) { }
        throw new IllegalArgumentException("Chọn quyền hợp lệ.");
    }
    private String chuoi(Map<String,String> data,String ten,int toiDa,boolean batBuoc) {
        String value=data.getOrDefault(ten,"").trim();
        if (batBuoc && value.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập họ tên và email nhân viên.");
        if (value.length()>toiDa) throw new IllegalArgumentException("Thông tin nhập quá dài. Vui lòng kiểm tra lại.");
        return value;
    }
}
