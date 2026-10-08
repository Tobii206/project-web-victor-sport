package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnhChiTietSanPhamRequest {
    @NotBlank(message = "Đường dẫn ảnh không được để trống")
    private String duongDanAnh;
    private Boolean laAnhDaiDien = false;
    private String moTa;
}
