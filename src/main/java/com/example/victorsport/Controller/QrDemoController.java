package com.example.victorsport.Controller;

import com.example.victorsport.Service.QrDemoService;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Controller
@RequestMapping("/thanh-toan/qr-demo")
public class QrDemoController {
    private final QrDemoService service;
    @org.springframework.beans.factory.annotation.Value("${thanhtoan.qr-demo.public-url:}")
    private String publicUrl="";
    public QrDemoController(QrDemoService service) { this.service=service; }
    private void kiemTra() { if(!service.dangBat()) throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
    private boolean phien(HttpSession s,int id) { return Boolean.TRUE.equals(s.getAttribute("donVangLai_"+id)); }
    @GetMapping("/{id}")
    public String xem(@PathVariable int id,HttpSession session,Model model,HttpServletResponse response,HttpServletRequest request) {
        kiemTra();response.setHeader("Cache-Control","no-store");
        var gd=service.lay(id,(Integer)session.getAttribute("khachHangOnlineDangNhap"),phien(session,id));
        if(gd==null || gd.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if(session.getAttribute("csrfQrDemo")==null) session.setAttribute("csrfQrDemo",UUID.randomUUID().toString());
        model.addAttribute("giaoDich",gd);model.addAttribute("csrfQrDemo",session.getAttribute("csrfQrDemo"));
        model.addAttribute("maQuet",gd.get("ma_yeu_cau"));
        String link=urlQuet(request,gd.get("ma_yeu_cau").toString());
        model.addAttribute("anhQrDemo",service.anhQr(link));
        model.addAttribute("linkQuetDemo",link);
        return "client/cart/qr-demo";
    }
    public String urlQuet(HttpServletRequest request,String ma) {
        String base=publicUrl.trim();
        if(base.isEmpty()) {
            String host=request.getServerName();
            if(Set.of("localhost","127.0.0.1","::1").contains(host)) {
                String lan=diaChiLan();if(lan!=null)host=lan;
            }
            int port=request.getServerPort();String scheme=request.getScheme();
            base=scheme+"://"+(host.contains(":")?"["+host+"]":host)
                +((port==80 && "http".equals(scheme) || port==443 && "https".equals(scheme))?"":":"+port)+request.getContextPath();
        }
        var uri=java.net.URI.create(base);
        if(!Set.of("http","https").contains(uri.getScheme()) || uri.getHost()==null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Địa chỉ thanh toán QR chưa hợp lệ.");
        return base.replaceAll("/+$","")+"/thanh-toan/qr-demo/quet/"+ma;
    }
    protected String diaChiLan() {
        try {
            for(var nic:Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
                if(!nic.isUp() || nic.isLoopback() || nic.isVirtual())continue;
                for(var address:Collections.list(nic.getInetAddresses()))
                    if(address instanceof java.net.Inet4Address && address.isSiteLocalAddress()) return address.getHostAddress();
            }
        } catch(java.net.SocketException ex) { /* Có thể cấu hình QR_DEMO_PUBLIC_URL khi máy không có LAN. */ }
        return null;
    }
    private String csrf(HttpSession session) {
        if(session.getAttribute("csrfQrDemo")==null)session.setAttribute("csrfQrDemo",UUID.randomUUID().toString());
        return session.getAttribute("csrfQrDemo").toString();
    }
    @GetMapping("/quet/{ma}")
    public String quet(@PathVariable String ma,HttpSession session,Model model,HttpServletResponse response) {
        kiemTra();response.setHeader("Cache-Control","no-store");response.setHeader("Referrer-Policy","no-referrer");
        var gd=service.layTheoMa(ma);
        if(gd==null || gd.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Mã QR không hợp lệ hoặc đã hết hạn.");
        session.setAttribute("qrDemoDaQuet_"+ma,true);
        model.addAttribute("giaoDich",gd);model.addAttribute("trenDienThoai",true);model.addAttribute("maQuet",ma);
        model.addAttribute("csrfQrDemo",csrf(session));
        return "client/cart/qr-demo";
    }
    @PostMapping("/quet/{ma}/xac-nhan") @ResponseBody
    public ResponseEntity<Map<String,String>> xacNhanQuet(@PathVariable String ma,@RequestParam String csrf,
            @RequestParam(defaultValue="thanh_cong") String ketQua,HttpSession session) {
        kiemTra();
        if(!Boolean.TRUE.equals(session.getAttribute("qrDemoDaQuet_"+ma)) || csrf.isBlank()
                || !Objects.equals(session.getAttribute("csrfQrDemo"),csrf)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("trangThai",service.xuLyQuet(ma,ketQua)));}
        catch(IllegalArgumentException ex) {return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));}
        catch(org.springframework.dao.DataAccessException ex) {return ResponseEntity.status(503).body(Map.of("error","Chưa cập nhật được thanh toán. Vui lòng thử lại."));}
    }
    @GetMapping("/{id}/trang-thai") @ResponseBody
    public ResponseEntity<Map<String,Object>> trangThai(@PathVariable int id,HttpSession session) {
        kiemTra();var gd=service.lay(id,(Integer)session.getAttribute("khachHangOnlineDangNhap"),phien(session,id));
        if(gd==null || gd.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("trangThai",gd.get("trang_thai"),
            "daHuy",((Number)gd.get("trang_thai_hien_tai")).intValue()==6,"hetHan",Boolean.TRUE.equals(gd.get("het_han")),"conLai",Objects.requireNonNullElse(gd.get("con_lai"),0)));
    }
    @PostMapping("/{id}/tai-lai") @ResponseBody
    public ResponseEntity<Map<String,String>> taiLai(@PathVariable int id,@RequestParam String csrf,@RequestParam String maCu,HttpSession session) {
        kiemTra();
        if(csrf.isBlank() || !Objects.equals(session.getAttribute("csrfQrDemo"),csrf)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            service.taoLai(id,(Integer)session.getAttribute("khachHangOnlineDangNhap"),phien(session,id),maCu);
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("url","/thanh-toan/qr-demo/"+id));
        } catch(IllegalArgumentException ex) {return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));}
        catch(org.springframework.dao.DataAccessException ex) {return ResponseEntity.status(503).body(Map.of("error","Chưa tạo lại được mã QR. Vui lòng thử lại."));}
    }
    @PostMapping("/{id}/ket-qua") @ResponseBody
    public ResponseEntity<Map<String,String>> xuLy(@PathVariable int id,@RequestParam String csrf,
            @RequestParam(defaultValue="thanh_cong") String ketQua,HttpSession session) {
        kiemTra();
        if(!Objects.equals(session.getAttribute("csrfQrDemo"),csrf) || csrf.isBlank()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            String tt=service.xuLy(id,(Integer)session.getAttribute("khachHangOnlineDangNhap"),phien(session,id),ketQua);
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("trangThai",tt));
        } catch(IllegalArgumentException ex) {return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));}
        catch(org.springframework.dao.DataAccessException ex) {return ResponseEntity.status(503).body(Map.of("error","Chưa cập nhật được thanh toán. Vui lòng thử lại."));}
    }
}
