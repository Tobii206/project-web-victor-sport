package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class ThanhToanForm {
    @Positive(message="Tỉnh GHN không hợp lệ.") private Integer tinhGhn;
    @Positive(message="Phường/xã GHN không hợp lệ.") private Integer xaGhn;
    public Integer getTinhGhn(){return tinhGhn;}
    public void setTinhGhn(Integer v){tinhGhn=v;}
    public Integer getXaGhn(){return xaGhn;}
    public void setXaGhn(Integer v){xaGhn=v;}

    @Positive(message="Phiếu giảm giá không hợp lệ.")
    private Integer idPhieuGiamGia;
    @DecimalMin(value="0",message="Tổng thanh toán không hợp lệ.")
    private java.math.BigDecimal tongTienDuKien;
    public Integer getIdPhieuGiamGia(){return idPhieuGiamGia;}
    public void setIdPhieuGiamGia(Integer value){idPhieuGiamGia=value;}
    public java.math.BigDecimal getTongTienDuKien(){return tongTienDuKien;}
    public void setTongTienDuKien(java.math.BigDecimal value){tongTienDuKien=value;}
    @Positive(message="Địa chỉ đã chọn không hợp lệ.")
    private Integer idDiaChi;
    public Integer getIdDiaChi(){return idDiaChi;}
    public void setIdDiaChi(Integer idDiaChi){this.idDiaChi=idDiaChi;}
    @Pattern(regexp = "COD|QR", message = "Vui lòng chọn phương thức thanh toán hợp lệ.")
    @NotBlank(message = "Vui lòng chọn phương thức thanh toán.")
    private String phuongThuc = "COD";

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

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
