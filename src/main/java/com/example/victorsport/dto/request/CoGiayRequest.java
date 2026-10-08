package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoGiayRequest {
    @NotBlank(message = "Tên cổ giày không được để trống")
    private String tenCoGiay;
    private Boolean trangThai = true;
}
