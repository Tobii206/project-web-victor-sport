package com.example.victorsport;
import com.example.victorsport.Controller.SanPhamOnlineController;
import com.example.victorsport.Service.*;
import com.example.victorsport.Dto.BaoGiaGhn;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import java.math.BigDecimal;
import java.time.Instant;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class DiaChiCheckoutGhnTests {
 @Test void diaChiDaLuuChiLaySoNhaDuongVaHoaDonDungDungDiaBanBaoGia() throws Exception {
  var service=mock(SanPhamOnlineService.class);var diaChi=mock(DiaChiKhachHangService.class);
  when(service.dungGhn()).thenReturn(true);
  when(diaChi.chiTiet(7,3)).thenReturn(Map.of("dia_chi_cu_the","Số 10 ngõ 67","dia_chi_day_du","Số 10 ngõ 67, Phường cũ sai, Hải Phòng"));
  var gio=Map.of(9,1);var q=new BaoGiaGhn(gio,"An","0900000001","Số 10 ngõ 67, Phường Lê Chân, Hải Phòng","COD",new BigDecimal("100000"),new BigDecimal("45000"),1001,1002,"Hải Phòng","Phường Lê Chân",1000,35,25,15,Instant.now().plusSeconds(60));
  when(service.baoGiaGhn(gio,"An","0900000001","Số 10 ngõ 67","COD",1001,1002)).thenReturn(q);
  when(service.datHang(anyMap(),eq(7),anyString(),anyString(),eq(q.diaChi()),eq(true),eq("COD"),isNull(),eq(new BigDecimal("145000")),eq(q))).thenAnswer(invocation -> { org.junit.jupiter.api.Assertions.assertEquals(gio, invocation.getArgument(0)); return 10; });
  var session=new MockHttpSession();session.setAttribute("khachHangOnlineDangNhap",7);session.setAttribute("csrfGhn","csrf");session.setAttribute("muaNgayOnline",new LinkedHashMap<>(gio));
  var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service,diaChi)).build();
  mvc.perform(post("/thanh-toan/ghn/bao-gia").session(session).param("loai","mua").param("idDiaChi","3").param("csrfGhn","csrf").param("ten","An").param("sdt","0900000001").param("diaChi","Nội dung form cũ").param("tinhGhn","1001").param("xaGhn","1002"))
      .andExpect(redirectedUrl("/thanh-toan?loai=mua")).andExpect(flash().attribute("diaChi","Số 10 ngõ 67"));
  verify(service).baoGiaGhn(gio,"An","0900000001","Số 10 ngõ 67","COD",1001,1002);
  mvc.perform(post("/thanh-toan").session(session).param("loai","mua").param("idDiaChi","3").param("ten","An").param("sdt","0900000001").param("diaChi","Nội dung form cũ").param("tinhGhn","1001").param("xaGhn","1002").param("tongTienDuKien","145000"))
      .andExpect(redirectedUrl("/don-hang/10"));
  verify(service).datHang(anyMap(),eq(7),eq("An"),eq("0900000001"),eq(q.diaChi()),eq(true),eq("COD"),isNull(),eq(new BigDecimal("145000")),eq(q));
 }
}
