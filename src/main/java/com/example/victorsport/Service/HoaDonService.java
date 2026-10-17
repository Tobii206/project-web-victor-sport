package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

@Service
public class HoaDonService {
    private final JdbcTemplate jdbcTemplate;

    public HoaDonService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> layDanhSachHoaDon(String tuKhoa) {
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        String tuKhoaTimKiem = "%" + tuKhoa.trim() + "%";
        String sql = """
                SELECT id, id_nhan_vien, loai_don, ma_hoa_don, ten_khach_hang, so_dien_thoai_khach_hang,
                       ngay_tao, tong_tien_sau_giam, trang_thai_hien_tai,
                       CASE WHEN trang_thai_hien_tai = 0 THEN N'Đơn chờ'
                            WHEN trang_thai_hien_tai = 1 THEN N'Chờ xác nhận'
                            WHEN trang_thai_hien_tai = 2 THEN N'Đã xác nhận đơn'
                            WHEN trang_thai_hien_tai = 3 THEN N'Đang chuẩn bị hàng'
                            WHEN trang_thai_hien_tai = 4 THEN N'Đang vận chuyển'
                            WHEN trang_thai_hien_tai = 5 AND loai_don IN (1,2) THEN N'Đã nhận hàng'
                            WHEN trang_thai_hien_tai = 5 THEN N'Hoàn tất'
                            WHEN trang_thai_hien_tai = 6 THEN N'Đã hủy'
                            ELSE N'Trạng thái ' + CAST(trang_thai_hien_tai AS NVARCHAR(10)) END AS ten_trang_thai
                FROM hoa_don
                WHERE ISNULL(xoa_mem, 0) = 0
                  AND (ma_hoa_don LIKE ? OR ten_khach_hang LIKE ? OR so_dien_thoai_khach_hang LIKE ?)
                ORDER BY id DESC
                """;
        return jdbcTemplate.queryForList(sql, tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem);
    }

    public Map<String, Object> layHoaDon(Integer id) {
        String sql = """
                SELECT hd.*, nv.ten_nhan_vien,
                       CASE WHEN hd.trang_thai_hien_tai = 0 THEN N'Đơn chờ'
                            WHEN hd.trang_thai_hien_tai = 1 THEN N'Chờ xác nhận'
                            WHEN hd.trang_thai_hien_tai = 2 THEN N'Đã xác nhận đơn'
                            WHEN hd.trang_thai_hien_tai = 3 THEN N'Đang chuẩn bị hàng'
                            WHEN hd.trang_thai_hien_tai = 4 THEN N'Đang vận chuyển'
                            WHEN hd.trang_thai_hien_tai = 5 AND hd.loai_don IN (1,2) THEN N'Đã nhận hàng'
                            WHEN hd.trang_thai_hien_tai = 5 THEN N'Hoàn tất'
                            WHEN hd.trang_thai_hien_tai = 6 THEN N'Đã hủy'
                            ELSE N'Trạng thái ' + CAST(hd.trang_thai_hien_tai AS NVARCHAR(10)) END AS ten_trang_thai
                FROM hoa_don hd
                LEFT JOIN nhan_vien nv ON nv.id = hd.id_nhan_vien
                WHERE hd.id = ? AND ISNULL(hd.xoa_mem, 0) = 0
                """;
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList(sql, id);
        if (danhSach.isEmpty()) {
            return null;
        }
        return danhSach.get(0);
    }

    public List<Map<String, Object>> laySanPhamTrongHoaDon(Integer idHoaDon) {
        String sql = """
                SELECT sp.ma_san_pham, sp.ten_san_pham, ms.ten_mau_sac, kt.ten_kich_thuoc,
                       hdct.so_luong, hdct.don_gia, hdct.thanh_tien
                FROM hoa_don_chi_tiet hdct
                LEFT JOIN chi_tiet_san_pham ct ON ct.id = hdct.id_chi_tiet_san_pham
                LEFT JOIN san_pham sp ON sp.id = ct.id_san_pham
                LEFT JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                LEFT JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE hdct.id_hoa_don = ? AND ISNULL(hdct.xoa_mem, 0) = 0
                ORDER BY hdct.id
                """;
        return jdbcTemplate.queryForList(sql, idHoaDon);
    }

    public List<Map<String,Object>> layLichSu(int idHoaDon) {
        return jdbcTemplate.queryForList("""
                SELECT trang_thai,thoi_gian,ghi_chu FROM lich_su_hoa_don
                WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0 ORDER BY thoi_gian,id
                """, idHoaDon);
    }

    private void kiemTraKhachHang(int idKhachHang) {
        if (jdbcTemplate.queryForList("""
                SELECT id FROM khach_hang WHERE id=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1
                """, idKhachHang).isEmpty())
            throw new IllegalArgumentException("Tài khoản khách hàng không còn hoạt động. Vui lòng đăng nhập lại.");
    }

    public List<Map<String,Object>> layDonCuaKhach(int idKhachHang) {
        kiemTraKhachHang(idKhachHang);
        return jdbcTemplate.queryForList("""
                SELECT id,ma_hoa_don,ngay_tao,tong_tien_sau_giam,trang_thai_hien_tai,
                    CASE WHEN trang_thai_hien_tai=1 THEN N'Chờ xác nhận'
                         WHEN trang_thai_hien_tai=2 THEN N'Đã xác nhận đơn'
                         WHEN trang_thai_hien_tai=3 THEN N'Đang chuẩn bị hàng'
                         WHEN trang_thai_hien_tai=4 THEN N'Đang vận chuyển'
                         WHEN trang_thai_hien_tai=5 THEN N'Đã nhận hàng'
                         WHEN trang_thai_hien_tai=6 THEN N'Đã hủy'
                         ELSE N'Đơn không còn xử lý' END AS ten_trang_thai
                FROM hoa_don WHERE id_khach_hang=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0 ORDER BY id DESC
                """, idKhachHang);
    }

    public Map<String,Object> layDonCuaKhach(int idHoaDon,int idKhachHang) {
        kiemTraKhachHang(idKhachHang);
        // Kiểm tra chủ đơn trước khi đọc thông tin và sản phẩm của đơn.
        if (jdbcTemplate.queryForList("""
                SELECT id FROM hoa_don WHERE id=? AND id_khach_hang=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """, idHoaDon,idKhachHang).isEmpty()) return null;
        return layHoaDon(idHoaDon);
    }

    @Transactional
    public void xacNhanThanhToan(int idHoaDon,int idAdmin) {
        var admin = jdbcTemplate.queryForList("""
                SELECT nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
                WHERE nv.id=? AND ISNULL(nv.xoa_mem,0)=0 AND nv.trang_thai=1
                    AND ISNULL(q.xoa_mem,0)=0 AND q.trang_thai=1 AND q.ten_quyen_han=N'Quản trị viên'
                """, idAdmin);
        if (admin.isEmpty()) throw new IllegalArgumentException("Chỉ quản trị viên được xác nhận thanh toán.");
        // Khóa hóa đơn để hai lần bấm không tạo trùng thanh toán và lịch sử.
        var ds = jdbcTemplate.queryForList("""
                SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
                WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """, idHoaDon);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn hàng online.");
        var hd = ds.get(0);
        int trangThai = ((Number) hd.get("trang_thai_hien_tai")).intValue();
        if (trangThai == 5) throw new IllegalArgumentException("Khách đã nhận hàng. Không được chỉnh trạng thái đơn này.");
        if (trangThai < 1 || trangThai > 3) throw new IllegalArgumentException("Chỉ xác nhận được đơn đang chờ hoặc đang chuẩn bị hàng.");
        if (jdbcTemplate.queryForList("""
                SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=? AND so_luong>0 AND ISNULL(xoa_mem,0)=0
                """, idHoaDon).isEmpty()) throw new IllegalArgumentException("Đơn hàng chưa có sản phẩm.");

        BigDecimal daThu = jdbcTemplate.queryForObject("""
                SELECT ISNULL(SUM(so_tien),0) FROM giao_dich_thanh_toan
                WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND ISNULL(xoa_mem,0)=0
                """, BigDecimal.class,idHoaDon);
        BigDecimal conLai = ((BigDecimal) hd.get("tong_tien_sau_giam")).subtract(daThu);
        if (conLai.signum() > 0) {
            var phuongThuc = jdbcTemplate.queryForList("""
                    SELECT TOP 1 id_phuong_thuc_thanh_toan AS id FROM giao_dich_thanh_toan
                    WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0 ORDER BY id DESC
                    """, idHoaDon);
            if (phuongThuc.isEmpty()) phuongThuc = jdbcTemplate.queryForList("""
                    SELECT TOP 1 id FROM phuong_thuc_thanh_toan
                    WHERE nha_cung_cap=N'COD' AND ISNULL(xoa_mem,0)=0 AND trang_thai=1 ORDER BY id
                    """);
            if (phuongThuc.isEmpty()) throw new IllegalArgumentException("Chưa có phương thức thanh toán cho đơn hàng.");
            int daCapNhatQr = jdbcTemplate.update("""
                    UPDATE gd SET trang_thai=N'thanh_cong',thoi_gian_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=?,
                        ghi_chu=N'Quản trị viên đã kiểm tra và xác nhận chuyển khoản QR'
                    FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
                    WHERE gd.id=(SELECT MIN(id) FROM giao_dich_thanh_toan WHERE id_hoa_don=?
                        AND trang_thai=N'cho_thanh_toan' AND so_tien=? AND ISNULL(xoa_mem,0)=0)
                      AND pt.nha_cung_cap=N'VietQR'
                    """, idAdmin,idHoaDon,conLai);
            if (daCapNhatQr == 0) jdbcTemplate.update("""
                    INSERT INTO giao_dich_thanh_toan (id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,nguoi_cap_nhat,ghi_chu)
                    VALUES (?,?,?,N'thanh_cong',?,N'Quản trị viên xác nhận đã thanh toán')
                    """, idHoaDon,phuongThuc.get(0).get("id"),conLai,idAdmin);
        }
        int dong = jdbcTemplate.update("""
                UPDATE hoa_don SET trang_thai_hien_tai=4,ngay_thanh_toan=COALESCE(ngay_thanh_toan,SYSDATETIME()),
                    ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=?
                WHERE id=? AND trang_thai_hien_tai IN (1,2,3) AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """, idAdmin,idHoaDon);
        if (dong != 1) throw new IllegalArgumentException("Trạng thái đơn đã thay đổi. Vui lòng tải lại trang.");
        jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (id_hoa_don,trang_thai,ghi_chu,nguoi_cap_nhat,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,4,N'Quản trị viên xác nhận thanh toán. Đơn đang vận chuyển.',?,?,'nhan_vien')
                """, idHoaDon,idAdmin,idAdmin);
        // Kho đã được trừ khi khách đặt hàng, không trừ lại khi xác nhận.
    }

    @Transactional
    public void xacNhanDaNhanHang(int idHoaDon,int idKhachHang) {
        kiemTraKhachHang(idKhachHang);
        int dong = jdbcTemplate.update("""
                UPDATE hoa_don SET trang_thai_hien_tai=5,ngay_cap_nhat=SYSDATETIME()
                WHERE id=? AND id_khach_hang=? AND loai_don IN (1,2) AND trang_thai_hien_tai=4 AND ISNULL(xoa_mem,0)=0
                """, idHoaDon,idKhachHang);
        if (dong != 1) throw new IllegalArgumentException("Không thể xác nhận. Đơn phải thuộc tài khoản của bạn và đang vận chuyển.");
        jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (id_hoa_don,trang_thai,ghi_chu,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,5,N'Khách hàng xác nhận đã nhận được hàng.',?,'khach_hang')
                """, idHoaDon,idKhachHang);
    }

    @Transactional
    public void huyDonCuaKhach(int idHoaDon,int idKhachHang) {
        kiemTraKhachHang(idKhachHang);
        huyDonChuaXacNhan(idHoaDon,idKhachHang,null);
    }

    @Transactional
    public void huyHoaDon(int idHoaDon,int idAdmin) {
        var admin = jdbcTemplate.queryForList("""
                SELECT nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
                WHERE nv.id=? AND nv.trang_thai=1 AND ISNULL(nv.xoa_mem,0)=0
                  AND q.ten_quyen_han=N'Quản trị viên' AND q.trang_thai=1 AND ISNULL(q.xoa_mem,0)=0
                """,idAdmin);
        if (admin.isEmpty()) throw new IllegalArgumentException("Chỉ quản trị viên được hủy hóa đơn.");
        huyDonChuaXacNhan(idHoaDon,null,idAdmin);
    }

    private void huyDonChuaXacNhan(int idHoaDon,Integer idKhachHang,Integer idAdmin) {
        // Dùng cùng khóa hóa đơn với thao tác xác nhận của admin.
        List<Map<String,Object>> ds;
        if (idAdmin != null) ds = jdbcTemplate.queryForList("""
                SELECT trang_thai_hien_tai,id_phieu_giam_gia FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
                WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """,idHoaDon);
        else ds = jdbcTemplate.queryForList("""
                SELECT trang_thai_hien_tai,id_phieu_giam_gia FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
                WHERE id=? AND id_khach_hang=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """, idHoaDon,idKhachHang);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn hàng cần hủy.");
        int trangThai = ((Number) ds.get(0).get("trang_thai_hien_tai")).intValue();
        if (trangThai != 1) throw new IllegalArgumentException("Chỉ được hủy đơn đang chờ xác nhận.");
        if (!jdbcTemplate.queryForList("""
                SELECT id FROM giao_dich_thanh_toan
                WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND so_tien>0 AND ISNULL(xoa_mem,0)=0
                """, idHoaDon).isEmpty())
            throw new IllegalArgumentException("Đơn đã có thanh toán. Vui lòng liên hệ cửa hàng để được hỗ trợ hủy.");

        var cacMau = jdbcTemplate.queryForList("""
                SELECT id_chi_tiet_san_pham,so_luong FROM hoa_don_chi_tiet
                WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0 ORDER BY id_chi_tiet_san_pham,id
                """, idHoaDon);
        for (var mau : cacMau) {
            int soLuong = ((Number) mau.get("so_luong")).intValue();
            if (soLuong <= 0) throw new IllegalArgumentException("Số lượng sản phẩm trong đơn không hợp lệ.");
            int dong = jdbcTemplate.update("""
                    UPDATE chi_tiet_san_pham SET so_luong=ISNULL(so_luong,0)+? WHERE id=?
                    """, soLuong,mau.get("id_chi_tiet_san_pham"));
            if (dong != 1) throw new IllegalArgumentException("Không hoàn lại được tồn kho. Vui lòng thử lại.");
        }
        int dong;
        if (idAdmin != null) dong = jdbcTemplate.update("""
                UPDATE hoa_don SET trang_thai_hien_tai=6,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=?
                WHERE id=? AND trang_thai_hien_tai=1 AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """,idAdmin,idHoaDon);
        else dong = jdbcTemplate.update("""
                UPDATE hoa_don SET trang_thai_hien_tai=6,ngay_cap_nhat=SYSDATETIME()
                WHERE id=? AND id_khach_hang=? AND trang_thai_hien_tai=1 AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """, idHoaDon,idKhachHang);
        if (dong != 1) throw new IllegalArgumentException("Trạng thái đơn đã thay đổi. Vui lòng tải lại trang.");
        jdbcTemplate.update("""
                UPDATE giao_dich_thanh_toan SET trang_thai=N'da_huy',thoi_gian_cap_nhat=SYSDATETIME()
                WHERE id_hoa_don=? AND trang_thai IN (N'cho_thanh_toan',N'khoi_tao') AND ISNULL(xoa_mem,0)=0
                """, idHoaDon);
        if(ds.get(0).get("id_phieu_giam_gia")!=null)
            jdbcTemplate.update("UPDATE phieu_giam_gia SET so_luong_su_dung=so_luong_su_dung+1 WHERE id=?",ds.get(0).get("id_phieu_giam_gia"));
        if (idAdmin != null) jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (id_hoa_don,trang_thai,ghi_chu,nguoi_cap_nhat,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,6,N'Quản trị viên hủy hóa đơn chưa xác nhận.',?,?,'nhan_vien')
                """,idHoaDon,idAdmin,idAdmin);
        else jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (id_hoa_don,trang_thai,ghi_chu,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,6,N'Khách hàng hủy đơn khi đang chờ xác nhận.',?,'khach_hang')
                """, idHoaDon,idKhachHang);
    }
}
