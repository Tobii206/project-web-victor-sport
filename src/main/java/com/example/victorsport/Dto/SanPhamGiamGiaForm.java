package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

public class SanPhamGiamGiaForm {
    @NotNull(message = "Vui lòng chọn mẫu giày.")
    @Positive(message = "Mẫu giày không hợp lệ.")
    private Integer id_chi_tiet_san_pham;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự.")
    private String ghi_chu;

    public Integer getId_chi_tiet_san_pham() { return id_chi_tiet_san_pham; }
    public void setId_chi_tiet_san_pham(Integer id_chi_tiet_san_pham) {
        this.id_chi_tiet_san_pham = id_chi_tiet_san_pham;
    }

    public String getGhi_chu() { return ghi_chu; }
    public void setGhi_chu(String ghi_chu) {
        this.ghi_chu = ghi_chu == null ? null : ghi_chu.trim();
    }

}
