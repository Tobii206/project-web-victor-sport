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

    // Chỉ xử lý một lần đăng nhập để đếm số lần sai chính xác.
    public synchronized KetQuaDangNhap dangNhap(String email, String matKhau) {
        String emailChuanHoa = "";
        if (email != null) {
            emailChuanHoa = email.trim().toLowerCase();
        }
        if (emailChuanHoa.isBlank() || matKhau == null || matKhau.isBlank()) {
            return KetQuaDangNhap.thatBai("Vui lòng nhập email và mật khẩu.");
        }

        int soLanSai = 0;
        if (soLanDangNhapSai.containsKey(emailChuanHoa)) {
            soLanSai = soLanDangNhapSai.get(emailChuanHoa);
        }
        if (soLanSai >= SO_LAN_SAI_TOI_DA) {
            return KetQuaDangNhap.thatBai("Tài khoản đã bị khóa do đăng nhập sai 5 lần. Vui lòng liên hệ quản lý.");
        }

        NguoiDungDangNhap nguoiDung = timNhanVienTheoEmailVaMatKhau(emailChuanHoa, matKhau);
        if (nguoiDung != null) {
            soLanDangNhapSai.remove(emailChuanHoa);
            return KetQuaDangNhap.thanhCong(nguoiDung);
        }

        soLanSai++;
        soLanDangNhapSai.put(emailChuanHoa, soLanSai);
        if (soLanSai >= SO_LAN_SAI_TOI_DA) {
            return KetQuaDangNhap.thatBai("Bạn đã nhập sai 5 lần. Tài khoản tạm khóa, vui lòng liên hệ quản lý.");
        }

        return KetQuaDangNhap.thatBai("Sai email hoặc mật khẩu. Còn " + (SO_LAN_SAI_TOI_DA - soLanSai) + " lần thử.");
    }

    private NguoiDungDangNhap timNhanVienTheoEmailVaMatKhau(String email, String matKhau) {
        String cauLenhSql = """
                SELECT TOP 1 nv.id, nv.email, nv.ten_nhan_vien, qh.ten_quyen_han
                FROM nhan_vien nv
                JOIN quyen_han qh ON qh.id = nv.id_quyen_han
                WHERE LOWER(nv.email) = ?
                  AND nv.mat_khau = ?
                  AND ISNULL(nv.trang_thai, 1) = 1
                  AND ISNULL(nv.xoa_mem, 0) = 0
                """;
        List<NguoiDungDangNhap> danhSach = jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new NguoiDungDangNhap(
                ketQua.getInt("id"),
                ketQua.getString("email"),
                ketQua.getString("ten_nhan_vien"),
                ketQua.getString("ten_quyen_han")
        ), email, matKhau);
        if (danhSach.isEmpty()) {
            return null;
        }
        return danhSach.get(0);
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
