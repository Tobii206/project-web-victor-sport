package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormChanRequest {
    @NotBlank(message = "Tên form chân không được để trống")
    private String tenFormChan;
    private Boolean trangThai = true;
}
