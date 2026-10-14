package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class ThanhToanForm {
    @NotBlank(message = "Vui lòng nhập tên người nhận.")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự.")
    private String ten;

    @NotBlank(message = "Vui lòng nhập số điện thoại.")
    @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại phải đủ 10 số và bắt đầu bằng số 0.")
    private String sdt;

    @NotBlank(message = "Vui lòng nhập địa chỉ chi tiết.")
    @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự.")
    private String diaChi;

    public String getTen() { return ten; }

    public void setTen(String ten) {
        this.ten = ten == null ? null : ten.trim();
    }

    public String getSdt() { return sdt; }

    public void setSdt(String sdt) {
        this.sdt = sdt == null ? null : sdt.trim();
    }

    public String getDiaChi() { return diaChi; }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi == null ? null : diaChi.trim();
    }

}
