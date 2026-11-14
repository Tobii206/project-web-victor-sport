package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

public record GiaoHangTaiQuayForm(
        @NotBlank(message="Vui lòng nhập tên người nhận.") @Size(max=100,message="Tên người nhận tối đa 100 ký tự.") String ten,
        @NotBlank(message="Vui lòng nhập số điện thoại.") @Pattern(regexp="0[0-9]{9}",message="Số điện thoại phải đủ 10 số và bắt đầu bằng 0.") String sdt,
        @NotBlank(message="Vui lòng nhập địa chỉ chi tiết.") @Size(max=255,message="Địa chỉ chi tiết tối đa 255 ký tự.") String diaChi,
        @NotNull(message="Vui lòng chọn tỉnh / thành phố.") @Positive(message="Tỉnh / thành phố không hợp lệ.") Integer tinh,
        @NotNull(message="Vui lòng chọn phường / xã.") @Positive(message="Phường / xã không hợp lệ.") Integer xa,
        @NotNull(message="Vui lòng chọn phương thức thanh toán.") @Positive(message="Phương thức thanh toán không hợp lệ.") Integer paymentMethodId) {}
