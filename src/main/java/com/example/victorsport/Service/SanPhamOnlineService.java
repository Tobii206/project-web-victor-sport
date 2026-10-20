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
    @org.springframework.beans.factory.annotation.Autowired
    private ChuongTrucTuyenService chuongTrucTuyen;
    private void baoChuong(int id) { if(chuongTrucTuyen!=null) chuongTrucTuyen.donThayDoi(id); }

    private final JdbcTemplate jdbc;
    @org.springframework.beans.factory.annotation.Autowired
    private GiaoHangGhnService giaoHangGhn;
    public boolean dungGhn(){return giaoHangGhn!=null && giaoHangGhn.dangBat();}
    public com.example.victorsport.Dto.BaoGiaGhn baoGiaGhn(Map<Integer,Integer> gio,String ten,String sdt,String diaChi,String pt,int tinh,int xa) {
        if(!dungGhn()) throw new IllegalArgumentException("Cửa hàng chưa bật giao hàng GHN.");
        var ds=layGioHang(gio);
        if(ds.isEmpty()) throw new IllegalArgumentException("Chưa có sản phẩm để giao hàng.");
        return giaoHangGhn.baoGia(gio,ten,sdt,diaChi,pt,tinhTong(ds),tinh,xa);
    }

    @org.springframework.beans.factory.annotation.Value("${thanhtoan.qr.ngan-hang:}")
    private String nganHang = "";
    @org.springframework.beans.factory.annotation.Value("${thanhtoan.qr.so-tai-khoan:}")
    private String soTaiKhoan = "";
    @org.springframework.beans.factory.annotation.Value("${thanhtoan.qr.chu-tai-khoan:}")
    private String chuTaiKhoan = "";
    @org.springframework.beans.factory.annotation.Value("${giaohang.phi-co-dinh:30000}")
    private BigDecimal phiGiaoHang=BigDecimal.ZERO;

    public BigDecimal phiGiaoHang() {
        if(phiGiaoHang==null || phiGiaoHang.signum()<0 || phiGiaoHang.compareTo(new BigDecimal("1000000"))>0
                || phiGiaoHang.stripTrailingZeros().scale()>0) throw new IllegalArgumentException("Phí giao hàng của cửa hàng chưa hợp lệ.");
        return phiGiaoHang;
    }

    public SanPhamOnlineService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<String,Object> layThongTinNguoiNhan(int idKhachHang) {
        var ds=jdbc.queryForList("""
                SELECT ten_khach_hang,so_dien_thoai FROM khach_hang
                WHERE id=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1
                """,idKhachHang);
        if(ds.isEmpty()) throw new IllegalArgumentException("Tài khoản không còn hoạt động. Vui lòng đăng nhập lại.");
        return ds.get(0);
    }

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
        return datHang(gio,idKhachHang,ten,sdt,diaChi,muaNgay,"COD");
    }

    @Transactional
    public Integer datHang(Map<Integer, Integer> gio, Integer idKhachHang, String ten, String sdt, String diaChi,boolean muaNgay,String phuongThuc) {
        return datHang(gio,idKhachHang,ten,sdt,diaChi,muaNgay,phuongThuc,null,null);
    }

    public BigDecimal tongTienGoc(List<Map<String,Object>> ds) {
        BigDecimal tong=BigDecimal.ZERO;
        for(var dong:ds) tong=tong.add((BigDecimal)dong.get("thanh_tien_goc"));
        return tong;
    }

    public BigDecimal tinhGiamPhieu(Map<String,Object> phieu,BigDecimal tongGoc) {
        BigDecimal giaTri=(BigDecimal)phieu.get("gia_tri_giam_gia");
        BigDecimal giam=giaTri;
        if(!Boolean.TRUE.equals(phieu.get("loai_phieu_giam_gia")))
            giam=tongGoc.multiply(giaTri).divide(BigDecimal.valueOf(100),2,java.math.RoundingMode.HALF_UP);
        BigDecimal toiDa=(BigDecimal)phieu.get("so_tien_giam_toi_da");
        if(toiDa!=null && toiDa.signum()>0) giam=giam.min(toiDa);
        return giam.max(BigDecimal.ZERO).min(tongGoc).setScale(2,java.math.RoundingMode.HALF_UP);
    }

    public List<Map<String,Object>> layPhieuKhaDung(BigDecimal tongGoc) {
        return layPhieuKhaDung(tongGoc,false);
    }

    public Integer chonPhieuTotNhat(List<Map<String,Object>> cacPhieu) {
        Integer id=null;
        BigDecimal giamTotNhat=BigDecimal.ZERO;
        for(var phieu:cacPhieu) {
            BigDecimal giam=((BigDecimal)phieu.get("tien_giam")).add((BigDecimal)phieu.getOrDefault("giam_phi_van_chuyen",BigDecimal.ZERO));
            if(id==null || giam.compareTo(giamTotNhat)>0) {
                id=((Number)phieu.get("id")).intValue();giamTotNhat=giam;
            }
        }
        return id;
    }

    // Database hiện chỉ có loại tiền / phần trăm; phiếu freeship được đặt tên riêng.
    public boolean laPhieuFreeship(Map<String,Object> phieu) {
        String ten=String.valueOf(phieu.getOrDefault("ten_phieu_giam_gia","")).trim().toLowerCase(java.util.Locale.ROOT);
        return ten.startsWith("freeship") || ten.startsWith("free ship") || ten.startsWith("miễn phí vận chuyển");
    }

    private String chuanHoaDiaBan(String ten) {
        return java.text.Normalizer.normalize(ten==null?"":ten,java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").trim().toLowerCase(java.util.Locale.ROOT);
    }
    public boolean laFreeshipNoiThanh(Map<String,Object> phieu) {
        return laPhieuFreeship(phieu) && chuanHoaDiaBan(String.valueOf(phieu.get("ten_phieu_giam_gia"))).contains("noi thanh");
    }
    public boolean laHaNoi(String tinh) {
        String ten=chuanHoaDiaBan(tinh);
        return ten.equals("ha noi") || ten.equals("thanh pho ha noi") || ten.equals("tp. ha noi") || ten.equals("tp ha noi");
    }
    private String tinhTuDiaChi(String diaChi) {
        int dau=diaChi.lastIndexOf(',');return dau<0?diaChi:diaChi.substring(dau+1);
    }
    public List<Map<String,Object>> layPhieuKhaDung(BigDecimal tongGoc,boolean coDotGiamGia,BigDecimal phi,String tenTinh) {
        var ds=layPhieuKhaDung(tongGoc,coDotGiamGia,phi);
        return ds.stream().filter(p->!laFreeshipNoiThanh(p) || laHaNoi(tenTinh)).toList();
    }

    public List<Map<String,Object>> layPhieuKhaDung(BigDecimal tongGoc,boolean coDotGiamGia) {
        return layPhieuKhaDung(tongGoc,coDotGiamGia,phiGiaoHang());
    }
    public List<Map<String,Object>> layPhieuKhaDung(BigDecimal tongGoc,boolean coDotGiamGia,BigDecimal phi) {
        var ds=jdbc.queryForList("""
                SELECT * FROM phieu_giam_gia WHERE ISNULL(xoa_mem,0)=0 AND trang_thai=1
                  AND CAST(GETDATE() AS DATE) BETWEEN ngay_bat_dau AND ngay_ket_thuc
                  AND so_luong_su_dung>0 AND ISNULL(hoa_don_toi_thieu,0)<=? ORDER BY id DESC
                """,tongGoc);
        var ketQua=new ArrayList<Map<String,Object>>();
        for(var phieu:ds) {
            boolean freeship=laPhieuFreeship(phieu);
            if(coDotGiamGia && !freeship) continue;
            phieu.put("freeship",freeship);
            phieu.put("noi_thanh",laFreeshipNoiThanh(phieu));
            phieu.put("tien_giam",freeship ? BigDecimal.ZERO : tinhGiamPhieu(phieu,tongGoc));
            phieu.put("giam_phi_van_chuyen",freeship?tinhGiamPhieu(phieu,phi):BigDecimal.ZERO);
            ketQua.add(phieu);
        }
        return ketQua;
    }

    @Transactional
    public Integer datHang(Map<Integer, Integer> gio, Integer idKhachHang, String ten, String sdt, String diaChi,
                           boolean muaNgay,String phuongThuc,Integer idPhieuGiamGia,BigDecimal tongTienDuKien) {
        return datHang(gio,idKhachHang,ten,sdt,diaChi,muaNgay,phuongThuc,idPhieuGiamGia,tongTienDuKien,null);
    }
    @Transactional
    public Integer datHang(Map<Integer,Integer> gio,Integer idKhachHang,String ten,String sdt,String diaChi,
                           boolean muaNgay,String phuongThuc,Integer idPhieuGiamGia,BigDecimal tongTienDuKien,
                           com.example.victorsport.Dto.BaoGiaGhn baoGia) {
        if(dungGhn() && baoGia==null) throw new IllegalArgumentException("Vui lòng chọn địa chỉ và lấy phí GHN trước khi đặt hàng.");
        if (!"COD".equals(phuongThuc) && !"QR".equals(phuongThuc))
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        if ("QR".equals(phuongThuc) && (nganHang.isBlank() || soTaiKhoan.isBlank() || chuTaiKhoan.isBlank()))
            throw new IllegalArgumentException("Cửa hàng chưa cấu hình tài khoản nhận tiền QR. Vui lòng chọn COD.");
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
        if(dungGhn()) baoGia.kiemTra(gio,ten,sdt,diaChi,phuongThuc,tinhTong(ds));
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
        BigDecimal tongGoc = tongTienGoc(ds);
        BigDecimal phi=dungGhn()?baoGia.phi():phiGiaoHang();
        Integer phieuApDung=null;
        if(idPhieuGiamGia!=null) {
            var cacPhieu=jdbc.queryForList("""
                    SELECT * FROM phieu_giam_gia WITH (UPDLOCK,HOLDLOCK)
                    WHERE id=? AND ISNULL(xoa_mem,0)=0 AND trang_thai=1 AND so_luong_su_dung>0
                      AND CAST(GETDATE() AS DATE) BETWEEN ngay_bat_dau AND ngay_ket_thuc
                      AND ISNULL(hoa_don_toi_thieu,0)<=?
                    """,idPhieuGiamGia,tongGoc);
            if(cacPhieu.isEmpty()) throw new IllegalArgumentException("Phiếu đã hết hạn, hết lượt hoặc đơn hàng chưa đủ điều kiện. Vui lòng chọn lại.");
            BigDecimal giamTheoDot=tongGoc.subtract(tong);
            boolean freeship=laPhieuFreeship(cacPhieu.get(0));
            String tinhNhan=dungGhn()?baoGia.tenTinh():tinhTuDiaChi(diaChi);
            if(laFreeshipNoiThanh(cacPhieu.get(0)) && !laHaNoi(tinhNhan))
                throw new IllegalArgumentException("Phiếu freeship nội thành chỉ áp dụng cho địa chỉ nhận hàng tại Hà Nội. Vui lòng chọn phiếu khác.");
            if(giamTheoDot.signum()>0 && !freeship)
                throw new IllegalArgumentException("Đơn có sản phẩm đang giảm giá chỉ được dùng voucher freeship.");
            if(!freeship) {
                BigDecimal giamPhieu=tinhGiamPhieu(cacPhieu.get(0),tongGoc);
                if(giamPhieu.signum()>0) {tong=tongGoc.subtract(giamPhieu);phieuApDung=idPhieuGiamGia;}
            }
            else {
                BigDecimal giamPhi=tinhGiamPhieu(cacPhieu.get(0),phi);
                phi=phi.subtract(giamPhi);
                if(giamPhi.signum()>0) phieuApDung=idPhieuGiamGia;
            }
            if(tongTienDuKien==null || tong.add(phi).compareTo(tongTienDuKien)!=0)
                throw new IllegalArgumentException("Ưu đãi hoặc tổng tiền đã thay đổi. Vui lòng kiểm tra lại trước khi đặt hàng.");
            if(phieuApDung!=null && jdbc.update("UPDATE phieu_giam_gia SET so_luong_su_dung=so_luong_su_dung-1 WHERE id=? AND so_luong_su_dung>0",phieuApDung)!=1)
                throw new IllegalArgumentException("Phiếu đã hết lượt sử dụng. Vui lòng chọn lại.");
        }
        tong=tong.add(phi);
        if(tongTienDuKien!=null && tong.compareTo(tongTienDuKien)!=0)
            throw new IllegalArgumentException("Giá, phí giao hàng hoặc ưu đãi đã thay đổi. Vui lòng kiểm tra lại trước khi đặt hàng.");
        if ("QR".equals(phuongThuc) && (tong.signum() <= 0 || tong.stripTrailingZeros().scale() > 0))
            throw new IllegalArgumentException("Số tiền chuyển khoản QR phải là số đồng nguyên và lớn hơn 0. Vui lòng chọn COD.");
        Integer id = jdbc.queryForObject("""
                INSERT INTO hoa_don (id_khach_hang, loai_don, tong_tien, tong_tien_sau_giam,
                    ten_khach_hang, so_dien_thoai_khach_hang, dia_chi_khach_hang, trang_thai_hien_tai, ghi_chu)
                OUTPUT INSERTED.id VALUES (?, 1, ?, ?, ?, ?, ?, 1, ?)
                """, Integer.class, idKhachHang, tongGoc, tong, ten, sdt, diaChi,
                "QR".equals(phuongThuc) ? "Chuyển khoản QR - chờ kiểm tra thanh toán" : "Thanh toán khi nhận hàng (COD)");
        for (Map<String, Object> dong : ds) {
            jdbc.update("INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia) VALUES (?, ?, ?, ?)",
                    id, dong.get("id"), dong.get("so_luong_mua"), dong.get("gia_ban"));
        }
        jdbc.update("UPDATE hoa_don SET phi_van_chuyen=?,id_phieu_giam_gia=? WHERE id=?",phi,phieuApDung,id);
        jdbc.update("INSERT INTO lich_su_hoa_don (id_hoa_don, trang_thai, ghi_chu) VALUES (?, 1, ?)", id,
                "Khách đặt hàng online, " + ("QR".equals(phuongThuc) ? "chuyển khoản QR" : "thanh toán khi nhận hàng"));
        if ("QR".equals(phuongThuc)) {
            var cachThanhToan = jdbc.queryForList("""
                    SELECT TOP 1 id FROM phuong_thuc_thanh_toan
                    WHERE nha_cung_cap=N'VietQR' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id
                    """);
            if (cachThanhToan.isEmpty()) throw new IllegalArgumentException("Chưa có phương thức VietQR đang hoạt động.");
            String noiDung = "HD" + String.format("%05d",id);
            String anhQr = "https://img.vietqr.io/image/" + maHoa(nganHang) + "-" + maHoa(soTaiKhoan)
                    + "-compact2.png?amount=" + tong.toBigInteger() + "&addInfo=" + noiDung + "&accountName=" + maHoa(chuTaiKhoan);
            jdbc.update("""
                    INSERT INTO giao_dich_thanh_toan
                    (id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,ma_tham_chieu,duong_dan_thanh_toan,thoi_gian_het_han,ghi_chu)
                    VALUES (?,?,?,N'cho_thanh_toan',?,?,DATEADD(MINUTE,30,SYSDATETIME()),N'QR chuyển khoản - admin kiểm tra tiền trước khi xác nhận')
                    """, id,cachThanhToan.get(0).get("id"),tong,noiDung,anhQr);
        } else {
            var cod=jdbc.queryForList("SELECT TOP 1 id FROM phuong_thuc_thanh_toan WHERE nha_cung_cap=N'COD' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id");
            if(cod.isEmpty()) throw new IllegalArgumentException("Chưa cấu hình phương thức COD.");
            jdbc.update("INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,ghi_chu) VALUES (?,?,?,N'cho_thanh_toan',N'Chờ thu tiền khi giao hàng')",id,cod.get(0).get("id"),tong);
        }
        if(dungGhn()) giaoHangGhn.luu(id,baoGia);
        // Cùng giao dịch với hóa đơn: đặt hàng lỗi thì giỏ vẫn được giữ nguyên.
        if(idGio!=null) jdbc.update("UPDATE chi_tiet_gio_hang SET xoa_mem=1 WHERE id_gio_hang=? AND ISNULL(xoa_mem,0)=0",idGio);
        baoChuong(id);
        return id;
    }

    private String maHoa(String giaTri) {
        return java.net.URLEncoder.encode(giaTri.trim(), java.nio.charset.StandardCharsets.UTF_8);
    }

    public Map<String,Object> layThanhToanQr(int id,Integer idKhachHang,boolean dungPhien) {
        if (!dungPhien && idKhachHang == null) return null;
        var ds = jdbc.queryForList("""
                SELECT TOP 1 hd.ma_hoa_don,hd.trang_thai_hien_tai,gd.so_tien,gd.thoi_gian_het_han,gd.trang_thai AS trang_thai_thanh_toan,
                       DATEDIFF(SECOND,SYSDATETIME(),gd.thoi_gian_het_han) AS con_lai_giay,
                       CAST(CASE WHEN gd.thoi_gian_het_han IS NOT NULL AND gd.thoi_gian_het_han<=SYSDATETIME() THEN 1 ELSE 0 END AS BIT) AS qua_han,
                       gd.ma_tham_chieu,gd.duong_dan_thanh_toan
                FROM hoa_don hd JOIN giao_dich_thanh_toan gd ON gd.id_hoa_don=hd.id
                JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
                WHERE hd.id=? AND hd.loai_don IN (1,2) AND ISNULL(hd.xoa_mem,0)=0
                  AND ISNULL(gd.xoa_mem,0)=0 AND pt.nha_cung_cap=N'VietQR'
                  AND (?=1 OR hd.id_khach_hang=?) ORDER BY gd.id
                """,id,dungPhien ? 1 : 0,idKhachHang);
        if (ds.isEmpty()) return null;
        var thanhToan = ds.get(0);
        thanhToan.put("ngan_hang",nganHang);
        thanhToan.put("so_tai_khoan",soTaiKhoan);
        thanhToan.put("chu_tai_khoan",chuTaiKhoan);
        return thanhToan;
    }
}
