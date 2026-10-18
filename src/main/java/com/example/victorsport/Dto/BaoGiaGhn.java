package com.example.victorsport.Dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

// Chỉ tạo ở máy chủ sau khi GHN trả phí, không nhận phí từ trình duyệt.
public record BaoGiaGhn(Map<Integer,Integer> gio, String ten, String sdt, String diaChi,
                       String phuongThuc, BigDecimal tienHang, BigDecimal phi,
                       int tinh, int xa, String tenTinh, String tenXa,
                       int khoiLuong, int dai, int rong, int cao, Instant hetHan) {
    public BaoGiaGhn { gio=Map.copyOf(gio); }
    public String diaChiChiTiet() {
        String duoi=", "+tenXa+", "+tenTinh;
        return diaChi.endsWith(duoi)?diaChi.substring(0,diaChi.length()-duoi.length()):diaChi;
    }
    public void kiemTra(Map<Integer,Integer> gioMoi,String tenMoi,String sdtMoi,String diaChiMoi,String cachTra,BigDecimal giaMoi) {
        if(hetHan==null || !hetHan.isAfter(Instant.now()) || !gio.equals(gioMoi)
                || !ten.equals(tenMoi) || !sdt.equals(sdtMoi) || !diaChi.equals(diaChiMoi)
                || !phuongThuc.equals(cachTra) || tienHang.compareTo(giaMoi)!=0)
            throw new IllegalArgumentException("Giỏ hàng, địa chỉ hoặc thông tin thanh toán đã thay đổi. Vui lòng lấy lại phí GHN.");
    }
}
