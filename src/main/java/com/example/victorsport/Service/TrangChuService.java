package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrangChuService {
    private final JdbcTemplate jdbcTemplate;
    public TrangChuService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Long layHanUuDai() {
        var han = jdbcTemplate.queryForObject("""
            SELECT DATEADD(day,1,CAST(MIN(ngay_ket_thuc) AS datetime2)) FROM dot_giam_gia
            WHERE trang_thai=1 AND ISNULL(xoa_mem,0)=0
              AND CAST(GETDATE() AS date) BETWEEN ngay_bat_dau AND ngay_ket_thuc
            """, java.sql.Timestamp.class);
        return han == null ? null : han.toLocalDateTime().atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
    }

    public List<Map<String, Object>> layThuongHieu() {
        return jdbcTemplate.queryForList("SELECT id, ten_thuong_hieu FROM thuong_hieu WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1 ORDER BY ten_thuong_hieu");
    }

    public Map<String, List<Map<String, Object>>> layBoLoc() {
        Map<String, List<Map<String, Object>>> ds = new LinkedHashMap<>();
        ds.put("nhuCau", jdbcTemplate.queryForList("SELECT id, ten_vi_tri AS ten FROM vi_tri_thi_dau WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1 ORDER BY id"));
        ds.put("chatLieu", jdbcTemplate.queryForList("SELECT id, ten_chat_lieu AS ten FROM chat_lieu WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1 ORDER BY ten_chat_lieu"));
        ds.put("xuatXu", jdbcTemplate.queryForList("SELECT id, ten_xuat_xu AS ten FROM xuat_xu WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1 ORDER BY ten_xuat_xu"));
        ds.put("kichThuoc", jdbcTemplate.queryForList("SELECT id, ten_kich_thuoc AS ten FROM kich_thuoc WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1 ORDER BY gia_tri_kich_thuoc, id"));
        ds.put("mauSac", jdbcTemplate.queryForList("SELECT id, ten_mau_sac AS ten FROM mau_sac WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1 ORDER BY ten_mau_sac"));
        return ds;
    }

    public List<Map<String, Object>> timSanPham(String tuKhoa, Integer thuongHieu, Integer nhuCau,
            Integer chatLieu, Integer xuatXu, Integer kichThuoc, Integer mauSac, boolean conHang, String sapXep) {
        if (tuKhoa == null) tuKhoa = "";
        String timKiem = "%" + tuKhoa.trim() + "%";
        List<Object> thamSo = new ArrayList<>();
        thamSo.add(timKiem); thamSo.add(timKiem); thamSo.add(timKiem);
        String sql = """
                SELECT sp.id, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta_ngan, th.ten_thuong_hieu,
                       cl.ten_chat_lieu, xx.ten_xuat_xu, vt.ten_vi_tri,
                       MIN(ct.gia_ban) AS gia_ban,
                       SUM(ISNULL(ct.so_luong, 0)) AS so_luong, COUNT(DISTINCT ct.id_kich_thuoc) AS so_size,
                       (SELECT TOP 1 a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                        JOIN chi_tiet_san_pham ca ON ca.id = a.id_chi_tiet_san_pham
                        WHERE ca.id_san_pham = sp.id AND ISNULL(a.xoa_mem, 0) = 0
                          AND ISNULL(ca.xoa_mem, 0) = 0 AND ISNULL(ca.trang_thai, 1) = 1
                        ORDER BY a.la_anh_dai_dien DESC, a.id) AS anh
                FROM san_pham sp JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                JOIN chi_tiet_san_pham ct ON ct.id_san_pham = sp.id
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN form_chan fc ON fc.id = ct.id_form_chan
                LEFT JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu AND ISNULL(cl.xoa_mem, 0) = 0
                LEFT JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu AND ISNULL(xx.xoa_mem, 0) = 0
                LEFT JOIN vi_tri_thi_dau vt ON vt.id = sp.id_vi_tri_thi_dau AND ISNULL(vt.xoa_mem, 0) = 0
                WHERE ISNULL(sp.xoa_mem, 0) = 0 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1
                  AND ISNULL(th.xoa_mem, 0) = 0 AND ISNULL(th.trang_thai, 1) = 1
                  AND ISNULL(kt.xoa_mem, 0) = 0 AND ISNULL(kt.trang_thai, 1) = 1
                  AND ISNULL(ms.xoa_mem, 0) = 0 AND ISNULL(ms.trang_thai, 1) = 1
                  AND ISNULL(fc.xoa_mem, 0) = 0 AND ISNULL(fc.trang_thai, 1) = 1
                  AND (sp.ten_san_pham LIKE ? OR sp.ma_san_pham LIKE ? OR th.ten_thuong_hieu LIKE ?)
                """;
        if (thuongHieu != null) { sql += " AND sp.id_thuong_hieu = ?"; thamSo.add(thuongHieu); }
        if (nhuCau != null) { sql += " AND vt.id = ? AND ISNULL(vt.trang_thai, 1) = 1"; thamSo.add(nhuCau); }
        if (chatLieu != null) { sql += " AND cl.id = ? AND ISNULL(cl.trang_thai, 1) = 1"; thamSo.add(chatLieu); }
        if (xuatXu != null) { sql += " AND xx.id = ? AND ISNULL(xx.trang_thai, 1) = 1"; thamSo.add(xuatXu); }
        if (kichThuoc != null) { sql += " AND ct.id_kich_thuoc = ?"; thamSo.add(kichThuoc); }
        if (mauSac != null) { sql += " AND ct.id_mau_sac = ?"; thamSo.add(mauSac); }
        if (conHang) sql += " AND ct.so_luong > 0";
        sql += " GROUP BY sp.id, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta_ngan, th.ten_thuong_hieu, cl.ten_chat_lieu, xx.ten_xuat_xu, vt.ten_vi_tri";
        if ("gia-tang".equals(sapXep)) sql += " ORDER BY gia_ban ASC, sp.id DESC";
        else if ("gia-giam".equals(sapXep)) sql += " ORDER BY gia_ban DESC, sp.id DESC";
        else sql += " ORDER BY sp.id DESC";
        var ds = jdbcTemplate.queryForList(sql, thamSo.toArray());
        GiaGiamSanPham.themGiaDanhSach(jdbcTemplate, ds, kichThuoc, mauSac, conHang);
        if ("gia-tang".equals(sapXep)) ds.sort((a,b) -> ((java.math.BigDecimal)a.get("gia_sau_giam")).compareTo((java.math.BigDecimal)b.get("gia_sau_giam")));
        if ("gia-giam".equals(sapXep)) ds.sort((a,b) -> ((java.math.BigDecimal)b.get("gia_sau_giam")).compareTo((java.math.BigDecimal)a.get("gia_sau_giam")));
        return ds;
    }
}
