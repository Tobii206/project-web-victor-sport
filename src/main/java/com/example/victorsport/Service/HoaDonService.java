package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HoaDonService {
    private final JdbcTemplate jdbcTemplate;

    public HoaDonService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> layDanhSachHoaDon(String tuKhoa) {
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        String tuKhoaTimKiem = "%" + tuKhoa.trim() + "%";
        String sql = """
                SELECT id, ma_hoa_don, ten_khach_hang, so_dien_thoai_khach_hang,
                       ngay_tao, tong_tien_sau_giam, trang_thai_hien_tai,
                       CASE WHEN trang_thai_hien_tai = 2 THEN N'Đã xác nhận đơn'
                            WHEN trang_thai_hien_tai = 3 THEN N'Đang chuẩn bị hàng'
                            WHEN trang_thai_hien_tai = 4 THEN N'Đang giao hàng'
                            WHEN trang_thai_hien_tai = 5 THEN N'Hoàn tất'
                            ELSE N'Trạng thái ' + CAST(trang_thai_hien_tai AS NVARCHAR(10)) END AS ten_trang_thai
                FROM hoa_don
                WHERE ISNULL(xoa_mem, 0) = 0
                  AND (ma_hoa_don LIKE ? OR ten_khach_hang LIKE ? OR so_dien_thoai_khach_hang LIKE ?)
                ORDER BY id DESC
                """;
        return jdbcTemplate.queryForList(sql, tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem);
    }

    public Map<String, Object> layHoaDon(Integer id) {
        String sql = """
                SELECT hd.*, nv.ten_nhan_vien,
                       CASE WHEN hd.trang_thai_hien_tai = 2 THEN N'Đã xác nhận đơn'
                            WHEN hd.trang_thai_hien_tai = 3 THEN N'Đang chuẩn bị hàng'
                            WHEN hd.trang_thai_hien_tai = 4 THEN N'Đang giao hàng'
                            WHEN hd.trang_thai_hien_tai = 5 THEN N'Hoàn tất'
                            ELSE N'Trạng thái ' + CAST(hd.trang_thai_hien_tai AS NVARCHAR(10)) END AS ten_trang_thai
                FROM hoa_don hd
                LEFT JOIN nhan_vien nv ON nv.id = hd.id_nhan_vien
                WHERE hd.id = ? AND ISNULL(hd.xoa_mem, 0) = 0
                """;
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList(sql, id);
        if (danhSach.isEmpty()) {
            return null;
        }
        return danhSach.get(0);
    }

    public List<Map<String, Object>> laySanPhamTrongHoaDon(Integer idHoaDon) {
        String sql = """
                SELECT sp.ma_san_pham, sp.ten_san_pham, ms.ten_mau_sac, kt.ten_kich_thuoc,
                       hdct.so_luong, hdct.don_gia, hdct.thanh_tien
                FROM hoa_don_chi_tiet hdct
                LEFT JOIN chi_tiet_san_pham ct ON ct.id = hdct.id_chi_tiet_san_pham
                LEFT JOIN san_pham sp ON sp.id = ct.id_san_pham
                LEFT JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                LEFT JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE hdct.id_hoa_don = ? AND ISNULL(hdct.xoa_mem, 0) = 0
                ORDER BY hdct.id
                """;
        return jdbcTemplate.queryForList(sql, idHoaDon);
    }
}
