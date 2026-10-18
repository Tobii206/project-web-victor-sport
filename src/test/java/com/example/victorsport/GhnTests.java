package com.example.victorsport;

import com.example.victorsport.Config.GhnConfig;
import com.example.victorsport.Dto.TinhPhiGhnForm;
import com.example.victorsport.Service.GhnService;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GhnTests {
    static final String URL="https://dev-online-gateway.ghn.vn/shiip/public-api";
    GhnConfig config() {
        var config=new GhnConfig(); config.setEnabled(true); config.setToken("token-kiem-tra"); config.setShopId(123);
        return config;
    }
    TinhPhiGhnForm form() { return new TinhPhiGhnForm(201,1444,"00123",1000,35,25,15,1000000); }
    void diaChiHopLe(MockRestServiceServer server) {
        server.expect(requestTo(URL+"/master-data/district?province_id=201")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\":200,\"data\":[{\"DistrictID\":1444,\"DistrictName\":\"Huyện thử\"}]}",MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL+"/master-data/ward?district_id=1444")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\":200,\"data\":[{\"WardCode\":\"00123\",\"WardName\":\"Xã thử\"}]}",MediaType.APPLICATION_JSON));
    }
    @Test void chuaCauHinhKhongGoiApi() {
        var builder=RestClient.builder(); var server=MockRestServiceServer.bindTo(builder).build();
        assertThrows(IllegalStateException.class,()->new GhnService(new GhnConfig(),builder).tinh());
        server.verify();
    }
    @Test void dungStagingHeaderVaGetDiaChiGiuSoKhongDauMaXa() {
        var builder=RestClient.builder(); var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL+"/master-data/ward?district_id=1444")).andExpect(method(HttpMethod.GET))
                .andExpect(header("Token","token-kiem-tra")).andExpect(header("ShopId","123"))
                .andRespond(withSuccess("{\"code\":200,\"data\":[{\"WardCode\":\"00123\",\"WardName\":\"Xã thử\"}]}",MediaType.APPLICATION_JSON));
        assertEquals("00123",new GhnService(config(),builder).xa(1444).get(0).get("id"));
        server.verify();
    }
    @Test void tinhPhiDungDonViKhongNhanSoTienTuTrinhDuyet() {
        var builder=RestClient.builder(); var server=MockRestServiceServer.bindTo(builder).build();
        diaChiHopLe(server);
        server.expect(requestTo(URL+"/v2/shipping-order/fee")).andExpect(method(HttpMethod.POST))
                .andExpect(header("Token","token-kiem-tra")).andExpect(header("ShopId","123"))
                .andExpect(content().json("""
                    {"to_district_id":1444,"to_ward_code":"00123","weight":1000,"length":35,"width":25,"height":15,"insurance_value":1000000,"service_type_id":2}
                    """))
                .andRespond(withSuccess("{\"code\":200,\"data\":{\"total\":32500}}",MediaType.APPLICATION_JSON));
        assertEquals(new BigDecimal("32500"),new GhnService(config(),builder).tinhPhi(form())); server.verify();
    }
    @Test void diaChiKhongKhopKhongGoiTinhPhi() {
        var builder=RestClient.builder(); var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL+"/master-data/district?province_id=201"))
                .andRespond(withSuccess("{\"code\":200,\"data\":[]}",MediaType.APPLICATION_JSON));
        assertThrows(IllegalArgumentException.class,()->new GhnService(config(),builder).tinhPhi(form())); server.verify();
    }
    @Test void loiGhnKhongBiBienThanhPhiKhongDongHoacLoToken() {
        var builder=RestClient.builder(); var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL+"/master-data/province"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("token-kiem-tra").contentType(MediaType.TEXT_PLAIN));
        var loi=assertThrows(IllegalStateException.class,()->new GhnService(config(),builder).tinh());
        assertFalse(loi.getMessage().contains("token-kiem-tra")); server.verify();
    }
    @Test void phiAmBiTuChoi() {
        var builder=RestClient.builder(); var server=MockRestServiceServer.bindTo(builder).build();
        diaChiHopLe(server);
        server.expect(requestTo(URL+"/v2/shipping-order/fee"))
                .andRespond(withSuccess("{\"code\":200,\"data\":{\"total\":-1}}",MediaType.APPLICATION_JSON));
        assertThrows(IllegalStateException.class,()->new GhnService(config(),builder).tinhPhi(form())); server.verify();
    }
}
