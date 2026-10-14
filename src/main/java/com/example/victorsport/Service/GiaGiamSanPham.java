package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class GiaGiamSanPham {
    public static BigDecimal giaSauGiam(JdbcTemplate jdbc, int idMau, BigDecimal giaGoc) {
        var cacDot = jdbc.queryForList("""
                SELECT d.loai_giam_gia,d.muc_uu_tien,
                       d.gia_tri_giam_gia AS gia_tri,
                       d.so_tien_giam_toi_da
                FROM chi_tiet_dot_giam_gia c JOIN dot_giam_gia d ON d.id=c.id_dot_giam_gia
                WHERE c.id_chi_tiet_san_pham=? AND ISNULL(c.xoa_mem,0)=0 AND ISNULL(d.xoa_mem,0)=0
                  AND c.trang_thai=1 AND d.trang_thai=1
                  AND CAST(GETDATE() AS DATE) BETWEEN d.ngay_bat_dau AND d.ngay_ket_thuc
                ORDER BY d.muc_uu_tien DESC,d.id
                """, idMau);
        BigDecimal tienGiam = BigDecimal.ZERO;
        Integer uuTien = null;
        for (var dot : cacDot) {
            int muc = dot.get("muc_uu_tien") == null ? 0 : ((Number) dot.get("muc_uu_tien")).intValue();
            if (uuTien == null) uuTien = muc;
            if (muc != uuTien) break;
            BigDecimal giaTri = (BigDecimal) dot.get("gia_tri");
            BigDecimal giam = giaTri;
            if (!Boolean.TRUE.equals(dot.get("loai_giam_gia")))
                giam = giaGoc.multiply(giaTri).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal toiDa = (BigDecimal) dot.get("so_tien_giam_toi_da");
            if (toiDa != null && toiDa.signum() > 0) giam = giam.min(toiDa);
            tienGiam = tienGiam.max(giam);
        }
        return giaGoc.subtract(tienGiam).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public static void themGia(JdbcTemplate jdbc, Map<String,Object> mau) {
        BigDecimal goc = (BigDecimal) mau.get("gia_ban");
        mau.put("gia_goc", goc);
        mau.put("gia_sau_giam", giaSauGiam(jdbc, ((Number) mau.get("id")).intValue(), goc));
    }

    public static void themGiaDanhSach(JdbcTemplate jdbc, List<Map<String,Object>> ds,
                                      Integer kichThuoc, Integer mauSac, boolean conHang) {
        for (var sp : ds) {
            String sql = """
                    SELECT ct.id,ct.gia_ban FROM chi_tiet_san_pham ct
                    JOIN kich_thuoc kt ON kt.id=ct.id_kich_thuoc JOIN mau_sac ms ON ms.id=ct.id_mau_sac
                    JOIN form_chan fc ON fc.id=ct.id_form_chan
                    WHERE ct.id_san_pham=? AND ISNULL(ct.xoa_mem,0)=0 AND ct.trang_thai=1
                      AND ISNULL(kt.xoa_mem,0)=0 AND kt.trang_thai=1 AND ISNULL(ms.xoa_mem,0)=0 AND ms.trang_thai=1
                      AND ISNULL(fc.xoa_mem,0)=0 AND fc.trang_thai=1
                    """;
            List<Object> args = new ArrayList<>(); args.add(sp.get("id"));
            if (kichThuoc != null) { sql += " AND ct.id_kich_thuoc=?"; args.add(kichThuoc); }
            if (mauSac != null) { sql += " AND ct.id_mau_sac=?"; args.add(mauSac); }
            if (conHang) sql += " AND ct.so_luong>0";
            BigDecimal thapNhat = null;
            for (var mau : jdbc.queryForList(sql + " ORDER BY ct.id", args.toArray())) {
                themGia(jdbc, mau);
                BigDecimal gia = (BigDecimal) mau.get("gia_sau_giam");
                if (thapNhat == null || gia.compareTo(thapNhat) < 0) {
                    thapNhat = gia;
                    sp.put("gia_goc", mau.get("gia_goc"));
                    sp.put("gia_sau_giam", gia);
                }
            }
        }
    }
}
