package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class MauGiayForm {
    @NotNull(message = "Vui lòng chọn màu sắc, kích thước và dáng giày.")
    @Positive(message = "Vui lòng chọn màu sắc, kích thước và dáng giày.")
    private Integer id_mau_sac;

    @NotNull(message = "Vui lòng chọn màu sắc, kích thước và dáng giày.")
    @Positive(message = "Vui lòng chọn màu sắc, kích thước và dáng giày.")
    private Integer id_kich_thuoc;

    @NotNull(message = "Vui lòng chọn màu sắc, kích thước và dáng giày.")
    @Positive(message = "Vui lòng chọn màu sắc, kích thước và dáng giày.")
    private Integer id_form_chan;

    @NotNull(message = "Vui lòng nhập giá.")
    @DecimalMin(value = "0", message = "Giá không được âm.")
    @Digits(integer = 16, fraction = 2, message = "Giá tối đa 16 chữ số và 2 chữ số thập phân.")
    private BigDecimal gia_ban;

    @NotNull(message = "Vui lòng nhập số lượng tồn kho.")
    @Min(value = 0, message = "Số lượng tồn kho không được âm.")
    private Integer so_luong;

    @Min(value = 0, message = "Số lượng tồn kho cũ không được âm.")
    private Integer so_luong_cu;

    @NotNull(message = "Vui lòng chọn trạng thái.")
    @Min(value = 0, message = "Trạng thái không hợp lệ.")
    @Max(value = 1, message = "Trạng thái không hợp lệ.")
    private Integer trang_thai;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự.")
    private String ghi_chu;

    public Integer getId_mau_sac() { return id_mau_sac; }

    public void setId_mau_sac(Integer id_mau_sac) {
        this.id_mau_sac = id_mau_sac;
    }

    public Integer getId_kich_thuoc() { return id_kich_thuoc; }

    public void setId_kich_thuoc(Integer id_kich_thuoc) {
        this.id_kich_thuoc = id_kich_thuoc;
    }

    public Integer getId_form_chan() { return id_form_chan; }

    public void setId_form_chan(Integer id_form_chan) {
        this.id_form_chan = id_form_chan;
    }

    public BigDecimal getGia_ban() { return gia_ban; }

    public void setGia_ban(BigDecimal gia_ban) {
        this.gia_ban = gia_ban;
    }

    public Integer getSo_luong() { return so_luong; }

    public void setSo_luong(Integer so_luong) {
        this.so_luong = so_luong;
    }

    public Integer getSo_luong_cu() { return so_luong_cu; }

    public void setSo_luong_cu(Integer so_luong_cu) {
        this.so_luong_cu = so_luong_cu;
    }

    public Integer getTrang_thai() { return trang_thai; }

    public void setTrang_thai(Integer trang_thai) {
        this.trang_thai = trang_thai;
    }

    public String getGhi_chu() { return ghi_chu; }

    public void setGhi_chu(String ghi_chu) {
        this.ghi_chu = ghi_chu == null ? null : ghi_chu.trim();
    }

}
