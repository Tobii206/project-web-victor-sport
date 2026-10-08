package com.example.victorsport.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemGioHangRequest {
    @NotNull(message = "ID chi tiết sản phẩm không được trống")
    private Integer idChiTietSanPham;

    @NotNull(message = "Số lượng không được trống")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer soLuong;
}
