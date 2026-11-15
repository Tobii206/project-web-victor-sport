package com.example.victorsport;

import com.example.victorsport.Config.TraHangConfig;
import com.example.victorsport.Service.TraHangService;
import com.example.victorsport.Service.TraHangService.Quyen;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mock.web.MockMultipartFile;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Chạy chủ động trên DB phát triển; dữ liệu đơn, tiền và kho của bài thử luôn rollback. */
@EnabledIfEnvironmentVariable(named="VICTORSPORT_SQL_TEST",matches="true")
class TraHangSqlServerTests {
    static JdbcTemplate jdbc;static TransactionTemplate tx;static int admin,variant;
    @BeforeAll static void setup() throws Exception {
        var env=new StandardEnvironment();env.getPropertySources().addLast(new ResourcePropertySource(new FileSystemResource("src/main/resources/application.properties")));
        var ds=new DriverManagerDataSource();ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        ds.setUrl(env.getRequiredProperty("spring.datasource.url"));ds.setUsername(env.getRequiredProperty("spring.datasource.username"));ds.setPassword(env.getRequiredProperty("spring.datasource.password"));
        new TraHangConfig(ds).run(null);new TraHangConfig(ds).run(null);
        jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        admin=jdbc.queryForObject("SELECT TOP 1 nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han WHERE nv.trang_thai=1 AND ISNULL(nv.xoa_mem,0)=0 AND q.ten_quyen_han=N'Quản trị viên' AND q.trang_thai=1 AND ISNULL(q.xoa_mem,0)=0 ORDER BY nv.id",Integer.class);
        variant=jdbc.queryForObject("SELECT TOP 1 id FROM chi_tiet_san_pham WHERE ISNULL(xoa_mem,0)=0 AND ISNULL(so_luong,0)<2147483600 ORDER BY id",Integer.class);
    }
    static int taoDon(){
        int id=jdbc.queryForObject("""
            INSERT INTO hoa_don(loai_don,tong_tien,tong_tien_sau_giam,phi_van_chuyen,ten_khach_hang,dia_chi_khach_hang,so_dien_thoai_khach_hang,trang_thai_hien_tai,ngay_thanh_toan)
            OUTPUT INSERTED.id VALUES(1,300,230,30,N'TEST_RETURN_ROLLBACK',N'Đơn kiểm thử rollback','0900000000',5,SYSDATETIME())
            """,Integer.class);
        jdbc.update("INSERT INTO hoa_don_chi_tiet(id_hoa_don,id_chi_tiet_san_pham,so_luong,don_gia) VALUES(?,?,3,100)",id,variant);
        jdbc.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,thoi_gian,ghi_chu) VALUES(?,5,DATEADD(DAY,-1,SYSDATETIME()),N'Nhận hàng kiểm thử')",id);
        int pt=jdbc.queryForObject("SELECT TOP 1 id FROM phuong_thuc_thanh_toan ORDER BY id",Integer.class);
        jdbc.update("INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai) VALUES(?,?,230,N'thanh_cong')",id,pt);
        return id;
    }
    static MockMultipartFile png() throws Exception {var out=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",out);return new MockMultipartFile("minhChung","anh.png","image/png",out.toByteArray());}
    static Map<String,String> form(){return new HashMap<>(Map.of("maTao",UUID.randomUUID().toString(),"lyDo","Sai size","hinhThuc","TAI_CUA_HANG","phuongThuc","CASH"));}
    static void duyetTatCa(TraHangService s,int yc,Quyen nv){
        var f=new HashMap<String,String>();for(var d:jdbc.queryForList("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",yc))f.put("duyet_"+d.get("id"),"1");
        s.quanTri(yc,nv,"duyet",f);
    }
    @Test void luongTraTungPhanHoanTienVaKhoDungTrenSqlServer() throws Exception {
        var photo=png();int tonTruoc=jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant);
        tx.execute(status->{status.setRollbackOnly();int hd=taoDon();var kh=new Quyen(null,true,null);var nv=new Quyen(null,false,admin);var s=new TraHangService(jdbc);
            int row=jdbc.queryForObject("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=?",Integer.class,hd);
            var f=form();int yc=s.tao(hd,kh,Map.of(row,1),f,List.of(photo));assertEquals(yc,s.tao(hd,kh,Map.of(row,1),f,List.of(photo)));
            assertThrows(IllegalArgumentException.class,()->s.tao(hd,kh,Map.of(row,1),form(),List.of(photo)));
            assertThrows(IllegalArgumentException.class,()->s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66")));
            duyetTatCa(s,yc,nv);s.quanTri(yc,nv,"nhan-hang",Map.of());s.quanTri(yc,nv,"kiem-tra",Map.of());
            int line=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",Integer.class,yc);
            s.quanTri(yc,nv,"duyet-hoan",Map.of("ghiChu","Hàng còn nguyên và đạt kiểm tra","nhap_"+line,"1","loi_"+line,"0","duyet_"+line,"1"));
            assertEquals(0,new BigDecimal("66").compareTo(jdbc.queryForObject("SELECT so_tien_hoan FROM yeu_cau_tra_hang WHERE id=?",BigDecimal.class,yc)));
            s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66"));s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66"));
            assertEquals(tonTruoc+1,jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM giao_dich_hoan_tien WHERE id_yeu_cau=?",Integer.class,yc));
            assertEquals(5,jdbc.queryForObject("SELECT trang_thai_hien_tai FROM hoa_don WHERE id=?",Integer.class,hd));
            assertEquals(1,jdbc.queryForObject("SELECT so_luong_da_tra FROM hoa_don_chi_tiet WHERE id=?",Integer.class,row));
            assertThrows(IllegalArgumentException.class,()->s.tao(hd,kh,Map.of(row,3),form(),List.of(photo)));
            int yc2=s.tao(hd,kh,Map.of(row,2),form(),List.of(photo));
            assertEquals(0,new BigDecimal("134").compareTo(jdbc.queryForObject("SELECT so_tien_hoan FROM yeu_cau_tra_hang WHERE id=?",BigDecimal.class,yc2)));
            var lichSuDon=new com.example.victorsport.Service.HoaDonService(jdbc).layLichSu(hd);
            var cacBuoc=lichSuDon.stream().filter(r->r.get("id_yeu_cau") instanceof Number id&&id.intValue()==yc).map(r->r.get("trang_thai_tra_hang")).toList();
            assertEquals(List.of("CHO_XAC_NHAN","DA_XAC_NHAN","DA_NHAN_HANG","DANG_KIEM_TRA","DA_DUYET_HOAN_TRA","DA_HOAN_TIEN"),cacBuoc);
            assertFalse(lichSuDon.stream().anyMatch(r->r.get("ghi_chu").toString().startsWith("Hoàn tiền theo yêu cầu TH"+yc+":")));
            assertTrue(s.form(hd,kh).containsKey("cacYeuCau"));assertTrue(s.chiTiet(yc,kh).containsKey("hoanTien"));
            assertTrue(s.anh(yc,jdbc.queryForObject("SELECT id FROM anh_minh_chung_tra_hang WHERE id_yeu_cau=?",Integer.class,yc),kh).length>0);
            assertTrue(new com.example.victorsport.Service.ThongKeService(jdbc).thongKe(java.time.LocalDate.now(),java.time.LocalDate.now(),"online").containsKey("tongQuan"));
            return null;});
        assertEquals(tonTruoc,jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant));
    }
    @Test void yeuCauCuDuKienVndNguyenVaKhongSuaTienDaHoanTruocDo() throws Exception {
        var photo=png();tx.execute(status->{status.setRollbackOnly();int hd=taoDon();var kh=new Quyen(null,true,null);var nv=new Quyen(null,false,admin);var s=new TraHangService(jdbc);
            int row=jdbc.queryForObject("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=?",Integer.class,hd);
            int yc=s.tao(hd,kh,Map.of(row,1),form(),List.of(photo));
            int line=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",Integer.class,yc);
            jdbc.update("UPDATE chi_tiet_tra_hang SET so_tien_hoan=66.66 WHERE id=?",line);jdbc.update("UPDATE yeu_cau_tra_hang SET so_tien_hoan=66.66 WHERE id=?",yc);
            var preview=(Map<?,?>)s.chiTiet(yc,nv).get("yeuCau");assertEquals(0,new BigDecimal("66").compareTo((BigDecimal)preview.get("so_tien_hoan")));
            assertEquals(0,new BigDecimal("66.66").compareTo(jdbc.queryForObject("SELECT so_tien_hoan FROM yeu_cau_tra_hang WHERE id=?",BigDecimal.class,yc)));
            duyetTatCa(s,yc,nv);s.quanTri(yc,nv,"nhan-hang",Map.of());s.quanTri(yc,nv,"kiem-tra",Map.of());
            s.quanTri(yc,nv,"duyet-hoan",Map.of("duyet_"+line,"1","nhap_"+line,"1","loi_"+line,"0","ghiChu","Hàng nguyên vẹn đạt kiểm tra"));s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66"));
            // Mô phỏng một giao dịch lịch sử đã chi tiền lẻ: các yêu cầu tiếp theo phải trừ đúng tiền thực chi.
            jdbc.update("UPDATE chi_tiet_tra_hang SET so_tien_hoan=66.66 WHERE id=?",line);jdbc.update("UPDATE yeu_cau_tra_hang SET so_tien_hoan=66.66 WHERE id=?",yc);
            jdbc.update("UPDATE giao_dich_hoan_tien SET so_tien=66.66 WHERE id_yeu_cau=?",yc);jdbc.update("UPDATE hoa_don SET tong_tien_da_hoan=66.66 WHERE id=?",hd);
            int yc2=s.tao(hd,kh,Map.of(row,2),form(),List.of(photo));
            assertEquals(0,new BigDecimal("133").compareTo(jdbc.queryForObject("SELECT so_tien_hoan FROM yeu_cau_tra_hang WHERE id=?",BigDecimal.class,yc2)));
            int line2=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",Integer.class,yc2);
            duyetTatCa(s,yc2,nv);s.quanTri(yc2,nv,"nhan-hang",Map.of());s.quanTri(yc2,nv,"kiem-tra",Map.of());
            s.quanTri(yc2,nv,"duyet-hoan",Map.of("duyet_"+line2,"1","nhap_"+line2,"2","loi_"+line2,"0","ghiChu","Hàng nguyên vẹn đạt kiểm tra"));s.quanTri(yc2,nv,"hoan-tien",Map.of("soTien","133"));
            assertEquals(0,new BigDecimal("199.66").compareTo(jdbc.queryForObject("SELECT tong_tien_da_hoan FROM hoa_don WHERE id=?",BigDecimal.class,hd)));
            assertEquals(0,new BigDecimal("66.66").compareTo(jdbc.queryForObject("SELECT so_tien FROM giao_dich_hoan_tien WHERE id_yeu_cau=?",BigDecimal.class,yc)));
            return null;});
    }
    @Test void chiHoanVaNhapKhoDongDuocTickTrenSqlServer() throws Exception {
        var photo=png();tx.execute(status->{status.setRollbackOnly();int hd=taoDon();var kh=new Quyen(null,true,null);var nv=new Quyen(null,false,admin);var s=new TraHangService(jdbc);
            int row=jdbc.queryForObject("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=?",Integer.class,hd);
            jdbc.update("UPDATE hoa_don_chi_tiet SET so_luong=2 WHERE id=?",row);
            int other=jdbc.queryForObject("SELECT TOP 1 id FROM chi_tiet_san_pham WHERE id<>? ORDER BY id",Integer.class,variant);
            int row2=jdbc.queryForObject("INSERT INTO hoa_don_chi_tiet(id_hoa_don,id_chi_tiet_san_pham,so_luong,don_gia) OUTPUT INSERTED.id VALUES(?,?,1,100)",Integer.class,hd,other);
            int ton=jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant);
            int ton2=jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,other);
            int yc=s.tao(hd,kh,Map.of(row,1,row2,1),form(),List.of(photo));
            int line=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=? AND id_hoa_don_chi_tiet=?",Integer.class,yc,row);
            s.quanTri(yc,nv,"duyet",Map.of("duyet_"+line,"1","ghiChu","Chỉ sản phẩm thứ nhất đủ điều kiện trả"));
            assertEquals(0,new BigDecimal("66").compareTo(jdbc.queryForObject("SELECT so_tien_hoan FROM yeu_cau_tra_hang WHERE id=?",BigDecimal.class,yc)));
            s.quanTri(yc,nv,"nhan-hang",Map.of());s.quanTri(yc,nv,"kiem-tra",Map.of());
            s.quanTri(yc,nv,"duyet-hoan",Map.of("duyet_"+line,"1","nhap_"+line,"1","loi_"+line,"0","ghiChu","Sản phẩm nguyên vẹn đạt kiểm tra"));
            s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66"));
            assertEquals(ton+1,jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant));
            assertEquals(ton2,jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,other));
            assertEquals(0,jdbc.queryForObject("SELECT so_luong_da_tra FROM hoa_don_chi_tiet WHERE id=?",Integer.class,row2));
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM san_pham_hoan_tra WHERE id_chi_tiet_tra IN (SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?)",Integer.class,yc));
            return null;});
    }
    @Test void nhanVienXuLyDonVaHoanTienTrenSqlServer() throws Exception {
        var staff=jdbc.queryForList("""
            SELECT TOP 1 nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
            WHERE nv.trang_thai=1 AND ISNULL(nv.xoa_mem,0)=0 AND q.trang_thai=1 AND ISNULL(q.xoa_mem,0)=0
              AND q.ten_quyen_han<>N'Quản trị viên' AND EXISTS (
                SELECT 1 FROM quyen_han_chuc_nang qcn JOIN chuc_nang cn ON cn.id=qcn.id_chuc_nang
                WHERE qcn.id_quyen_han=q.id AND ISNULL(qcn.xoa_mem,0)=0
                  AND cn.ma_chuc_nang='ORDER_WRITE' AND ISNULL(cn.xoa_mem,0)=0 AND ISNULL(cn.trang_thai,1)=1)
            ORDER BY nv.id
            """);
        Assumptions.assumeTrue(!staff.isEmpty(),"Database chưa có nhân viên hoạt động với ORDER_WRITE.");
        int idNv=((Number)staff.get(0).get("id")).intValue();var photo=png();
        tx.execute(status->{status.setRollbackOnly();int hd=taoDon();var nv=new Quyen(null,false,idNv);var s=new TraHangService(jdbc);
            jdbc.update("UPDATE hoa_don SET trang_thai_hien_tai=2 WHERE id=?",hd);
            new com.example.victorsport.Service.HoaDonService(jdbc).batDauChuanBi(hd,idNv);
            assertEquals(3,jdbc.queryForObject("SELECT trang_thai_hien_tai FROM hoa_don WHERE id=?",Integer.class,hd));
            jdbc.update("UPDATE hoa_don SET trang_thai_hien_tai=5 WHERE id=?",hd);
            int row=jdbc.queryForObject("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=?",Integer.class,hd);
            int yc=s.tao(hd,nv,Map.of(row,1),form(),List.of(photo));
            duyetTatCa(s,yc,nv);s.quanTri(yc,nv,"nhan-hang",Map.of());s.quanTri(yc,nv,"kiem-tra",Map.of());
            int line=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",Integer.class,yc);
            s.quanTri(yc,nv,"duyet-hoan",Map.of("ghiChu","Hàng nguyên vẹn đạt kiểm tra","nhap_"+line,"1","loi_"+line,"0","duyet_"+line,"1"));
            s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66"));
            assertEquals(idNv,jdbc.queryForObject("SELECT id_nhan_vien FROM giao_dich_hoan_tien WHERE id_yeu_cau=?",Integer.class,yc));
            var history=new com.example.victorsport.Service.HoaDonService(jdbc).layLichSu(hd);
            var tenNv=jdbc.queryForObject("SELECT ten_nhan_vien FROM nhan_vien WHERE id=?",String.class,idNv);
            var maNv=jdbc.queryForObject("SELECT ma_nhan_vien FROM nhan_vien WHERE id=?",String.class,idNv);
            assertTrue(history.stream().anyMatch(h->tenNv.equals(h.get("nguoi_thao_tac"))&&maNv.equals(h.get("ma_nguoi_thao_tac"))));
            jdbc.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,loai_nguoi_thuc_hien) VALUES(?,5,N'Nhận hàng kiểm thử khách vãng lai','khach_hang')",hd);
            assertTrue(new com.example.victorsport.Service.HoaDonService(jdbc).layLichSu(hd).stream().anyMatch(h->"Khách vãng lai".equals(h.get("vai_tro_thao_tac"))));
            // Thu hồi quyền có hiệu lực ở tầng service ngay cả khi phiên đăng nhập còn cũ.
            jdbc.update("UPDATE nhan_vien SET trang_thai=0 WHERE id=?",idNv);
            assertThrows(IllegalArgumentException.class,()->s.chiTiet(yc,nv));
            return null;});
    }
    @Test void nhapRoHangDatVaHangLoiChiCongKhoPhanDat() throws Exception {
        var photo=png();tx.execute(status->{status.setRollbackOnly();int hd=taoDon();var kh=new Quyen(null,true,null);var nv=new Quyen(null,false,admin);var s=new TraHangService(jdbc);
            int row=jdbc.queryForObject("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=?",Integer.class,hd);
            int ton=jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant);
            int yc=s.tao(hd,kh,Map.of(row,3),form(),List.of(photo));int line=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",Integer.class,yc);
            duyetTatCa(s,yc,nv);s.quanTri(yc,nv,"nhan-hang",Map.of());s.quanTri(yc,nv,"kiem-tra",Map.of());
            s.quanTri(yc,nv,"duyet-hoan",Map.of("duyet_"+line,"1","nhap_"+line,"1","loi_"+line,"2","ghiChu","Một đôi đạt và hai đôi lỗi cần tách riêng"));
            s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","200"));
            assertEquals(ton+1,jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant));
            assertEquals(2,jdbc.queryForObject("SELECT so_luong FROM san_pham_hoan_tra WHERE id_chi_tiet_tra=?",Integer.class,line));
            assertEquals(3,jdbc.queryForObject("SELECT so_luong_da_tra FROM hoa_don_chi_tiet WHERE id=?",Integer.class,row));return null;});
    }
    @Test void hangLoiKhongNhapKhoVaHanTinhTuLanNhanDau() throws Exception {
        var photo=png();int ton=jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant);
        tx.execute(status->{status.setRollbackOnly();int hd=taoDon();var kh=new Quyen(null,true,null);var nv=new Quyen(null,false,admin);var s=new TraHangService(jdbc);
            int row=jdbc.queryForObject("SELECT id FROM hoa_don_chi_tiet WHERE id_hoa_don=?",Integer.class,hd);
            int yc=s.tao(hd,kh,Map.of(row,1),form(),List.of(photo));duyetTatCa(s,yc,nv);s.quanTri(yc,nv,"nhan-hang",Map.of());s.quanTri(yc,nv,"kiem-tra",Map.of());
            int line=jdbc.queryForObject("SELECT id FROM chi_tiet_tra_hang WHERE id_yeu_cau=?",Integer.class,yc);
            s.quanTri(yc,nv,"duyet-hoan",Map.of("ghiChu","Sản phẩm lỗi không thể nhập kho bán","nhap_"+line,"0","loi_"+line,"1","duyet_"+line,"1"));s.quanTri(yc,nv,"hoan-tien",Map.of("soTien","66"));
            assertEquals(ton,jdbc.queryForObject("SELECT so_luong FROM chi_tiet_san_pham WHERE id=?",Integer.class,variant));
            assertEquals(1,jdbc.queryForObject("SELECT so_luong FROM san_pham_hoan_tra WHERE id_chi_tiet_tra=?",Integer.class,line));assertFalse(s.hangLoi(nv).isEmpty());
            assertFalse(((List<?>)s.danhSach(nv,"","DA_HOAN_TIEN",1).get("danhSach")).isEmpty());
            jdbc.update("UPDATE lich_su_hoa_don SET thoi_gian=DATEADD(DAY,-16,SYSDATETIME()) WHERE id_hoa_don=?",hd);
            jdbc.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu) VALUES(?,5,N'Ghi chú sau khi nhận hàng')",hd);
            assertThrows(IllegalArgumentException.class,()->s.tao(hd,kh,Map.of(row,1),form(),List.of(photo)));
            return null;});
    }
}
