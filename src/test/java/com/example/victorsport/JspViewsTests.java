package com.example.victorsport;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JspViewsTests {
    @Test
    void bienDichTatCaTrangVaIncludeJsp() throws Exception {
        Path webapp = Path.of("src/main/webapp").toAbsolutePath();
        var tomcat = new Tomcat();
        tomcat.setBaseDir(Path.of("target/jsp-test-tomcat").toAbsolutePath().toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addWebapp("", webapp.toString());
        context.setParentClassLoader(getClass().getClassLoader());
        Tomcat.addServlet(context, "kiemTraJsp", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                String page = request.getParameter("page");
                if (page == null || !page.startsWith("/WEB-INF/view/") || page.contains("..")) {
                    response.sendError(400);
                    return;
                }
                if("gioi-tinh".equals(request.getParameter("duLieuMau"))) {
                    request.setAttribute("sanPham",java.util.Map.of("id",8,"ten_san_pham","Giày kiểm tra giới tính","ma_san_pham","SP00008","ten_thuong_hieu","VictorSport","gia_ban",new java.math.BigDecimal("200000")));
                    request.setAttribute("cacDoiTuong",com.example.victorsport.Service.QuanLySanPhamService.DOI_TUONG);
                    request.setAttribute("gioiTinhCoSan",java.util.Set.of("NAM","NU","UNISEX"));
                    request.setAttribute("gioiTinhDaChon","NAM");request.setAttribute("tongTonKho",7);
                    var rows=new java.util.ArrayList<java.util.Map<String,Object>>();int id=1;
                    for(String gender:java.util.List.of("NAM","NU","UNISEX")) {
                        var row=new java.util.HashMap<String,Object>();row.put("id",id++);row.put("doi_tuong",gender);row.put("ten_gioi_tinh",com.example.victorsport.Service.QuanLySanPhamService.tenDoiTuong(gender));
                        row.put("so_luong",gender.equals("UNISEX")?0:gender.equals("NAM")?5:2);row.put("gia_sau_giam",200000);row.put("gia_goc",200000);
                        row.put("id_mau_sac",1);row.put("ten_kich_thuoc","Size 40");row.put("ten_mau_sac","Đen");row.put("ten_form_chan","Tiêu chuẩn");rows.add(row);
                    }
                    request.setAttribute("phienBan",rows);
                }
                String mau=request.getParameter("duLieuMau");
                if(mau!=null && mau.startsWith("qr-payment-")) {
                    var gd=new java.util.HashMap<String,Object>();
                    gd.put("id",111);gd.put("id_khach_hang",7);gd.put("ma_hoa_don","HD00111");
                    gd.put("so_tien",new java.math.BigDecimal("2726850"));gd.put("trang_thai_hien_tai",1);
                    gd.put("trang_thai",mau.endsWith("success")?"thanh_cong":"cho_thanh_toan");gd.put("het_han",false);
                    gd.put("con_lai",120);
                    request.setAttribute("giaoDich",gd);request.setAttribute("trenDienThoai",mau.endsWith("phone"));
                    request.setAttribute("csrfQrDemo","preview-token");request.setAttribute("maQuet","DEMO111"+"a".repeat(32));
                    request.setAttribute("anhQrDemo",new com.example.victorsport.Service.QrDemoService(null).anhQr("https://example.com/payment/preview"));
                }
                if(mau!=null && mau.startsWith("return-")) {
                    boolean admin=mau.equals("return-admin")||mau.equals("return-list");
                    request.setAttribute("quanTri",admin);request.setAttribute("baseTra",admin?"/quan-ly/tra-hang":"/tra-hang");
                    request.setAttribute("csrfTraHang","test-csrf");request.setAttribute("maTao",java.util.UUID.randomUUID().toString());
                    request.setAttribute("activeMenu","tra-hang");request.setAttribute("nganHang",com.example.victorsport.Service.TraHangService.NGAN_HANG);
                    request.setAttribute("lyDo",com.example.victorsport.Service.TraHangService.LY_DO);request.setAttribute("trangThaiTra",com.example.victorsport.Service.TraHangService.TRANG_THAI);
                    request.setAttribute("currentUser",new com.example.victorsport.Dto.NguoiDungDangNhap(1,"test@local","Quản trị viên","Quản trị viên"));
                    var hd=new java.util.HashMap<String,Object>();hd.put("id",111);hd.put("id_khach_hang",7);hd.put("ma_hoa_don","HD00111");hd.put("ten_khach_hang","Khách kiểm tra");hd.put("trang_thai_hien_tai",5);request.setAttribute("don",hd);
                    var d=new java.util.HashMap<String,Object>();d.put("id",21);d.put("duoc_duyet",true);d.put("ten_san_pham","Nike Air Zoom Pegasus 40");d.put("ten_mau_sac","Đen");d.put("ten_kich_thuoc","Size 41");d.put("gioi_tinh","Nam");d.put("so_luong",2);d.put("so_luong_da_tra",0);d.put("con_tra",2);d.put("so_tien_hoan",new java.math.BigDecimal("1800000"));d.put("gia_tri_hoan",new java.math.BigDecimal("1800000"));
                    request.setAttribute("dong",java.util.List.of(d));request.setAttribute("dongTra",java.util.List.of(d));request.setAttribute("dongDaChon",java.util.List.of(21));request.setAttribute("loiDieuKien","");request.setAttribute("cacYeuCau",java.util.List.of());
                    var y=new java.util.HashMap<String,Object>();y.put("id",8);y.put("ma_yeu_cau","TH000008");y.put("ma_hoa_don","HD00111");y.put("ten_khach_hang","Khách kiểm tra");y.put("so_tien_hoan",new java.math.BigDecimal("1800000"));y.put("trang_thai","DANG_KIEM_TRA");y.put("ten_trang_thai","Đang kiểm tra");y.put("ly_do","Sai size");y.put("hinh_thuc_tra","TAI_CUA_HANG");y.put("phuong_thuc_hoan","BANK");y.put("ngan_hang","Vietcombank");y.put("so_tai_khoan","000012345678");y.put("chu_tai_khoan","KHACH KIEM TRA");y.put("ngay_tao",java.sql.Timestamp.valueOf("2026-11-14 10:00:00"));
                    if(!mau.equals("return-form")&&!mau.equals("return-list"))request.setAttribute("yeuCau",y);
                    request.setAttribute("danhSach",java.util.List.of(y));request.setAttribute("tong",1);request.setAttribute("trang",1);request.setAttribute("soTrang",1);request.setAttribute("tuKhoa","");request.setAttribute("trangThai","");
                    request.setAttribute("lichSuTra",java.util.List.of(java.util.Map.of("ngay_tao",java.sql.Timestamp.valueOf("2026-11-14 10:00:00"),"noi_dung","Cửa hàng đã nhận hàng trả và bắt đầu kiểm tra.")));
                }
                if("order-detail".equals(mau)) {
                    var hd=new java.util.HashMap<String,Object>();hd.put("id",112);hd.put("loai_don",1);hd.put("ma_hoa_don","HD00112");hd.put("ten_khach_hang","Khách kiểm tra");hd.put("so_dien_thoai_khach_hang","0900000000");hd.put("dia_chi_khach_hang","Cầu Giấy, Hà Nội");hd.put("trang_thai_hien_tai",5);hd.put("ten_trang_thai","Đã nhận hàng");hd.put("ngay_tao",java.sql.Timestamp.valueOf("2026-11-14 14:24:00"));hd.put("vanDonGhn",java.util.Map.of("ma_van_don","LYU76G"));hd.put("da_thanh_toan",true);hd.put("qr_demo",true);hd.put("phuong_thuc","QR");hd.put("tong_tien",2190000);hd.put("tong_tien_giam",0);hd.put("phi_van_chuyen",2900);hd.put("tong_tien_sau_giam",2192900);request.setAttribute("don",hd);
                }
                if("invoice-list".equals(mau)) {
                    request.setAttribute("currentUser",new com.example.victorsport.Dto.NguoiDungDangNhap(2,"admin@local","Quản trị viên","Quản trị viên"));
                    var rows=new java.util.ArrayList<java.util.Map<String,Object>>();
                    for(int i=0;i<4;i++){var hd=new java.util.HashMap<String,Object>();hd.put("id",112-i);hd.put("ma_hoa_don","HD00"+(112-i));hd.put("ten_khach_hang","Long Nguyễn Thành");hd.put("so_dien_thoai_khach_hang","0900000000");hd.put("ngay_tao",java.sql.Timestamp.valueOf("2026-11-14 14:24:00"));hd.put("tong_tien_sau_giam",2192900);hd.put("tong_tien_da_hoan",i==0?2160000:0);hd.put("trang_thai_hien_tai",i==1?1:i==2?3:5);hd.put("ten_trang_thai",i==1?"Chờ xác nhận":i==2?"Đang chuẩn bị hàng":"Đã nhận hàng");hd.put("loai_don",1);hd.put("da_thanh_toan",true);hd.put("qr_demo",true);rows.add(hd);}
                    request.setAttribute("danhSachHoaDon",rows);request.setAttribute("tuDong",1);request.setAttribute("denDong",4);request.setAttribute("tongHoaDon",4);request.setAttribute("trang",1);request.setAttribute("soTrang",1);request.setAttribute("trangBatDau",1);request.setAttribute("trangKetThuc",1);
                }
                if("uu-dai-read".equals(mau)) {
                    request.setAttribute("chiXem",true);request.setAttribute("idUuDai",1);request.setAttribute("loai","0");
                    request.setAttribute("currentUser",new com.example.victorsport.Dto.NguoiDungDangNhap(3,"nv@local","Nhân viên","Nhân viên"));
                    var u=new java.util.HashMap<String,Object>();u.put("id",1);u.put("ten_dot_giam_gia","Ưu đãi kiểm tra");u.put("ten_phieu_giam_gia","Ưu đãi kiểm tra");u.put("trang_thai",true);u.put("ten_trang_thai","Đang áp dụng");u.put("gia_tri_giam_gia",10);u.put("so_luong_su_dung",20);
                    request.setAttribute("uuDai",u);request.setAttribute("danhSach",java.util.List.of(u));
                }
                request.getRequestDispatcher(page).forward(request, response);
            }
        });
        context.addServletMappingDecoded("/kiem-tra-jsp", "kiemTraJsp");
        try {
            tomcat.start();
            var client = HttpClient.newHttpClient();
            var errors = new ArrayList<String>();
            try (var files = Files.walk(webapp.resolve("WEB-INF/view"))) {
                for (Path file : files.filter(p -> p.toString().endsWith(".jsp")).toList()) {
                    String page = "/" + webapp.relativize(file).toString().replace('\\', '/');
                    URI uri = URI.create("http://127.0.0.1:" + tomcat.getConnector().getLocalPort()
                            + "/kiem-tra-jsp?jsp_precompile=true&page=" + page);
                    var result = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
                    if (result.statusCode() != 200) errors.add(page + ": " + result.statusCode());
                }
            }
            assertTrue(errors.isEmpty(), String.join("\n", errors));
            var fixture=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()
                    +"/kiem-tra-jsp?page=/WEB-INF/view/client/product/detail.jsp&duLieuMau=gioi-tinh")).GET().build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            assertTrue(fixture.statusCode()==200,fixture.body());
            for(String gender:java.util.List.of("NAM","NU","UNISEX"))assertTrue(fixture.body().contains("data-gioi-tinh=\""+gender+"\""));
            Files.createDirectories(Path.of("target/gioi-tinh-preview"));Files.writeString(Path.of("target/gioi-tinh-preview/san-pham.html"),fixture.body());
            Files.createDirectories(Path.of("target/qr-payment-preview"));
            for(String state:java.util.List.of("desktop","phone","success")) {
                var qr=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()
                    +"/kiem-tra-jsp?page=/WEB-INF/view/client/cart/qr-demo.jsp&duLieuMau=qr-payment-"+state)).GET().build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
                assertTrue(qr.statusCode()==200,qr.body());
                assertTrue(qr.body().contains("0000 1234 5678") && qr.body().contains("VICTOR SPORT"));
                assertTrue(qr.body().contains("2.726.850"));
                String visible=qr.body().replaceAll("(?s)<script>.*?</script>|<style>.*?</style>","").replaceAll("<[^>]+>","");
                assertTrue(!visible.toLowerCase(java.util.Locale.ROOT).contains("demo"));
                if(state.equals("success"))assertTrue(!qr.body().contains("<img class=\"qr-demo-picture\""));
                Files.writeString(Path.of("target/qr-payment-preview/"+state+".html"),qr.body());
            }
            var order=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()
                    +"/kiem-tra-jsp?page=/WEB-INF/view/client/order/detail.jsp&duLieuMau=order-detail")).GET().build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            assertTrue(order.statusCode()==200,order.body());
            Files.createDirectories(Path.of("target/order-detail-preview"));Files.writeString(Path.of("target/order-detail-preview/index.html"),order.body());
            for(String page:java.util.List.of("promotion/show.jsp","promotion/update.jsp","voucher/show.jsp","voucher/update.jsp")) {
                var read=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()
                    +"/kiem-tra-jsp?page=/WEB-INF/view/admin/"+page+"&duLieuMau=uu-dai-read")).GET().build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
                assertTrue(read.statusCode()==200,read.body());
                assertTrue(!read.body().contains(">Lưu đợt giảm giá</button>")&&!read.body().contains(">Lưu phiếu giảm giá</button>")&&!read.body().contains(">Ngừng áp dụng</button>")&&!read.body().contains(">Bỏ áp dụng</button>"));
                if(page.endsWith("update.jsp"))assertTrue(read.body().contains("class=\"promotion-fields\" disabled"));
                else assertTrue(read.body().contains(">Xem chi tiết</a>"));
            }
            var listInvoice=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()
                    +"/kiem-tra-jsp?page=/WEB-INF/view/admin/order/show.jsp&duLieuMau=invoice-list")).GET().build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            assertTrue(listInvoice.statusCode()==200,listInvoice.body());
            assertTrue(listInvoice.body().contains("14/11/2026")&&listInvoice.body().contains("2.192.900"),listInvoice.body());
            Files.createDirectories(Path.of("target/invoice-list-preview"));Files.writeString(Path.of("target/invoice-list-preview/index.html"),listInvoice.body());
            Files.createDirectories(Path.of("target/return-preview"));
            for(String state:java.util.List.of("form","client","admin","list")) {
                String page=state.equals("admin")||state.equals("list")?"admin/return/show.jsp":"client/return/show.jsp";
                var result=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+tomcat.getConnector().getLocalPort()
                    +"/kiem-tra-jsp?page=/WEB-INF/view/"+page+"&duLieuMau=return-"+state)).GET().build(),HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
                assertTrue(result.statusCode()==200,result.body());assertTrue(result.body().contains("Nike Air Zoom Pegasus 40")||state.equals("list"));
                Files.writeString(Path.of("target/return-preview/"+state+".html"),result.body());
            }
        } finally {
            tomcat.stop();
            tomcat.destroy();
        }
    }
}
