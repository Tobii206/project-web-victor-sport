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
                                  @RequestParam(required = false) Integer nhuCau,
                                  @RequestParam(required = false) Integer chatLieu,
                                  @RequestParam(required = false) Integer xuatXu,
                                  @RequestParam(required = false) Integer kichThuoc,
                                  @RequestParam(required = false) Integer mauSac,
                                  @RequestParam(defaultValue = "false") boolean conHang,
                                  @RequestParam(value = "sapXep", defaultValue = "moi-nhat") String sapXep,
                                  Model model) {
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("thuongHieuDangChon", thuongHieu);
        model.addAttribute("sapXep", sapXep);
        model.addAttribute("nhuCauDangChon", nhuCau);
        model.addAttribute("chatLieuDangChon", chatLieu);
        model.addAttribute("xuatXuDangChon", xuatXu);
        model.addAttribute("kichThuocDangChon", kichThuoc);
        model.addAttribute("mauSacDangChon", mauSac);
        model.addAttribute("conHang", conHang);
        try {
            model.addAttribute("thuongHieu", trangChuService.layThuongHieu());
            model.addAttribute("boLoc", trangChuService.layBoLoc());
            model.addAttribute("sanPham", trangChuService.timSanPham(tuKhoa, thuongHieu, nhuCau, chatLieu, xuatXu, kichThuoc, mauSac, conHang, sapXep));
        } catch (DataAccessException loi) {
            model.addAttribute("thuongHieu", Collections.emptyList());
            model.addAttribute("sanPham", Collections.emptyList());
            model.addAttribute("boLoc", Collections.emptyMap());
            model.addAttribute("loiDuLieu", "Chưa tải được sản phẩm. Vui lòng thử lại sau.");
        }
        return "client/homepage/show";
    }
}
