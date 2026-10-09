package com.example.victorsport.Service;

import com.example.victorsport.Dto.AnhSanPham;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SanPhamOnlineService {
    private final JdbcTemplate jdbc;

    public SanPhamOnlineService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<String, Object> laySanPham(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList("""
                SELECT sp.id, sp.ten_san_pham, sp.ma_san_pham, sp.mo_ta_chi_tiet, sp.mo_ta_ngan,
                       th.ten_thuong_hieu
                FROM san_pham sp JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                WHERE sp.id = ? AND ISNULL(sp.xoa_mem, 0) = 0 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND ISNULL(th.xoa_mem, 0) = 0 AND ISNULL(th.trang_thai, 1) = 1
                """, id);
        if (ds.isEmpty()) { throw new IllegalArgumentException("Sản phẩm không còn được bán."); }
        return ds.get(0);
    }

    public List<Map<String, Object>> layPhienBan(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList(sqlPhienBan() + " AND ct.id_san_pham = ? ORDER BY kt.ten_kich_thuoc, ms.ten_mau_sac, ct.id", id);
        for (Map<String, Object> pb : ds) { ganAnh(pb); }
        return ds;
    }

    public Map<String, Object> layPhienBanTheoId(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList(sqlPhienBan() + " AND ct.id = ?", id);
        if (ds.isEmpty()) { throw new IllegalArgumentException("Mẫu giày đã chọn không còn được bán."); }
        ganAnh(ds.get(0));
        return ds.get(0);
    }

    private void ganAnh(Map<String, Object> pb) {
        String anh = AnhSanPham.layAnhDaiDien((String) pb.get("ten_san_pham"));
        if (anh != null) { pb.put("anh", anh); }
    }

    private String sqlPhienBan() {
        return """
                SELECT ct.id, ct.id_san_pham, ct.ma_chi_tiet_san_pham, ct.so_luong,
                       ISNULL(ct.gia_ban, ct.gia_niem_yet) AS gia_ban, ct.gia_niem_yet,
                       sp.ten_san_pham, kt.ten_kich_thuoc, ms.ten_mau_sac, fc.ten_form_chan,
                       (SELECT TOP 1 a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                        WHERE a.id_chi_tiet_san_pham = ct.id AND ISNULL(a.xoa_mem, 0) = 0
                        ORDER BY a.la_anh_dai_dien DESC, a.id) AS anh
                FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id = ct.id_san_pham
                JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN form_chan fc ON fc.id = ct.id_form_chan
                WHERE ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1
                  AND ISNULL(sp.xoa_mem, 0) = 0 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND ISNULL(th.xoa_mem, 0) = 0 AND ISNULL(th.trang_thai, 1) = 1
                """;
    }

    public List<Map<String, Object>> layAnh(int id) {
        Map<String, Object> sanPham = laySanPham(id);
        List<Map<String, Object>> anhCoSan = AnhSanPham.layDanhSachAnh((String) sanPham.get("ten_san_pham"));
        if (!anhCoSan.isEmpty()) { return anhCoSan; }
        return jdbc.queryForList("""
                SELECT a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                JOIN chi_tiet_san_pham ct ON ct.id = a.id_chi_tiet_san_pham
                WHERE ct.id_san_pham = ? AND ISNULL(a.xoa_mem, 0) = 0
                  AND ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1
                ORDER BY a.la_anh_dai_dien DESC, a.id
                """, id);
    }

    public void kiemTraSoLuong(Map<String, Object> phienBan, int soLuong) {
        int ton = ((Number) phienBan.get("so_luong")).intValue();
        if (soLuong < 1) { throw new IllegalArgumentException("Số lượng phải từ 1 trở lên."); }
        if (soLuong > ton) { throw new IllegalArgumentException("Không đủ hàng. Mẫu đã chọn còn " + ton + " sản phẩm."); }
    }

    public List<Map<String, Object>> layGioHang(Map<Integer, Integer> gio) {
        List<Map<String, Object>> ds = new ArrayList<>();
        for (Integer id : gio.keySet()) {
            Map<String, Object> dong = layPhienBanTheoId(id);
            int soLuong = gio.get(id);
            dong.put("so_luong_mua", soLuong);
            dong.put("thanh_tien", ((BigDecimal) dong.get("gia_ban")).multiply(BigDecimal.valueOf(soLuong)));
            ds.add(dong);
        }
        return ds;
    }

    public BigDecimal tinhTong(List<Map<String, Object>> ds) {
        BigDecimal tong = BigDecimal.ZERO;
        for (Map<String, Object> dong : ds) { tong = tong.add((BigDecimal) dong.get("thanh_tien")); }
        return tong;
    }

    @Transactional
    public Integer datHang(Map<Integer, Integer> gio, Integer idKhachHang, String ten, String sdt, String diaChi) {
        ten = ten.trim(); sdt = sdt.trim(); diaChi = diaChi.trim();
        if (ten.isEmpty() || ten.length() > 100 || !sdt.matches("0[0-9]{9}") || diaChi.isEmpty() || diaChi.length() > 255) {
            throw new IllegalArgumentException("Nhập tên, số điện thoại 10 chữ số bắt đầu bằng 0 và địa chỉ nhận hàng hợp lệ.");
        }
        if (gio.isEmpty()) { throw new IllegalArgumentException("Chưa có sản phẩm để đặt hàng."); }
        List<Map<String, Object>> ds = layGioHang(gio);
        for (Map<String, Object> dong : ds) {
            int soLuong = (Integer) dong.get("so_luong_mua");
            kiemTraSoLuong(dong, soLuong);
            int capNhat = jdbc.update("""
                    UPDATE chi_tiet_san_pham SET so_luong = so_luong - ?
                    WHERE id = ? AND so_luong >= ? AND ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1
                      AND ISNULL(gia_ban, gia_niem_yet) = ?
                    """, soLuong, dong.get("id"), soLuong, dong.get("gia_ban"));
            if (capNhat != 1) { throw new IllegalArgumentException("Giá hoặc tồn kho đã thay đổi. Vui lòng kiểm tra lại đơn hàng."); }
        }
        BigDecimal tong = tinhTong(ds);
        Integer id = jdbc.queryForObject("""
                INSERT INTO hoa_don (id_khach_hang, loai_don, tong_tien, tong_tien_sau_giam,
                    ten_khach_hang, so_dien_thoai_khach_hang, dia_chi_khach_hang, trang_thai_hien_tai, ghi_chu)
                OUTPUT INSERTED.id VALUES (?, 1, ?, ?, ?, ?, ?, 1, N'Thanh toán khi nhận hàng (COD)')
                """, Integer.class, idKhachHang, tong, tong, ten, sdt, diaChi);
        for (Map<String, Object> dong : ds) {
            jdbc.update("INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia) VALUES (?, ?, ?, ?)",
                    id, dong.get("id"), dong.get("so_luong_mua"), dong.get("gia_ban"));
        }
        jdbc.update("INSERT INTO lich_su_hoa_don (id_hoa_don, trang_thai, ghi_chu) VALUES (?, 1, N'Khách đặt hàng online, thanh toán khi nhận hàng')", id);
        return id;
    }
}
