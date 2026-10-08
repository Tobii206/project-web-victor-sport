package com.example.victorsport.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChiTietSanPhamRequest {
    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Integer idSanPham;

    @NotNull(message = "Vui lòng chọn màu sắc")
    private Integer idMauSac;

    @NotNull(message = "Vui lòng chọn kích thước")
    private Integer idKichThuoc;

    @NotNull(message = "Vui lòng chọn form chân")
    private Integer idFormChan;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer soLuong;

    @NotNull(message = "Giá niêm yết không được để trống")
    @DecimalMin(value = "0.0", message = "Giá niêm yết phải >= 0")
    private BigDecimal giaNiemYet;

    @DecimalMin(value = "0.0", message = "Giá bán phải >= 0")
    private BigDecimal giaBan;

    private Boolean trangThai = true;
    private String ghiChu;
    private Integer nguoiThaoTac;
}
