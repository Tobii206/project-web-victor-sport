package com.example.victorsport.Service;

import com.example.victorsport.Dto.AnhSanPham;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TrangChuService {
    private final JdbcTemplate jdbcTemplate;

    public TrangChuService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> layThuongHieu() {
        return jdbcTemplate.queryForList("""
                SELECT id, ten_thuong_hieu FROM thuong_hieu
                WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1
                ORDER BY ten_thuong_hieu
                """);
    }

    public List<Map<String, Object>> timSanPham(String tuKhoa, Integer idThuongHieu, String sapXep) {
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        String timKiem = "%" + tuKhoa.trim() + "%";
        String sql = """
                SELECT sp.id, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta_ngan, th.ten_thuong_hieu,
                       MIN(ISNULL(ct.gia_ban, ct.gia_niem_yet)) AS gia_ban,
                       SUM(ISNULL(ct.so_luong, 0)) AS so_luong,
                       (SELECT TOP 1 a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                        JOIN chi_tiet_san_pham ca ON ca.id = a.id_chi_tiet_san_pham
                        WHERE ca.id_san_pham = sp.id AND ISNULL(a.xoa_mem, 0) = 0
                          AND ISNULL(ca.xoa_mem, 0) = 0 AND ISNULL(ca.trang_thai, 1) = 1
                        ORDER BY a.la_anh_dai_dien DESC, a.id) AS anh
                FROM san_pham sp
                JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                JOIN chi_tiet_san_pham ct ON ct.id_san_pham = sp.id
                WHERE ISNULL(sp.xoa_mem, 0) = 0 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1
                  AND ISNULL(th.xoa_mem, 0) = 0 AND ISNULL(th.trang_thai, 1) = 1
                  AND (sp.ten_san_pham LIKE ? OR sp.ma_san_pham LIKE ? OR th.ten_thuong_hieu LIKE ?)
                  AND (? IS NULL OR sp.id_thuong_hieu = ?)
                GROUP BY sp.id, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta_ngan, th.ten_thuong_hieu
                """;
        if ("gia-tang".equals(sapXep)) {
            sql += " ORDER BY gia_ban ASC, sp.id DESC";
        } else if ("gia-giam".equals(sapXep)) {
            sql += " ORDER BY gia_ban DESC, sp.id DESC";
        } else {
            sql += " ORDER BY sp.id DESC";
        }
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList(sql, timKiem, timKiem, timKiem, idThuongHieu, idThuongHieu);
        for (Map<String, Object> sanPham : danhSach) {
            String anh = AnhSanPham.layAnhDaiDien((String) sanPham.get("ten_san_pham"));
            if (anh != null) { sanPham.put("anh", anh); }
        }
        return danhSach;
    }
}
