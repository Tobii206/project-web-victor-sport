package com.example.victorsport.Service;

import com.example.victorsport.Dto.PhieuGiamGiaForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class QuanLyPhieuGiamGiaService {
    private final JdbcTemplate jdbc;
    public QuanLyPhieuGiamGiaService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String,Object>> tim(String tuKhoa, String trangThai) {
        String sql = "SELECT * FROM phieu_giam_gia WHERE ISNULL(xoa_mem,0)=0 AND (ten_phieu_giam_gia LIKE ? OR ma_phieu_giam_gia LIKE ?)";
        List<Object> args = new ArrayList<>();
        args.add("%" + tuKhoa.trim() + "%"); args.add("%" + tuKhoa.trim() + "%");
        if ("0".equals(trangThai) || "1".equals(trangThai)) { sql += " AND trang_thai=?"; args.add(Integer.valueOf(trangThai)); }
        return jdbc.queryForList(sql + " ORDER BY id DESC", args.toArray());
    }

    public Map<String,Object> lay(int id) {
        var ds = jdbc.queryForList("SELECT * FROM phieu_giam_gia WHERE id=? AND ISNULL(xoa_mem,0)=0", id);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy phiếu giảm giá.");
        return ds.get(0);
    }

    @Transactional
    public int luu(Integer id, PhieuGiamGiaForm f) {
        KiemTraUuDai.kiemTra(f.getLoai_phieu_giam_gia(), f.getGia_tri_giam_gia(), f.getNgay_bat_dau(), f.getNgay_ket_thuc());
        if (id == null) {
            return jdbc.queryForObject("""
                INSERT INTO phieu_giam_gia (ten_phieu_giam_gia,loai_phieu_giam_gia,gia_tri_giam_gia,
                    so_tien_giam_toi_da,hoa_don_toi_thieu,so_luong_su_dung,ngay_bat_dau,ngay_ket_thuc,trang_thai,mo_ta)
                OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?,?,?,?)
                """, Integer.class, f.getTen_phieu_giam_gia(), f.getLoai_phieu_giam_gia(), f.getGia_tri_giam_gia(),
                    f.getSo_tien_giam_toi_da(), f.getHoa_don_toi_thieu(), f.getSo_luong_su_dung(),
                    f.getNgay_bat_dau(), f.getNgay_ket_thuc(), f.getTrang_thai(), f.getMo_ta());
        }
        if (f.getSo_luong_cu() == null) throw new IllegalArgumentException("Vui lòng tải lại phiếu giảm giá trước khi sửa.");
        // Không ghi đè số lượt nếu có khách vừa sử dụng phiếu.
        int dong = jdbc.update("""
                UPDATE phieu_giam_gia SET ten_phieu_giam_gia=?,loai_phieu_giam_gia=?,gia_tri_giam_gia=?,
                    so_tien_giam_toi_da=?,hoa_don_toi_thieu=?,so_luong_su_dung=?,ngay_bat_dau=?,ngay_ket_thuc=?,trang_thai=?,mo_ta=?
                WHERE id=? AND ISNULL(xoa_mem,0)=0 AND so_luong_su_dung=?
                """, f.getTen_phieu_giam_gia(), f.getLoai_phieu_giam_gia(), f.getGia_tri_giam_gia(),
                    f.getSo_tien_giam_toi_da(), f.getHoa_don_toi_thieu(), f.getSo_luong_su_dung(),
                    f.getNgay_bat_dau(), f.getNgay_ket_thuc(), f.getTrang_thai(), f.getMo_ta(), id, f.getSo_luong_cu());
        if (dong != 1) throw new IllegalArgumentException("Phiếu hoặc số lượt sử dụng đã thay đổi. Vui lòng tải lại trang.");
        return id;
    }

    public void ngung(int id) {
        if (jdbc.update("UPDATE phieu_giam_gia SET trang_thai=0 WHERE id=? AND ISNULL(xoa_mem,0)=0", id) != 1)
            throw new IllegalArgumentException("Không tìm thấy phiếu giảm giá.");
    }
}
