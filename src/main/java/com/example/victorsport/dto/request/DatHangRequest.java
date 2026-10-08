package com.example.victorsport.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DatHangRequest {
    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    private String email;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String diaChi;

    private String ghiChu;

    private Integer idPhuongThucThanhToan; // 2: Chuyển khoản, 4: COD

    private String maPhieuGiamGia;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    @Valid
    private List<ItemGioHangRequest> danhSachSanPham;
}
