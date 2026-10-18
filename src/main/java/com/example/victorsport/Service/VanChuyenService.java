package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class VanChuyenService {
    private final JdbcTemplate jdbc;
    public VanChuyenService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public Map<String,Object> danhSach(String tuKhoa, Integer trangThai, int trang) {
        if (trangThai != null && (trangThai < 1 || trangThai > 6))
            throw new IllegalArgumentException("Trạng thái vận chuyển không hợp lệ.");
        if (trang < 1 || trang > 100000) throw new IllegalArgumentException("Số trang không hợp lệ.");
        String loc = " WHERE loai_don IN (1,2) AND trang_thai_hien_tai BETWEEN 1 AND 6 AND ISNULL(xoa_mem,0)=0 ";
        List<Object> thamSo = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            if (tuKhoa.length()>100) throw new IllegalArgumentException("Từ khóa tối đa 100 ký tự.");
            loc += " AND (ma_hoa_don LIKE ? OR ten_khach_hang LIKE ? OR so_dien_thoai_khach_hang LIKE ?) ";
            for (int i=0;i<3;i++) thamSo.add("%"+tuKhoa.trim()+"%");
        }
        if (trangThai != null) { loc += " AND trang_thai_hien_tai=? "; thamSo.add(trangThai); }
        var tongQuan = jdbc.queryForMap("""
                SELECT COUNT(*) AS so_don,
                  ISNULL(SUM(CASE WHEN trang_thai_hien_tai BETWEEN 1 AND 3 THEN 1 ELSE 0 END),0) AS cho_giao,
                  ISNULL(SUM(CASE WHEN trang_thai_hien_tai=4 THEN 1 ELSE 0 END),0) AS dang_giao,
                  ISNULL(SUM(CASE WHEN trang_thai_hien_tai=5 THEN 1 ELSE 0 END),0) AS da_nhan,
                  ISNULL(SUM(CASE WHEN trang_thai_hien_tai=6 THEN 1 ELSE 0 END),0) AS da_huy
                FROM hoa_don
                """ + loc, thamSo.toArray());
        int soTrang = Math.max(1, (((Number)tongQuan.get("so_don")).intValue()+19)/20);
        int trangHienTai = Math.min(trang,soTrang);
        thamSo.add((trangHienTai-1)*20);
        var donHang = jdbc.queryForList("""
                SELECT id,ma_hoa_don,ten_khach_hang,so_dien_thoai_khach_hang,dia_chi_khach_hang,
                  ngay_tao,phi_van_chuyen,tong_tien_sau_giam,trang_thai_hien_tai,
                  vd.id_hoa_don AS ghn_id_hoa_don,vd.ma_van_don,vd.trang_thai AS ghn_trang_thai,vd.phi_thuc_te,
                  CASE trang_thai_hien_tai WHEN 1 THEN N'Chờ xác nhận' WHEN 2 THEN N'Đã xác nhận đơn'
                    WHEN 3 THEN N'Đang chuẩn bị hàng' WHEN 4 THEN N'Đang vận chuyển'
                    WHEN 5 THEN N'Đã nhận hàng' WHEN 6 THEN N'Đã hủy' END AS ten_trang_thai
                FROM hoa_don LEFT JOIN van_don_ghn vd ON vd.id_hoa_don=hoa_don.id
                """ + loc + " ORDER BY ngay_tao DESC,id DESC OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY", thamSo.toArray());
        return Map.of("tongQuan",tongQuan,"donHang",donHang,"trang",trangHienTai,"soTrang",soTrang);
    }
}
