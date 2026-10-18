package com.example.victorsport.Service;

import com.example.victorsport.Config.GhnConfig;
import com.example.victorsport.Dto.TinhPhiGhnForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class GhnService {
    // Chỉ gọi staging: không tạo vận đơn hoặc phát sinh giao hàng thật.
    private static final String URL = "https://dev-online-gateway.ghn.vn/shiip/public-api";
    private final GhnConfig config;
    private final RestClient client;

    @Autowired
    public GhnService(GhnConfig config) { this(config, taoClient()); }
    public GhnService(GhnConfig config, RestClient.Builder builder) {
        this.config=config;
        this.client=builder.baseUrl(URL).build();
    }
    private static RestClient.Builder taoClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return RestClient.builder().requestFactory(factory);
    }
    public boolean sanSang() { return config.sanSang(); }

    private Object goi(String duongDan, Map<String,Object> body) {
        if (!sanSang()) throw new IllegalStateException("Chưa cấu hình GHN thử nghiệm. Vui lòng điền GHN_TOKEN, GHN_SHOP_ID và bật GHN_ENABLED.");
        try {
            RestClient.RequestHeadersSpec<?> request;
            if(duongDan.startsWith("/master-data/")) {
                request=client.get().uri(uri -> {
                    uri.path(duongDan);
                    body.forEach(uri::queryParam);
                    return uri.build();
                });
            } else request=client.post().uri(duongDan).body(body);
            var response=request.header("Token",config.getToken())
                    .header("ShopId",String.valueOf(config.getShopId()))
                    .retrieve().body(new ParameterizedTypeReference<Map<String,Object>>() {});
            if (response==null || !(response.get("code") instanceof Number code) || code.intValue()!=200 || response.get("data")==null)
                throw new IllegalStateException("GHN chưa trả về kết quả hợp lệ. Vui lòng kiểm tra cấu hình thử nghiệm và địa chỉ.");
            return response.get("data");
        } catch (RestClientException e) {
            // Không đưa response/header chứa token lên giao diện hoặc log.
            throw new IllegalStateException("Không lấy được dữ liệu GHN. Kiểm tra Token, Shop ID thử nghiệm và kết nối rồi thử lại.");
        }
    }

    private List<Map<String,Object>> diaChi(String duongDan, Map<String,Object> body, String ma, String ten) {
        Object data=goi(duongDan,body);
        if (!(data instanceof List<?> list)) throw new IllegalStateException("Dữ liệu địa chỉ GHN chưa hợp lệ.");
        var ketQua=new ArrayList<Map<String,Object>>();
        for(Object item:list) if(item instanceof Map<?,?> row && row.get(ma)!=null && row.get(ten)!=null)
            ketQua.add(Map.of("id",row.get(ma).toString(),"ten",row.get(ten).toString()));
        return ketQua;
    }
    public List<Map<String,Object>> tinh() {
        return diaChi("/master-data/province",Map.of(),"ProvinceID","ProvinceName");
    }
    public List<Map<String,Object>> huyen(int tinh) {
        if(tinh<=0) throw new IllegalArgumentException("Vui lòng chọn tỉnh/thành phố.");
        return diaChi("/master-data/district",Map.of("province_id",tinh),"DistrictID","DistrictName");
    }
    public List<Map<String,Object>> xa(int huyen) {
        if(huyen<=0) throw new IllegalArgumentException("Vui lòng chọn quận/huyện GHN.");
        return diaChi("/master-data/ward",Map.of("district_id",huyen),"WardCode","WardName");
    }
    public BigDecimal tinhPhi(TinhPhiGhnForm form) {
        if(huyen(form.tinh()).stream().noneMatch(r -> r.get("id").equals(form.huyen().toString()))
                || xa(form.huyen()).stream().noneMatch(r -> r.get("id").equals(form.xa())))
            throw new IllegalArgumentException("Tỉnh, huyện và phường/xã GHN không khớp. Vui lòng chọn lại.");
        Map<String,Object> body=new LinkedHashMap<>();
        body.put("to_district_id",form.huyen()); body.put("to_ward_code",form.xa());
        body.put("service_type_id",2); body.put("weight",form.khoiLuong());
        body.put("length",form.dai()); body.put("width",form.rong()); body.put("height",form.cao());
        body.put("insurance_value",form.giaTriHang());
        if(config.getFromDistrictId()>0) body.put("from_district_id",config.getFromDistrictId());
        if(!config.getFromWardCode().isBlank()) body.put("from_ward_code",config.getFromWardCode());
        Object data=goi("/v2/shipping-order/fee",body);
        if (!(data instanceof Map<?,?> row) || !(row.get("total") instanceof Number total))
            throw new IllegalStateException("GHN chưa trả phí giao hàng. Vui lòng thử lại.");
        BigDecimal phi=new BigDecimal(total.toString());
        if(phi.signum()<0) throw new IllegalStateException("Phí GHN không hợp lệ.");
        return phi;
    }
}
