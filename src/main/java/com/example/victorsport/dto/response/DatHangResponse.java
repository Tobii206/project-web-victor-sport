package com.example.victorsport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatHangResponse {
    private Integer idHoaDon;
    private String maHoaDon;
    private BigDecimal tongTien;
    private BigDecimal tongTienGiam;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;
    private String tenPhuongThucThanhToan;
    private String qrPaymentUrl;
    private String thongBao;
}
