package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

@Service
public class HoaDonService {
    @org.springframework.beans.factory.annotation.Autowired
    private ChuongTrucTuyenService chuongTrucTuyen;
    private void baoChuong(int id) { if(chuongTrucTuyen!=null) chuongTrucTuyen.donThayDoi(id); }

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
        var danhSach=jdbcTemplate.queryForList(sql, tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem);
        for(var hd:danhSach) themThanhToan(hd);
        return danhSach;
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
        var hd=danhSach.get(0);themThanhToan(hd);
        var vd=jdbcTemplate.queryForList("SELECT ma_van_don,trang_thai,phi_thuc_te,ngay_cap_nhat FROM van_don_ghn WHERE id_hoa_don=?",id);
        if(!vd.isEmpty()) hd.put("vanDonGhn",vd.get(0));
        return hd;
    }

    private void themThanhToan(Map<String,Object> hd) {
        var ds=jdbcTemplate.queryForList("""
            SELECT ISNULL(SUM(CASE WHEN trang_thai=N'thanh_cong' THEN so_tien ELSE 0 END),0) AS da_thu
            FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0
            """,hd.get("id"));
        BigDecimal daThu=ds.isEmpty()?BigDecimal.ZERO:(BigDecimal)ds.get(0).get("da_thu");
        hd.put("da_thu",daThu);hd.put("phuong_thuc",((Number)hd.get("loai_don")).intValue()==0?"Tại quầy":(laCod(((Number)hd.get("id")).intValue(),hd)?"COD":"QR / chuyển khoản"));
        hd.put("da_thanh_toan",daThu.compareTo((BigDecimal)hd.get("tong_tien_sau_giam"))>=0);
    }
    private boolean laCod(int id,Map<String,Object> hd) {
        var pt=jdbcTemplate.queryForList("""
            SELECT TOP 1 pt.nha_cung_cap FROM giao_dich_thanh_toan gd
            JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND gd.trang_thai<>N'da_huy' AND ISNULL(gd.xoa_mem,0)=0 ORDER BY gd.id DESC
            """,id);
        if(!pt.isEmpty()) return "COD".equals(pt.get(0).get("nha_cung_cap"));
        return !String.valueOf(hd.get("ghi_chu")).contains("QR");
    }
    private void kiemTraAdmin(int id) {
        if(jdbcTemplate.queryForList("""
            SELECT nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
            WHERE nv.id=? AND nv.trang_thai=1 AND ISNULL(nv.xoa_mem,0)=0
              AND q.ten_quyen_han=N'Quản trị viên' AND q.trang_thai=1 AND ISNULL(q.xoa_mem,0)=0
            """,id).isEmpty()) throw new IllegalArgumentException("Chỉ quản trị viên được thực hiện thao tác này.");
    }
    @Transactional
    public void xacNhanDonCod(int id,int admin) {
        kiemTraAdmin(admin);
        var ds=jdbcTemplate.queryForList("SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty() || ((Number)ds.get(0).get("trang_thai_hien_tai")).intValue()!=1 || !laCod(id,ds.get(0)))
            throw new IllegalArgumentException("Chỉ xác nhận được đơn COD đang chờ xác nhận.");
        if(jdbcTemplate.queryForList("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=? AND so_luong>0 AND ISNULL(xoa_mem,0)=0",id).isEmpty())
            throw new IllegalArgumentException("Đơn chưa có sản phẩm.");
        jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=2,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND trang_thai_hien_tai=1",admin,id);
        ghiLichSuAdmin(id,2,"Quản trị viên xác nhận đơn COD. Đơn chờ chuẩn bị hàng.",admin);
    }

    @Transactional
    public void batDauChuanBi(int id,int admin) {
        kiemTraAdmin(admin);
        int dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=3,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND loai_don IN (1,2) AND trang_thai_hien_tai=2 AND ISNULL(xoa_mem,0)=0",admin,id);
        if(dong!=1) throw new IllegalArgumentException("Chỉ bắt đầu chuẩn bị được đơn đã xác nhận.");
        ghiLichSuAdmin(id,3,"Cửa hàng bắt đầu chuẩn bị hàng.",admin);
    }

    @Transactional
    public void banGiaoShipper(int id,int admin) {
        kiemTraAdmin(admin);
        int dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=4,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND loai_don IN (1,2) AND trang_thai_hien_tai=3 AND ISNULL(xoa_mem,0)=0",admin,id);
        if(dong!=1) throw new IllegalArgumentException("Chỉ bàn giao shipper được đơn đang chuẩn bị hàng.");
        ghiLichSuAdmin(id,4,"Shipper đã lấy kiện hàng; đơn chuyển sang đang vận chuyển.",admin);
    }
    @Transactional
    public void xacNhanThuTienCod(int id,int admin) {
        kiemTraAdmin(admin);
        var ds=jdbcTemplate.queryForList("SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn.");
        var hd=ds.get(0);int tt=((Number)hd.get("trang_thai_hien_tai")).intValue();
        if(tt!=5 || !laCod(id,hd)) throw new IllegalArgumentException("Chỉ ghi nhận thu tiền COD sau khi khách đã xác nhận nhận hàng.");
        BigDecimal daThu=jdbcTemplate.queryForObject("SELECT ISNULL(SUM(so_tien),0) FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND ISNULL(xoa_mem,0)=0",BigDecimal.class,id);
        BigDecimal conLai=((BigDecimal)hd.get("tong_tien_sau_giam")).subtract(daThu);
        if(conLai.signum()<=0) return;
        var pt=jdbcTemplate.queryForList("SELECT TOP 1 id FROM phuong_thuc_thanh_toan WHERE nha_cung_cap=N'COD' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id");
        if(pt.isEmpty()) throw new IllegalArgumentException("Chưa có phương thức COD đang hoạt động.");
        jdbcTemplate.update("UPDATE giao_dich_thanh_toan SET trang_thai=N'da_huy',thoi_gian_cap_nhat=SYSDATETIME() WHERE id_hoa_don=? AND trang_thai=N'cho_thanh_toan' AND ISNULL(xoa_mem,0)=0",id);
        jdbcTemplate.update("INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,nguoi_cap_nhat,ghi_chu) VALUES (?,?,?,N'thanh_cong',?,N'Đã đối soát tiền thu hộ COD')",id,pt.get(0).get("id"),conLai,admin);
        jdbcTemplate.update("UPDATE hoa_don SET ngay_thanh_toan=SYSDATETIME(),ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=?",admin,id);
        ghiLichSuAdmin(id,tt,"Shipper xác nhận đã thu COD; cửa hàng ghi nhận khoản thu.",admin);
    }
    @Transactional
    public void xacNhanGiaoKhachVangLai(int id,int admin) {
        kiemTraAdmin(admin);
        int dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=5,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND id_khach_hang IS NULL AND loai_don IN (1,2) AND trang_thai_hien_tai=4 AND ISNULL(xoa_mem,0)=0",admin,id);
        if(dong!=1) throw new IllegalArgumentException("Chỉ xác nhận giao thành công đơn khách vãng lai đang vận chuyển.");
        ghiLichSuAdmin(id,5,"Cửa hàng xác nhận đã giao hàng cho khách vãng lai.",admin);
    }
    private void ghiLichSuAdmin(int id,int tt,String ghiChu,int admin) {
        jdbcTemplate.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,nguoi_cap_nhat,nguoi_thuc_hien,loai_nguoi_thuc_hien) VALUES (?,?,?,?,?,'nhan_vien')",id,tt,ghiChu,admin,admin);
        baoChuong(id);
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
        if (trangThai != 1) throw new IllegalArgumentException("Chỉ xác nhận thanh toán và đơn hàng đang chờ xác nhận.");
        if(laCod(idHoaDon,hd)) throw new IllegalArgumentException("Đơn COD phải xác nhận đơn trước; chỉ ghi nhận thanh toán sau khi thu tiền.");
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
                UPDATE hoa_don SET trang_thai_hien_tai=2,ngay_thanh_toan=COALESCE(ngay_thanh_toan,SYSDATETIME()),
                    ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=?
                WHERE id=? AND trang_thai_hien_tai=1 AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """, idAdmin,idHoaDon);
        if (dong != 1) throw new IllegalArgumentException("Trạng thái đơn đã thay đổi. Vui lòng tải lại trang.");
        jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (id_hoa_don,trang_thai,ghi_chu,nguoi_cap_nhat,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,2,N'Quản trị viên xác nhận thanh toán và xác nhận đơn hàng.',?,?,'nhan_vien')
                """, idHoaDon,idAdmin,idAdmin);
        baoChuong(idHoaDon);
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
        baoChuong(idHoaDon);
    }

    @Transactional
    public void huyDonCuaKhach(int idHoaDon,int idKhachHang) {
        kiemTraKhachHang(idKhachHang);
        huyDonChuaXacNhan(idHoaDon,idKhachHang,null);
    }

    public Map<String,Object> layDonVangLai(int id,boolean dungPhien) {
        if(!dungPhien) return null;
        var hd=layHoaDon(id);
        if(hd==null || hd.get("id_khach_hang")!=null || ((Number)hd.get("loai_don")).intValue()==0) return null;
        return hd;
    }
    public Integer traCuuDon(String ma,String sdt) {
        if(ma==null || !ma.trim().toUpperCase().matches("HD[0-9]{5}") || sdt==null || !sdt.matches("0[0-9]{9}")) return null;
        var ds=jdbcTemplate.queryForList("SELECT id FROM hoa_don WHERE ma_hoa_don=? AND so_dien_thoai_khach_hang=? AND id_khach_hang IS NULL AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",ma.trim().toUpperCase(),sdt);
        return ds.size()==1?((Number)ds.get(0).get("id")).intValue():null;
    }
    @Transactional
    public void huyDonVangLai(int id,boolean dungPhien) {
        if(!dungPhien) throw new IllegalArgumentException("Bạn không có quyền xử lý đơn hàng này.");
        huyDonChuaXacNhan(id,null,null);
    }
    @Transactional
    public void nhanDonVangLai(int id,boolean dungPhien) {
        if(!dungPhien) throw new IllegalArgumentException("Bạn không có quyền xử lý đơn hàng này.");
        int dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=5,ngay_cap_nhat=SYSDATETIME() WHERE id=? AND id_khach_hang IS NULL AND loai_don IN (1,2) AND trang_thai_hien_tai=4 AND ISNULL(xoa_mem,0)=0",id);
        if(dong!=1) throw new IllegalArgumentException("Đơn hàng phải đang vận chuyển.");
        jdbcTemplate.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,loai_nguoi_thuc_hien) VALUES (?,5,N'Khách vãng lai xác nhận đã nhận hàng.','khach_hang')",id);
        baoChuong(id);
    }
    @Transactional
    public void chuyenQrSangCod(int id,Integer kh,boolean dungPhien) {
        if(kh==null && !dungPhien) throw new IllegalArgumentException("Vui lòng xác minh đơn hàng trước.");
        if(kh!=null) kiemTraKhachHang(kh);
        var ds=jdbcTemplate.queryForList("SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn.");
        var hd=ds.get(0);
        if(!(kh!=null && kh.equals(hd.get("id_khach_hang"))) && !(dungPhien && hd.get("id_khach_hang")==null))
            throw new IllegalArgumentException("Bạn không có quyền xử lý đơn hàng này.");
        if(((Number)hd.get("trang_thai_hien_tai")).intValue()!=1 || laCod(id,hd))
            throw new IllegalArgumentException("Chỉ chuyển COD khi đơn QR chưa được xác nhận.");
        if(!jdbcTemplate.queryForList("SELECT id FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND ISNULL(xoa_mem,0)=0",id).isEmpty())
            throw new IllegalArgumentException("Đơn đã được ghi nhận thanh toán. Vui lòng liên hệ cửa hàng.");
        var pt=jdbcTemplate.queryForList("SELECT TOP 1 id FROM phuong_thuc_thanh_toan WHERE nha_cung_cap=N'COD' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id");
        if(pt.isEmpty()) throw new IllegalArgumentException("Chưa có phương thức COD.");
        jdbcTemplate.update("UPDATE giao_dich_thanh_toan SET trang_thai=N'da_huy',thoi_gian_cap_nhat=SYSDATETIME() WHERE id_hoa_don=? AND trang_thai IN (N'cho_thanh_toan',N'khoi_tao') AND ISNULL(xoa_mem,0)=0",id);
        jdbcTemplate.update("INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,ghi_chu) VALUES (?,?,?,N'cho_thanh_toan',N'Khách chuyển sang thanh toán khi nhận hàng')",id,pt.get(0).get("id"),hd.get("tong_tien_sau_giam"));
        jdbcTemplate.update("UPDATE hoa_don SET ghi_chu=N'Thanh toán khi nhận hàng (COD)',ngay_cap_nhat=SYSDATETIME() WHERE id=?",id);
        jdbcTemplate.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,nguoi_thuc_hien,loai_nguoi_thuc_hien) VALUES (?,1,N'Khách đổi từ QR sang COD.',?,'khach_hang')",id,kh);
        baoChuong(id);
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
        else if(idKhachHang==null) ds=jdbcTemplate.queryForList("SELECT trang_thai_hien_tai,id_phieu_giam_gia FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND id_khach_hang IS NULL AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",idHoaDon);
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
        else if(idKhachHang==null) dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=6,ngay_cap_nhat=SYSDATETIME() WHERE id=? AND id_khach_hang IS NULL AND trang_thai_hien_tai=1 AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",idHoaDon);
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
        baoChuong(idHoaDon);
    }
}
