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
        } finally {
            tomcat.stop();
            tomcat.destroy();
        }
    }
}
