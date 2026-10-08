package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.*;
import com.example.victorsport.dto.request.DatHangRequest;
import com.example.victorsport.dto.request.ItemGioHangRequest;
import com.example.victorsport.dto.response.DatHangResponse;
import com.example.victorsport.dto.response.TraCuuDonHangResponse;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.*;
import com.example.victorsport.service.DonHangOnlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DonHangOnlineServiceImpl implements DonHangOnlineService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final AnhChiTietSanPhamRepository anhChiTietSanPhamRepository;

    public DonHangOnlineServiceImpl(HoaDonRepository hoaDonRepository,
                                   HoaDonChiTietRepository hoaDonChiTietRepository,
                                   ChiTietSanPhamRepository chiTietSanPhamRepository,
                                   LichSuHoaDonRepository lichSuHoaDonRepository,
                                   PhieuGiamGiaRepository phieuGiamGiaRepository,
                                   PhuongThucThanhToanRepository phuongThucThanhToanRepository,
                                   GiaoDichThanhToanRepository giaoDichThanhToanRepository,
                                   AnhChiTietSanPhamRepository anhChiTietSanPhamRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.lichSuHoaDonRepository = lichSuHoaDonRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.giaoDichThanhToanRepository = giaoDichThanhToanRepository;
        this.anhChiTietSanPhamRepository = anhChiTietSanPhamRepository;
    }

    @Override
    @Transactional
    public DatHangResponse datHang(DatHangRequest request) {
        if (request.getDanhSachSanPham() == null || request.getDanhSachSanPham().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng của bạn đang trống!");
        }

        BigDecimal tongTienHang = BigDecimal.ZERO;
        Map<Integer, ChiTietSanPham> spMap = new HashMap<>();

        // 1. Kiểm tra tồn kho & tính tổng tiền hàng
        for (ItemGioHangRequest item : request.getDanhSachSanPham()) {
            ChiTietSanPham ct = chiTietSanPhamRepository.findByIdAndXoaMemFalse(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh (ID: " + item.getIdChiTietSanPham() + ")"));

            if (ct.getSoLuong() == null || ct.getSoLuong() < item.getSoLuong()) {
                throw new IllegalArgumentException("Sản phẩm '" + ct.getSanPham().getTenSanPham() +
                        " - " + ct.getMauSac().getTenMauSac() + " (Size " + ct.getKichThuoc().getTenKichThuoc() + ")' không đủ số lượng trong kho! (Còn " + (ct.getSoLuong() == null ? 0 : ct.getSoLuong()) + ")");
            }

            BigDecimal donGia = ct.getGiaBan() != null ? ct.getGiaBan() : ct.getGiaNiemYet();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(item.getSoLuong()));
            tongTienHang = tongTienHang.add(thanhTien);
            spMap.put(ct.getId(), ct);
        }

        // 2. Tính phí vận chuyển (Đơn trên 1 triệu freeship, còn lại 30.000đ)
        BigDecimal phiVanChuyen = tongTienHang.compareTo(new BigDecimal("1000000")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("30000");

        // 3. Xử lý Voucher / Phiếu giảm giá
        BigDecimal tongTienGiam = BigDecimal.ZERO;
        PhieuGiamGia pgg = null;
        if (request.getMaPhieuGiamGia() != null && !request.getMaPhieuGiamGia().trim().isEmpty()) {
            Optional<PhieuGiamGia> pggOpt = phieuGiamGiaRepository.findByMaPhieuGiamGiaAndXoaMemFalse(request.getMaPhieuGiamGia().trim().toUpperCase());
            if (pggOpt.isPresent()) {
                pgg = pggOpt.get();
                LocalDate today = LocalDate.now();
                if (Boolean.TRUE.equals(pgg.getTrangThai())
                        && (pgg.getNgayBatDau() == null || !today.isBefore(pgg.getNgayBatDau()))
                        && (pgg.getNgayKetThuc() == null || !today.isAfter(pgg.getNgayKetThuc()))
                        && (pgg.getSoLuongSuDung() == null || pgg.getSoLuongSuDung() > 0)
                        && (pgg.getHoaDonToiThieu() == null || tongTienHang.compareTo(pgg.getHoaDonToiThieu()) >= 0)) {

                    if (Boolean.TRUE.equals(pgg.getLoaiPhieuGiamGia())) {
                        // Giảm tiền cố định
                        tongTienGiam = pgg.getGiaTriGiamGia();
                    } else {
                        // Giảm %
                        tongTienGiam = tongTienHang.multiply(pgg.getGiaTriGiamGia()).divide(new BigDecimal("100"));
                        if (pgg.getSoTienGiamToiDa() != null && tongTienGiam.compareTo(pgg.getSoTienGiamToiDa()) > 0) {
                            tongTienGiam = pgg.getSoTienGiamToiDa();
                        }
                    }

                    if (pgg.getSoLuongSuDung() != null && pgg.getSoLuongSuDung() > 0) {
                        pgg.setSoLuongSuDung(pgg.getSoLuongSuDung() - 1);
                        phieuGiamGiaRepository.save(pgg);
                    }
                }
            }
        }

        BigDecimal tongTienSauGiam = tongTienHang.subtract(tongTienGiam).max(BigDecimal.ZERO).add(phiVanChuyen);

        // 4. Tạo Hóa Đơn
        HoaDon hd = new HoaDon();
        hd.setPhieuGiamGia(pgg);
        hd.setLoaiDon(1); // 1 = Online
        hd.setPhiVanChuyen(phiVanChuyen);
        hd.setTongTien(tongTienHang);
        hd.setTongTienSauGiam(tongTienSauGiam);
        hd.setTenKhachHang(request.getTenKhachHang());
        hd.setSoDienThoaiKhachHang(request.getSoDienThoai());
        hd.setEmailKhachHang(request.getEmail());
        hd.setDiaChiKhachHang(request.getDiaChi());
        hd.setGhiChu(request.getGhiChu());
        hd.setTrangThaiHienTai(1); // 1 = Chờ xác nhận
        hd.setNgayTao(LocalDateTime.now());
        hd.setXoaMem(false);
        HoaDon savedHd = hoaDonRepository.save(hd);

        // 5. Lưu HoaDonChiTiet và trừ kho
        for (ItemGioHangRequest item : request.getDanhSachSanPham()) {
            ChiTietSanPham ct = spMap.get(item.getIdChiTietSanPham());
            BigDecimal donGia = ct.getGiaBan() != null ? ct.getGiaBan() : ct.getGiaNiemYet();

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(savedHd);
            hdct.setChiTietSanPham(ct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(donGia);
            hdct.setGhiChu("Đơn hàng online website");
            hdct.setXoaMem(false);
            hoaDonChiTietRepository.save(hdct);

            // Trừ kho
            ct.setSoLuong(ct.getSoLuong() - item.getSoLuong());
            chiTietSanPhamRepository.save(ct);
        }

        // 6. Ghi vết lịch sử đơn hàng
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setHoaDon(savedHd);
        ls.setTrangThai(1);
        ls.setThoiGian(LocalDateTime.now());
        ls.setGhiChu("Khách hàng đã đặt đơn hàng online thành công qua Website");
        ls.setLoaiNguoiThucHien("KHACH_HANG");
        ls.setXoaMem(false);
        lichSuHoaDonRepository.save(ls);

        // 7. Xử lý thanh toán
        Integer idPttt = request.getIdPhuongThucThanhToan() != null ? request.getIdPhuongThucThanhToan() : 4; // Mặc định 4: COD
        PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(idPttt).orElse(null);
        String tenPttt = pttt != null ? pttt.getTenPhuongThucThanhToan() : "Thanh toán khi nhận hàng (COD)";

        GiaoDichThanhToan gd = new GiaoDichThanhToan();
        gd.setHoaDon(savedHd);
        gd.setPhuongThucThanhToan(pttt);
        gd.setSoTien(tongTienSauGiam);
        gd.setTrangThai(idPttt == 2 ? "cho_thanh_toan" : "khoi_tao");
        gd.setThoiGianTao(LocalDateTime.now());
        gd.setXoaMem(false);

        String qrUrl = null;
        if (idPttt == 2) {
            // Ngân hàng VietQR - Tạo QR chuyển khoản tự động
            String noiDungCk = savedHd.getMaHoaDon() != null ? savedHd.getMaHoaDon() : ("HD" + savedHd.getId());
            qrUrl = "https://img.vietqr.io/image/MB-0388999888-compact2.png?amount=" +
                    tongTienSauGiam.longValue() +
                    "&addInfo=" + noiDungCk +
                    "&accountName=VICTOR%20SPORT";
            gd.setDuongDanThanhToan(qrUrl);
            gd.setMaThamChieu(noiDungCk);
        }
        giaoDichThanhToanRepository.save(gd);

        return DatHangResponse.builder()
                .idHoaDon(savedHd.getId())
                .maHoaDon(savedHd.getMaHoaDon() != null ? savedHd.getMaHoaDon() : "HD" + String.format("%05d", savedHd.getId()))
                .tongTien(tongTienHang)
                .tongTienGiam(tongTienGiam)
                .phiVanChuyen(phiVanChuyen)
                .tongTienSauGiam(tongTienSauGiam)
                .tenPhuongThucThanhToan(tenPttt)
                .qrPaymentUrl(qrUrl)
                .thongBao("Đặt hàng thành công! Đơn hàng của bạn đang được xử lý.")
                .build();
    }

    @Override
    public TraCuuDonHangResponse traCuuDonHang(String maHoaDon) {
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + maHoaDon));

        List<HoaDonChiTiet> listHdct = hoaDonChiTietRepository.findByHoaDonId(hd.getId());
        List<TraCuuDonHangResponse.ItemDonHangResponse> items = listHdct.stream().map(item -> {
            ChiTietSanPham ct = item.getChiTietSanPham();
            String anhDaiDien = "/images/default-shoe.png";
            List<AnhChiTietSanPham> anhs = anhChiTietSanPhamRepository.findByChiTietSanPhamIdAndXoaMemFalse(ct.getId());
            if (!anhs.isEmpty()) {
                anhDaiDien = anhs.stream().filter(a -> Boolean.TRUE.equals(a.getLaAnhDaiDien()))
                        .map(AnhChiTietSanPham::getDuongDanAnh)
                        .findFirst()
                        .orElse(anhs.get(0).getDuongDanAnh());
            }

            return TraCuuDonHangResponse.ItemDonHangResponse.builder()
                    .tenSanPham(ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "")
                    .mauSac(ct.getMauSac() != null ? ct.getMauSac().getTenMauSac() : "")
                    .kichThuoc(ct.getKichThuoc() != null ? ct.getKichThuoc().getTenKichThuoc() : "")
                    .formChan(ct.getFormChan() != null ? ct.getFormChan().getTenFormChan() : "")
                    .soLuong(item.getSoLuong())
                    .donGia(item.getDonGia())
                    .thanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())))
                    .hinhAnh(anhDaiDien)
                    .build();
        }).toList();

        List<LichSuHoaDon> listLs = lichSuHoaDonRepository.findByHoaDonIdOrderByThoiGianAsc(hd.getId());
        List<TraCuuDonHangResponse.LichSuDonHangResponse> lichSus = listLs.stream().map(ls ->
                TraCuuDonHangResponse.LichSuDonHangResponse.builder()
                        .trangThai(ls.getTrangThai())
                        .tenTrangThai(getTenTrangThai(ls.getTrangThai()))
                        .thoiGian(ls.getThoiGian())
                        .ghiChu(ls.getGhiChu())
                        .nguoiThucHien(ls.getLoaiNguoiThucHien())
                        .build()
        ).toList();

        // Lấy phương thức thanh toán
        List<GiaoDichThanhToan> gdList = giaoDichThanhToanRepository.findByHoaDonId(hd.getId());
        String tenPttt = "COD - Thanh toán khi nhận hàng";
        if (!gdList.isEmpty() && gdList.get(0).getPhuongThucThanhToan() != null) {
            tenPttt = gdList.get(0).getPhuongThucThanhToan().getTenPhuongThucThanhToan();
        }

        return TraCuuDonHangResponse.builder()
                .idHoaDon(hd.getId())
                .maHoaDon(hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + String.format("%05d", hd.getId()))
                .tenKhachHang(hd.getTenKhachHang())
                .soDienThoai(hd.getSoDienThoaiKhachHang())
                .email(hd.getEmailKhachHang())
                .diaChi(hd.getDiaChiKhachHang())
                .ghiChu(hd.getGhiChu())
                .trangThai(hd.getTrangThaiHienTai())
                .tenTrangThai(getTenTrangThai(hd.getTrangThaiHienTai()))
                .tongTien(hd.getTongTien())
                .tongTienGiam(hd.getTongTienGiam() != null ? hd.getTongTienGiam() : BigDecimal.ZERO)
                .phiVanChuyen(hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO)
                .tongTienSauGiam(hd.getTongTienSauGiam())
                .tenPhuongThucThanhToan(tenPttt)
                .ngayTao(hd.getNgayTao())
                .ngayThanhToan(hd.getNgayThanhToan())
                .danhSachSanPham(items)
                .lichSu(lichSus)
                .build();
    }

    @Override
    public Map<String, Object> kiemTraVoucher(String maVoucher, BigDecimal tongTien) {
        Map<String, Object> res = new HashMap<>();
        if (maVoucher == null || maVoucher.trim().isEmpty()) {
            res.put("hopLe", false);
            res.put("thongBao", "Vui lòng nhập mã voucher!");
            return res;
        }

        Optional<PhieuGiamGia> opt = phieuGiamGiaRepository.findByMaPhieuGiamGiaAndXoaMemFalse(maVoucher.trim().toUpperCase());
        if (opt.isEmpty()) {
            res.put("hopLe", false);
            res.put("thongBao", "Mã voucher không tồn tại!");
            return res;
        }

        PhieuGiamGia pgg = opt.get();
        LocalDate now = LocalDate.now();

        if (!Boolean.TRUE.equals(pgg.getTrangThai())) {
            res.put("hopLe", false);
            res.put("thongBao", "Voucher hiện đang bị khóa!");
            return res;
        }
        if (pgg.getNgayBatDau() != null && now.isBefore(pgg.getNgayBatDau())) {
            res.put("hopLe", false);
            res.put("thongBao", "Voucher chưa đến thời gian áp dụng!");
            return res;
        }
        if (pgg.getNgayKetThuc() != null && now.isAfter(pgg.getNgayKetThuc())) {
            res.put("hopLe", false);
            res.put("thongBao", "Voucher đã hết hạn sử dụng!");
            return res;
        }
        if (pgg.getSoLuongSuDung() != null && pgg.getSoLuongSuDung() <= 0) {
            res.put("hopLe", false);
            res.put("thongBao", "Voucher đã hết lượt sử dụng!");
            return res;
        }
        if (pgg.getHoaDonToiThieu() != null && tongTien.compareTo(pgg.getHoaDonToiThieu()) < 0) {
            res.put("hopLe", false);
            res.put("thongBao", "Đơn hàng tối thiểu để áp dụng voucher là " + pgg.getHoaDonToiThieu().longValue() + " đ!");
            return res;
        }

        BigDecimal giamGia;
        if (Boolean.TRUE.equals(pgg.getLoaiPhieuGiamGia())) {
            giamGia = pgg.getGiaTriGiamGia();
        } else {
            giamGia = tongTien.multiply(pgg.getGiaTriGiamGia()).divide(new BigDecimal("100"));
            if (pgg.getSoTienGiamToiDa() != null && giamGia.compareTo(pgg.getSoTienGiamToiDa()) > 0) {
                giamGia = pgg.getSoTienGiamToiDa();
            }
        }

        res.put("hopLe", true);
        res.put("maVoucher", pgg.getMaPhieuGiamGia());
        res.put("tenVoucher", pgg.getTenPhieuGiamGia());
        res.put("soTienGiam", giamGia);
        res.put("thongBao", "Áp dụng mã giảm giá thành công: -" + giamGia.longValue() + " đ");
        return res;
    }

    private String getTenTrangThai(Integer status) {
        if (status == null) return "Không xác định";
        return switch (status) {
            case 1 -> "Chờ xác nhận";
            case 2 -> "Đã xác nhận";
            case 3 -> "Đang chuẩn bị hàng";
            case 4 -> "Đang giao hàng";
            case 5 -> "Giao thành công";
            case 6 -> "Đã hủy đơn";
            default -> "Đang xử lý (" + status + ")";
        };
    }
}
