package com.example.victorsport.Controller;

import com.example.victorsport.Service.TrangChuService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class TrangChuController {
    private final TrangChuService trangChuService;

    public TrangChuController(TrangChuService trangChuService) {
        this.trangChuService = trangChuService;
    }

    @GetMapping({"/", "/trang-chu"})
    public String hienThiTrangChu(@RequestParam(value = "tuKhoa", defaultValue = "") String tuKhoa,
                                  @RequestParam(value = "thuongHieu", required = false) Integer thuongHieu,
                                  @RequestParam(value = "sapXep", defaultValue = "moi-nhat") String sapXep,
                                  Model model) {
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("thuongHieuDangChon", thuongHieu);
        model.addAttribute("sapXep", sapXep);
        try {
            model.addAttribute("thuongHieu", trangChuService.layThuongHieu());
            model.addAttribute("sanPham", trangChuService.timSanPham(tuKhoa, thuongHieu, sapXep));
        } catch (DataAccessException loi) {
            model.addAttribute("thuongHieu", Collections.emptyList());
            model.addAttribute("sanPham", Collections.emptyList());
            model.addAttribute("loiDuLieu", "Chưa tải được sản phẩm. Vui lòng thử lại sau.");
        }
        return "trang-chu";
    }
}
