package com.example.victorsport.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

public class KiemTraUuDai {
    public static void kiemTra(Integer loai, BigDecimal giaTri, LocalDate batDau, LocalDate ketThuc) {
        if (batDau == null || ketThuc == null || ketThuc.isBefore(batDau))
            throw new IllegalArgumentException("Ngày kết thúc phải từ ngày bắt đầu trở đi.");
        if (loai == null || (loai != 0 && loai != 1))
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        if (giaTri == null || giaTri.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
        if (loai == 0 && giaTri.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Giảm theo phần trăm không được vượt quá 100%.");
    }
}
