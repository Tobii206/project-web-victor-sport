package com.example.victorsport.service;

import com.example.victorsport.Entity.PhieuGiamGia;
import com.example.victorsport.dto.request.DatHangRequest;
import com.example.victorsport.dto.response.DatHangResponse;
import com.example.victorsport.dto.response.TraCuuDonHangResponse;

import java.math.BigDecimal;
import java.util.Map;

public interface DonHangOnlineService {
    DatHangResponse datHang(DatHangRequest request);
    TraCuuDonHangResponse traCuuDonHang(String maHoaDon);
    Map<String, Object> kiemTraVoucher(String maVoucher, BigDecimal tongTien);
}
