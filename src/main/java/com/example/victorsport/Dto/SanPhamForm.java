package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class SanPhamForm {
    @NotBlank(message = "Vui lòng nhập tên sản phẩm.")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự.")
    private String ten_san_pham;

    @NotNull(message = "Chọn thương hiệu.")
    @Positive(message = "Chọn thương hiệu.")
    private Integer id_thuong_hieu;

    @Positive(message = "Thông tin đã chọn không hợp lệ.")
    private Integer id_xuat_xu;

    @Positive(message = "Thông tin đã chọn không hợp lệ.")
    private Integer id_vi_tri_thi_dau;

    @Positive(message = "Thông tin đã chọn không hợp lệ.")
    private Integer id_phong_cach_choi;

    @Positive(message = "Thông tin đã chọn không hợp lệ.")
    private Integer id_co_giay;

    @Positive(message = "Thông tin đã chọn không hợp lệ.")
    private Integer id_chat_lieu;

    @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự.")
    private String mo_ta_ngan;

    @Size(max = 20000, message = "Mô tả chi tiết tối đa 20000 ký tự.")
    private String mo_ta_chi_tiet;

    @NotNull(message = "Vui lòng chọn trạng thái.")
    @Min(value = 0, message = "Trạng thái không hợp lệ.")
    @Max(value = 1, message = "Trạng thái không hợp lệ.")
    private Integer trang_thai_kinh_doanh;

    public String getTen_san_pham() { return ten_san_pham; }

    public void setTen_san_pham(String ten_san_pham) {
        this.ten_san_pham = ten_san_pham == null ? null : ten_san_pham.trim();
    }

    public Integer getId_thuong_hieu() { return id_thuong_hieu; }

    public void setId_thuong_hieu(Integer id_thuong_hieu) {
        this.id_thuong_hieu = id_thuong_hieu;
    }

    public Integer getId_xuat_xu() { return id_xuat_xu; }

    public void setId_xuat_xu(Integer id_xuat_xu) {
        this.id_xuat_xu = id_xuat_xu;
    }

    public Integer getId_vi_tri_thi_dau() { return id_vi_tri_thi_dau; }

    public void setId_vi_tri_thi_dau(Integer id_vi_tri_thi_dau) {
        this.id_vi_tri_thi_dau = id_vi_tri_thi_dau;
    }

    public Integer getId_phong_cach_choi() { return id_phong_cach_choi; }

    public void setId_phong_cach_choi(Integer id_phong_cach_choi) {
        this.id_phong_cach_choi = id_phong_cach_choi;
    }

    public Integer getId_co_giay() { return id_co_giay; }

    public void setId_co_giay(Integer id_co_giay) {
        this.id_co_giay = id_co_giay;
    }

    public Integer getId_chat_lieu() { return id_chat_lieu; }

    public void setId_chat_lieu(Integer id_chat_lieu) {
        this.id_chat_lieu = id_chat_lieu;
    }

    public String getMo_ta_ngan() { return mo_ta_ngan; }

    public void setMo_ta_ngan(String mo_ta_ngan) {
        this.mo_ta_ngan = mo_ta_ngan == null ? null : mo_ta_ngan.trim();
    }

    public String getMo_ta_chi_tiet() { return mo_ta_chi_tiet; }

    public void setMo_ta_chi_tiet(String mo_ta_chi_tiet) {
        this.mo_ta_chi_tiet = mo_ta_chi_tiet == null ? null : mo_ta_chi_tiet.trim();
    }

    public Integer getTrang_thai_kinh_doanh() { return trang_thai_kinh_doanh; }

    public void setTrang_thai_kinh_doanh(Integer trang_thai_kinh_doanh) {
        this.trang_thai_kinh_doanh = trang_thai_kinh_doanh;
    }

}
