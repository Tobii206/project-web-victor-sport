package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaiKhoanService {
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Integer> soLanSai = new HashMap<>();

    public TaiKhoanService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized void moKhoaSauDoiMatKhau(String taiKhoan) {
        if(taiKhoan!=null) soLanSai.remove(taiKhoan.trim().toLowerCase(java.util.Locale.ROOT));
        if(taiKhoan!=null) for(var kh:jdbcTemplate.queryForList("SELECT id FROM khach_hang WHERE LOWER(ten_tai_khoan)=?",taiKhoan.trim().toLowerCase(java.util.Locale.ROOT)))
            new BaoMatTaiKhoanService(jdbcTemplate).moKhoa(((Number)kh.get("id")).intValue(),false);
    }

    public synchronized void moKhoaBangEmail(String email) {
        if(email==null) return;
        soLanSai.remove(email.trim().toLowerCase(java.util.Locale.ROOT));
        for(var kh:jdbcTemplate.queryForList("SELECT ten_tai_khoan FROM khach_hang WHERE LOWER(email)=?",email.trim().toLowerCase(java.util.Locale.ROOT)))
            moKhoaSauDoiMatKhau(kh.get("ten_tai_khoan").toString());
    }

    public synchronized Integer dangNhapKhachHang(String taiKhoan, String matKhau) {
        if (taiKhoan == null || taiKhoan.isBlank() || matKhau == null || matKhau.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tài khoản và mật khẩu.");
        }
        taiKhoan = taiKhoan.trim().toLowerCase(java.util.Locale.ROOT);
        KiemTraThongTin.kiemTraMatKhau(matKhau);
        if(taiKhoan.contains("@")&&!KiemTraThongTin.laEmailGmail(taiKhoan))
            throw new IllegalArgumentException("Email phải đúng định dạng, ví dụ: ten@gmail.com.");
        int soLan = soLanSai.getOrDefault(taiKhoan, 0);
        if (soLan >= 5) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa do nhập sai 5 lần. Vui lòng liên hệ cửa hàng.");
        }
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList("""
                SELECT id, ten_tai_khoan, mat_khau FROM khach_hang
                WHERE LOWER(ten_tai_khoan) = ? AND ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1
                ORDER BY id
                """, taiKhoan);
        if(danhSach.isEmpty()) {
            danhSach=jdbcTemplate.queryForList("""
                    SELECT id, ten_tai_khoan, mat_khau FROM khach_hang
                    WHERE LOWER(email)=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1
                    ORDER BY id
                    """,taiKhoan);
            if(danhSach.size()>1) throw new IllegalArgumentException("Email được dùng cho nhiều khách hàng. Vui lòng đăng nhập bằng tên tài khoản.");
        }
        if (!danhSach.isEmpty()) {
            Map<String, Object> khachHang = danhSach.get(0);
            var baoMat=new BaoMatTaiKhoanService(jdbcTemplate);
            int idKh=((Number)khachHang.get("id")).intValue();
            if(baoMat.biKhoa(idKh,false)) throw new IllegalArgumentException("Tài khoản đã bị khóa. Vui lòng liên hệ cửa hàng.");
            // Tên tài khoản và email cùng dùng một bộ đếm số lần nhập sai.
            taiKhoan=khachHang.getOrDefault("ten_tai_khoan",taiKhoan).toString().trim().toLowerCase(java.util.Locale.ROOT);
            soLan=soLanSai.getOrDefault(taiKhoan,0);
            if(soLan>=5) throw new IllegalArgumentException("Tài khoản đã bị khóa do nhập sai 5 lần. Vui lòng liên hệ cửa hàng.");
            String matKhauDaLuu = (String) khachHang.get("mat_khau");
            boolean dungMatKhau = false;
            if (matKhauDaLuu != null) {
                if (matKhauDaLuu.startsWith("$2a$") || matKhauDaLuu.startsWith("$2b$") || matKhauDaLuu.startsWith("$2y$")) {
                    dungMatKhau = new BCryptPasswordEncoder().matches(matKhau, matKhauDaLuu);
                } else {
                    // Dữ liệu mẫu cũ đang lưu mật khẩu dạng văn bản.
                    dungMatKhau = matKhau.equals(matKhauDaLuu);
                }
            }
            if (dungMatKhau) {
                baoMat.dangNhapDung(idKh,false);
                soLanSai.remove(taiKhoan);
                return ((Number) khachHang.get("id")).intValue();
            }
        }
        soLan++;
        if(!danhSach.isEmpty()) soLan=Math.max(soLan,new BaoMatTaiKhoanService(jdbcTemplate).ghiSai(((Number)danhSach.get(0).get("id")).intValue(),false));
        soLanSai.put(taiKhoan, soLan);
        if (soLan >= 5) {
            throw new IllegalArgumentException("Bạn đã nhập sai 5 lần. Tài khoản bị khóa, vui lòng liên hệ cửa hàng.");
        }
        throw new IllegalArgumentException("Sai tài khoản hoặc mật khẩu. Còn " + (5 - soLan) + " lần thử.");
    }

    public Map<String, Object> layThongTinNhanVien(Integer id) {
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList("""
                SELECT nv.ma_nhan_vien AS ma_tai_khoan, nv.ten_nhan_vien AS ho_ten, nv.email,
                       nv.so_dien_thoai, nv.ngay_sinh, nv.dia_chi_cu_the, nv.phuong, nv.quan, nv.thanh_pho,
                       qh.ten_quyen_han AS vai_tro
                FROM nhan_vien nv JOIN quyen_han qh ON qh.id = nv.id_quyen_han
                WHERE nv.id = ? AND ISNULL(nv.xoa_mem, 0) = 0 AND ISNULL(nv.trang_thai, 1) = 1
                """, id);
        return layDongDauTien(danhSach);
    }

    public Map<String, Object> layThongTinKhachHang(Integer id) {
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList("""
                SELECT TOP 1 kh.ma_khach_hang AS ma_tai_khoan, kh.ten_khach_hang AS ho_ten,
                       kh.ten_tai_khoan, kh.email, kh.so_dien_thoai, kh.ngay_sinh,
                       dc.dia_chi_cu_the, dc.phuong, dc.quan, dc.thanh_pho, N'Khách hàng' AS vai_tro
                FROM khach_hang kh
                LEFT JOIN dia_chi_khach_hang dc ON dc.id_khach_hang = kh.id AND ISNULL(dc.xoa_mem, 0) = 0
                WHERE kh.id = ? AND ISNULL(kh.xoa_mem, 0) = 0 AND ISNULL(kh.trang_thai, 1) = 1
                ORDER BY dc.mac_dinh DESC, dc.id
                """, id);
        return layDongDauTien(danhSach);
    }

    private Map<String, Object> layDongDauTien(List<Map<String, Object>> danhSach) {
        if (danhSach.isEmpty()) {
            throw new IllegalArgumentException("Tài khoản không còn hoạt động. Vui lòng đăng nhập lại.");
        }
        return danhSach.get(0);
    }
}
