package com.example.victorsport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnhChiTietResponse {
    private Integer id;
    private String duongDanAnh;
    private Boolean laAnhDaiDien;
    private String moTa;
}
