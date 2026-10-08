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
public class ChiTietSanPhamResponse {
    private Integer id;
    private String maChiTietSanPham;
    private Integer idSanPham;
    private String tenSanPham;
    private Integer idMauSac;
    private String tenMauSac;
    private String maMauHex;
    private Integer idKichThuoc;
    private String tenKichThuoc;
    private BigDecimal giaTriKichThuoc;
    private Integer idFormChan;
    private String tenFormChan;
    private Integer soLuong;
    private BigDecimal giaNiemYet;
    private BigDecimal giaBan;
    private Boolean trangThai;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private List<AnhChiTietResponse> danhSachAnh;
}
