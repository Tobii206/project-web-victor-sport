package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XuatXuRequest {
    @NotBlank(message = "Tên xuất xứ không được để trống")
    private String tenXuatXu;
    private Boolean trangThai = true;
}
