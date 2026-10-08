package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThuongHieuRequest {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String tenThuongHieu;
    private Boolean trangThai = true;
}
