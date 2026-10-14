package com.example.victorsport.Service;

public class KiemTraThongTin {
    public static boolean laEmailGmail(String email) {
        return email!=null && email.length()<=100
                && email.matches("(?i)[a-z0-9]+([._%+\\-][a-z0-9]+)*@gmail\\.com");
    }

    public static void kiemTraMatKhau(String matKhau) {
        if(matKhau==null || matKhau.isBlank() || matKhau.length()<6)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
    }
}
