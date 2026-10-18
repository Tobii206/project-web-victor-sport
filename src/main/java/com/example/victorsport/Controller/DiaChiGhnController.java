package com.example.victorsport.Controller;

import com.example.victorsport.Service.GhnService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.Map;

@Controller
@RequestMapping("/thanh-toan/ghn")
public class DiaChiGhnController {
    private final GhnService ghn;
    public DiaChiGhnController(GhnService ghn){this.ghn=ghn;}
    @GetMapping("/dia-chi") @ResponseBody
    public ResponseEntity<?> diaChi(@RequestParam(defaultValue="tinh") String cap,@RequestParam(defaultValue="0") int tinh) {
        try {
            Object data=switch(cap){case "tinh"->ghn.tinhMoi();case "xa"->ghn.xaMoi(tinh);default->throw new IllegalArgumentException("Cấp địa chỉ không hợp lệ.");};
            return tra(HttpStatus.OK,data);
        }catch(IllegalArgumentException e){return tra(HttpStatus.BAD_REQUEST,Map.of("error",e.getMessage()));}
        catch(IllegalStateException e){return tra(HttpStatus.SERVICE_UNAVAILABLE,Map.of("error",e.getMessage()));}
    }
    private ResponseEntity<?> tra(HttpStatus status,Object body){return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(body);}
}
