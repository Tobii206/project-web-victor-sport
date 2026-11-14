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
    private GiaoHangGhnService giaoHangGhn;
    @org.springframework.beans.factory.annotation.Autowired
    private ChuongTrucTuyenService chuongTrucTuyen;
    private void baoChuong(int id) { if(chuongTrucTuyen!=null) chuongTrucTuyen.donThayDoi(id); }

    private final JdbcTemplate jdbcTemplate;

    public HoaDonService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> layDanhSachHoaDon(String tuKhoa) {
        return layDanhSachHoaDon(tuKhoa,null,"","",null,null);
    }

    public List<Map<String,Object>> layDanhSachHoaDon(String tuKhoa,Integer trangThai,String loaiDon,String thanhToan,
                                                     java.time.LocalDate tuNgay,java.time.LocalDate denNgay) {
        return docDanhSachHoaDon(locHoaDon(tuKhoa,trangThai,loaiDon,thanhToan,tuNgay,denNgay),null);
    }

    private record LocHoaDon(String sql,List<Object> args) {}

    @Transactional(readOnly=true)
    public Map<String,Object> layTrangHoaDon(String tuKhoa,Integer trangThai,String loaiDon,String thanhToan,
                                             java.time.LocalDate tuNgay,java.time.LocalDate denNgay,int trang) {
        var loc=locHoaDon(tuKhoa,trangThai,loaiDon,thanhToan,tuNgay,denNgay);
        Integer dem=jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hoa_don"+loc.sql(),Integer.class,loc.args().toArray());
        int tong=dem==null?0:dem,soTrang=(int)Math.max(1,(tong+9L)/10);
        int hienTai=Math.max(1,Math.min(trang,soTrang));
        int batDau=Math.max(1,Math.min(hienTai-2,soTrang-4));
        return Map.of("danhSachHoaDon",docDanhSachHoaDon(loc,hienTai),"trang",hienTai,"soTrang",soTrang,"tongHoaDon",tong,
                "tuDong",tong==0?0:(hienTai-1)*10+1,"denDong",Math.min(hienTai*10L,tong),
                "trangBatDau",batDau,"trangKetThuc",Math.min(soTrang,batDau+4));
    }

    private LocHoaDon locHoaDon(String tuKhoa,Integer trangThai,String loaiDon,String thanhToan,
                                java.time.LocalDate tuNgay,java.time.LocalDate denNgay) {
        if(trangThai!=null && (trangThai<0 || trangThai>6)) throw new IllegalArgumentException("Trạng thái hóa đơn không hợp lệ.");
        loaiDon=loaiDon==null?"":loaiDon;thanhToan=thanhToan==null?"":thanhToan;
        if(!java.util.Set.of("","online","tai-quay").contains(loaiDon) || !java.util.Set.of("","da-thanh-toan","chua-thanh-toan").contains(thanhToan))
            throw new IllegalArgumentException("Bộ lọc hóa đơn không hợp lệ.");
        if(tuNgay!=null && denNgay!=null && tuNgay.isAfter(denNgay)) throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        String tuKhoaTimKiem = "%" + tuKhoa.trim() + "%";
        String sql=" WHERE ISNULL(xoa_mem,0)=0 AND (ma_hoa_don LIKE ? OR ten_khach_hang COLLATE Vietnamese_100_CI_AI LIKE ? OR so_dien_thoai_khach_hang LIKE ?)";
        var args=new java.util.ArrayList<Object>(List.of(tuKhoaTimKiem,tuKhoaTimKiem,tuKhoaTimKiem));
        if(trangThai!=null) {sql+=" AND trang_thai_hien_tai=?";args.add(trangThai);}
        if("online".equals(loaiDon)) sql+=" AND loai_don=1";
        else if("tai-quay".equals(loaiDon)) sql+=" AND loai_don IN (0,2)";
        if(tuNgay!=null) {sql+=" AND ngay_tao>=?";args.add(tuNgay.atStartOfDay());}
        if(denNgay!=null) {sql+=" AND ngay_tao<?";args.add(denNgay.plusDays(1).atStartOfDay());}
        if(!thanhToan.isEmpty()) {
            sql+=" AND (SELECT ISNULL(SUM(gd.so_tien),0) FROM giao_dich_thanh_toan gd WHERE gd.id_hoa_don=hoa_don.id"
                    +" AND gd.trang_thai=N'thanh_cong' AND ISNULL(gd.xoa_mem,0)=0)"
                    +("da-thanh-toan".equals(thanhToan)?">=":"<")+"ISNULL(tong_tien_sau_giam,0)";
        }
        return new LocHoaDon(sql,args);
    }

    private List<Map<String,Object>> docDanhSachHoaDon(LocHoaDon loc,Integer trang) {
        String sql = """
                SELECT id, id_nhan_vien, id_khach_hang, loai_don, ma_hoa_don, ten_khach_hang, so_dien_thoai_khach_hang,
                       ngay_tao, ngay_thanh_toan, tong_tien_sau_giam, trang_thai_hien_tai,
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
                """;
        sql+=loc.sql()+" ORDER BY COALESCE(ngay_thanh_toan,ngay_tao) DESC,id DESC";
        var args=new java.util.ArrayList<>(loc.args());
        if(trang!=null) {sql+=" OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY";args.add((trang-1)*10);}
        var danhSach=jdbcTemplate.queryForList(sql,args.toArray());
        for(var hd:danhSach) themThanhToan(hd);
        return danhSach;
    }

    public Map<String, Object> layHoaDon(Integer id) {
        String sql = """
                SELECT hd.*, nv.ten_nhan_vien,
                       COALESCE(NULLIF(LTRIM(RTRIM(hd.email_khach_hang)), ''),
                                NULLIF(LTRIM(RTRIM(kh.email)), '')) AS email_hien_thi,
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
                LEFT JOIN khach_hang kh ON kh.id = hd.id_khach_hang AND ISNULL(kh.xoa_mem, 0) = 0
                WHERE hd.id = ? AND ISNULL(hd.xoa_mem, 0) = 0
                """;
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList(sql, id);
        if (danhSach.isEmpty()) {
            return null;
        }
        var hd=danhSach.get(0);themThanhToan(hd);
        if (((Number) hd.get("loai_don")).intValue() != 1 && Boolean.TRUE.equals(hd.get("da_thanh_toan"))) {
            var thanhToan = jdbcTemplate.queryForList("""
                SELECT TOP 1 pt.ten_phuong_thuc_thanh_toan, gd.ghi_chu
                FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
                WHERE gd.id_hoa_don=? AND gd.trang_thai=N'thanh_cong' AND ISNULL(gd.xoa_mem,0)=0
                ORDER BY gd.id DESC
                """, id);
            if (!thanhToan.isEmpty()) {
                hd.put("phuong_thuc", thanhToan.get(0).get("ten_phuong_thuc_thanh_toan"));
                String ghiChu = java.util.Objects.toString(thanhToan.get(0).get("ghi_chu"), "");
                if (ghiChu.startsWith("Thanh toán tại quầy. Tiền khách đưa: "))
                    hd.put("ghi_chu_tien_mat", ghiChu.substring("Thanh toán tại quầy. ".length()));
            }
        }
        var vd=jdbcTemplate.queryForList("SELECT ma_van_don,trang_thai,phi_thuc_te,ngay_cap_nhat,ten_tinh,ten_xa FROM van_don_ghn WHERE id_hoa_don=?",id);
        if(!vd.isEmpty()) hd.put("vanDonGhn",vd.get(0));
        String diaChi = java.util.Objects.toString(hd.get("dia_chi_khach_hang"), "");
        if (((Number) hd.get("loai_don")).intValue() == 0 && "Mua trực tiếp tại cửa hàng Victor Sport".equals(diaChi)) {
            diaChi = BanHangTaiQuayService.DIA_CHI_CUA_HANG;
            hd.put("dia_chi_khach_hang", diaChi);
        }
        if (!vd.isEmpty()) {
            String phanKhuVuc = ", " + vd.get(0).get("ten_xa") + ", " + vd.get(0).get("ten_tinh");
            if (diaChi.endsWith(phanKhuVuc)) diaChi = diaChi.substring(0, diaChi.length() - phanKhuVuc.length());
        }
        hd.put("dia_chi_chi_tiet", diaChi);
        return hd;
    }

    @Transactional
    public void suaSoLuongDonCod(int id, int idDong, int soLuongCu, int soLuongMoi, int nhanVien) {
        if (soLuongMoi < 1) throw new IllegalArgumentException("Số lượng phải từ 1 trở lên.");
        var danhSach = jdbcTemplate.queryForList("""
            SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
            WHERE id=? AND loai_don=1 AND trang_thai_hien_tai BETWEEN 1 AND 3 AND ISNULL(xoa_mem,0)=0
            """, id);
        if (danhSach.isEmpty()) throw new IllegalArgumentException("Đơn đã chuyển sang bước giao hàng hoặc đã hủy.");
        var don = danhSach.get(0);
        themThanhToan(don);
        if (!"COD".equals(don.get("phuong_thuc")) || Boolean.TRUE.equals(don.get("co_thanh_toan")) || don.get("ngay_thanh_toan") != null
                || don.get("id_phieu_giam_gia") != null || ((BigDecimal) don.get("tong_tien_giam")).signum() != 0)
            throw new IllegalArgumentException("Chỉ sửa số lượng cho đơn COD chưa thu tiền và không có giảm giá.");
        var vanDon = jdbcTemplate.queryForList("""
            SELECT ma_van_don FROM van_don_ghn WITH (UPDLOCK,HOLDLOCK) WHERE id_hoa_don=?
            """, id);
        if (!vanDon.isEmpty() && vanDon.get(0).get("ma_van_don") != null)
            throw new IllegalArgumentException("Đơn đã có mã vận đơn.");
        var dong = jdbcTemplate.queryForList("""
            SELECT id_chi_tiet_san_pham,so_luong FROM hoa_don_chi_tiet
            WHERE id=? AND id_hoa_don=? AND ISNULL(xoa_mem,0)=0
            """, idDong, id);
        if (dong.isEmpty() || ((Number) dong.get(0).get("so_luong")).intValue() != soLuongCu)
            throw new IllegalArgumentException("Số lượng đã thay đổi. Vui lòng tải lại hóa đơn.");
        int chenh = soLuongMoi - soLuongCu;
        if (chenh == 0) return;
        int soDong = jdbcTemplate.update("""
            UPDATE ct SET so_luong=ct.so_luong-?,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME()
            FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id=ct.id_san_pham
            WHERE ct.id=? AND CAST(ct.so_luong AS BIGINT)-? BETWEEN 0 AND 2147483647
                AND (?<0 OR (ISNULL(ct.xoa_mem,0)=0 AND ISNULL(ct.trang_thai,1)=1
                    AND ISNULL(sp.xoa_mem,0)=0 AND ISNULL(sp.trang_thai_kinh_doanh,1)=1))
            """, chenh, nhanVien, dong.get(0).get("id_chi_tiet_san_pham"), chenh, chenh);
        if (soDong != 1) throw new IllegalArgumentException("Mẫu đã ngừng bán hoặc không đủ tồn kho.");
        jdbcTemplate.update("UPDATE hoa_don_chi_tiet SET so_luong=? WHERE id=? AND id_hoa_don=?", soLuongMoi, idDong, id);
        var tong = jdbcTemplate.queryForMap("""
            SELECT SUM(thanh_tien) AS tien_hang, SUM(CAST(so_luong AS BIGINT)) AS so_doi
            FROM hoa_don_chi_tiet WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0
            """, id);
        if (!vanDon.isEmpty()) giaoHangGhn.capNhatKichThuocKien(id, ((Number) tong.get("so_doi")).longValue());
        BigDecimal tienHang = (BigDecimal) tong.get("tien_hang");
        BigDecimal phi = don.get("phi_van_chuyen") instanceof BigDecimal tien ? tien : BigDecimal.ZERO;
        BigDecimal canThu = tienHang.add(phi);
        if (canThu.precision() - canThu.scale() > 16)
            throw new IllegalArgumentException("Tổng tiền mới vượt giới hạn lưu hóa đơn.");
        if (!vanDon.isEmpty() && (canThu.stripTrailingZeros().scale() > 0 || canThu.compareTo(new BigDecimal("50000000")) > 0))
            throw new IllegalArgumentException("Số tiền thu hộ GHN phải là số đồng nguyên và tối đa 50 triệu đồng.");
        jdbcTemplate.update("""
            UPDATE hoa_don SET tong_tien=?,tong_tien_sau_giam=?,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME() WHERE id=?
            """, tienHang, canThu, nhanVien, id);
        jdbcTemplate.update("""
            UPDATE gd SET so_tien=? FROM giao_dich_thanh_toan gd
            JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND pt.nha_cung_cap=N'COD' AND gd.trang_thai=N'cho_thanh_toan' AND ISNULL(gd.xoa_mem,0)=0
            """, canThu, id);
        ghiLichSuAdmin(id, ((Number) don.get("trang_thai_hien_tai")).intValue(),
                "Cập nhật số lượng mẫu từ " + soLuongCu + " thành " + soLuongMoi + ".", nhanVien);
        baoChuong(id);
    }

    @Transactional
    public void suaDiaChiGiaoHang(int id, int nhanVien, String ten, String sdt, String diaChi, Integer tinh, Integer xa) {
        suaDiaChiGiaoHang(id, nhanVien, null, ten, sdt, diaChi, tinh, xa);
    }

    @Transactional
    public void suaDiaChiCuaKhach(int id, int khachHang, String ten, String sdt, String diaChi, Integer tinh, Integer xa) {
        kiemTraKhachHang(khachHang);
        suaDiaChiGiaoHang(id, null, khachHang, ten, sdt, diaChi, tinh, xa);
    }

    private void suaDiaChiGiaoHang(int id, Integer nhanVien, Integer khachHang, String ten, String sdt, String diaChi, Integer tinh, Integer xa) {
        ten = ten.trim();
        sdt = sdt.trim();
        diaChi = diaChi.trim();
        if (ten.isEmpty() || ten.length() > 100 || !sdt.matches("0[0-9]{9}") || diaChi.isEmpty())
            throw new IllegalArgumentException("Nhập tên người nhận, số điện thoại 10 chữ số và địa chỉ giao hàng.");
        if ((tinh == null) != (xa == null))
            throw new IllegalArgumentException("Khi đổi khu vực, vui lòng chọn đủ tỉnh và phường/xã.");
        String locDon = """
            SELECT trang_thai_hien_tai FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
            WHERE id=? AND loai_don IN (1,2) AND trang_thai_hien_tai IN (1,2,3) AND ISNULL(xoa_mem,0)=0
            """;
        var don = khachHang == null ? jdbcTemplate.queryForList(locDon, id)
                : jdbcTemplate.queryForList(locDon + " AND id_khach_hang=?", id, khachHang);
        if (don.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn có thể sửa thông tin nhận hàng.");
        var vanDon = jdbcTemplate.queryForList("""
            SELECT ma_van_don,ten_tinh,ten_xa,phi_bao_gia FROM van_don_ghn WITH (UPDLOCK,HOLDLOCK) WHERE id_hoa_don=?
            """, id);
        if (!vanDon.isEmpty()) {
            var thongTin = vanDon.get(0);
            if (thongTin.get("ma_van_don") != null) throw new IllegalArgumentException("Đơn đã có vận đơn GHN.");
            String tenTinh = (String) thongTin.get("ten_tinh");
            String tenXa = (String) thongTin.get("ten_xa");
            if (tinh != null) {
                var khuVuc = giaoHangGhn.layKhuVuc(tinh, xa);
                if (khachHang != null && (!khuVuc.tinh().equals(tenTinh) || !khuVuc.xa().equals(tenXa))) {
                    kiemTraPhiKhiKhachDoiKhuVuc(id, ten, sdt, diaChi, tinh, xa, thongTin, khuVuc.tinh());
                }
                tenTinh = khuVuc.tinh();
                tenXa = khuVuc.xa();
                jdbcTemplate.update("""
                    UPDATE van_don_ghn SET ten_tinh=?,ten_xa=?,ngay_cap_nhat=SYSDATETIME()
                    WHERE id_hoa_don=? AND ma_van_don IS NULL
                    """, tenTinh, tenXa, id);
            }
            diaChi += ", " + tenXa + ", " + tenTinh;
        } else if (tinh != null) {
            throw new IllegalArgumentException("Đơn này dùng địa chỉ giao hàng nhập trực tiếp.");
        }
        if (diaChi.length() > 255) throw new IllegalArgumentException("Địa chỉ đầy đủ tối đa 255 ký tự.");
        jdbcTemplate.update("""
            UPDATE hoa_don SET ten_khach_hang=?,so_dien_thoai_khach_hang=?,dia_chi_khach_hang=?,
                nguoi_cap_nhat=COALESCE(?,nguoi_cap_nhat),ngay_cap_nhat=SYSDATETIME() WHERE id=?
            """, ten, sdt, diaChi, nhanVien, id);
        int trangThai = ((Number) don.get(0).get("trang_thai_hien_tai")).intValue();
        if (khachHang == null) {
            ghiLichSuAdmin(id, trangThai, "Quản trị viên cập nhật địa chỉ giao hàng.", nhanVien);
        } else {
            jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,?,N'Khách hàng cập nhật thông tin nhận hàng.',?,'khach_hang')
                """, id, trangThai, khachHang);
        }
        baoChuong(id);
    }

    private void kiemTraPhiKhiKhachDoiKhuVuc(int id, String ten, String sdt, String diaChi, int tinh, int xa,
                                          Map<String, Object> vanDon, String tinhMoi) {
        var don = jdbcTemplate.queryForMap("SELECT tong_tien,phi_van_chuyen,ghi_chu FROM hoa_don WHERE id=?", id);
        BigDecimal phiCu = (BigDecimal) vanDon.get("phi_bao_gia");
        BigDecimal phiDaChot = (BigDecimal) don.get("phi_van_chuyen");
        String baoLoi = "Địa chỉ mới có thể làm thay đổi phí hoặc ưu đãi vận chuyển. Vui lòng liên hệ cửa hàng để cập nhật.";
        if (phiCu == null || phiDaChot == null
                || !tinhMoi.equals(vanDon.get("ten_tinh")) && phiDaChot.compareTo(phiCu) < 0)
            throw new IllegalArgumentException(baoLoi);
        var gio = new java.util.LinkedHashMap<Integer, Integer>();
        for (var dong : jdbcTemplate.queryForList("""
            SELECT id_chi_tiet_san_pham,SUM(so_luong) AS so_luong FROM hoa_don_chi_tiet
            WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0 GROUP BY id_chi_tiet_san_pham
            """, id)) {
            gio.put(((Number) dong.get("id_chi_tiet_san_pham")).intValue(), ((Number) dong.get("so_luong")).intValue());
        }
        var baoGia = giaoHangGhn.baoGia(gio, ten, sdt, diaChi, laCod(id, don) ? "COD" : "QR",
                (BigDecimal) don.get("tong_tien"), tinh, xa);
        if (phiCu.compareTo(baoGia.phi()) != 0) throw new IllegalArgumentException(baoLoi);
    }

    private void themThanhToan(Map<String,Object> hd) {
        var ds=jdbcTemplate.queryForList("""
            SELECT ISNULL(SUM(CASE WHEN trang_thai=N'thanh_cong' THEN so_tien ELSE 0 END),0) AS da_thu
            FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0
            """,hd.get("id"));
        BigDecimal daThu=ds.isEmpty()?BigDecimal.ZERO:(BigDecimal)ds.get(0).get("da_thu");
        hd.put("vnpay",laVnpay(((Number)hd.get("id")).intValue()));
        hd.put("qr_demo",laQrDemo(((Number)hd.get("id")).intValue()));
        hd.put("da_thu",daThu);hd.put("phuong_thuc",((Number)hd.get("loai_don")).intValue()!=1?"Tại quầy":(laCod(((Number)hd.get("id")).intValue(),hd)?"COD":(Boolean.TRUE.equals(hd.get("qr_demo"))?"Thanh toán QR":(Boolean.TRUE.equals(hd.get("vnpay"))?"VNPAY Sandbox":"QR / chuyển khoản"))));
        BigDecimal canThu = (BigDecimal) hd.get("tong_tien_sau_giam");
        hd.put("da_thanh_toan", daThu.compareTo(canThu) >= 0 && (canThu.signum() > 0 || hd.get("ngay_thanh_toan") != null));
        hd.put("co_thanh_toan",daThu.signum()>0);
        if(Boolean.TRUE.equals(hd.get("qr_demo")) && hd.get("ghi_chu") instanceof String note
                && (note.contains("QR DEMO") || note.equals("QR mô phỏng") || note.equals("Chờ thanh toán online")))
            hd.put("ghi_chu",Boolean.TRUE.equals(hd.get("da_thanh_toan"))?"Đã thanh toán online":"Chờ thanh toán online");
    }
    private boolean laCod(int id,Map<String,Object> hd) {
        var pt=jdbcTemplate.queryForList("""
            SELECT TOP 1 pt.nha_cung_cap FROM giao_dich_thanh_toan gd
            JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND (gd.trang_thai<>N'da_huy' OR pt.nha_cung_cap IN (N'VNPAY_SANDBOX',N'QR_DEMO')) AND ISNULL(gd.xoa_mem,0)=0 ORDER BY gd.id DESC
            """,id);
        if(!pt.isEmpty()) return "COD".equals(pt.get(0).get("nha_cung_cap"));
        return !String.valueOf(hd.get("ghi_chu")).contains("QR");
    }
    private boolean laVnpay(int id) {
        return !jdbcTemplate.queryForList("""
            SELECT gd.id FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND pt.nha_cung_cap=N'VNPAY_SANDBOX' AND ISNULL(gd.xoa_mem,0)=0
            """,id).isEmpty();
    }
    private boolean laQrDemo(int id) {
        return !jdbcTemplate.queryForList("""
            SELECT gd.id FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND pt.nha_cung_cap=N'QR_DEMO' AND ISNULL(gd.xoa_mem,0)=0
            """,id).isEmpty();
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
        var ds=jdbcTemplate.queryForList("SELECT trang_thai_hien_tai FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty() || ((Number)ds.get(0).get("trang_thai_hien_tai")).intValue()!=3)
            throw new IllegalArgumentException("Chỉ gửi được đơn đang chuẩn bị hàng.");
        if(!jdbcTemplate.queryForList("SELECT id_hoa_don FROM van_don_ghn WHERE id_hoa_don=?",id).isEmpty())
            giaoHangGhn.taoVanDon(id);
        int dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=4,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND loai_don IN (1,2) AND trang_thai_hien_tai=3 AND ISNULL(xoa_mem,0)=0",admin,id);
        if(dong!=1) throw new IllegalArgumentException("Chỉ bàn giao shipper được đơn đang chuẩn bị hàng.");
        ghiLichSuAdmin(id,4,"Đã gửi hàng cho shipper.",admin);
        ghiLichSuAdmin(id,4,"Đơn hàng đang vận chuyển.",admin);
    }
    @Transactional
    public void xacNhanThuTienCod(int id,int admin) {
        kiemTraAdmin(admin);
        ghiNhanCodKhiNhanHang(id,admin);
    }
    @Transactional
    public boolean dongBoCodDonDaNhan(int id) {
        boolean daGhi=ghiNhanCodKhiNhanHang(id,null);
        if(daGhi) {
            jdbcTemplate.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,loai_nguoi_thuc_hien) VALUES (?,5,N'Đồng bộ thanh toán COD cho đơn đã nhận hàng theo luồng hoàn tất đơn.','he_thong')",id);
            baoChuong(id);
        }
        return daGhi;
    }
    private boolean ghiNhanCodKhiNhanHang(int id,Integer admin) {
        var ds=jdbcTemplate.queryForList("SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn.");
        var hd=ds.get(0);int tt=((Number)hd.get("trang_thai_hien_tai")).intValue();
        if(tt!=5) throw new IllegalArgumentException("Chỉ ghi nhận thanh toán COD khi đã xác nhận nhận hàng.");
        if(!laCod(id,hd)) return false;
        BigDecimal daThu=jdbcTemplate.queryForObject("SELECT ISNULL(SUM(so_tien),0) FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND ISNULL(xoa_mem,0)=0",BigDecimal.class,id);
        BigDecimal conLai=((BigDecimal)hd.get("tong_tien_sau_giam")).subtract(daThu);
        if(conLai.signum()<=0) return false;
        var pt=jdbcTemplate.queryForList("SELECT TOP 1 id FROM phuong_thuc_thanh_toan WHERE nha_cung_cap=N'COD' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id");
        if(pt.isEmpty()) throw new IllegalArgumentException("Chưa có phương thức COD đang hoạt động.");
        jdbcTemplate.update("UPDATE giao_dich_thanh_toan SET trang_thai=N'da_huy',thoi_gian_cap_nhat=SYSDATETIME() WHERE id_hoa_don=? AND trang_thai=N'cho_thanh_toan' AND ISNULL(xoa_mem,0)=0",id);
        jdbcTemplate.update("INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,nguoi_cap_nhat,ghi_chu) VALUES (?,?,?,N'thanh_cong',?,N'Xác nhận đã nhận hàng và đã trả tiền COD')",id,pt.get(0).get("id"),conLai,admin);
        jdbcTemplate.update("UPDATE hoa_don SET ngay_thanh_toan=SYSDATETIME(),ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=COALESCE(?,nguoi_cap_nhat) WHERE id=?",admin,id);
        return true;
    }
    @Transactional
    public void xacNhanGiaoKhachVangLai(int id,int admin) {
        kiemTraAdmin(admin);
        int dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=5,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND (id_khach_hang IS NULL OR loai_don=2) AND loai_don IN (1,2) AND trang_thai_hien_tai=4 AND ISNULL(xoa_mem,0)=0",admin,id);
        if(dong!=1) throw new IllegalArgumentException("Chỉ xác nhận hộ đơn khách vãng lai hoặc đơn tại quầy đang vận chuyển.");
        ghiNhanCodKhiNhanHang(id,admin);
        ghiLichSuAdmin(id,5,"Quản trị viên xác nhận khách đã nhận hàng; nếu thanh toán COD, đã trả đủ tiền COD.",admin);
    }

    private void ghiLichSuAdmin(int id,int tt,String ghiChu,int admin) {
        jdbcTemplate.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,nguoi_cap_nhat,nguoi_thuc_hien,loai_nguoi_thuc_hien) VALUES (?,?,?,?,?,'nhan_vien')",id,tt,ghiChu,admin,admin);
        baoChuong(id);
    }

    public List<Map<String, Object>> laySanPhamTrongHoaDon(Integer idHoaDon) {
        String sql = """
                SELECT ct.doi_tuong, CASE ct.doi_tuong WHEN 'NAM' THEN N'Nam' WHEN 'NU' THEN N'Nữ' ELSE N'Unisex' END AS ten_gioi_tinh, sp.ma_san_pham, sp.ten_san_pham, ms.ten_mau_sac, kt.ten_kich_thuoc,
                       hdct.id AS id_dong, hdct.so_luong, hdct.don_gia, hdct.thanh_tien
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
        var lichSu=jdbcTemplate.queryForList("""
                SELECT trang_thai,thoi_gian,ghi_chu FROM lich_su_hoa_don
                WHERE id_hoa_don=? AND ISNULL(xoa_mem,0)=0 ORDER BY thoi_gian,id
                """, idHoaDon);
        var ketQua=new java.util.ArrayList<Map<String,Object>>();
        for(var buoc:lichSu) {
            if(buoc.get("ghi_chu") instanceof String note && note.contains("QR DEMO"))
                buoc.put("ghi_chu",note.replace("QR DEMO","Thanh toán QR").replace("tự động giả lập đã thanh toán, không thu tiền thật.","đã xác nhận thanh toán."));
            if("Shipper đã lấy kiện hàng; đơn chuyển sang đang vận chuyển.".equals(buoc.get("ghi_chu"))) {
                // Tách cách hiển thị sự kiện cũ, giữ nguyên thời điểm đã ghi nhận.
                for(String nhan:List.of("Đã gửi hàng cho shipper.","Đơn hàng đang vận chuyển.")) {
                    var tach=new java.util.HashMap<>(buoc);tach.put("ghi_chu",nhan);ketQua.add(tach);
                }
            } else ketQua.add(buoc);
        }
        return ketQua;
    }

    private void kiemTraKhachHang(int idKhachHang) {
        if (jdbcTemplate.queryForList("""
                SELECT id FROM khach_hang WHERE id=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1
                """, idKhachHang).isEmpty())
            throw new IllegalArgumentException("Tài khoản khách hàng không còn hoạt động. Vui lòng đăng nhập lại.");
    }

    public List<Map<String,Object>> layDonCuaKhach(int idKhachHang) {
        return layDonCuaKhach(idKhachHang, "");
    }

    public List<Map<String,Object>> layDonCuaKhach(int idKhachHang,String tuKhoa) {
        kiemTraKhachHang(idKhachHang);
        String ma=tuKhoa==null?"":tuKhoa.trim().toUpperCase(java.util.Locale.ROOT);
        if(!ma.isEmpty() && !ma.matches("[A-Z0-9_-]{1,50}")) return List.of();
        String sql="""
                SELECT id,ma_hoa_don,ngay_tao,tong_tien_sau_giam,trang_thai_hien_tai,
                    CAST(CASE WHEN EXISTS (SELECT 1 FROM giao_dich_thanh_toan gd WHERE gd.id_hoa_don=hoa_don.id
                        AND gd.trang_thai=N'thanh_cong' AND gd.so_tien>0 AND ISNULL(gd.xoa_mem,0)=0) THEN 1 ELSE 0 END AS BIT) AS co_thanh_toan,
                    (SELECT TOP 1 vd.ma_van_don FROM van_don_ghn vd WHERE vd.id_hoa_don=hoa_don.id) AS ma_van_don,
                    CASE WHEN trang_thai_hien_tai=1 THEN N'Chờ xác nhận'
                         WHEN trang_thai_hien_tai=2 THEN N'Đã xác nhận đơn'
                         WHEN trang_thai_hien_tai=3 THEN N'Đang chuẩn bị hàng'
                         WHEN trang_thai_hien_tai=4 THEN N'Đang vận chuyển'
                         WHEN trang_thai_hien_tai=5 THEN N'Đã nhận hàng'
                         WHEN trang_thai_hien_tai=6 THEN N'Đã hủy'
                         ELSE N'Đơn không còn xử lý' END AS ten_trang_thai
                FROM hoa_don WHERE id_khach_hang=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
                """;
        String sapXep = " ORDER BY COALESCE(ngay_thanh_toan, ngay_tao) DESC, id DESC";
        if (ma.isEmpty()) return jdbcTemplate.queryForList(sql + sapXep, idKhachHang);
        return jdbcTemplate.queryForList(sql + """
                AND (ma_hoa_don=? OR EXISTS (SELECT 1 FROM van_don_ghn vd WHERE vd.id_hoa_don=hoa_don.id AND vd.ma_van_don=?))
                """ + sapXep, idKhachHang, ma, ma);
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
        if((laVnpay(idHoaDon) || laQrDemo(idHoaDon)) && conLai.signum()>0)
            throw new IllegalArgumentException("Đơn chưa có xác nhận thanh toán tự động. Không được ghi nhận tiền thủ công.");
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
        ghiNhanCodKhiNhanHang(idHoaDon,null);
        jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (id_hoa_don,trang_thai,ghi_chu,nguoi_thuc_hien,loai_nguoi_thuc_hien)
                VALUES (?,5,N'Khách hàng xác nhận đã nhận hàng; nếu thanh toán COD, đã trả đủ tiền COD.',?,'khach_hang')
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
        if(ma==null || !ma.trim().toUpperCase(java.util.Locale.ROOT).matches("[A-Z0-9_-]{1,50}") || sdt==null || !sdt.trim().matches("0[0-9]{9}")) return null;
        String tim=ma.trim().toUpperCase(java.util.Locale.ROOT);
        var ds=jdbcTemplate.queryForList("SELECT id FROM hoa_don WHERE (ma_hoa_don=? OR EXISTS (SELECT 1 FROM van_don_ghn vd WHERE vd.id_hoa_don=hoa_don.id AND vd.ma_van_don=?)) AND so_dien_thoai_khach_hang=? AND id_khach_hang IS NULL AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",tim,tim,sdt.trim());
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
        ghiNhanCodKhiNhanHang(id,null);
        jdbcTemplate.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,loai_nguoi_thuc_hien) VALUES (?,5,N'Khách vãng lai xác nhận đã nhận hàng; nếu thanh toán COD, đã trả đủ tiền COD.','khach_hang')",id);
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
        if (trangThai != 1 && !(idAdmin == null && (trangThai == 2 || trangThai == 3)))
            throw new IllegalArgumentException(idAdmin != null ? "Chỉ được hủy đơn đang chờ xác nhận." : "Chỉ được hủy đơn trước khi gửi hàng cho shipper.");
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
        else if(idKhachHang==null) dong=jdbcTemplate.update("UPDATE hoa_don SET trang_thai_hien_tai=6,ngay_cap_nhat=SYSDATETIME() WHERE id=? AND id_khach_hang IS NULL AND trang_thai_hien_tai IN (1,2,3) AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",idHoaDon);
        else dong = jdbcTemplate.update("""
                UPDATE hoa_don SET trang_thai_hien_tai=6,ngay_cap_nhat=SYSDATETIME()
                WHERE id=? AND id_khach_hang=? AND trang_thai_hien_tai IN (1,2,3) AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0
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
                VALUES (?,6,N'Khách hàng hủy đơn trước khi gửi hàng cho shipper.',?,'khach_hang')
                """, idHoaDon,idKhachHang);
        baoChuong(idHoaDon);
    }
}
