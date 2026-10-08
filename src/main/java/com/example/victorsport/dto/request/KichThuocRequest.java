package com.example.victorsport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class KichThuocRequest {
    @NotBlank(message = "Tên kích thước không được để trống")
    private String tenKichThuoc;
    private BigDecimal giaTriKichThuoc;
    private Boolean trangThai = true;
}
