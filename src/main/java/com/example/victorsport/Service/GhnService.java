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
    // Cố định staging, kể cả khi cấu hình nhầm token production.
    private static final String URL = "https://dev-online-gateway.ghn.vn/shiip/public-api";
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(GhnService.class);
    private final Map<String,List<Map<String,Object>>> diaChiDaLuu=new HashMap<>();
    private long hanDiaChi;
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
            if(duongDan.contains("master-data/") || duongDan.endsWith("/detail")) {
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
        } catch (org.springframework.web.client.RestClientResponseException e) {
            String maLoi="UNKNOWN";
            var khop=java.util.regex.Pattern.compile("\"code_message\"\\s*:\"?\\s*\"([A-Z0-9_]{1,80})\"").matcher(e.getResponseBodyAsString());
            if(khop.find()) maLoi=khop.group(1);
            log.warn("GHN {}: HTTP {}, code {}",duongDan,e.getStatusCode().value(),maLoi);
            if(e.getResponseBodyAsString().contains("FROM_ADDRESS_CONVERT_FAIL"))
                throw new IllegalStateException("Địa chỉ kho gửi chưa hợp lệ trên GHN. Vui lòng liên hệ cửa hàng.");
            if(e.getResponseBodyAsString().contains("TO_ADDRESS_CONVERT_FAIL") || e.getResponseBodyAsString().contains("TO_ADDRESS_CONFLICT"))
                throw new IllegalStateException("Địa chỉ nhận chưa khớp danh mục GHN. Vui lòng chọn lại tỉnh và phường/xã.");
            if(e.getStatusCode().value()==401 || e.getStatusCode().value()==403)
                throw new IllegalStateException("GHN chưa chấp nhận thông tin cửa hàng. Vui lòng liên hệ cửa hàng để kiểm tra kết nối.");
            if(maLoi.equals("ROUTE_NOT_FOUND_SERVICE") || maLoi.equals("SERVICE_NOT_FOUND_CONFIG_FEE"))
                throw new IllegalStateException("GHN chưa hỗ trợ dịch vụ cho tuyến giao hàng này. Vui lòng liên hệ cửa hàng.");
            throw new IllegalStateException("GHN chưa chấp nhận thông tin giao hàng. Vui lòng kiểm tra tỉnh/phường và thử lại.");
        } catch (RestClientException e) {
            // Chỉ ghi loại lỗi, không in Token, header, dữ liệu khách hoặc response.
            log.warn("GHN {}: {}",duongDan,e.getClass().getSimpleName());
            throw new IllegalStateException("Tạm thời không kết nối được GHN. Vui lòng thử tải lại địa chỉ.");
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
    public boolean dangBat() {return config.isEnabled();}
    private synchronized List<Map<String,Object>> diaChiMoi(String khoa,String path,Map<String,Object> thamSo) {
        if(!sanSang()) throw new IllegalStateException("Cửa hàng chưa sẵn sàng giao qua GHN. Vui lòng liên hệ cửa hàng.");
        if(System.currentTimeMillis()>=hanDiaChi) {diaChiDaLuu.clear();hanDiaChi=System.currentTimeMillis()+300000;}
        var cu=diaChiDaLuu.get(khoa);
        if(cu!=null) return cu;
        var ds=List.copyOf(diaChi(path,thamSo,"_id","name"));
        if(ds.isEmpty()) throw new IllegalStateException("GHN chưa có danh mục địa chỉ phù hợp. Vui lòng thử lại.");
        diaChiDaLuu.put(khoa,ds);return ds;
    }
    public List<Map<String,Object>> tinhMoi() {
        return diaChiMoi("tinh","/v3/master-data/province/all",Map.of("limit",200));
    }
    public List<Map<String,Object>> xaMoi(int tinh) {
        if(tinh<=0) throw new IllegalArgumentException("Vui lòng chọn tỉnh/thành phố.");
        return diaChiMoi("xa-"+tinh,"/v3/master-data/ward/all-by-province-id",Map.of("province_id",tinh,"limit",200));
    }
    public Map<String,Object> xemTruoc(Map<String,Object> body) {return banDo(goi("/v2/shipping-order/preview",themKhoGui(body)));}
    public Map<String,Object> taoDon(Map<String,Object> body) {return banDo(goi("/v2/shipping-order/create",themKhoGui(body)));}
    private Map<String,Object> themKhoGui(Map<String,Object> body) {
        var ketQua=new LinkedHashMap<>(body);
        if(!config.getFromAddress().isBlank()) {
            if(config.getFromProvinceName().isBlank() || config.getFromWardName().isBlank() || !config.getFromPhone().matches("0[0-9]{9}"))
                throw new IllegalStateException("Thông tin kho gửi của cửa hàng chưa đầy đủ.");
            ketQua.put("from_name",config.getFromName());ketQua.put("from_phone",config.getFromPhone());
            ketQua.put("from_address",config.getFromAddress());ketQua.put("from_province_name",config.getFromProvinceName());
            ketQua.put("from_ward_name",config.getFromWardName());ketQua.put("is_new_from_address",true);
        }
        return ketQua;
    }
    public Map<String,Object> chiTietDon(String ma) {
        if(ma==null || !ma.matches("[a-zA-Z0-9_-]{1,50}")) throw new IllegalArgumentException("Mã vận đơn GHN không hợp lệ.");
        return banDo(goi("/v2/shipping-order/detail",Map.of("order_code",ma)));
    }
    private Map<String,Object> banDo(Object data) {
        if(!(data instanceof Map<?,?> row)) throw new IllegalStateException("Dữ liệu vận đơn GHN không hợp lệ.");
        var result=new LinkedHashMap<String,Object>();row.forEach((k,v)->result.put(k.toString(),v));return result;
    }
    public int[] kichThuoc(int soDoi) {
        long nang=(long)soDoi*config.getWeightPerPair(),cao=(long)soDoi*config.getHeightPerPair();
        if(soDoi<1 || nang<1 || nang>=20000 || cao<1 || cao>200 || config.getLength()<1 || config.getLength()>200
                || config.getWidth()<1 || config.getWidth()>200)
            throw new IllegalArgumentException("Kiện hàng vượt kích thước giao GHN thông thường. Vui lòng liên hệ cửa hàng.");
        return new int[]{(int)nang,config.getLength(),config.getWidth(),(int)cao};
    }
    public BigDecimal docPhi(Map<String,Object> data) {
        if(!(data.get("total_fee") instanceof Number so)) throw new IllegalStateException("GHN chưa trả phí giao hàng hợp lệ.");
        BigDecimal phi=new BigDecimal(so.toString());
        if(phi.signum()<0 || phi.compareTo(new BigDecimal("1000000"))>0 || phi.stripTrailingZeros().scale()>0)
            throw new IllegalStateException("Phí GHN không hợp lệ.");
        return phi;
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
