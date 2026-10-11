package com.example.victorsport.Dto;

public class NguoiDungDangNhap {
    private final Integer id;
    private final String email;
    private final String hoTen;
    private final String tenQuyen;
    private final java.util.List<String> chucNang = new java.util.ArrayList<>();

    public NguoiDungDangNhap(Integer id, String email, String hoTen, String tenQuyen) {
        this.id = id;
        this.email = email;
        this.hoTen = hoTen;
        this.tenQuyen = tenQuyen;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenQuyen() {
        return tenQuyen;
    }

    public boolean isQuanLy() {
        if (tenQuyen == null) { return false; }
        String quyen = tenQuyen.trim().toLowerCase(java.util.Locale.ROOT);
        return quyen.equals("quản trị viên");
    }

    public void themChucNang(String ma) { if (ma != null && !chucNang.contains(ma)) chucNang.add(ma); }
    public boolean coChucNang(String ma) { return chucNang.contains(ma); }
    public boolean isBanHangTaiQuay() { return coChucNang("ORDER_WRITE"); }
    public boolean isXemSanPham() { return coChucNang("PRODUCT_READ") || coChucNang("PRODUCT_WRITE"); }
    public boolean isSuaSanPham() { return isQuanLy() && coChucNang("PRODUCT_WRITE"); }
    public boolean isXemHoaDon() { return coChucNang("ORDER_READ") || coChucNang("ORDER_WRITE"); }
    public boolean isSuaHoaDon() { return coChucNang("ORDER_WRITE"); }
    public boolean isXemKhachHang() { return coChucNang("CUSTOMER_READ"); }

    public String getTrangLamViec() {
        if (isBanHangTaiQuay()) return "/pos";
        if (isXemSanPham()) return "/quan-ly/san-pham";
        if (isXemHoaDon()) return "/quan-ly/hoa-don";
        return "/tai-khoan";
    }
}
