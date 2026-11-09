package com.example.victorsport.Service;

import com.example.victorsport.Dto.GioHangTaiQuay;
import com.example.victorsport.Dto.LuaChonKhachHang;
import com.example.victorsport.Dto.LuaChonPhieuGiamGia;
import com.example.victorsport.Dto.LuaChonPhuongThucThanhToan;
import com.example.victorsport.Dto.LuaChonSanPham;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamTrongGio;
import com.example.victorsport.Dto.TongKetBanHang;
import com.example.victorsport.Dto.BaoGiaGhn;
import com.example.victorsport.Dto.GiaoHangTaiQuayForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

@Service
public class BanHangTaiQuayService {
    static final String DIA_CHI_CUA_HANG = "Cửa hàng Victor Sport, Hà Nội";
    private final JdbcTemplate jdbcTemplate;
    @org.springframework.beans.factory.annotation.Autowired
    private GiaoHangGhnService giaoHangGhn;

    public BanHangTaiQuayService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Integer,Integer> gioVanChuyen(GioHangTaiQuay gio) {
        var result=new java.util.LinkedHashMap<Integer,Integer>();
        if(gio==null || gio.isRong()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        for(var sp:gio.getDanhSachSanPham()) {
            if(sp.getSoLuongMua()<=0) throw new IllegalArgumentException("Số lượng giao hàng không hợp lệ.");
            result.put(sp.getIdChiTietSanPham(),sp.getSoLuongMua());
        }
        return result;
    }

    private void kiemTraThanhToanTruoc(Integer id) {
        var ds=jdbcTemplate.queryForList("SELECT nha_cung_cap FROM phuong_thuc_thanh_toan WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Phương thức thanh toán không còn hoạt động.");
        if("COD".equals(ds.get(0).get("nha_cung_cap")))
            throw new IllegalArgumentException("Đơn giao hàng tại quầy cần thanh toán trước, không áp dụng COD.");
    }

    public BaoGiaGhn baoGiaGiaoHang(GioHangTaiQuay gio,Integer phieu,GiaoHangTaiQuayForm f) {
        if(f.ten()==null || f.ten().isBlank() || f.ten().trim().length()>100
                || f.sdt()==null || !f.sdt().trim().matches("0[0-9]{9}")
                || f.diaChi()==null || f.diaChi().isBlank() || f.tinh()==null || f.xa()==null)
            throw new IllegalArgumentException("Vui lòng nhập đủ tên, số điện thoại và địa chỉ nhận hàng hợp lệ.");
        var gioGui=gioVanChuyen(gio);
        kiemTraThanhToanTruoc(f.paymentMethodId());
        return giaoHangGhn.baoGia(gioGui,f.ten().trim(),f.sdt().trim(),f.diaChi().trim(),
                "QR",tinhTongKet(gio,phieu).getTongThanhToan(),f.tinh(),f.xa());
    }

    public List<LuaChonSanPham> timSanPham(String tuKhoa) {
        String tuKhoaTimKiem = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        String cauLenhSql = """
                SELECT TOP 50 ct.id AS id_chi_tiet_san_pham, sp.ma_san_pham, sp.ten_san_pham, th.ten_thuong_hieu,
                       ms.ten_mau_sac, kt.ten_kich_thuoc, ISNULL(ct.so_luong, 0) AS so_luong,
                       ct.gia_ban AS gia_ban,
                       (SELECT TOP 1 a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                        JOIN chi_tiet_san_pham cung_mau ON cung_mau.id=a.id_chi_tiet_san_pham
                        WHERE cung_mau.id_san_pham=ct.id_san_pham AND cung_mau.id_mau_sac=ct.id_mau_sac AND ISNULL(a.xoa_mem,0)=0
                        ORDER BY ISNULL(a.la_anh_dai_dien,0) DESC,a.id) AS anh_dai_dien
                FROM chi_tiet_san_pham ct
                JOIN san_pham sp ON sp.id = ct.id_san_pham
                JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE ISNULL(ct.xoa_mem, 0) = 0
                  AND ISNULL(sp.xoa_mem, 0) = 0
                  AND ISNULL(ct.trang_thai, 1) = 1
                  AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND (sp.ten_san_pham COLLATE Vietnamese_100_CI_AI LIKE ? OR sp.ma_san_pham LIKE ?
                       OR th.ten_thuong_hieu COLLATE Vietnamese_100_CI_AI LIKE ? OR ct.ma_chi_tiet_san_pham = ?)
                ORDER BY sp.ten_san_pham, kt.gia_tri_kich_thuoc
                """;
        var ds = jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new LuaChonSanPham(
                ketQua.getInt("id_chi_tiet_san_pham"),
                ketQua.getString("ma_san_pham"),
                ketQua.getString("ten_san_pham"),
                ketQua.getString("ten_thuong_hieu"),
                ketQua.getString("ten_mau_sac"),
                ketQua.getString("ten_kich_thuoc"),
                ketQua.getInt("so_luong"),
                ketQua.getBigDecimal("gia_ban"),
                ketQua.getString("anh_dai_dien")
        ), tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem, tuKhoa == null ? "" : tuKhoa.trim());
        for (var mau : ds) mau.setGiaSauGiam(GiaGiamSanPham.giaSauGiam(jdbcTemplate, mau.getIdChiTietSanPham(), mau.getGiaBan()));
        return ds;
    }

    public SanPhamTrongGio taoSanPhamTrongGio(Integer idChiTietSanPham, Integer soLuong) {
        String cauLenhSql = """
                SELECT ct.id AS id_chi_tiet_san_pham, sp.ma_san_pham, sp.ten_san_pham, ms.ten_mau_sac,
                       kt.ten_kich_thuoc, ISNULL(ct.so_luong, 0) AS so_luong,
                       ct.gia_ban AS gia_ban
                FROM chi_tiet_san_pham ct
                JOIN san_pham sp ON sp.id = ct.id_san_pham
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE ct.id = ?
                  AND ISNULL(ct.xoa_mem, 0) = 0
                  AND ISNULL(sp.xoa_mem, 0) = 0
                  AND ISNULL(ct.trang_thai, 1) = 1 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                """;
        List<SanPhamTrongGio> danhSach = jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> {
            int soLuongTon = ketQua.getInt("so_luong");
            int soLuongHopLe = 1;
            if (soLuong != null) {
                soLuongHopLe = Math.max(1, Math.min(soLuong, soLuongTon));
            }
            return new SanPhamTrongGio(
                    ketQua.getInt("id_chi_tiet_san_pham"),
                    ketQua.getString("ma_san_pham"),
                    ketQua.getString("ten_san_pham"),
                    ketQua.getString("ten_mau_sac"),
                    ketQua.getString("ten_kich_thuoc"),
                    soLuongHopLe,
                    soLuongTon,
                    ketQua.getBigDecimal("gia_ban")
            );
        }, idChiTietSanPham);
        if (danhSach.isEmpty()) {
            return null;
        }
        return danhSach.get(0);
    }

    public List<LuaChonKhachHang> timKhachHang(String tuKhoa) {
        String tuKhoaTimKiem = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        String cauLenhSql = """
                SELECT TOP 20 id, ma_khach_hang, ten_khach_hang, so_dien_thoai, email
                FROM khach_hang
                WHERE ISNULL(xoa_mem, 0) = 0
                  AND ISNULL(trang_thai, 1) = 1
                  AND (ten_khach_hang COLLATE Vietnamese_100_CI_AI LIKE ? OR ma_khach_hang LIKE ? OR so_dien_thoai LIKE ? OR email LIKE ?)
                ORDER BY ten_khach_hang
                """;
        return jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new LuaChonKhachHang(
                ketQua.getInt("id"),
                ketQua.getString("ma_khach_hang"),
                ketQua.getString("ten_khach_hang"),
                ketQua.getString("so_dien_thoai"),
                ketQua.getString("email")
        ), tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem);
    }

    public List<LuaChonPhuongThucThanhToan> layPhuongThucThanhToan() {
        String cauLenhSql = """
                SELECT id, ten_phuong_thuc_thanh_toan, nha_cung_cap
                FROM phuong_thuc_thanh_toan
                WHERE ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1
                ORDER BY id
                """;
        return jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new LuaChonPhuongThucThanhToan(
                ketQua.getInt("id"),
                ketQua.getString("ten_phuong_thuc_thanh_toan"),
                ketQua.getString("nha_cung_cap")
        ));
    }

    public LuaChonKhachHang layKhachHang(Integer idKhachHang) {
        List<LuaChonKhachHang> danhSach = jdbcTemplate.query("""
                SELECT id, ma_khach_hang, ten_khach_hang, so_dien_thoai, email
                FROM khach_hang
                WHERE id = ? AND ISNULL(xoa_mem, 0) = 0 AND ISNULL(trang_thai, 1) = 1
                """, (ketQua, soDong) -> new LuaChonKhachHang(
                ketQua.getInt("id"), ketQua.getString("ma_khach_hang"), ketQua.getString("ten_khach_hang"),
                ketQua.getString("so_dien_thoai"), ketQua.getString("email")
        ), idKhachHang);
        if (danhSach.isEmpty()) {
            throw new IllegalArgumentException("Khách hàng không còn tồn tại hoặc đã ngừng hoạt động.");
        }
        return danhSach.get(0);
    }

    public Map<String,Object> layDiaChiGiaoHangMacDinh(int khachHang) {
        var ds=jdbcTemplate.queryForList("""
                SELECT TOP 1 dc.thanh_pho,dc.phuong,dc.dia_chi_cu_the
                FROM dia_chi_khach_hang dc JOIN khach_hang kh ON kh.id=dc.id_khach_hang
                WHERE dc.id_khach_hang=? AND ISNULL(dc.xoa_mem,0)=0
                    AND ISNULL(kh.xoa_mem,0)=0 AND ISNULL(kh.trang_thai,1)=1
                ORDER BY ISNULL(dc.mac_dinh,0) DESC,dc.id DESC
                """,khachHang);
        return ds.isEmpty()?Map.of():ds.get(0);
    }

    @Transactional
    public Integer themKhachHangMoi(String ten, String soDienThoai, String email, Integer idNhanVien) {
        ten = layGiaTriMacDinh(ten, "");
        soDienThoai = layGiaTriMacDinh(soDienThoai, "");
        email = layGiaTriMacDinh(email, "");
        if (ten.isEmpty() || ten.length() > 100) {
            throw new IllegalArgumentException("Vui lòng nhập tên khách hàng, tối đa 100 ký tự.");
        }
        if (!soDienThoai.isEmpty() && !soDienThoai.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        }
        if (!email.isEmpty() && !KiemTraThongTin.laEmailGmail(email)) {
            throw new IllegalArgumentException("Email phải đúng định dạng ten@gmail.com và tối đa 100 ký tự.");
        }
        // Bảng khách hàng yêu cầu tài khoản và mật khẩu, tự tạo cho khách mua tại quầy.
        String taiKhoan = "quay_" + UUID.randomUUID().toString().replace("-", "");
        String matKhau = new BCryptPasswordEncoder().encode(UUID.randomUUID().toString());
        return jdbcTemplate.queryForObject("""
                INSERT INTO khach_hang (ten_khach_hang, ten_tai_khoan, mat_khau, so_dien_thoai, email, nguoi_tao)
                OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?)
                """, Integer.class, ten, taiKhoan, matKhau, soDienThoai, email, idNhanVien);
    }

    public List<LuaChonPhieuGiamGia> layPhieuGiamGiaKhaDung(BigDecimal tamTinh) {
        String cauLenhSql = """
                SELECT id, ma_phieu_giam_gia, ten_phieu_giam_gia, loai_phieu_giam_gia,
                       gia_tri_giam_gia, hoa_don_toi_thieu
                FROM phieu_giam_gia
                WHERE ISNULL(xoa_mem, 0) = 0
                  AND ISNULL(trang_thai, 1) = 1
                  AND CAST(GETDATE() AS DATE) BETWEEN ngay_bat_dau AND ngay_ket_thuc
                  AND ISNULL(hoa_don_toi_thieu, 0) <= ?
                  AND ISNULL(so_luong_su_dung, 0) > 0
                  AND ISNULL(ap_dung_van_chuyen, 0) = 0
                ORDER BY ten_phieu_giam_gia
                """;
        return jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new LuaChonPhieuGiamGia(
                ketQua.getInt("id"),
                ketQua.getString("ma_phieu_giam_gia"),
                ketQua.getString("ten_phieu_giam_gia"),
                ketQua.getBoolean("loai_phieu_giam_gia"),
                ketQua.getBigDecimal("gia_tri_giam_gia"),
                ketQua.getBigDecimal("hoa_don_toi_thieu")
        ), tamTinh == null ? BigDecimal.ZERO : tamTinh);
    }

    public TongKetBanHang tinhTongKet(GioHangTaiQuay gioHang) {
        return tinhTongKet(gioHang, null);
    }

    public TongKetBanHang tinhTongKet(GioHangTaiQuay gioHang, Integer idPhieuGiamGiaDangChon) {
        BigDecimal tamTinh = gioHang.getTamTinh();
        UuDaiHoaDon uuDaiHoaDon;
        if (idPhieuGiamGiaDangChon == null) {
            uuDaiHoaDon = timUuDaiHoaDonTotNhat(tamTinh);
        } else {
            uuDaiHoaDon = timUuDaiHoaDonTheoPhieu(tamTinh, idPhieuGiamGiaDangChon);
        }
        BigDecimal giamTheoSanPham = timUuDaiSanPham(gioHang);
        BigDecimal giamTheoHoaDon = uuDaiHoaDon.getTienGiam();
        boolean apDungPhieu = giamTheoHoaDon.compareTo(BigDecimal.ZERO) > 0
                && (idPhieuGiamGiaDangChon != null || giamTheoHoaDon.compareTo(giamTheoSanPham) >= 0);
        BigDecimal tienGiam = (apDungPhieu ? giamTheoHoaDon : giamTheoSanPham).setScale(2, RoundingMode.HALF_UP);
        String tenUuDai = "Không áp dụng";
        Integer idPhieuGiamGia = null;
        if (apDungPhieu) {
            tenUuDai = uuDaiHoaDon.getTenPhieu();
            idPhieuGiamGia = uuDaiHoaDon.getIdPhieuGiamGia();
        } else if (tienGiam.compareTo(BigDecimal.ZERO) > 0) {
            tenUuDai = "Đợt giảm giá sản phẩm";
        }
        BigDecimal tongThanhToan = tamTinh.subtract(tienGiam).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new TongKetBanHang(tamTinh, tienGiam, tongThanhToan, tenUuDai, idPhieuGiamGia);
    }

    private UuDaiHoaDon timUuDaiHoaDonTotNhat(BigDecimal tamTinh) {
        String cauLenhSql = """
                SELECT id, ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_tien_giam_toi_da
                FROM phieu_giam_gia
                WHERE ISNULL(xoa_mem, 0) = 0
                  AND ISNULL(trang_thai, 1) = 1
                  AND CAST(GETDATE() AS DATE) BETWEEN ngay_bat_dau AND ngay_ket_thuc
                  AND ISNULL(hoa_don_toi_thieu, 0) <= ?
                  AND ISNULL(so_luong_su_dung, 0) > 0
                  AND ISNULL(ap_dung_van_chuyen, 0) = 0
                """;
        List<UuDaiHoaDon> danhSach = jdbcTemplate.query(cauLenhSql,
                (ketQua, soDong) -> taoUuDaiHoaDonTuKetQua(tamTinh, ketQua), tamTinh);
        if (danhSach.isEmpty()) {
            return UuDaiHoaDon.khongApDung();
        }
        UuDaiHoaDon uuDaiTotNhat = danhSach.get(0);
        for (UuDaiHoaDon uuDai : danhSach) {
            if (uuDai.getTienGiam().compareTo(uuDaiTotNhat.getTienGiam()) > 0) {
                uuDaiTotNhat = uuDai;
            }
        }
        return uuDaiTotNhat;
    }

    private UuDaiHoaDon timUuDaiHoaDonTheoPhieu(BigDecimal tamTinh, Integer idPhieuGiamGia) {
        String cauLenhSql = """
                SELECT id, ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_tien_giam_toi_da
                FROM phieu_giam_gia
                WHERE id = ?
                  AND ISNULL(xoa_mem, 0) = 0
                  AND ISNULL(trang_thai, 1) = 1
                  AND CAST(GETDATE() AS DATE) BETWEEN ngay_bat_dau AND ngay_ket_thuc
                  AND ISNULL(hoa_don_toi_thieu, 0) <= ?
                  AND ISNULL(so_luong_su_dung, 0) > 0
                  AND ISNULL(ap_dung_van_chuyen, 0) = 0
                """;
        List<UuDaiHoaDon> danhSach = jdbcTemplate.query(cauLenhSql,
                (ketQua, soDong) -> taoUuDaiHoaDonTuKetQua(tamTinh, ketQua), idPhieuGiamGia, tamTinh);
        if (danhSach.isEmpty()) {
            return UuDaiHoaDon.khongApDung();
        }
        return danhSach.get(0);
    }

    private UuDaiHoaDon taoUuDaiHoaDonTuKetQua(BigDecimal tamTinh, java.sql.ResultSet ketQua) throws java.sql.SQLException {
        boolean laTienMat = ketQua.getBoolean("loai_phieu_giam_gia");
        BigDecimal giaTri = ketQua.getBigDecimal("gia_tri_giam_gia");
        BigDecimal tienGiamToiDa = ketQua.getBigDecimal("so_tien_giam_toi_da");
        BigDecimal tienGiam;
        if (laTienMat) {
            tienGiam = giaTri;
        } else {
            tienGiam = tamTinh.multiply(giaTri).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (tienGiamToiDa != null && tienGiamToiDa.compareTo(BigDecimal.ZERO) > 0) {
            tienGiam = tienGiam.min(tienGiamToiDa);
        }
        return new UuDaiHoaDon(
                ketQua.getInt("id"),
                "Phiếu: " + ketQua.getString("ten_phieu_giam_gia"),
                tienGiam
        );
    }

    private BigDecimal timUuDaiSanPham(GioHangTaiQuay gioHang) {
        BigDecimal tongTienGiam = BigDecimal.ZERO;
        for (SanPhamTrongGio sp : gioHang.getDanhSachSanPham()) {
            BigDecimal giaSauGiam = GiaGiamSanPham.giaSauGiam(jdbcTemplate, sp.getIdChiTietSanPham(), sp.getDonGia());
            BigDecimal giam = sp.getDonGia().subtract(giaSauGiam).multiply(BigDecimal.valueOf(sp.getSoLuongMua()));
            tongTienGiam = tongTienGiam.add(giam);
        }
        return tongTienGiam.setScale(2, RoundingMode.HALF_UP);
    }
    @Transactional
    public Integer thanhToan(GioHangTaiQuay gioHang, YeuCauThanhToan yeuCau, NguoiDungDangNhap nhanVien) {
        return thanhToan(gioHang, yeuCau, nhanVien, null);
    }

    @Transactional
    public Integer thanhToan(GioHangTaiQuay gioHang, YeuCauThanhToan yeuCau,
                             NguoiDungDangNhap nhanVien, Integer idDonCho) {
        if (gioHang == null || gioHang.isRong()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống.");
        }
        if (idDonCho != null) {
            kiemTraDonCho(idDonCho, nhanVien.getId());
        }

        for (SanPhamTrongGio sanPham : gioHang.getDanhSachSanPham()) {
            List<Integer> tonKho = jdbcTemplate.queryForList(
                    """
                    SELECT ISNULL(ct.so_luong, 0) FROM chi_tiet_san_pham ct WITH (UPDLOCK, HOLDLOCK)
                    WHERE ct.id = ? AND ISNULL(ct.xoa_mem, 0) = 0 AND ISNULL(ct.trang_thai, 1) = 1
                      AND EXISTS (SELECT 1 FROM san_pham sp WHERE sp.id = ct.id_san_pham
                          AND ISNULL(sp.xoa_mem, 0) = 0 AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1)
                    """,
                    Integer.class,
                    sanPham.getIdChiTietSanPham()
            );
            if (tonKho.isEmpty()) {
                throw new IllegalArgumentException("Sản phẩm " + sanPham.getTenSanPham() + " không còn được bán.");
            }
            Integer soLuongTon = tonKho.get(0);
            if (soLuongTon == null || sanPham.getSoLuongMua() <= 0 || sanPham.getSoLuongMua() > soLuongTon) {
                throw new IllegalArgumentException("Sản phẩm " + sanPham.getTenSanPham() + " không đủ tồn kho.");
            }
        }

        TongKetBanHang tongKet = tinhTongKet(gioHang, yeuCau.getIdPhieuGiamGia());
        BaoGiaGhn giao=yeuCau.getGiaoHang();
        if(giao!=null) {
            kiemTraThanhToanTruoc(yeuCau.getIdPhuongThucThanhToan());
            giao.kiemTra(gioVanChuyen(gioHang),yeuCau.getTenKhachHang(),yeuCau.getSoDienThoai(),
                    yeuCau.getDiaChi(),"QR",tongKet.getTongThanhToan());
            if(giao.phi()==null || giao.phi().signum()<0) throw new IllegalArgumentException("Phí giao hàng không hợp lệ.");
        }
        BigDecimal canThu=tongKet.getTongThanhToan().add(giao==null?BigDecimal.ZERO:giao.phi());
        BigDecimal tienDaThu=yeuCau.getTienKhachDaThanhToan();
        if(tienDaThu==null || tienDaThu.signum()<0 || tienDaThu.stripTrailingZeros().scale()>2
                || tienDaThu.precision()-tienDaThu.scale()>16
                || tienDaThu.compareTo(canThu)<0)
            throw new IllegalArgumentException("Tiền khách đưa phải hợp lệ và không được nhỏ hơn tổng tiền cần thu.");
        BigDecimal tienThua=tienDaThu.subtract(canThu);
        if(tienThua.signum()>0 && jdbcTemplate.queryForList("""
                SELECT id FROM phuong_thuc_thanh_toan WHERE id=? AND nha_cung_cap=N'POS'
                    AND trang_thai=1 AND ISNULL(xoa_mem,0)=0
                """,yeuCau.getIdPhuongThucThanhToan()==null?1:yeuCau.getIdPhuongThucThanhToan()).isEmpty())
            throw new IllegalArgumentException("Chuyển khoản hoặc ví điện tử phải đúng tổng thanh toán. Tiền thừa chỉ áp dụng khi trả tiền mặt.");
        Integer idKhachHang = yeuCau.getIdKhachHang();
        if(idKhachHang!=null && giao!=null) layKhachHang(idKhachHang);
        ThongTinKhachHang thongTinKhachHang = giao==null?layThongTinKhachHang(idKhachHang, yeuCau):
                new ThongTinKhachHang(giao.ten(),giao.sdt(),layGiaTriMacDinh(yeuCau.getEmail(),""),giao.diaChi());
        Integer idPhuongThucThanhToan = yeuCau.getIdPhuongThucThanhToan() == null ? 1 : yeuCau.getIdPhuongThucThanhToan();

        Integer idHoaDon;
        if (idDonCho != null) {
            idHoaDon = idDonCho;
            capNhatThongTinDonCho(idHoaDon, yeuCau, tongKet);
            jdbcTemplate.update("""
                    UPDATE hoa_don SET trang_thai_hien_tai = 5, ngay_thanh_toan = SYSDATETIME()
                    WHERE id = ?
                    """, idHoaDon);
            jdbcTemplate.update("DELETE FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", idHoaDon);
        } else {
            KeyHolder khoaTuTang = new GeneratedKeyHolder();
            jdbcTemplate.update(ketNoi -> {
                PreparedStatement cauLenh = ketNoi.prepareStatement("""
                        INSERT INTO hoa_don (
                            id_khach_hang, id_nhan_vien, id_phieu_giam_gia, loai_don, phi_van_chuyen,
                            tong_tien, tong_tien_sau_giam, ten_khach_hang, dia_chi_khach_hang,
                            so_dien_thoai_khach_hang, email_khach_hang, trang_thai_hien_tai,
                            ngay_thanh_toan, ghi_chu, nguoi_tao
                        ) VALUES (?, ?, ?, 0, 0, ?, ?, ?, ?, ?, ?, 5, SYSDATETIME(), ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ganSoNguyenCoTheNull(cauLenh, 1, idKhachHang);
                cauLenh.setInt(2, nhanVien.getId());
                ganSoNguyenCoTheNull(cauLenh, 3, tongKet.getIdPhieuGiamGia());
                cauLenh.setBigDecimal(4, tongKet.getTamTinh());
                cauLenh.setBigDecimal(5, tongKet.getTongThanhToan());
                cauLenh.setString(6, thongTinKhachHang.getTenKhachHang());
                cauLenh.setString(7, thongTinKhachHang.getDiaChi());
                cauLenh.setString(8, thongTinKhachHang.getSoDienThoai());
                cauLenh.setString(9, thongTinKhachHang.getEmail());
                cauLenh.setString(10, yeuCau.getGhiChu());
                cauLenh.setInt(11, nhanVien.getId());
                return cauLenh;
            }, khoaTuTang);

            idHoaDon = Objects.requireNonNull(khoaTuTang.getKey()).intValue();
        }
        if(giao!=null) {
            // Loại 2: bán tại quầy có giao hàng; giữ tách biệt với loại 1 online.
            jdbcTemplate.update("""
                    UPDATE hoa_don SET loai_don=2,phi_van_chuyen=?,tong_tien_sau_giam=?,
                        ten_khach_hang=?,so_dien_thoai_khach_hang=?,dia_chi_khach_hang=?,
                        trang_thai_hien_tai=2,ngay_thanh_toan=SYSDATETIME(),
                        ngay_cap_nhat=SYSDATETIME() WHERE id=?
                    """,giao.phi(),canThu,giao.ten(),giao.sdt(),giao.diaChi(),idHoaDon);
            jdbcTemplate.update("DELETE FROM van_don_ghn WHERE id_hoa_don=? AND ma_van_don IS NULL",idHoaDon);
            giaoHangGhn.luu(idHoaDon,giao);
        } else if(idDonCho!=null) {
            jdbcTemplate.update("UPDATE hoa_don SET loai_don=0,phi_van_chuyen=0 WHERE id=?",idHoaDon);
            jdbcTemplate.update("DELETE FROM van_don_ghn WHERE id_hoa_don=? AND ma_van_don IS NULL",idHoaDon);
        }
        for (SanPhamTrongGio sanPham : gioHang.getDanhSachSanPham()) {
            jdbcTemplate.update("""
                    INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia, ghi_chu)
                    VALUES (?, ?, ?, ?, ?)
                    """, idHoaDon, sanPham.getIdChiTietSanPham(), sanPham.getSoLuongMua(), sanPham.getDonGia(), "Bán hàng tại quầy");
            jdbcTemplate.update("""
                    UPDATE chi_tiet_san_pham
                    SET so_luong = so_luong - ?, ngay_cap_nhat = SYSDATETIME(), nguoi_cap_nhat = ?
                    WHERE id = ?
                    """, sanPham.getSoLuongMua(), nhanVien.getId(), sanPham.getIdChiTietSanPham());
        }

        jdbcTemplate.update("""
                INSERT INTO giao_dich_thanh_toan (
                    id_hoa_don, id_phuong_thuc_thanh_toan, so_tien, trang_thai,
                    ma_yeu_cau, ma_tham_chieu, thoi_gian_tao, nguoi_cap_nhat, ghi_chu
                ) VALUES (?, ?, ?, N'thanh_cong', ?, ?, SYSDATETIME(), ?, ?)
                """, idHoaDon, idPhuongThucThanhToan, canThu,
                "POS-" + idHoaDon, "HD" + idHoaDon, nhanVien.getId(), tienThua.signum()>0?
                        "Thanh toán tại quầy. Tiền khách đưa: "+tienDaThu.toPlainString()+" đ. Tiền thừa trả khách: "+tienThua.toPlainString()+" đ.":"Thanh toán tại quầy");

        jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (
                    id_hoa_don, trang_thai, thoi_gian, ghi_chu, nguoi_cap_nhat, nguoi_thuc_hien, loai_nguoi_thuc_hien
                ) VALUES (?, ?, SYSDATETIME(), ?, ?, ?, 'NHAN_VIEN')
                """, idHoaDon,giao==null?5:2,giao==null?"Hoàn tất bán hàng tại quầy":
                        "Đã thanh toán tại quầy và xác nhận giao hàng. Chờ chuẩn bị hàng.", nhanVien.getId(), nhanVien.getId());

        if (tongKet.getIdPhieuGiamGia() != null) {
            int soPhieuCapNhat = jdbcTemplate.update("""
                    UPDATE phieu_giam_gia
                    SET so_luong_su_dung = so_luong_su_dung - 1
                    WHERE id = ? AND so_luong_su_dung > 0
                    """, tongKet.getIdPhieuGiamGia());
            if (soPhieuCapNhat == 0) {
                throw new IllegalArgumentException("Phiếu giảm giá đã hết lượt sử dụng. Vui lòng chọn lại phiếu.");
            }
        }

        yeuCau.tienThua=tienThua;
        return idHoaDon;
    }

    public List<Map<String, Object>> layDanhSachDonCho(Integer idNhanVien) {
        return jdbcTemplate.queryForList("""
                SELECT id, ma_hoa_don, ten_khach_hang, tong_tien_sau_giam, ngay_tao
                FROM hoa_don
                WHERE id_nhan_vien = ? AND trang_thai_hien_tai = 0
                  AND loai_don IN (0,2) AND ISNULL(xoa_mem, 0) = 0
                ORDER BY id DESC
                """, idNhanVien);
    }

    public Map<String, Object> layDonCho(Integer idDonCho, Integer idNhanVien) {
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList("""
                SELECT * FROM hoa_don
                WHERE id = ? AND id_nhan_vien = ? AND trang_thai_hien_tai = 0
                  AND loai_don IN (0,2) AND ISNULL(xoa_mem, 0) = 0
                """, idDonCho, idNhanVien);
        if (danhSach.isEmpty()) {
            throw new IllegalArgumentException("Đơn chờ không còn tồn tại hoặc không thuộc nhân viên này.");
        }
        var don=new java.util.LinkedHashMap<>(danhSach.get(0));
        if(don.get("loai_don") instanceof Number n && n.intValue()==2) {
            var vd=jdbcTemplate.queryForList("SELECT ten_tinh,ten_xa FROM van_don_ghn WHERE id_hoa_don=?",idDonCho);
            if(!vd.isEmpty()) {
                don.put("ten_tinh_ghn",vd.get(0).get("ten_tinh"));don.put("ten_xa_ghn",vd.get(0).get("ten_xa"));
                String diaChi=Objects.toString(don.get("dia_chi_khach_hang"),"");
                String duoi=", "+vd.get(0).get("ten_xa")+", "+vd.get(0).get("ten_tinh");
                don.put("dia_chi_chi_tiet",diaChi.endsWith(duoi)?diaChi.substring(0,diaChi.length()-duoi.length()):diaChi);
            }
        }
        return don;
    }

    public GioHangTaiQuay layGioHangDonCho(Integer idDonCho, Integer idNhanVien) {
        layDonCho(idDonCho, idNhanVien);
        List<SanPhamTrongGio> danhSach = jdbcTemplate.query("""
                SELECT ct.id, sp.ma_san_pham, sp.ten_san_pham, ms.ten_mau_sac, kt.ten_kich_thuoc,
                       hdct.so_luong, ISNULL(ct.so_luong, 0) AS so_luong_ton, hdct.don_gia
                FROM hoa_don_chi_tiet hdct
                JOIN chi_tiet_san_pham ct ON ct.id = hdct.id_chi_tiet_san_pham
                JOIN san_pham sp ON sp.id = ct.id_san_pham
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE hdct.id_hoa_don = ? AND ISNULL(hdct.xoa_mem, 0) = 0
                ORDER BY hdct.id
                """, (ketQua, soDong) -> new SanPhamTrongGio(
                ketQua.getInt("id"), ketQua.getString("ma_san_pham"), ketQua.getString("ten_san_pham"),
                ketQua.getString("ten_mau_sac"), ketQua.getString("ten_kich_thuoc"),
                ketQua.getInt("so_luong"), ketQua.getInt("so_luong_ton"), ketQua.getBigDecimal("don_gia")
        ), idDonCho);
        GioHangTaiQuay gioHang = new GioHangTaiQuay();
        gioHang.getDanhSachSanPham().addAll(danhSach);
        return gioHang;
    }

    @Transactional
    public Integer luuDonCho(GioHangTaiQuay gioHang, YeuCauThanhToan yeuCau,
                            NguoiDungDangNhap nhanVien, Integer idDonCho) {
        if (idDonCho == null) {
            // Khóa dòng nhân viên để hai lần tạo cùng lúc không vượt quá 10 đơn.
            jdbcTemplate.queryForObject("SELECT id FROM nhan_vien WITH (UPDLOCK, HOLDLOCK) WHERE id = ?",
                    Integer.class, nhanVien.getId());
            Integer soDonCho = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM hoa_don
                    WHERE id_nhan_vien = ? AND trang_thai_hien_tai = 0
                      AND loai_don IN (0,2) AND ISNULL(xoa_mem, 0) = 0
                    """, Integer.class, nhanVien.getId());
            if (soDonCho != null && soDonCho >= 10) {
                throw new IllegalArgumentException("Thêm tối đa 10 hóa đơn chờ");
            }
            idDonCho = jdbcTemplate.queryForObject("""
                    INSERT INTO hoa_don (id_nhan_vien, loai_don, tong_tien, tong_tien_sau_giam,
                        ten_khach_hang, dia_chi_khach_hang, so_dien_thoai_khach_hang,
                        trang_thai_hien_tai, nguoi_tao)
                    OUTPUT INSERTED.id
                    VALUES (?, 0, 0, 0, N'Khách vãng lai', ?, '', 0, ?)
                    """, Integer.class, nhanVien.getId(), DIA_CHI_CUA_HANG, nhanVien.getId());
        } else {
            kiemTraDonCho(idDonCho, nhanVien.getId());
        }
        TongKetBanHang tongKet = tinhTongKet(gioHang, yeuCau.getIdPhieuGiamGia());
        capNhatThongTinDonCho(idDonCho, yeuCau, tongKet);
        if(yeuCau.getGiaoHang()!=null) {
            var q=yeuCau.getGiaoHang();
            kiemTraThanhToanTruoc(yeuCau.getIdPhuongThucThanhToan());
            q.kiemTra(gioVanChuyen(gioHang),yeuCau.getTenKhachHang(),yeuCau.getSoDienThoai(),yeuCau.getDiaChi(),"QR",tongKet.getTongThanhToan());
            jdbcTemplate.update("""
                    UPDATE hoa_don SET loai_don=2,phi_van_chuyen=?,tong_tien_sau_giam=?,
                        ten_khach_hang=?,so_dien_thoai_khach_hang=?,dia_chi_khach_hang=? WHERE id=?
                    """,q.phi(),tongKet.getTongThanhToan().add(q.phi()),q.ten(),q.sdt(),q.diaChi(),idDonCho);
            jdbcTemplate.update("DELETE FROM van_don_ghn WHERE id_hoa_don=? AND ma_van_don IS NULL",idDonCho);
            giaoHangGhn.luu(idDonCho,q);
        } else {
            jdbcTemplate.update("UPDATE hoa_don SET loai_don=0,phi_van_chuyen=0 WHERE id=?",idDonCho);
            jdbcTemplate.update("DELETE FROM van_don_ghn WHERE id_hoa_don=? AND ma_van_don IS NULL",idDonCho);
        }
        jdbcTemplate.update("DELETE FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", idDonCho);
        for (SanPhamTrongGio sanPham : gioHang.getDanhSachSanPham()) {
            if (sanPham.getSoLuongMua() <= 0) {
                throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
            }
            jdbcTemplate.update("""
                    INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia, ghi_chu)
                    VALUES (?, ?, ?, ?, ?)
                    """, idDonCho, sanPham.getIdChiTietSanPham(), sanPham.getSoLuongMua(), sanPham.getDonGia(), "Đơn chờ tại quầy");
        }
        return idDonCho;
    }

    private void capNhatThongTinDonCho(Integer idDonCho, YeuCauThanhToan yeuCau, TongKetBanHang tongKet) {
        ThongTinKhachHang khachHang = layThongTinKhachHang(yeuCau.getIdKhachHang(), yeuCau);
        jdbcTemplate.update("""
                UPDATE hoa_don SET id_khach_hang = ?, id_phieu_giam_gia = ?, tong_tien = ?,
                    tong_tien_sau_giam = ?, ten_khach_hang = ?, dia_chi_khach_hang = ?,
                    so_dien_thoai_khach_hang = ?, email_khach_hang = ?, ghi_chu = ?, ngay_cap_nhat = SYSDATETIME()
                WHERE id = ?
                """, yeuCau.getIdKhachHang(), tongKet.getIdPhieuGiamGia(), tongKet.getTamTinh(),
                tongKet.getTongThanhToan(), khachHang.getTenKhachHang(), khachHang.getDiaChi(),
                khachHang.getSoDienThoai(), khachHang.getEmail(), yeuCau.getGhiChu(), idDonCho);
    }

    private void kiemTraDonCho(Integer idDonCho, Integer idNhanVien) {
        List<Map<String, Object>> danhSach = jdbcTemplate.queryForList("""
                SELECT id FROM hoa_don WITH (UPDLOCK, HOLDLOCK)
                WHERE id = ? AND id_nhan_vien = ? AND trang_thai_hien_tai = 0
                  AND loai_don IN (0,2) AND ISNULL(xoa_mem, 0) = 0
                """, idDonCho, idNhanVien);
        if (danhSach.isEmpty()) {
            throw new IllegalArgumentException("Đơn chờ không còn tồn tại hoặc không thuộc nhân viên này.");
        }
    }

    @Transactional
    public void huyDonCho(Integer idDonCho, Integer idNhanVien) {
        kiemTraDonCho(idDonCho, idNhanVien);
        jdbcTemplate.update("DELETE FROM van_don_ghn WHERE id_hoa_don=? AND ma_van_don IS NULL",idDonCho);
        jdbcTemplate.update("DELETE FROM lich_su_hoa_don WHERE id_hoa_don = ?", idDonCho);
        jdbcTemplate.update("DELETE FROM giao_dich_thanh_toan WHERE id_hoa_don = ?", idDonCho);
        jdbcTemplate.update("DELETE FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", idDonCho);
        jdbcTemplate.update("DELETE FROM hoa_don WHERE id = ?", idDonCho);
    }

    private ThongTinKhachHang layThongTinKhachHang(Integer idKhachHang, YeuCauThanhToan yeuCau) {
        if (idKhachHang != null) {
            String cauLenhSql = """
                    SELECT TOP 1 ten_khach_hang, so_dien_thoai, email
                    FROM khach_hang
                    WHERE id = ? AND ISNULL(xoa_mem, 0) = 0
                    """;
            List<ThongTinKhachHang> danhSach = jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new ThongTinKhachHang(
                    ketQua.getString("ten_khach_hang"),
                    layGiaTriMacDinh(ketQua.getString("so_dien_thoai"), ""),
                    layGiaTriMacDinh(ketQua.getString("email"), ""),
                    DIA_CHI_CUA_HANG
            ), idKhachHang);
            if (!danhSach.isEmpty()) {
                return danhSach.get(0);
            }
        }

        return new ThongTinKhachHang(
                layGiaTriMacDinh(yeuCau.getTenKhachHang(), "Khách vãng lai"),
                layGiaTriMacDinh(yeuCau.getSoDienThoai(), ""),
                layGiaTriMacDinh(yeuCau.getEmail(), ""),
                layGiaTriMacDinh(yeuCau.getDiaChi(), DIA_CHI_CUA_HANG)
        );
    }

    private void ganSoNguyenCoTheNull(PreparedStatement cauLenh, int viTri, Integer giaTri) throws java.sql.SQLException {
        if (giaTri == null) {
            cauLenh.setObject(viTri, null);
        } else {
            cauLenh.setInt(viTri, giaTri);
        }
    }

    private String layGiaTriMacDinh(String giaTri, String giaTriMacDinh) {
        if (giaTri == null || giaTri.trim().isBlank()) {
            return giaTriMacDinh;
        }
        return giaTri.trim();
    }

    private static class ThongTinKhachHang {
        private final String tenKhachHang;
        private final String soDienThoai;
        private final String email;
        private final String diaChi;

        public ThongTinKhachHang(String tenKhachHang, String soDienThoai, String email, String diaChi) {
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.email = email;
            this.diaChi = diaChi;
        }

        public String getTenKhachHang() {
            return tenKhachHang;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public String getEmail() {
            return email;
        }

        public String getDiaChi() {
            return diaChi;
        }
    }

    public static class YeuCauThanhToan {
        private final Integer idKhachHang;
        private final String tenKhachHang;
        private final String soDienThoai;
        private final String email;
        private final String diaChi;
        private final Integer idPhuongThucThanhToan;
        private final String ghiChu;
        private final Integer idPhieuGiamGia;
        private final BigDecimal tienKhachDaThanhToan;
        private BigDecimal tienThua=BigDecimal.ZERO;
        public BigDecimal getTienThua(){return tienThua;}
        private BaoGiaGhn giaoHang;
        public BaoGiaGhn getGiaoHang(){return giaoHang;}
        public void setGiaoHang(BaoGiaGhn value){giaoHang=value;}

        public YeuCauThanhToan(Integer idKhachHang, String tenKhachHang, String soDienThoai, String email, String diaChi, Integer idPhuongThucThanhToan, String ghiChu, Integer idPhieuGiamGia) {
            this(idKhachHang,tenKhachHang,soDienThoai,email,diaChi,idPhuongThucThanhToan,ghiChu,idPhieuGiamGia,null);
        }
        public YeuCauThanhToan(Integer idKhachHang, String tenKhachHang, String soDienThoai, String email, String diaChi, Integer idPhuongThucThanhToan, String ghiChu, Integer idPhieuGiamGia, BigDecimal tienKhachDaThanhToan) {
            this.idKhachHang = idKhachHang;
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.email = email;
            this.diaChi = diaChi;
            this.idPhuongThucThanhToan = idPhuongThucThanhToan;
            this.ghiChu = ghiChu;
            this.idPhieuGiamGia = idPhieuGiamGia;
            this.tienKhachDaThanhToan=tienKhachDaThanhToan;
        }

        public BigDecimal getTienKhachDaThanhToan() {return tienKhachDaThanhToan;}

        public Integer getIdKhachHang() {
            return idKhachHang;
        }

        public String getTenKhachHang() {
            return tenKhachHang;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public String getEmail() {
            return email;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public Integer getIdPhuongThucThanhToan() {
            return idPhuongThucThanhToan;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public Integer getIdPhieuGiamGia() {
            return idPhieuGiamGia;
        }
    }

    private static class UuDaiHoaDon {
        private final Integer idPhieuGiamGia;
        private final String tenPhieu;
        private final BigDecimal tienGiam;

        public UuDaiHoaDon(Integer idPhieuGiamGia, String tenPhieu, BigDecimal tienGiam) {
            this.idPhieuGiamGia = idPhieuGiamGia;
            this.tenPhieu = tenPhieu;
            this.tienGiam = tienGiam;
        }

        public Integer getIdPhieuGiamGia() {
            return idPhieuGiamGia;
        }

        public String getTenPhieu() {
            return tenPhieu;
        }

        public BigDecimal getTienGiam() {
            return tienGiam;
        }

        static UuDaiHoaDon khongApDung() {
            return new UuDaiHoaDon(null, "Không áp dụng", BigDecimal.ZERO);
        }
    }
}
