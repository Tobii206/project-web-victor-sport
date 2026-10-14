package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class SanPhamOnlineService {
    private final JdbcTemplate jdbc;

    public SanPhamOnlineService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<Integer,Integer> layGioDaLuu(int idKhachHang) {
        kiemTraKhachHang(idKhachHang, false);
        Map<Integer,Integer> gio=new LinkedHashMap<>();
        var ds=jdbc.queryForList("""
                SELECT ct.id_chi_tiet_san_pham,ct.so_luong FROM chi_tiet_gio_hang ct
                JOIN gio_hang g ON g.id=ct.id_gio_hang
                WHERE g.id_khach_hang=? AND ISNULL(g.xoa_mem,0)=0 AND g.trang_thai=1
                  AND ISNULL(ct.xoa_mem,0)=0 AND ct.so_luong>0 ORDER BY ct.id
                """,idKhachHang);
        for(var dong:ds) gio.put(((Number)dong.get("id_chi_tiet_san_pham")).intValue(),((Number)dong.get("so_luong")).intValue());
        return gio;
    }

    @Transactional
    public Map<Integer,Integer> themVaoGioDaLuu(int idKhachHang,int idPhienBan,int soLuong) {
        int idGio=layVaKhoaGio(idKhachHang);
        var pb=layPhienBanTheoId(idPhienBan);
        kiemTraSoLuong(pb,soLuong);
        var ds=jdbc.queryForList("SELECT so_luong,xoa_mem FROM chi_tiet_gio_hang WHERE id_gio_hang=? AND id_chi_tiet_san_pham=?",idGio,idPhienBan);
        int tong=soLuong;
        if(!ds.isEmpty()&&!Boolean.TRUE.equals(ds.get(0).get("xoa_mem"))) tong+=((Number)ds.get(0).get("so_luong")).intValue();
        kiemTraSoLuong(pb,tong);
        if(ds.isEmpty()) jdbc.update("INSERT INTO chi_tiet_gio_hang(id_gio_hang,id_chi_tiet_san_pham,so_luong,don_gia) VALUES (?,?,?,?)",idGio,idPhienBan,tong,pb.get("gia_ban"));
        else jdbc.update("UPDATE chi_tiet_gio_hang SET so_luong=?,don_gia=?,xoa_mem=0 WHERE id_gio_hang=? AND id_chi_tiet_san_pham=?",tong,pb.get("gia_ban"),idGio,idPhienBan);
        return layGioDaLuu(idKhachHang);
    }

    @Transactional
    public Map<Integer,Integer> suaGioDaLuu(int idKhachHang,int idPhienBan,int soLuong) {
        int idGio=layVaKhoaGio(idKhachHang);
        if(soLuong<=0) {
            jdbc.update("UPDATE chi_tiet_gio_hang SET xoa_mem=1 WHERE id_gio_hang=? AND id_chi_tiet_san_pham=?",idGio,idPhienBan);
        } else {
            var pb=layPhienBanTheoId(idPhienBan);kiemTraSoLuong(pb,soLuong);
            jdbc.update("UPDATE chi_tiet_gio_hang SET so_luong=?,don_gia=? WHERE id_gio_hang=? AND id_chi_tiet_san_pham=? AND ISNULL(xoa_mem,0)=0",soLuong,pb.get("gia_ban"),idGio,idPhienBan);
        }
        return layGioDaLuu(idKhachHang);
    }

    private void kiemTraKhachHang(int idKhachHang,boolean khoa) {
        String sql="SELECT id FROM khach_hang "+(khoa?"WITH (UPDLOCK,HOLDLOCK) ":"")+"WHERE id=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1";
        if(jdbc.queryForList(sql,idKhachHang).isEmpty()) throw new IllegalArgumentException("Tài khoản khách hàng không còn hoạt động. Vui lòng đăng nhập lại.");
    }

    private int layVaKhoaGio(int idKhachHang) {
        // Khóa theo khách để hai thiết bị không tạo trùng giỏ hoặc ghi đè số lượng.
        kiemTraKhachHang(idKhachHang,true);
        var ds=jdbc.queryForList("SELECT id FROM gio_hang WHERE id_khach_hang=?",idKhachHang);
        if(ds.isEmpty()) return jdbc.queryForObject("INSERT INTO gio_hang(id_khach_hang) OUTPUT INSERTED.id VALUES (?)",Integer.class,idKhachHang);
        int id=((Number)ds.get(0).get("id")).intValue();
        jdbc.update("UPDATE gio_hang SET trang_thai=1,xoa_mem=0 WHERE id=?",id);
        return id;
    }

    public Map<String, Object> laySanPham(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList("""
                SELECT sp.id, sp.ten_san_pham, sp.ma_san_pham, sp.mo_ta_chi_tiet, sp.mo_ta_ngan,
                       th.id AS id_thuong_hieu, th.ten_thuong_hieu, cl.ten_chat_lieu,
                       xx.ten_xuat_xu, vt.ten_vi_tri, pc.ten_phong_cach, cg.ten_co_giay
                FROM san_pham sp JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                LEFT JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu AND ISNULL(cl.xoa_mem, 0) = 0
                LEFT JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu AND ISNULL(xx.xoa_mem, 0) = 0
                LEFT JOIN vi_tri_thi_dau vt ON vt.id = sp.id_vi_tri_thi_dau AND ISNULL(vt.xoa_mem, 0) = 0
                LEFT JOIN phong_cach_choi pc ON pc.id = sp.id_phong_cach_choi AND ISNULL(pc.xoa_mem, 0) = 0
                LEFT JOIN co_giay cg ON cg.id = sp.id_co_giay AND ISNULL(cg.xoa_mem, 0) = 0
                WHERE sp.id = ? AND ISNULL(sp.xoa_mem, 0) = 0 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND ISNULL(th.xoa_mem, 0) = 0 AND ISNULL(th.trang_thai, 1) = 1
                """, id);
        if (ds.isEmpty()) { throw new IllegalArgumentException("Sản phẩm không còn được bán."); }
        return ds.get(0);
    }

    public List<Map<String, Object>> layPhienBan(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList(sqlPhienBan() + " AND ct.id_san_pham = ? ORDER BY kt.ten_kich_thuoc, ms.ten_mau_sac, ct.id", id);
        for (var mau : ds) GiaGiamSanPham.themGia(jdbc, mau);
        return ds;
    }

    public List<Map<String, Object>> laySanPhamCungLoai(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList("""
                SELECT TOP 4 sp.id, sp.ma_san_pham, sp.ten_san_pham, th.ten_thuong_hieu,
                       MIN(ct.gia_ban) AS gia_ban,
                       SUM(ct.so_luong) AS so_luong,
                       (SELECT TOP 1 a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                        JOIN chi_tiet_san_pham ca ON ca.id = a.id_chi_tiet_san_pham
                        WHERE ca.id_san_pham = sp.id AND ISNULL(a.xoa_mem, 0) = 0
                          AND ISNULL(ca.xoa_mem, 0) = 0 AND ISNULL(ca.trang_thai, 1) = 1
                        ORDER BY a.la_anh_dai_dien DESC, a.id) AS anh
                FROM san_pham sp
                JOIN san_pham hien_tai ON hien_tai.id = ?
                JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                JOIN chi_tiet_san_pham ct ON ct.id_san_pham = sp.id
                LEFT JOIN vi_tri_thi_dau vt ON vt.id = sp.id_vi_tri_thi_dau
                LEFT JOIN vi_tri_thi_dau vt_hien_tai ON vt_hien_tai.id = hien_tai.id_vi_tri_thi_dau
                WHERE sp.id <> hien_tai.id AND ISNULL(sp.xoa_mem, 0) = 0
                  AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND ISNULL(th.xoa_mem, 0) = 0 AND ISNULL(th.trang_thai, 1) = 1
                  AND ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1 AND ct.so_luong > 0
                  AND (sp.id_vi_tri_thi_dau = hien_tai.id_vi_tri_thi_dau
                       OR (vt.ten_vi_tri IN (N'Tiền đạo', N'Tiền vệ', N'Hậu vệ', N'Thủ môn')
                           AND vt_hien_tai.ten_vi_tri IN (N'Tiền đạo', N'Tiền vệ', N'Hậu vệ', N'Thủ môn')))
                GROUP BY sp.id, sp.ma_san_pham, sp.ten_san_pham, th.ten_thuong_hieu
                ORDER BY sp.id DESC
                """, id);
        GiaGiamSanPham.themGiaDanhSach(jdbc, ds, null, null, true);
        return ds;
    }

    public Map<String, Object> layPhienBanTheoId(int id) {
        List<Map<String, Object>> ds = jdbc.queryForList(sqlPhienBan() + " AND ct.id = ?", id);
        if (ds.isEmpty()) { throw new IllegalArgumentException("Mẫu giày đã chọn không còn được bán."); }
        GiaGiamSanPham.themGia(jdbc, ds.get(0));
        return ds.get(0);
    }

    private String sqlPhienBan() {
        return """
                SELECT ct.id, ct.id_san_pham, ct.ma_chi_tiet_san_pham, ct.so_luong,
                       ct.gia_ban AS gia_ban,
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
                  AND ISNULL(kt.xoa_mem, 0) = 0 AND ISNULL(kt.trang_thai, 1) = 1
                  AND ISNULL(ms.xoa_mem, 0) = 0 AND ISNULL(ms.trang_thai, 1) = 1
                  AND ISNULL(fc.xoa_mem, 0) = 0 AND ISNULL(fc.trang_thai, 1) = 1
                """;
    }

    public List<Map<String, Object>> layAnh(int id) {
        return jdbc.queryForList("""
                SELECT DISTINCT a.duong_dan_anh, a.la_anh_dai_dien FROM anh_chi_tiet_san_pham a
                JOIN chi_tiet_san_pham ct ON ct.id = a.id_chi_tiet_san_pham
                WHERE ct.id_san_pham = ? AND ISNULL(a.xoa_mem, 0) = 0
                  AND ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1
                ORDER BY a.la_anh_dai_dien DESC, a.duong_dan_anh
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
            dong.put("thanh_tien_goc", ((BigDecimal) dong.get("gia_ban")).multiply(BigDecimal.valueOf(soLuong)));
            dong.put("thanh_tien", ((BigDecimal) dong.get("gia_sau_giam")).multiply(BigDecimal.valueOf(soLuong)));
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
        return datHang(gio,idKhachHang,ten,sdt,diaChi,false);
    }

    @Transactional
    public Integer datHang(Map<Integer, Integer> gio, Integer idKhachHang, String ten, String sdt, String diaChi,boolean muaNgay) {
        ten = ten.trim(); sdt = sdt.trim(); diaChi = diaChi.trim();
        if (ten.isEmpty() || ten.length() > 100 || !sdt.matches("0[0-9]{9}") || diaChi.isEmpty() || diaChi.length() > 255) {
            throw new IllegalArgumentException("Nhập tên, số điện thoại 10 chữ số bắt đầu bằng 0 và địa chỉ nhận hàng hợp lệ.");
        }
        Integer idGio=null;
        if(idKhachHang!=null) {
            if(muaNgay) kiemTraKhachHang(idKhachHang,false);
            else {idGio=layVaKhoaGio(idKhachHang);gio=layGioDaLuu(idKhachHang);}
        }
        if (gio.isEmpty()) { throw new IllegalArgumentException("Chưa có sản phẩm để đặt hàng."); }
        List<Map<String, Object>> ds = layGioHang(gio);
        for (Map<String, Object> dong : ds) {
            int soLuong = (Integer) dong.get("so_luong_mua");
            kiemTraSoLuong(dong, soLuong);
            int capNhat = jdbc.update("""
                    UPDATE chi_tiet_san_pham SET so_luong = so_luong - ?
                    WHERE id = ? AND so_luong >= ? AND ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1
                      AND gia_ban = ?
                    """, soLuong, dong.get("id"), soLuong, dong.get("gia_ban"));
            if (capNhat != 1) { throw new IllegalArgumentException("Giá hoặc tồn kho đã thay đổi. Vui lòng kiểm tra lại đơn hàng."); }
        }
        BigDecimal tong = tinhTong(ds);
        BigDecimal tongGoc = BigDecimal.ZERO;
        for (var dong : ds) tongGoc = tongGoc.add((BigDecimal) dong.get("thanh_tien_goc"));
        Integer id = jdbc.queryForObject("""
                INSERT INTO hoa_don (id_khach_hang, loai_don, tong_tien, tong_tien_sau_giam,
                    ten_khach_hang, so_dien_thoai_khach_hang, dia_chi_khach_hang, trang_thai_hien_tai, ghi_chu)
                OUTPUT INSERTED.id VALUES (?, 1, ?, ?, ?, ?, ?, 1, N'Thanh toán khi nhận hàng (COD)')
                """, Integer.class, idKhachHang, tongGoc, tong, ten, sdt, diaChi);
        for (Map<String, Object> dong : ds) {
            jdbc.update("INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia) VALUES (?, ?, ?, ?)",
                    id, dong.get("id"), dong.get("so_luong_mua"), dong.get("gia_ban"));
        }
        jdbc.update("INSERT INTO lich_su_hoa_don (id_hoa_don, trang_thai, ghi_chu) VALUES (?, 1, N'Khách đặt hàng online, thanh toán khi nhận hàng')", id);
        // Cùng giao dịch với hóa đơn: đặt hàng lỗi thì giỏ vẫn được giữ nguyên.
        if(idGio!=null) jdbc.update("UPDATE chi_tiet_gio_hang SET xoa_mem=1 WHERE id_gio_hang=? AND ISNULL(xoa_mem,0)=0",idGio);
        return id;
    }
}
