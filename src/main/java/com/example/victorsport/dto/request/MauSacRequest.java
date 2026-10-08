package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MauSacRequest {
    @NotBlank(message = "Tên màu sắc không được để trống")
    private String tenMauSac;
    private String maMauHex;
    private Boolean trangThai = true;
}
