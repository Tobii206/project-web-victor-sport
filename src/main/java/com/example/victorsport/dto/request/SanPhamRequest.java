package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanPhamRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String tenSanPham;

    @NotNull(message = "Vui lòng chọn thương hiệu")
    private Integer idThuongHieu;

    private Integer idXuatXu;
    private Integer idViTriThiDau;
    private Integer idPhongCachChoi;
    private Integer idCoGiay;
    private Integer idChatLieu;

    private String moTaNgan;
    private String moTaChiTiet;
    private Boolean trangThaiKinhDoanh = true;
    private Integer nguoiThaoTac;
}
