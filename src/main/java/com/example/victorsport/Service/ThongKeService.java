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
        var tongQuan = new LinkedHashMap<String,Object>(jdbc.queryForMap("""
                SELECT COUNT(*) AS so_don,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=5 THEN hd.tong_tien_sau_giam ELSE 0 END),0) AS doanh_thu,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=5 THEN 1 ELSE 0 END),0) AS hoan_tat,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai BETWEEN 1 AND 4 THEN 1 ELSE 0 END),0) AS dang_xu_ly,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=6 THEN 1 ELSE 0 END),0) AS da_huy,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=1 THEN 1 ELSE 0 END),0) AS cho_xac_nhan,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=2 THEN 1 ELSE 0 END),0) AS da_xac_nhan,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=3 THEN 1 ELSE 0 END),0) AS chuan_bi,
                  ISNULL(SUM(CASE WHEN hd.trang_thai_hien_tai=4 THEN 1 ELSE 0 END),0) AS van_chuyen
                FROM hoa_don hd
                """ + dieuKien + " AND hd.trang_thai_hien_tai BETWEEN 1 AND 6", thamSo));
        BigDecimal tongDoanhThu = so(tongQuan.get("doanh_thu")), hoanTat = so(tongQuan.get("hoan_tat"));
        tongQuan.put("trung_binh_don", hoanTat.signum()==0 ? BigDecimal.ZERO : tongDoanhThu.divide(hoanTat,0,RoundingMode.HALF_UP));
        BigDecimal soDon = so(tongQuan.get("so_don"));
        tongQuan.put("ty_le_huy", soDon.signum()==0 ? BigDecimal.ZERO : so(tongQuan.get("da_huy")).multiply(new BigDecimal("100")).divide(soDon,1,RoundingMode.HALF_UP));
        ketQua.put("tongQuan", tongQuan);
        ketQua.put("chiSo", jdbc.queryForMap("""
                SELECT ISNULL(SUM(hang.so_doi),0) AS so_doi,
                  COUNT(DISTINCT hd.id_khach_hang) AS khach_mua,
                  ISNULL(SUM(hd.phi_van_chuyen),0) AS phi_ship,
                  ISNULL(SUM(hd.tong_tien_giam),0) AS uu_dai,
                  ISNULL(SUM(CASE WHEN thu.so_tien>hd.tong_tien_sau_giam THEN hd.tong_tien_sau_giam ELSE thu.so_tien END),0) AS da_thu
                FROM hoa_don hd
                OUTER APPLY (SELECT SUM(ct.so_luong) AS so_doi FROM hoa_don_chi_tiet ct
                  WHERE ct.id_hoa_don=hd.id AND ISNULL(ct.xoa_mem,0)=0) hang
                OUTER APPLY (SELECT ISNULL(SUM(gd.so_tien),0) AS so_tien FROM giao_dich_thanh_toan gd
                  WHERE gd.id_hoa_don=hd.id AND gd.trang_thai=N'thanh_cong' AND ISNULL(gd.xoa_mem,0)=0) thu
                """ + dieuKien + " AND hd.trang_thai_hien_tai=5", thamSo));
        ketQua.put("theoKenh", jdbc.queryForList("""
                SELECT CASE WHEN hd.loai_don=0 THEN N'Tại quầy' ELSE N'Online' END AS kenh,
                  COUNT(*) AS so_don, SUM(hd.tong_tien_sau_giam) AS doanh_thu
                FROM hoa_don hd
                """ + dieuKien + " AND hd.trang_thai_hien_tai=5 GROUP BY CASE WHEN hd.loai_don=0 THEN N'Tại quầy' ELSE N'Online' END", thamSo));
        ketQua.put("khachHang", jdbc.queryForList("""
                SELECT TOP 5 hd.id_khach_hang, kh.ten_khach_hang, kh.ma_khach_hang,
                  COUNT(*) AS so_don, SUM(hd.tong_tien_sau_giam) AS chi_tieu
                FROM hoa_don hd JOIN khach_hang kh ON kh.id=hd.id_khach_hang
                """ + dieuKien + " AND hd.trang_thai_hien_tai=5 GROUP BY hd.id_khach_hang,kh.ten_khach_hang,kh.ma_khach_hang ORDER BY chi_tieu DESC,hd.id_khach_hang", thamSo));
        ketQua.put("tonKho", jdbc.queryForMap("""
                SELECT COUNT(*) AS so_mau, ISNULL(SUM(ct.so_luong),0) AS so_doi,
                  ISNULL(SUM(CASE WHEN ct.so_luong=0 THEN 1 ELSE 0 END),0) AS het_hang,
                  ISNULL(SUM(CASE WHEN ct.so_luong BETWEEN 1 AND 5 THEN 1 ELSE 0 END),0) AS sap_het
                FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id=ct.id_san_pham
                WHERE ISNULL(ct.xoa_mem,0)=0 AND ct.trang_thai=1 AND ISNULL(sp.xoa_mem,0)=0 AND sp.trang_thai_kinh_doanh=1
                """));
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

    private static BigDecimal so(Object value) {
        return value instanceof Number ? new BigDecimal(value.toString()) : BigDecimal.ZERO;
    }
}
