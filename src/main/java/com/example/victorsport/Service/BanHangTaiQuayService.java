package com.example.victorsport.Service;

import com.example.victorsport.Dto.GioHangTaiQuay;
import com.example.victorsport.Dto.LuaChonKhachHang;
import com.example.victorsport.Dto.LuaChonPhieuGiamGia;
import com.example.victorsport.Dto.LuaChonPhuongThucThanhToan;
import com.example.victorsport.Dto.LuaChonSanPham;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamTrongGio;
import com.example.victorsport.Dto.TongKetBanHang;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Service
public class BanHangTaiQuayService {
    private final JdbcTemplate jdbcTemplate;

    public BanHangTaiQuayService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LuaChonSanPham> timSanPham(String tuKhoa) {
        String tuKhoaTimKiem = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        String cauLenhSql = """
                SELECT TOP 50 ct.id AS id_chi_tiet_san_pham, sp.ma_san_pham, sp.ten_san_pham, th.ten_thuong_hieu,
                       ms.ten_mau_sac, kt.ten_kich_thuoc, ISNULL(ct.so_luong, 0) AS so_luong,
                       ISNULL(ct.gia_ban, ct.gia_niem_yet) AS gia_ban
                FROM chi_tiet_san_pham ct
                JOIN san_pham sp ON sp.id = ct.id_san_pham
                JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE ISNULL(ct.xoa_mem, 0) = 0
                  AND ISNULL(sp.xoa_mem, 0) = 0
                  AND ISNULL(ct.trang_thai, 1) = 1
                  AND ISNULL(sp.trang_thai_kinh_doanh, 1) = 1
                  AND (sp.ten_san_pham LIKE ? OR sp.ma_san_pham LIKE ? OR th.ten_thuong_hieu LIKE ?)
                ORDER BY sp.ten_san_pham, kt.gia_tri_kich_thuoc
                """;
        return jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> new LuaChonSanPham(
                ketQua.getInt("id_chi_tiet_san_pham"),
                ketQua.getString("ma_san_pham"),
                ketQua.getString("ten_san_pham"),
                ketQua.getString("ten_thuong_hieu"),
                ketQua.getString("ten_mau_sac"),
                ketQua.getString("ten_kich_thuoc"),
                ketQua.getInt("so_luong"),
                ketQua.getBigDecimal("gia_ban")
        ), tuKhoaTimKiem, tuKhoaTimKiem, tuKhoaTimKiem);
    }

    public SanPhamTrongGio taoSanPhamTrongGio(Integer idChiTietSanPham, Integer soLuong) {
        String cauLenhSql = """
                SELECT ct.id AS id_chi_tiet_san_pham, sp.ma_san_pham, sp.ten_san_pham, ms.ten_mau_sac,
                       kt.ten_kich_thuoc, ISNULL(ct.so_luong, 0) AS so_luong,
                       ISNULL(ct.gia_ban, ct.gia_niem_yet) AS gia_ban
                FROM chi_tiet_san_pham ct
                JOIN san_pham sp ON sp.id = ct.id_san_pham
                JOIN mau_sac ms ON ms.id = ct.id_mau_sac
                JOIN kich_thuoc kt ON kt.id = ct.id_kich_thuoc
                WHERE ct.id = ?
                  AND ISNULL(ct.xoa_mem, 0) = 0
                  AND ISNULL(sp.xoa_mem, 0) = 0
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
                  AND (ten_khach_hang LIKE ? OR ma_khach_hang LIKE ? OR so_dien_thoai LIKE ? OR email LIKE ?)
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
        BigDecimal tienGiam = giamTheoHoaDon.max(giamTheoSanPham).setScale(2, RoundingMode.HALF_UP);
        boolean apDungPhieu = giamTheoHoaDon.compareTo(BigDecimal.ZERO) > 0
                && giamTheoHoaDon.compareTo(giamTheoSanPham) >= 0;
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
        for (SanPhamTrongGio sanPham : gioHang.getDanhSachSanPham()) {
            String cauLenhSql = """
                    SELECT TOP 1 dgg.loai_giam_gia, COALESCE(ctdgg.gia_tri_giam_rieng, dgg.gia_tri_giam_gia) AS gia_tri,
                           ctdgg.so_tien_giam_toi_da_rieng
                    FROM chi_tiet_dot_giam_gia ctdgg
                    JOIN dot_giam_gia dgg ON dgg.id = ctdgg.id_dot_giam_gia
                    WHERE ctdgg.id_chi_tiet_san_pham = ?
                      AND ISNULL(ctdgg.xoa_mem, 0) = 0
                      AND ISNULL(dgg.xoa_mem, 0) = 0
                      AND ISNULL(ctdgg.trang_thai, 1) = 1
                      AND ISNULL(dgg.trang_thai, 1) = 1
                      AND CAST(GETDATE() AS DATE) BETWEEN dgg.ngay_bat_dau AND dgg.ngay_ket_thuc
                    ORDER BY dgg.muc_uu_tien DESC, gia_tri DESC
                    """;
            List<BigDecimal> danhSachTienGiam = jdbcTemplate.query(cauLenhSql, (ketQua, soDong) -> {
                boolean laTienMat = ketQua.getBoolean("loai_giam_gia");
                BigDecimal giaTri = ketQua.getBigDecimal("gia_tri");
                BigDecimal thanhTien = sanPham.getThanhTien();
                BigDecimal tienGiamSanPham;
                if (laTienMat) {
                    tienGiamSanPham = giaTri.multiply(BigDecimal.valueOf(sanPham.getSoLuongMua()));
                } else {
                    tienGiamSanPham = thanhTien.multiply(giaTri).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                BigDecimal tienGiamToiDa = ketQua.getBigDecimal("so_tien_giam_toi_da_rieng");
                if (tienGiamToiDa != null && tienGiamToiDa.compareTo(BigDecimal.ZERO) > 0) {
                    tienGiamSanPham = tienGiamSanPham.min(tienGiamToiDa);
                }
                return tienGiamSanPham;
            }, sanPham.getIdChiTietSanPham());
            if (!danhSachTienGiam.isEmpty()) {
                tongTienGiam = tongTienGiam.add(danhSachTienGiam.get(0));
            }
        }
        return tongTienGiam.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Integer thanhToan(GioHangTaiQuay gioHang, YeuCauThanhToan yeuCau, NguoiDungDangNhap nhanVien) {
        if (gioHang == null || gioHang.isRong()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống.");
        }

        for (SanPhamTrongGio sanPham : gioHang.getDanhSachSanPham()) {
            Integer soLuongTon = jdbcTemplate.queryForObject(
                    "SELECT ISNULL(so_luong, 0) FROM chi_tiet_san_pham WHERE id = ?",
                    Integer.class,
                    sanPham.getIdChiTietSanPham()
            );
            if (soLuongTon == null || sanPham.getSoLuongMua() > soLuongTon) {
                throw new IllegalArgumentException("Sản phẩm " + sanPham.getTenSanPham() + " không đủ tồn kho.");
            }
        }

        TongKetBanHang tongKet = tinhTongKet(gioHang, yeuCau.getIdPhieuGiamGia());
        Integer idKhachHang = yeuCau.getIdKhachHang();
        ThongTinKhachHang thongTinKhachHang = layThongTinKhachHang(idKhachHang, yeuCau);
        Integer idPhuongThucThanhToan = yeuCau.getIdPhuongThucThanhToan() == null ? 1 : yeuCau.getIdPhuongThucThanhToan();

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

        Integer idHoaDon = Objects.requireNonNull(khoaTuTang.getKey()).intValue();
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
                """, idHoaDon, idPhuongThucThanhToan, tongKet.getTongThanhToan(),
                "POS-" + idHoaDon, "HD" + idHoaDon, nhanVien.getId(), "Thanh toán tại quầy");

        jdbcTemplate.update("""
                INSERT INTO lich_su_hoa_don (
                    id_hoa_don, trang_thai, thoi_gian, ghi_chu, nguoi_cap_nhat, nguoi_thuc_hien, loai_nguoi_thuc_hien
                ) VALUES (?, 5, SYSDATETIME(), ?, ?, ?, 'NHAN_VIEN')
                """, idHoaDon, "Hoàn tất bán hàng tại quầy", nhanVien.getId(), nhanVien.getId());

        if (tongKet.getIdPhieuGiamGia() != null) {
            jdbcTemplate.update("""
                    UPDATE phieu_giam_gia
                    SET so_luong_su_dung = so_luong_su_dung - 1
                    WHERE id = ? AND so_luong_su_dung > 0
                    """, tongKet.getIdPhieuGiamGia());
        }

        return idHoaDon;
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
                    "Mua trực tiếp tại cửa hàng Victor Sport"
            ), idKhachHang);
            if (!danhSach.isEmpty()) {
                return danhSach.get(0);
            }
        }

        return new ThongTinKhachHang(
                layGiaTriMacDinh(yeuCau.getTenKhachHang(), "Khách vãng lai"),
                layGiaTriMacDinh(yeuCau.getSoDienThoai(), ""),
                layGiaTriMacDinh(yeuCau.getEmail(), ""),
                layGiaTriMacDinh(yeuCau.getDiaChi(), "Mua trực tiếp tại cửa hàng Victor Sport")
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

        public YeuCauThanhToan(Integer idKhachHang, String tenKhachHang, String soDienThoai, String email, String diaChi, Integer idPhuongThucThanhToan, String ghiChu, Integer idPhieuGiamGia) {
            this.idKhachHang = idKhachHang;
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.email = email;
            this.diaChi = diaChi;
            this.idPhuongThucThanhToan = idPhuongThucThanhToan;
            this.ghiChu = ghiChu;
            this.idPhieuGiamGia = idPhieuGiamGia;
        }

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
