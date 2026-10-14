package com.example.victorsport.Service;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class KiemTraForm {
    public static void kiemTra(BindingResult loi) {
        if (!loi.hasErrors()) return;
        FieldError truong = loi.getFieldError();
        if (truong != null && truong.isBindingFailure()) {
            throw new IllegalArgumentException("Ngày hoặc số đã nhập không hợp lệ. Vui lòng kiểm tra lại.");
        }
        throw new IllegalArgumentException(loi.getAllErrors().get(0).getDefaultMessage());
    }
}
