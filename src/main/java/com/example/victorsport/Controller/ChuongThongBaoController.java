package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/thong-bao-don-hang")
public class ChuongThongBaoController {
    @org.springframework.beans.factory.annotation.Autowired
    private ChuongTrucTuyenService trucTuyen;
    private final ChuongThongBaoService service;
    private final PhanQuyenService quyen;
    private final JdbcTemplate jdbc;
    public ChuongThongBaoController(ChuongThongBaoService service,PhanQuyenService quyen,JdbcTemplate jdbc) {
        this.service=service;this.quyen=quyen;this.jdbc=jdbc;
    }
    private int chu(HttpSession session,String kenh) {
        if("cong-khai".equals(kenh)) return 0;
        if("admin".equals(kenh) || "noi-bo".equals(kenh)) {
            if(!(session.getAttribute("nguoiDungDangNhap") instanceof NguoiDungDangNhap user))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            var moi=quyen.layQuyen(user.getId());
            if(moi==null || "admin".equals(kenh) && !moi.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            return moi.getId();
        }
        if(!"khach".equals(kenh)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if(!(session.getAttribute("khachHangOnlineDangNhap") instanceof Integer kh))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if(jdbc.queryForList("SELECT id FROM khach_hang WHERE id=? AND ISNULL(xoa_mem,0)=0 AND trang_thai=1",kh).isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return kh;
    }
    @GetMapping
    public ResponseEntity<?> danhSach(@RequestParam String kenh,HttpSession session) {
        kiemTraKenhChuong(kenh);
        int id=chu(session,kenh);
        String csrf;
        synchronized(session) {
            if(session.getAttribute("csrfChuong")==null) session.setAttribute("csrfChuong",UUID.randomUUID().toString());
            csrf=(String)session.getAttribute("csrfChuong");
        }
        var ketQua=new HashMap<>(service.danhSach("admin".equals(kenh),id));ketQua.put("csrf",csrf);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ketQua);
    }
    @GetMapping(value="/truc-tuyen",produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter trucTuyen(@RequestParam String kenh,HttpSession session,
            jakarta.servlet.http.HttpServletResponse response) {
        int id=chu(session,kenh);
        response.setHeader("Cache-Control","no-store");response.setHeader("X-Accel-Buffering","no");
        return trucTuyen.ketNoi(kenh,id,()->{
            try {return chu(session,kenh)==id;}catch(RuntimeException e){return false;}
        });
    }
    @PostMapping("/da-doc")
    public ResponseEntity<?> daDoc(@RequestParam String kenh,@RequestParam int id,
            @RequestParam(defaultValue="false") boolean tatCa,@RequestHeader(value="X-CSRF-Chuong",required=false) String csrf,HttpSession session) {
        kiemTraKenhChuong(kenh);
        int chu=chu(session,kenh);
        if(csrf==null || !csrf.equals(session.getAttribute("csrfChuong"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if(id<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        service.danhDau("admin".equals(kenh),chu,id,tatCa);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
    private void kiemTraKenhChuong(String kenh) {
        if(!"admin".equals(kenh) && !"khach".equals(kenh)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> loiDuLieu() {
        return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).body(Map.of("error","Chưa tải được thông báo. Vui lòng thử lại."));
    }
}
