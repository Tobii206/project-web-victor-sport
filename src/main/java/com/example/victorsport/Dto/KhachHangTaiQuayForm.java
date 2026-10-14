package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class KhachHangTaiQuayForm {
    @NotBlank(message = "Vui lòng nhập tên khách hàng.")
    @Size(max = 100, message = "Tên khách hàng tối đa 100 ký tự.")
    private String tenKhachHangMoi;

    @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại phải đủ 10 số và bắt đầu bằng số 0.")
    private String soDienThoaiMoi;

    @Email(message = "Email không hợp lệ.")
    @Pattern(regexp = "(?i)[a-z0-9]+([._%+\\-][a-z0-9]+)*@gmail\\.com", message = "Email phải đúng định dạng, ví dụ: ten@gmail.com.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String emailMoi;

    public String getTenKhachHangMoi() { return tenKhachHangMoi; }

    public void setTenKhachHangMoi(String tenKhachHangMoi) {
        this.tenKhachHangMoi = tenKhachHangMoi == null ? null : tenKhachHangMoi.trim();
    }

    public String getSoDienThoaiMoi() { return soDienThoaiMoi; }

    public void setSoDienThoaiMoi(String soDienThoaiMoi) {
        this.soDienThoaiMoi = soDienThoaiMoi == null || soDienThoaiMoi.isBlank() ? null : soDienThoaiMoi.trim();
    }

    public String getEmailMoi() { return emailMoi; }

    public void setEmailMoi(String emailMoi) {
        this.emailMoi = emailMoi == null || emailMoi.isBlank() ? null : emailMoi.trim();
    }

}
