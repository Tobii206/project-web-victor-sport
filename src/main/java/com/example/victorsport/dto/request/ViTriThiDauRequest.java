package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViTriThiDauRequest {
    @NotBlank(message = "Tên vị trí thi đấu không được để trống")
    private String tenViTri;
    private Boolean trangThai = true;
}
