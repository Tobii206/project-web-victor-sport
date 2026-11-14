package com.example.victorsport.Controller;

import com.example.victorsport.Service.VnpaySandboxService;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Controller
@RequestMapping("/thanh-toan/vnpay")
public class VnpaySandboxController {
    private final VnpaySandboxService service;
    public VnpaySandboxController(VnpaySandboxService service) { this.service=service; }

    private Map<String,String> thamSo(HttpServletRequest request) {
        var p=new TreeMap<String,String>();
        request.getParameterMap().forEach((k,v)-> {
            if(k.startsWith("vnp_")) {
                if(v.length!=1 || v[0].length()>2048) throw new IllegalArgumentException("Tham số VNPAY không hợp lệ.");
                p.put(k,v[0]);
            }
        });
        return p;
    }
    @GetMapping("/ipn") @ResponseBody
    public Map<String,String> ipn(HttpServletRequest request) {
        try {
            var kq=service.nhanIpn(thamSo(request));
            return Map.of("RspCode",kq.RspCode(),"Message",kq.Message());
        } catch(IllegalArgumentException ex) { return Map.of("RspCode","97","Message","Invalid data"); }
        catch(org.springframework.dao.DataAccessException ex) { return Map.of("RspCode","99","Message","Please retry"); }
    }
    private Map<String,Object> giaoDich(int id,HttpSession session) {
        var gd=service.layGiaoDich(id,(Integer)session.getAttribute("khachHangOnlineDangNhap"),
                Boolean.TRUE.equals(session.getAttribute("donVangLai_"+id)));
        if(gd==null || gd.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return gd;
    }
    @GetMapping("/{id:[0-9]+}")
    public String thanhToan(@PathVariable int id,HttpSession session,Model model,HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store");
        model.addAttribute("giaoDich",giaoDich(id,session));
        return "client/cart/vnpay";
    }
    @GetMapping("/{id:[0-9]+}/trang-thai") @ResponseBody
    public ResponseEntity<Map<String,Object>> trangThai(@PathVariable int id,HttpSession session) {
        var gd=giaoDich(id,session);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("trangThai",gd.get("trang_thai")));
    }
    @GetMapping("/ket-qua")
    public String ketQua(HttpServletRequest request,HttpSession session,Model model,HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Referrer-Policy","no-referrer");
        try {
            var p=thamSo(request);
            if(!service.chuKyHopLe(p)) throw new IllegalArgumentException("Kết quả VNPAY có chữ ký không hợp lệ. Vui lòng kiểm tra đơn hàng của bạn.");
            Integer id=service.timDonTheoMa(p.get("vnp_TxnRef"));
            if(id==null) throw new IllegalArgumentException("Không tìm thấy giao dịch VNPAY.");
            model.addAttribute("giaoDich",giaoDich(id,session));
            if(!"00".equals(p.get("vnp_ResponseCode")))
                model.addAttribute("thongBao","24".equals(p.get("vnp_ResponseCode"))?"Bạn đã hủy giao dịch VNPAY. Đơn hàng chưa tự hủy.":"Giao dịch VNPAY chưa thành công. Vui lòng kiểm tra trạng thái đơn hàng.");
            else model.addAttribute("thongBao","Đã quay lại từ VNPAY. Hệ thống đang đối chiếu thông báo thanh toán.");
        } catch(IllegalArgumentException ex) { model.addAttribute("loiVnpay",ex.getMessage()); }
        return "client/cart/vnpay";
    }
}
