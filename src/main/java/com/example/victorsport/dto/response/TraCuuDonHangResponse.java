package com.example.victorsport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraCuuDonHangResponse {
    private Integer idHoaDon;
    private String maHoaDon;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String ghiChu;
    private Integer trangThai;
    private String tenTrangThai;
    private BigDecimal tongTien;
    private BigDecimal tongTienGiam;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;
    private String tenPhuongThucThanhToan;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private List<ItemDonHangResponse> danhSachSanPham;
    private List<LichSuDonHangResponse> lichSu;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDonHangResponse {
        private String tenSanPham;
        private String mauSac;
        private String kichThuoc;
        private String formChan;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
        private String hinhAnh;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LichSuDonHangResponse {
        private Integer trangThai;
        private String tenTrangThai;
        private LocalDateTime thoiGian;
        private String ghiChu;
        private String nguoiThucHien;
    }
}
