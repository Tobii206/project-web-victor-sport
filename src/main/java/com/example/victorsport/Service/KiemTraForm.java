package com.example.victorsport.Service;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class KiemTraForm {
    public static void kiemTra(BindingResult loi) {
        if (!loi.hasErrors()) return;
        FieldError truong = loi.getFieldError();
        if (truong != null && truong.isBindingFailure()) {
            String thongBao = switch (truong.getField()) {
                case "ngay_sinh", "ngaySinh" -> "Ngày sinh không hợp lệ. Vui lòng chọn ngày tồn tại trong lịch.";
                case "ngay_bat_dau", "ngay_ket_thuc" -> "Ngày bắt đầu hoặc kết thúc không hợp lệ. Vui lòng chọn lại.";
                case "so_luong", "so_luong_cu", "so_luong_su_dung", "muc_uu_tien" -> "Số lượng hoặc mức ưu tiên phải là số nguyên từ 0 đến 2.147.483.647.";
                case "gia_ban", "gia_tri_giam_gia", "so_tien_giam_toi_da", "hoa_don_toi_thieu", "tongTienDuKien" -> "Giá trị tiền phải là số hợp lệ, tối đa 16 chữ số và 2 chữ số thập phân.";
                default -> "Thông tin đã nhập hoặc lựa chọn không hợp lệ. Vui lòng kiểm tra lại.";
            };
            throw new IllegalArgumentException(thongBao);
        }
        throw new IllegalArgumentException(loi.getAllErrors().get(0).getDefaultMessage());
    }
}
