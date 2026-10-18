package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

public record TinhPhiGhnForm(
        @NotNull @Positive Integer tinh,
        @NotNull @Positive Integer huyen,
        @NotBlank @Pattern(regexp="[0-9]{1,20}") String xa,
        @NotNull @Min(1) @Max(19999) Integer khoiLuong,
        @NotNull @Min(1) @Max(150) Integer dai,
        @NotNull @Min(1) @Max(150) Integer rong,
        @NotNull @Min(1) @Max(150) Integer cao,
        @NotNull @Min(0) @Max(5000000) Integer giaTriHang) {}
