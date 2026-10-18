package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ThongKeService {
    private final JdbcTemplate jdbc;
    public ThongKeService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public Map<String, Object> thongKe(LocalDate tuNgay, LocalDate denNgay, String kenh) {
        kiemTraKhoangNgay(tuNgay, denNgay);
        String loaiDon = switch (kenh) {
            case "online" -> " AND hd.loai_don IN (1,2) ";
            case "quay" -> " AND hd.loai_don=0 ";
            case "tat-ca" -> "";
            default -> throw new IllegalArgumentException("Kênh bán hàng không hợp lệ.");
        };
        // Lọc theo ngày tạo đơn. Chỉ trạng thái 5 được tính vào doanh thu.
        String dieuKien = " WHERE ISNULL(hd.xoa_mem,0)=0 AND hd.ngay_tao>=? AND hd.ngay_tao<? " + loaiDon;
        Object[] thamSo = {tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay()};
        Map<String,Object> ketQua = new LinkedHashMap<>();
        ketQua.put("tongQuan", jdbc.queryForMap("""
                SELECT COUNT(*) AS so_don,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=5 THEN hd.tong_tien_sau_giam ELSE 0 END),0) AS doanh_thu,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=5 THEN 1 ELSE 0 END),0) AS hoan_tat,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai BETWEEN 1 AND 4 THEN 1 ELSE 0 END),0) AS dang_xu_ly,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=6 THEN 1 ELSE 0 END),0) AS da_huy
                FROM hoa_don hd
                """ + dieuKien + " AND hd.trang_thai_hien_tai BETWEEN 1 AND 6", thamSo));
        var theoNgay = jdbc.queryForList("""
                SELECT CAST(hd.ngay_tao AS DATE) AS ngay,
                  SUM(hd.tong_tien_sau_giam) AS doanh_thu, COUNT(*) AS so_don
                FROM hoa_don hd
                """ + dieuKien + " AND hd.trang_thai_hien_tai=5 GROUP BY CAST(hd.ngay_tao AS DATE) ORDER BY ngay", thamSo);
        Map<LocalDate,Map<String,Object>> theoNgayMap = new HashMap<>();
        BigDecimal caoNhat = BigDecimal.ONE;
        for (var dong : theoNgay) {
            LocalDate ngay = LocalDate.parse(dong.get("ngay").toString());
            theoNgayMap.put(ngay, dong);
            caoNhat = caoNhat.max((BigDecimal) dong.get("doanh_thu"));
        }
        List<Map<String,Object>> bieuDo = new ArrayList<>();
        for (LocalDate ngay = tuNgay; !ngay.isAfter(denNgay); ngay = ngay.plusDays(1)) {
            var dong = theoNgayMap.getOrDefault(ngay, Map.of("doanh_thu", BigDecimal.ZERO, "so_don", 0));
            BigDecimal doanhThu = (BigDecimal) dong.get("doanh_thu");
            bieuDo.add(Map.of("ngay", ngay.toString(), "nhan", ngay.getDayOfMonth()+"/"+ngay.getMonthValue(),
                    "doanh_thu", doanhThu, "so_don", dong.get("so_don"),
                    "cao", doanhThu.multiply(BigDecimal.valueOf(100)).divide(caoNhat, 2, RoundingMode.HALF_UP)));
        }
        ketQua.put("bieuDo", bieuDo);
        ketQua.put("banChay", jdbc.queryForList("""
                SELECT TOP 10 sp.id, sp.ma_san_pham, sp.ten_san_pham,
                  SUM(ct.so_luong) AS so_luong, SUM(ct.thanh_tien) AS tien_hang
                FROM hoa_don hd JOIN hoa_don_chi_tiet ct ON ct.id_hoa_don=hd.id AND ISNULL(ct.xoa_mem,0)=0
                JOIN chi_tiet_san_pham mau ON mau.id=ct.id_chi_tiet_san_pham
                JOIN san_pham sp ON sp.id=mau.id_san_pham
                """ + dieuKien + " AND hd.trang_thai_hien_tai=5 GROUP BY sp.id,sp.ma_san_pham,sp.ten_san_pham ORDER BY so_luong DESC,sp.id", thamSo));
        return ketQua;
    }

    public static void kiemTraKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        if (ChronoUnit.DAYS.between(tuNgay,denNgay)>365 || denNgay.getYear()>9998 || tuNgay.getYear()<1900)
            throw new IllegalArgumentException("Vui lòng chọn khoảng thời gian tối đa 366 ngày, từ năm 1900.");
    }
}
