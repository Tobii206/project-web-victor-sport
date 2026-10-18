package com.example.victorsport.Service;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DangNhapService {
    private static final int SO_LAN_SAI_TOI_DA = 5;

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Integer> soLanDangNhapSai = new HashMap<>();

    public DangNhapService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean laTaiKhoanNhanVien(String taiKhoan) {
        if (taiKhoan == null || taiKhoan.isBlank()) { return false; }
        Integer soTaiKhoan = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nhan_vien WHERE LOWER(email) = ?",
                Integer.class, taiKhoan.trim().toLowerCase(java.util.Locale.ROOT));
        return soTaiKhoan != null && soTaiKhoan > 0;
    }

    // Chỉ xử lý một lần đăng nhập để đếm số lần sai chính xác.
    public synchronized KetQuaDangNhap dangNhap(String email, String matKhau) {
        String emailChuanHoa = "";
        if (email != null) {
            emailChuanHoa = email.trim().toLowerCase();
        }
        if (emailChuanHoa.isBlank() || matKhau == null || matKhau.isBlank()) {
            return KetQuaDangNhap.thatBai("Vui lòng nhập email và mật khẩu.");
        }
        if(matKhau.length()<6) return KetQuaDangNhap.thatBai("Mật khẩu phải có ít nhất 6 ký tự.");
        if(!KiemTraThongTin.laEmailGmail(emailChuanHoa)) return KetQuaDangNhap.thatBai("Email phải đúng định dạng, ví dụ: ten@gmail.com.");

        int soLanSai = 0;
        if (soLanDangNhapSai.containsKey(emailChuanHoa)) {
            soLanSai = soLanDangNhapSai.get(emailChuanHoa);
        }
        var taiKhoan=jdbcTemplate.queryForList("SELECT id FROM nhan_vien WHERE LOWER(email)=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",emailChuanHoa);
        Integer idNv=taiKhoan.isEmpty()?null:((Number)taiKhoan.get(0).get("id")).intValue();
        if(idNv==null && soLanSai>=5) return KetQuaDangNhap.thatBai("Tài khoản đã bị khóa do đăng nhập sai 5 lần. Vui lòng liên hệ quản lý.");
        var baoMat=new BaoMatTaiKhoanService(jdbcTemplate);
        if(idNv!=null && baoMat.biKhoa(idNv,true)) return KetQuaDangNhap.thatBai("Tài khoản đã bị khóa do đăng nhập sai 5 lần. Vui lòng liên hệ quản lý.");
        NguoiDungDangNhap nguoiDung = timNhanVienTheoEmailVaMatKhau(emailChuanHoa, matKhau);
        if (nguoiDung != null) {
            nguoiDung = new PhanQuyenService(jdbcTemplate).layQuyen(nguoiDung.getId());
            if (nguoiDung == null) return KetQuaDangNhap.thatBai("Tài khoản hoặc quyền không còn hoạt động.");
            if(idNv!=null) baoMat.dangNhapDung(idNv,true);
            soLanDangNhapSai.remove(emailChuanHoa);
            return KetQuaDangNhap.thanhCong(nguoiDung);
        }

        soLanSai++;
        if(idNv!=null) soLanSai=baoMat.ghiSai(idNv,true);
        soLanDangNhapSai.put(emailChuanHoa, soLanSai);
        if (soLanSai >= SO_LAN_SAI_TOI_DA) {
            return KetQuaDangNhap.thatBai("Bạn đã nhập sai 5 lần. Tài khoản tạm khóa, vui lòng liên hệ quản lý.");
        }

        return KetQuaDangNhap.thatBai("Sai email hoặc mật khẩu. Còn " + (SO_LAN_SAI_TOI_DA - soLanSai) + " lần thử.");
    }

    private NguoiDungDangNhap timNhanVienTheoEmailVaMatKhau(String email, String matKhau) {
        String cauLenhSql = """
                SELECT TOP 1 nv.id, nv.email, nv.ten_nhan_vien, nv.mat_khau, qh.ten_quyen_han
                FROM nhan_vien nv
                JOIN quyen_han qh ON qh.id = nv.id_quyen_han
                WHERE LOWER(nv.email) = ?
                  AND ISNULL(nv.trang_thai, 1) = 1
                  AND ISNULL(nv.xoa_mem, 0) = 0
                  AND ISNULL(qh.trang_thai, 1) = 1 AND ISNULL(qh.xoa_mem, 0) = 0
                """;
        var danhSach = jdbcTemplate.queryForList(cauLenhSql,email);
        if (danhSach.isEmpty()) {
            return null;
        }
        var nv=danhSach.get(0);
        if(!TuPhucVuTaiKhoanService.dungMatKhau(matKhau,(String)nv.get("mat_khau"))) return null;
        return new NguoiDungDangNhap(((Number)nv.get("id")).intValue(),nv.get("email").toString(),nv.get("ten_nhan_vien").toString(),nv.get("ten_quyen_han").toString());
    }

    public static class KetQuaDangNhap {
        private final boolean thanhCong;
        private final String thongBao;
        private final NguoiDungDangNhap nguoiDung;

        private KetQuaDangNhap(boolean thanhCong, String thongBao, NguoiDungDangNhap nguoiDung) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.nguoiDung = nguoiDung;
        }

        public static KetQuaDangNhap thanhCong(NguoiDungDangNhap nguoiDung) {
            return new KetQuaDangNhap(true, null, nguoiDung);
        }

        public static KetQuaDangNhap thatBai(String thongBao) {
            return new KetQuaDangNhap(false, thongBao, null);
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongBao() {
            return thongBao;
        }

        public NguoiDungDangNhap getNguoiDung() {
            return nguoiDung;
        }
    }
}
