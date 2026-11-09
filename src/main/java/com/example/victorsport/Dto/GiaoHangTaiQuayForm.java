package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

public record GiaoHangTaiQuayForm(
        @NotBlank @Size(max=100) String ten,
        @NotBlank @Pattern(regexp="0[0-9]{9}") String sdt,
        @NotBlank @Size(max=255) String diaChi,
        @NotNull @Positive Integer tinh,
        @NotNull @Positive Integer xa,
        @NotNull @Positive Integer paymentMethodId) {}
