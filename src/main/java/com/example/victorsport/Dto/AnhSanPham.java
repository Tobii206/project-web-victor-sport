package com.example.victorsport.Dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class AnhSanPham {
    public static String layTenBoAnh(String tenSanPham) {
        if (tenSanPham == null) { return null; }
        String ten = tenSanPham.toLowerCase(Locale.ROOT);
        if (ten.contains("adidas samba og")) { return "adidas-samba-og"; }
        if (ten.contains("new balance 550")) { return "new-balance-550"; }
        return null;
    }

    public static String layAnhDaiDien(String tenSanPham) {
        String boAnh = layTenBoAnh(tenSanPham);
        if (boAnh == null) { return null; }
        return "/images/products/" + boAnh + "-1.png";
    }

    public static List<Map<String, Object>> layDanhSachAnh(String tenSanPham) {
        List<Map<String, Object>> ds = new ArrayList<>();
        String boAnh = layTenBoAnh(tenSanPham);
        if (boAnh == null) { return ds; }
        for (int i = 1; i <= 4; i++) {
            Map<String, Object> anh = new HashMap<>();
            anh.put("duong_dan_anh", "/images/products/" + boAnh + "-" + i + ".png");
            ds.add(anh);
        }
        return ds;
    }
}
