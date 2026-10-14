package com.example.victorsport.Controller;

import com.example.victorsport.Service.QuanLySanPhamService;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamForm;
import com.example.victorsport.Dto.MauGiayForm;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;
import java.util.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.io.IOException;

@Controller
@RequestMapping("/quan-ly/san-pham")
public class QuanLySanPhamController {
    private final QuanLySanPhamService service;
    public QuanLySanPhamController(QuanLySanPhamService service) {this.service=service;}
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String anhQuaLon(jakarta.servlet.http.HttpServletRequest request,RedirectAttributes flash) {
        flash.addFlashAttribute("error","Ảnh vượt quá dung lượng cho phép. Vui lòng chọn ảnh tối đa 5 MB.");
        String[] muc=request.getRequestURI().split("/");
        if(muc.length>3 && muc[3].matches("[0-9]+")) return "redirect:/quan-ly/san-pham/"+muc[3]+"#anh-san-pham";
        return "redirect:/quan-ly/san-pham";
    }
    private NguoiDungDangNhap nguoiDung(HttpSession session,Model model) {
        model.addAttribute("activeMenu","san-pham");
        return DangNhapController.layNguoiDungDangNhap(session,model);
    }
    @GetMapping
    public String danhSach(@RequestParam Map<String,String> loc,HttpSession session,Model model) {
        if (nguoiDung(session,model)==null) return "redirect:/login";
        model.addAttribute("loc",loc);
        try {model.addAttribute("danhMuc",service.danhMuc());model.addAttribute("sanPham",service.tim(loc));}
        catch(IllegalArgumentException e) {model.addAttribute("error",e.getMessage());model.addAttribute("sanPham",List.of());}
        catch(DataAccessException e) {model.addAttribute("error","Chưa tải được sản phẩm. Vui lòng thử lại.");model.addAttribute("sanPham",List.of());}
        return "Admin/quan-ly-san-pham";
    }
    @GetMapping("/them")
    public String them(HttpSession session,Model model) {
        if (nguoiDung(session,model)==null) return "redirect:/login";
        model.addAttribute("danhMuc",service.danhMuc());
        if (!model.containsAttribute("sanPham")) model.addAttribute("sanPham",Map.of("trang_thai_kinh_doanh",true));
        return "Admin/sua-san-pham";
    }
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable int id,@RequestParam(required=false) Integer mau,HttpSession session,Model model,RedirectAttributes flash) {
        if (nguoiDung(session,model)==null) return "redirect:/login";
        try {
            var sp=service.lay(id);
            if (!model.containsAttribute("sanPham")) model.addAttribute("sanPham",sp);
            model.addAttribute("idSanPham",id);model.addAttribute("maSanPham",sp.get("ma_san_pham"));
            model.addAttribute("danhMuc",service.danhMuc());
            var cacMau=service.layMau(id);model.addAttribute("cacMau",cacMau);model.addAttribute("cacAnh",service.layAnh(id));
            if (!model.containsAttribute("mauAnhDangChon") && !cacMau.isEmpty())
                model.addAttribute("mauAnhDangChon",mau==null?cacMau.get(0).get("id"):mau);
            if (!model.containsAttribute("mauForm")) {
                Map<String,Object> mauForm=new LinkedHashMap<>(Map.of("trang_thai",true,"so_luong",0));
                if (mau!=null) {
                    boolean timThay=false;
                    for(var pb:cacMau) if(((Number)pb.get("id")).intValue()==mau) {mauForm=pb;timThay=true;break;}
                    if(!timThay)throw new IllegalArgumentException("Không tìm thấy mẫu giày.");
                }
                model.addAttribute("mauForm",mauForm);
            }
            return "Admin/sua-san-pham";
        } catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());return "redirect:/quan-ly/san-pham";}
    }
    @PostMapping({"/luu","/{id}/luu"})
    public String luu(@PathVariable(required=false) Integer id,@Valid @ModelAttribute SanPhamForm duLieu,BindingResult loiForm,@RequestParam Map<String,String> data,HttpSession session,Model model,RedirectAttributes flash) {
        var user=nguoiDung(session,model);if(user==null)return "redirect:/login";
        try {KiemTraForm.kiemTra(loiForm);int ketQua=service.luu(id,data,user.getId());flash.addFlashAttribute("success","Đã lưu sản phẩm. Bạn có thể thêm các mẫu size, màu và ảnh bên dưới.");return "redirect:/quan-ly/san-pham/"+ketQua;}
        catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {flash.addFlashAttribute("error","Không lưu được sản phẩm. Vui lòng thử lại.");}
        // Giữ dữ liệu vừa nhập để người dùng sửa lỗi.
        Map<String,Object> form=new LinkedHashMap<>(data);form.put("trang_thai_kinh_doanh","1".equals(data.get("trang_thai_kinh_doanh")));
        flash.addFlashAttribute("sanPham",form);
        return "redirect:/quan-ly/san-pham/"+(id==null?"them":id);
    }
    @PostMapping("/{id}/xoa")
    public String xoa(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        var user=nguoiDung(session,model);if(user==null)return "redirect:/login";
        try {service.xoa(id,user.getId());flash.addFlashAttribute("success","Đã xóa sản phẩm khỏi danh sách bán. Dữ liệu hóa đơn được giữ lại.");}
        catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/quan-ly/san-pham";
    }
    @PostMapping("/{id}/mau/luu")
    public String luuMau(@PathVariable int id,@RequestParam(required=false) Integer idMau,@Valid @ModelAttribute MauGiayForm duLieu,BindingResult loiForm,@RequestParam Map<String,String> data,HttpSession session,Model model,RedirectAttributes flash) {
        var user=nguoiDung(session,model);if(user==null)return "redirect:/login";
        try {KiemTraForm.kiemTra(loiForm);service.luuMau(id,idMau,data,user.getId());flash.addFlashAttribute("success","Đã lưu mẫu giày và tồn kho.");}
        catch(IllegalArgumentException | DataAccessException e) {
            flash.addFlashAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Không lưu được mẫu giày. Vui lòng thử lại.");
            Map<String,Object> form=new LinkedHashMap<>(data);if(idMau!=null)form.put("id",idMau);
            form.put("trang_thai","1".equals(data.get("trang_thai")));flash.addFlashAttribute("mauForm",form);
        }
        return "redirect:/quan-ly/san-pham/"+id+"#mau-giay";
    }
    @PostMapping("/{id}/mau/{mau}/xoa")
    public String xoaMau(@PathVariable int id,@PathVariable int mau,HttpSession session,Model model,RedirectAttributes flash) {
        var user=nguoiDung(session,model);if(user==null)return "redirect:/login";
        try {service.xoaMau(id,mau,user.getId());flash.addFlashAttribute("success","Đã xóa mẫu giày khỏi danh sách bán.");}
        catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/quan-ly/san-pham/"+id+"#mau-giay";
    }
    @PostMapping("/{id}/anh/them")
    public String themAnh(@PathVariable int id,@RequestParam int idMau,@RequestParam MultipartFile tepAnh,HttpSession session,Model model,RedirectAttributes flash) {
        if(nguoiDung(session,model)==null)return "redirect:/login";
        flash.addFlashAttribute("mauAnhDangChon",idMau);
        Path tep=null;
        try {
            if(tepAnh.isEmpty() || tepAnh.getSize()>5*1024*1024)throw new IllegalArgumentException("Chọn ảnh JPG hoặc PNG, dung lượng tối đa 5 MB.");
            String tenGoc=tepAnh.getOriginalFilename()==null?"":tepAnh.getOriginalFilename().toLowerCase(Locale.ROOT);
            if(!tenGoc.endsWith(".jpg") && !tenGoc.endsWith(".jpeg") && !tenGoc.endsWith(".png"))
                throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG hoặc PNG.");
            java.awt.image.BufferedImage anh;
            try(var luong=tepAnh.getInputStream()) {anh=ImageIO.read(luong);}
            if(anh==null)throw new IllegalArgumentException("Tệp không phải ảnh JPG hoặc PNG hợp lệ.");
            String ten=UUID.randomUUID()+".png";
            Path thuMuc=Path.of("uploads","products").toAbsolutePath();Files.createDirectories(thuMuc);tep=thuMuc.resolve(ten);
            ImageIO.write(anh,"png",tep.toFile());
            service.themAnh(id,idMau,"/uploads/products/"+ten);flash.addFlashAttribute("success","Đã thêm ảnh cho mẫu giày.");
        } catch(IllegalArgumentException | IOException | DataAccessException e) {
            if(tep!=null)try {Files.deleteIfExists(tep);} catch(IOException boQua) { }
            flash.addFlashAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Không lưu được ảnh. Vui lòng thử lại.");
        }
        return "redirect:/quan-ly/san-pham/"+id+"#anh-san-pham";
    }
    @PostMapping("/{id}/anh/{anh}/{thaoTac}")
    public String suaAnh(@PathVariable int id,@PathVariable int anh,@PathVariable String thaoTac,@RequestParam(required=false) Integer idMau,HttpSession session,Model model,RedirectAttributes flash) {
        if(nguoiDung(session,model)==null)return "redirect:/login";
        if(idMau!=null)flash.addFlashAttribute("mauAnhDangChon",idMau);
        try {
            if(!thaoTac.equals("xoa") && !thaoTac.equals("dai-dien"))throw new IllegalArgumentException("Thao tác ảnh không hợp lệ.");
            service.suaAnh(id,anh,thaoTac.equals("xoa"));flash.addFlashAttribute("success","Đã cập nhật ảnh sản phẩm.");
        }catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/quan-ly/san-pham/"+id+"#anh-san-pham";
    }
}
