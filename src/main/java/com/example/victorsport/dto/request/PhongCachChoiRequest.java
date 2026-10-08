package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhongCachChoiRequest {
    @NotBlank(message = "Tên phong cách chơi không được để trống")
    private String tenPhongCach;
    private Boolean trangThai = true;
}
