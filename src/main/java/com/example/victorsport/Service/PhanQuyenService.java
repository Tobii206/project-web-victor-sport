package com.example.victorsport.Service;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class PhanQuyenService {
    private final JdbcTemplate jdbc;

    public PhanQuyenService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public NguoiDungDangNhap layQuyen(Integer idNhanVien) {
        List<Map<String, Object>> danhSach = jdbc.queryForList("""
                SELECT nv.id, nv.email, nv.ten_nhan_vien, qh.ten_quyen_han, cn.ma_chuc_nang
                FROM nhan_vien nv
                JOIN quyen_han qh ON qh.id = nv.id_quyen_han
                LEFT JOIN quyen_han_chuc_nang qcn ON qcn.id_quyen_han = qh.id AND ISNULL(qcn.xoa_mem, 0) = 0
                LEFT JOIN chuc_nang cn ON cn.id = qcn.id_chuc_nang
                    AND ISNULL(cn.xoa_mem, 0) = 0 AND ISNULL(cn.trang_thai, 1) = 1
                WHERE nv.id = ? AND ISNULL(nv.xoa_mem, 0) = 0 AND ISNULL(nv.trang_thai, 1) = 1
                    AND ISNULL(qh.xoa_mem, 0) = 0 AND ISNULL(qh.trang_thai, 1) = 1
                """, idNhanVien);
        if (danhSach.isEmpty()) return null;
        Map<String, Object> dong = danhSach.get(0);
        NguoiDungDangNhap nguoiDung = new NguoiDungDangNhap(((Number) dong.get("id")).intValue(),
                (String) dong.get("email"), (String) dong.get("ten_nhan_vien"), (String) dong.get("ten_quyen_han"));
        for (Map<String, Object> quyen : danhSach) nguoiDung.themChucNang((String) quyen.get("ma_chuc_nang"));
        return nguoiDung;
    }
}
