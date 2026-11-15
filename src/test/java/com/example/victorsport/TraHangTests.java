package com.example.victorsport;

import com.example.victorsport.Service.TraHangService;
import com.example.victorsport.Service.TraHangService.Quyen;
import com.example.victorsport.Controller.TraHangController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TraHangTests {
    private static final Quyen KH=new Quyen(7,false,null),ADMIN=new Quyen(null,false,2);
    private static final BigDecimal AMOUNT=new BigDecimal("66");
    private Map<String,Object> hd(){var d=new HashMap<String,Object>();d.put("id",10);d.put("loai_don",1);d.put("trang_thai_hien_tai",5);d.put("con_han",1);d.put("tong_tien_sau_giam",new BigDecimal("230"));d.put("phi_van_chuyen",new BigDecimal("30"));d.put("da_thu",new BigDecimal("230"));d.put("tong_tien_da_hoan",BigDecimal.ZERO);return d;}
    private Map<String,Object> dong(){return new HashMap<>(Map.of("id",20,"so_luong",3,"so_luong_da_tra",0,"don_gia",new BigDecimal("100"),"id_chi_tiet_san_pham",50));}
    private Map<String,Object> yc(String tt){return new HashMap<>(Map.of("id",8,"id_hoa_don",10,"trang_thai",tt,"so_tien_hoan",AMOUNT,"phuong_thuc_hoan","CASH","hinh_thuc_tra","GUI_HANG","ly_do","Sai size"));}
    private Map<String,Object> dongTra(int dat,int loi){return new HashMap<>(Map.of("id",21,"id_hoa_don_chi_tiet",20,"id_chi_tiet_san_pham",50,"so_luong",1,"so_luong_nhap",dat,"so_luong_loi",loi,"duoc_duyet",true,"so_tien_hoan",AMOUNT));}
    private JdbcTemplate jdbc(String state){
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("SELECT nv.id"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(j.queryForList(contains("FROM hoa_don hd"),eq(10),eq(7))).thenAnswer(a->List.of(hd()));
        when(j.queryForList(contains("FROM hoa_don hd"),eq(10))).thenAnswer(a->List.of(hd()));
        when(j.queryForList(contains("SELECT id_hoa_don FROM yeu_cau_tra_hang"),eq(8))).thenReturn(List.of(Map.of("id_hoa_don",10)));
        when(j.queryForMap(contains("SELECT * FROM yeu_cau_tra_hang"),eq(8))).thenAnswer(a->yc(state));
        when(j.queryForList(contains("FROM hoa_don_chi_tiet ct"),eq(10))).thenAnswer(a->List.of(dong()));
        when(j.queryForList(contains("FROM chi_tiet_tra_hang ct"),eq(8))).thenAnswer(a->List.of(dongTra(1,0)));
        when(j.queryForObject(contains("INSERT INTO yeu_cau_tra_hang"),eq(Integer.class),any(Object[].class))).thenReturn(8);
        when(j.update(anyString(),any(Object[].class))).thenReturn(1);return j;
    }
    private Map<String,String> form(){return new HashMap<>(Map.of("maTao",UUID.randomUUID().toString(),"lyDo","Sai size","hinhThuc","GUI_HANG","phuongThuc","CASH"));}
    private MockMultipartFile png() throws Exception {var image=new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB);var out=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",out);return new MockMultipartFile("minhChung","anh.png","image/png",out.toByteArray());}
    @Test void phanBoTienSauGiamKhongVuotThucTraVaTraTungPhanKhongMatTienLe(){
        var rows=List.of(dong());var p=TraHangService.phanBo(rows,new BigDecimal("200"));BigDecimal sum=BigDecimal.ZERO;
        for(int i=0;i<3;i++)sum=sum.add(TraHangService.tienTra(p.get(20),i,1,3));
        assertEquals(0,sum.compareTo(new BigDecimal("200")));assertEquals(AMOUNT,TraHangService.tienTra(p.get(20),0,1,3));
        assertEquals(0,TraHangService.tienTra(p.get(20),0,3,3).compareTo(sum));
    }
    @Test void nhieuDongVaNhieuLanTraLuonKhopNganSach(){
        for(int i=1;i<=100;i++){
            var a=new HashMap<>(dong());a.put("id",1);a.put("so_luong",i);a.put("don_gia",new BigDecimal("123.45"));
            var b=new HashMap<>(dong());b.put("id",2);b.put("don_gia",new BigDecimal("57.21"));
            var net=new BigDecimal("100.01");var budgets=TraHangService.phanBo(List.of(a,b),net);
            assertEquals(0,budgets.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(net.setScale(0,RoundingMode.DOWN)));
            BigDecimal total=BigDecimal.ZERO;for(int k=0;k<i;k++)total=total.add(TraHangService.tienTra(budgets.get(1),k,1,i));
            total=total.add(TraHangService.tienTra(budgets.get(2),0,3,3));assertEquals(0,total.compareTo(net.setScale(0,RoundingMode.DOWN)));
        }
    }
    @Test void tienVndNguyenTheoAnhBaoLoiVaTraNhieuLanVanKhopTong(){
        var a=new HashMap<>(dong());a.put("id",1);a.put("so_luong",1);a.put("don_gia",new BigDecimal("2190000"));
        var b=new HashMap<>(dong());b.put("id",2);b.put("so_luong",1);b.put("don_gia",new BigDecimal("2490000"));
        var p=TraHangService.phanBo(List.of(a,b),new BigDecimal("4380000"));
        assertEquals(new BigDecimal("2049615"),p.get(1));assertEquals(new BigDecimal("2330385"),p.get(2));
        assertEquals(new BigDecimal("4380000"),p.get(1).add(p.get(2)));
        var budget=new BigDecimal("200");assertEquals(new BigDecimal("66"),TraHangService.tienTra(budget,0,1,3));
        assertEquals(new BigDecimal("67"),TraHangService.tienTra(budget,1,1,3));assertEquals(new BigDecimal("67"),TraHangService.tienTra(budget,2,1,3));
    }
    @Test void xemYeuCauCuCoTienLeChiChuanHoaDuKienKhongSuaGiaoDichDaHoan(){
        for(String state:List.of("CHO_XAC_NHAN","DA_HOAN_TIEN")){
            var j=jdbc(state);var y=yc(state);y.put("so_tien_hoan",new BigDecimal("4380000"));
            when(j.queryForMap(contains("SELECT * FROM yeu_cau_tra_hang"),eq(8))).thenReturn(y);
            var a=dongTra(0,0);a.put("so_tien_hoan",new BigDecimal("2049615.39"));var b=dongTra(0,0);b.put("id",22);b.put("so_tien_hoan",new BigDecimal("2330384.61"));
            when(j.queryForList(contains("FROM chi_tiet_tra_hang ct"),eq(8))).thenReturn(List.of(a,b));
            var model=new TraHangService(j).chiTiet(8,ADMIN);
            var rows=(List<Map<String,Object>>)model.get("dongTra");
            assertEquals(0,((BigDecimal)rows.get(0).get("so_tien_hoan")).compareTo(new BigDecimal(state.equals("CHO_XAC_NHAN")?"2049615":"2049615.39")));
            assertEquals(0,((BigDecimal)rows.get(1).get("so_tien_hoan")).compareTo(new BigDecimal(state.equals("CHO_XAC_NHAN")?"2330385":"2330384.61")));
            verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void soLuongAmThapPhanQuaLonVaDongTrungBiChan(){
        for(String v:List.of("-1","1.5","abc","2147483648","0"))assertThrows(IllegalArgumentException.class,()->TraHangService.soLuong(List.of(20),Map.of("sl_20",v),"sl_"));
        assertThrows(IllegalArgumentException.class,()->TraHangService.soLuong(List.of(20,20),Map.of("sl_20","1"),"sl_"));
        assertThrows(IllegalArgumentException.class,()->TraHangService.tienTra(AMOUNT,1,3,3));
        assertThrows(IllegalArgumentException.class,()->TraHangService.phanBo(List.of(dong()),new BigDecimal("301")));
    }
    @Test void taoYeuCauChiChotTienHangKhongHoanPhiShip() throws Exception {
        var j=jdbc("CHO_XAC_NHAN");assertEquals(8,new TraHangService(j).tao(10,KH,Map.of(20,1),form(),List.of(png())));
        var args=org.mockito.ArgumentCaptor.forClass(Object[].class);verify(j).queryForObject(contains("INSERT INTO yeu_cau_tra_hang"),eq(Integer.class),args.capture());
        assertEquals(AMOUNT,args.getValue()[9]);
        verify(j,never()).update(contains("UPDATE chi_tiet_san_pham"),any(Object[].class));
        verify(j,never()).update(contains("INSERT INTO giao_dich_hoan_tien"),any(Object[].class));
    }
    @Test void donTaiQuayPhaiMangDenCuaHangDeTra() throws Exception {
        var j=jdbc("CHO_XAC_NHAN");var h=hd();h.put("loai_don",0);
        when(j.queryForList(contains("FROM hoa_don hd"),eq(10))).thenReturn(List.of(h));
        var s=new TraHangService(j);var f=form();
        assertThrows(IllegalArgumentException.class,()->s.tao(10,ADMIN,Map.of(20,1),f,List.of(png())));
        f.put("hinhThuc","TAI_CUA_HANG");assertEquals(8,s.tao(10,ADMIN,Map.of(20,1),f,List.of(png())));
    }
    @Test void nguoiKhacVaNhanVienKhongDuQuyenKhongTheThaoTac() {
        var j=jdbc("CHO_XAC_NHAN");var s=new TraHangService(j);
        assertThrows(IllegalArgumentException.class,()->s.form(10,new Quyen(99,false,null)));
        assertThrows(IllegalArgumentException.class,()->s.form(10,new Quyen(null,false,null)));
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,new Quyen(null,false,3),"duyet",Map.of()));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void chuaNhanChuaThanhToanHoacHetHanKhongTaoYeuCau() throws Exception {
        for(String mode:List.of("ship","unpaid","expired")) {
            var j=jdbc("CHO_XAC_NHAN");var h=hd();if(mode.equals("ship"))h.put("trang_thai_hien_tai",4);if(mode.equals("unpaid"))h.put("da_thu",BigDecimal.ZERO);if(mode.equals("expired"))h.put("con_han",0);
            when(j.queryForList(contains("FROM hoa_don hd"),eq(10),eq(7))).thenReturn(List.of(h));
            assertThrows(IllegalArgumentException.class,()->new TraHangService(j).tao(10,KH,Map.of(20,1),form(),List.of(png())));
            verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void traQuaSoLuongTraDongKhacVaTrungYeuCauDangXuLyBiChan() {
        for(String mode:List.of("qty","foreign","active")){
            var j=jdbc("CHO_XAC_NHAN");if(mode.equals("active"))when(j.queryForList(contains("trang_thai NOT IN"),eq(10))).thenReturn(List.of(Map.of("id",9)));
            var qty=mode.equals("foreign")?Map.of(999,1):Map.of(20,mode.equals("qty")?4:1);
            assertThrows(IllegalArgumentException.class,()->new TraHangService(j).tao(10,KH,qty,form(),List.of()));verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void submitLapMaTaoTraVeYeuCauCuKhongTaoLanHai(){
        var j=jdbc("CHO_XAC_NHAN");var f=form();when(j.queryForList(contains("AND ma_tao=?"),eq(10),eq(f.get("maTao")))).thenReturn(List.of(Map.of("id",8)));
        assertEquals(8,new TraHangService(j).tao(10,KH,Map.of(20,1),f,List.of()));verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void anhGiaVaThongTinNganHangGiaKhongDuocLuu(){
        var j=jdbc("CHO_XAC_NHAN");var fake=new MockMultipartFile("minhChung","fake.png","image/png","<script>bad</script>".getBytes());
        assertThrows(IllegalArgumentException.class,()->new TraHangService(j).tao(10,KH,Map.of(20,1),form(),List.of(fake)));
        var f=form();f.putAll(Map.of("phuongThuc","BANK","nganHang","Bank fake","soTaiKhoan","123456","chuTaiKhoan","<script>"));
        assertThrows(IllegalArgumentException.class,()->new TraHangService(j).tao(10,KH,Map.of(20,1),f,List.of(fake)));verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void khongTheBoQuaBuocDeHoanTien(){
        for(String state:List.of("CHO_XAC_NHAN","DA_XAC_NHAN","DANG_GUI_HANG","DA_NHAN_HANG","DANG_KIEM_TRA","DA_HUY","TU_CHOI")){
            var j=jdbc(state);assertThrows(IllegalArgumentException.class,()->new TraHangService(j).quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66")));verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void tuChoiCanLyDoVaKhongCongKho(){
        var j=jdbc("CHO_XAC_NHAN");var s=new TraHangService(j);
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"tu-choi",Map.of("ghiChu","")));
        s.quanTri(8,ADMIN,"tu-choi",Map.of("ghiChu","Thông tin yêu cầu chưa phù hợp"));
        verify(j).update(contains("UPDATE yeu_cau_tra_hang SET trang_thai"),eq("TU_CHOI"),eq(8),eq("CHO_XAC_NHAN"));
        verify(j,never()).update(contains("chi_tiet_san_pham SET"),any(Object[].class));
    }
    @Test void khachChiHuyTruocKhiGuiHangVaCanVanDon(){
        var j=jdbc("DANG_GUI_HANG");assertThrows(IllegalArgumentException.class,()->new TraHangService(j).khachXuLy(8,KH,"huy",null,null));
        var cho=jdbc("DA_XAC_NHAN");assertThrows(IllegalArgumentException.class,()->new TraHangService(cho).khachXuLy(8,KH,"gui-hang","GHN",""));
        new TraHangService(cho).khachXuLy(8,KH,"gui-hang","GHN","GHN123");
        verify(cho).update(contains("UPDATE yeu_cau_tra_hang SET trang_thai"),eq("DANG_GUI_HANG"),eq(8),eq("DA_XAC_NHAN"));
    }
    @Test void kiemTraCanNhapDuSoLuongDatVaGhiChu(){
        var j=jdbc("DANG_KIEM_TRA");var s=new TraHangService(j);
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"duyet-hoan",Map.of("ghiChu","Đã kiểm tra sản phẩm","nhap_21","2","loi_21","0","duyet_21","1")));
        s.quanTri(8,ADMIN,"duyet-hoan",Map.of("ghiChu","Đã kiểm tra sản phẩm","nhap_21","0","loi_21","1","duyet_21","1"));
        verify(j).update(contains("UPDATE chi_tiet_tra_hang"),eq(0),eq(1),eq(21),eq(8));
    }
    @Test void hangLoiPhaiKhaiBaoHopLeVaTongBangSoLuongTra(){
        for(String loi:List.of("", "-1", "2", "0.5", "0")){
            var j=jdbc("DANG_KIEM_TRA");
            assertThrows(IllegalArgumentException.class,()->new TraHangService(j).quanTri(8,ADMIN,"duyet-hoan",Map.of("duyet_21","1","nhap_21","0","loi_21",loi,"ghiChu","Đã kiểm tra đầy đủ sản phẩm")));
            verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void tickDuyetTungSanPhamTinhLaiTienVaCanLyDoBoChon(){
        var j=jdbc("CHO_XAC_NHAN");var a=dongTra(0,0);var b=dongTra(0,0);b.put("id",22);b.put("id_hoa_don_chi_tiet",30);
        when(j.queryForList(contains("FROM chi_tiet_tra_hang ct"),eq(8))).thenReturn(List.of(a,b));var s=new TraHangService(j);
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"duyet",Map.of()));
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"duyet",Map.of("duyet_999","1")));
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"duyet",Map.of("duyet_21","1")));
        verify(j,never()).update(anyString(),any(Object[].class));
        s.quanTri(8,ADMIN,"duyet",Map.of("duyet_21","1","ghiChu","Sản phẩm còn lại không đủ điều kiện trả"));
        verify(j).update(contains("SET duoc_duyet=?"),eq(1),eq(21),eq(8));verify(j).update(contains("SET duoc_duyet=?"),eq(0),eq(22),eq(8));
        verify(j).update(contains("UPDATE yeu_cau_tra_hang SET so_tien_hoan"),eq(AMOUNT),eq(8));
        verify(j,never()).update(contains("UPDATE chi_tiet_san_pham"),any(Object[].class));
    }
    @Test void sanPhamKhongDuyetKhongDuocHoanTienVaKhongCongKho(){
        var j=jdbc("DA_DUYET_HOAN_TRA");var bo=dongTra(1,0);bo.put("id",22);bo.put("id_hoa_don_chi_tiet",30);bo.put("id_chi_tiet_san_pham",51);bo.put("duoc_duyet",false);
        when(j.queryForList(contains("FROM chi_tiet_tra_hang ct"),eq(8))).thenReturn(List.of(dongTra(1,0),bo));
        new TraHangService(j).quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66"));
        verify(j).update(contains("UPDATE chi_tiet_san_pham"),eq(1),eq(50),eq(1));
        verify(j,never()).update(contains("UPDATE chi_tiet_san_pham"),anyInt(),eq(51),anyInt());
        verify(j,never()).update(contains("UPDATE hoa_don_chi_tiet SET so_luong_da_tra"),anyInt(),eq(30),anyInt(),anyInt());
    }
    @Test void kiemTraKhongDuocDuyetLaiDongDaBiLoaiTuDau(){
        var j=jdbc("DANG_KIEM_TRA");var bo=dongTra(0,0);bo.put("duoc_duyet",false);
        when(j.queryForList(contains("FROM chi_tiet_tra_hang ct"),eq(8))).thenReturn(List.of(bo));
        assertThrows(IllegalArgumentException.class,()->new TraHangService(j).quanTri(8,ADMIN,"duyet-hoan",Map.of("duyet_21","1","nhap_21","1","loi_21","0","ghiChu","Kiểm tra đầy đủ sản phẩm")));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void saiSoTienVaMaGiaoDichTrungKhongHoan(){
        var j=jdbc("DA_DUYET_HOAN_TRA");var s=new TraHangService(j);
        for(String v:List.of("-1","65","67","abc","66.5"))assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"hoan-tien",Map.of("soTien",v)));
        when(j.queryForList(contains("SELECT id FROM giao_dich_hoan_tien"),eq(8),eq("PC-TH000008"))).thenReturn(List.of(Map.of("id",1)));
        assertThrows(IllegalArgumentException.class,()->s.quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66")));verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void hoanHangDatNhapLaiKhoMotLanVaKhongDoiTrangThaiGiaoHang(){
        var j=jdbc("DA_DUYET_HOAN_TRA");new TraHangService(j).quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66"));
        verify(j).update(contains("UPDATE chi_tiet_san_pham"),eq(1),eq(50),eq(1));
        verify(j).update(contains("UPDATE hoa_don_chi_tiet SET so_luong_da_tra"),eq(1),eq(20),eq(10),eq(1));
        verify(j).update(contains("INSERT INTO giao_dich_hoan_tien"),eq(8),eq(10),eq(AMOUNT),eq("CASH"),eq("PC-TH000008"),eq(2));
        verify(j,never()).update(contains("SET trang_thai_hien_tai"),any(Object[].class));
        var done=jdbc("DA_HOAN_TIEN");new TraHangService(done).quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66"));verify(done,never()).update(anyString(),any(Object[].class));
    }
    @Test void maPhieuChiTuDienCaChuyenKhoanVaCoTheDungMaNganHang(){
        for(String ma:List.of("", "BANK-12345")){
            var j=jdbc("DA_DUYET_HOAN_TRA");var y=yc("DA_DUYET_HOAN_TRA");y.put("phuong_thuc_hoan","BANK");
            when(j.queryForMap(contains("SELECT * FROM yeu_cau_tra_hang"),eq(8))).thenReturn(y);
            new TraHangService(j).quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66","maGiaoDich",ma));
            verify(j).update(contains("INSERT INTO giao_dich_hoan_tien"),eq(8),eq(10),eq(AMOUNT),eq("BANK"),eq(ma.isBlank()?"PC-TH000008":ma),eq(2));
        }
    }
    @Test void hangLoiTachRiengKhongCongKhoBan(){
        var j=jdbc("DA_DUYET_HOAN_TRA");when(j.queryForList(contains("FROM chi_tiet_tra_hang ct"),eq(8))).thenReturn(List.of(dongTra(0,1)));
        new TraHangService(j).quanTri(8,ADMIN,"hoan-tien",Map.of("soTien","66"));
        verify(j).update(contains("INSERT INTO san_pham_hoan_tra"),eq(21),eq(50),eq(1),eq("Sai size"));
        verify(j,never()).update(contains("UPDATE chi_tiet_san_pham"),any(Object[].class));
    }
    @Test void anhMinhChungKhongDuocXemBangTaiKhoanKhac(){
        var j=jdbc("CHO_XAC_NHAN");assertThrows(IllegalArgumentException.class,()->new TraHangService(j).anh(8,3,new Quyen(99,false,null)));
        verify(j,never()).queryForList(contains("SELECT du_lieu"),any(Object[].class));
    }
    @Test void cacPostCanCsrfVaAdminDuQuyen() throws Exception {
        var s=mock(TraHangService.class);var mvc=MockMvcBuilders.standaloneSetup(new TraHangController(s)).build();var session=new MockHttpSession();
        mvc.perform(post("/tra-hang/8/huy").session(session).param("csrf","fake")).andExpect(status().isForbidden());
        session.setAttribute("csrfTraHang","token");session.setAttribute("nguoiDungDangNhap",new NguoiDungDangNhap(3,"nv@local","Nhân viên","Nhân viên"));
        mvc.perform(post("/quan-ly/tra-hang/8/xu-ly").session(session).param("csrf","token").param("action","hoan-tien")).andExpect(status().isForbidden());
        verifyNoInteractions(s);
    }
    @Test void nhanVienCoQuyenXuLyDuocXacNhanHoanTienVaVanCanCsrf() throws Exception {
        var s=mock(TraHangService.class);var mvc=MockMvcBuilders.standaloneSetup(new TraHangController(s)).build();
        var nv=new NguoiDungDangNhap(3,"nv@local","Nhân viên","Nhân viên");nv.themChucNang("ORDER_WRITE");
        var session=new MockHttpSession();session.setAttribute("nguoiDungDangNhap",nv);session.setAttribute("csrfTraHang","token");
        mvc.perform(post("/quan-ly/tra-hang/8/xu-ly").session(session).param("csrf","sai").param("action","hoan-tien")).andExpect(status().isForbidden());
        verifyNoInteractions(s);
        mvc.perform(post("/quan-ly/tra-hang/8/xu-ly").session(session).param("csrf","token").param("action","hoan-tien").param("soTien","66"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/quan-ly/tra-hang/8"));
        verify(s).quanTri(eq(8),eq(new Quyen(null,false,3)),eq("hoan-tien"),argThat(f->"66".equals(f.get("soTien"))));
    }
    @Test void khachVangLaiPhaiCoPhienTraCuuDungDon() throws Exception {
        var s=mock(TraHangService.class);when(s.idHoaDon(8)).thenReturn(10);
        var mvc=MockMvcBuilders.standaloneSetup(new TraHangController(s)).build();
        mvc.perform(get("/tra-hang/8")).andExpect(status().isNotFound());verify(s,never()).chiTiet(anyInt(),any());
        var session=new MockHttpSession();session.setAttribute("donVangLai_10",true);
        when(s.chiTiet(eq(8),any())).thenReturn(Map.of());
        mvc.perform(get("/tra-hang/8").session(session)).andExpect(status().isOk()).andExpect(view().name("client/return/show"));
        verify(s).chiTiet(8,new Quyen(null,true,null));
    }
}
