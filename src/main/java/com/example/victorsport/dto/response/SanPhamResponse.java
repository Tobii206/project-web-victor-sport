package com.example.victorsport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private Integer idThuongHieu;
    private String tenThuongHieu;
    private Integer idXuatXu;
    private String tenXuatXu;
    private Integer idViTriThiDau;
    private String tenViTriThiDau;
    private Integer idPhongCachChoi;
    private String tenPhongCachChoi;
    private Integer idCoGiay;
    private String tenCoGiay;
    private Integer idChatLieu;
    private String tenChatLieu;
    private String moTaNgan;
    private String moTaChiTiet;
    private Boolean trangThaiKinhDoanh;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}
