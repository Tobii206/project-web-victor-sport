package com.example.victorsport.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Chỉ kết nối môi trường thử nghiệm; IPN là nguồn cập nhật thanh toán. */
@Service
public class VnpaySandboxService {
    private static final String CONG = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final JdbcTemplate jdbc;
    @Value("${thanhtoan.vnpay.tmn-code:}") private String tmnCode = "";
    @Value("${thanhtoan.vnpay.hash-secret:}") private String hashSecret = "";
    @Value("${thanhtoan.vnpay.return-url:http://localhost:8080/thanh-toan/vnpay/ket-qua}")
    private String returnUrl = "http://localhost:8080/thanh-toan/vnpay/ket-qua";
    @org.springframework.beans.factory.annotation.Autowired(required=false)
    private ChuongTrucTuyenService chuong;
    public VnpaySandboxService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean sanSang() { return !tmnCode.isBlank() && !hashSecret.isBlank(); }
    public void kiemTraCauHinh() {
        if (!sanSang()) throw new IllegalArgumentException("VNPAY Sandbox chưa được cấu hình. Vui lòng chọn phương thức khác.");
        var uri = URI.create(returnUrl);
        if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getHost()==null || uri.getRawQuery()!=null || uri.getFragment()!=null)
            throw new IllegalArgumentException("Địa chỉ nhận kết quả VNPAY chưa hợp lệ.");
    }
    public String taoDuongDan(String ma, BigDecimal tien, String ip, ZonedDateTime ngayTao) {
        kiemTraCauHinh();
        if(tien==null || tien.signum()<=0 || tien.stripTrailingZeros().scale()>0)
            throw new IllegalArgumentException("Số tiền VNPAY phải là số đồng nguyên và lớn hơn 0.");
        var p = new TreeMap<String,String>();
        p.put("vnp_Version", "2.1.0"); p.put("vnp_Command", "pay"); p.put("vnp_TmnCode", tmnCode);
        p.put("vnp_Amount", tien.movePointRight(2).toBigIntegerExact().toString());
        p.put("vnp_CurrCode", "VND"); p.put("vnp_TxnRef", ma);
        p.put("vnp_OrderInfo", "Thanh toan don hang Victor Sport " + ma);
        p.put("vnp_OrderType", "other"); p.put("vnp_Locale", "vn"); p.put("vnp_ReturnUrl", returnUrl);
        p.put("vnp_IpAddr", ip); p.put("vnp_CreateDate", NGAY.format(ngayTao.withZoneSameInstant(MUI_GIO)));
        p.put("vnp_ExpireDate", NGAY.format(ngayTao.plusMinutes(15).withZoneSameInstant(MUI_GIO)));
        String duLieu = chuoiKy(p);
        return CONG + "?" + duLieu + "&vnp_SecureHash=" + ky(duLieu);
    }
    private static String chuoiKy(Map<String,String> p) {
        var join = new StringJoiner("&");
        new TreeMap<>(p).forEach((k,v)-> { if(v!=null && !v.isEmpty()) join.add(maHoa(k)+"="+maHoa(v)); });
        return join.toString();
    }
    private static String maHoa(String v) { return URLEncoder.encode(v, StandardCharsets.UTF_8); }
    private String ky(String v) {
        try {
            var mac=Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(v.getBytes(StandardCharsets.UTF_8)));
        } catch(java.security.GeneralSecurityException ex) { throw new IllegalStateException("Không tạo được chữ ký VNPAY.",ex); }
    }
    public boolean chuKyHopLe(Map<String,String> p) {
        if(!sanSang() || !tmnCode.equals(p.get("vnp_TmnCode"))) return false;
        String hash=p.get("vnp_SecureHash");
        if(hash==null || !hash.matches("[a-fA-F0-9]{128}")) return false;
        var fields=new TreeMap<String,String>();
        p.forEach((k,v)-> {if(k.startsWith("vnp_") && !Set.of("vnp_SecureHash","vnp_SecureHashType").contains(k)) fields.put(k,v);});
        return MessageDigest.isEqual(HexFormat.of().parseHex(hash),HexFormat.of().parseHex(ky(chuoiKy(fields))));
    }

    // Gọi trong cùng transaction tạo đơn: lỗi cấu hình/kết nối DB sẽ hoàn lại giỏ, kho và phiếu.
    public void taoGiaoDich(int id, BigDecimal tien) {
        kiemTraCauHinh();
        String ip="127.0.0.1";
        if(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                instanceof org.springframework.web.context.request.ServletRequestAttributes a) ip=a.getRequest().getRemoteAddr();
        String ma="VS"+id+UUID.randomUUID().toString().replace("-", "");
        var now=ZonedDateTime.now(MUI_GIO);
        String url=taoDuongDan(ma,tien,ip,now);
        jdbc.update("""
            IF NOT EXISTS (SELECT 1 FROM phuong_thuc_thanh_toan WITH (UPDLOCK,HOLDLOCK) WHERE nha_cung_cap=N'VNPAY_SANDBOX' AND ISNULL(xoa_mem,0)=0 AND trang_thai=1)
            INSERT INTO phuong_thuc_thanh_toan(ten_phuong_thuc_thanh_toan,nha_cung_cap,trang_thai,xoa_mem)
            VALUES(N'VNPAY Sandbox (thử nghiệm)',N'VNPAY_SANDBOX',1,0)
            """);
        Integer pt=jdbc.queryForObject("SELECT TOP 1 id FROM phuong_thuc_thanh_toan WHERE nha_cung_cap=N'VNPAY_SANDBOX' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id",Integer.class);
        jdbc.update("""
            INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,ma_yeu_cau,ma_tham_chieu,duong_dan_thanh_toan,thoi_gian_het_han,ghi_chu)
            VALUES(?,?,?,N'cho_thanh_toan',?,?,?,?,N'VNPAY Sandbox - chờ thông báo thanh toán tự động')
            """,id,pt,tien,ma,ma,url,java.sql.Timestamp.valueOf(now.plusMinutes(15).toLocalDateTime()));
    }

    public Map<String,Object> layGiaoDich(int id,Integer kh,boolean phienKhachVangLai) {
        if(kh==null && !phienKhachVangLai) return null;
        var ds=jdbc.queryForList("""
            SELECT TOP 1 hd.id,hd.id_khach_hang,hd.ma_hoa_don,hd.trang_thai_hien_tai,gd.so_tien,gd.trang_thai,
                gd.duong_dan_thanh_toan,CASE WHEN gd.thoi_gian_het_han<=SYSDATETIME() THEN 1 ELSE 0 END AS het_han
            FROM hoa_don hd JOIN giao_dich_thanh_toan gd ON gd.id_hoa_don=hd.id
            JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE hd.id=? AND ISNULL(hd.xoa_mem,0)=0 AND ISNULL(gd.xoa_mem,0)=0 AND pt.nha_cung_cap=N'VNPAY_SANDBOX'
              AND ((? IS NOT NULL AND hd.id_khach_hang=?) OR (?=1 AND hd.id_khach_hang IS NULL))
            ORDER BY gd.id DESC
            """,id,kh,kh,phienKhachVangLai?1:0);
        return ds.isEmpty()?null:ds.get(0);
    }
    public Integer timDonTheoMa(String ma) {
        if(ma==null || !ma.matches("VS[0-9]+[a-f0-9]{32}")) return null;
        var ds=jdbc.queryForList("SELECT id_hoa_don FROM giao_dich_thanh_toan WHERE ma_yeu_cau=? AND ISNULL(xoa_mem,0)=0",ma);
        return ds.size()==1?((Number)ds.get(0).get("id_hoa_don")).intValue():null;
    }
    public record KetQua(String RspCode,String Message) {}
    private KetQua loi(String ma,String nd) { return new KetQua(ma,nd); }

    @Transactional
    public KetQua nhanIpn(Map<String,String> p) {
        if(!chuKyHopLe(p)) return loi("97","Invalid signature");
        String ma=p.get("vnp_TxnRef"),amount=p.get("vnp_Amount");
        if(ma==null || !ma.matches("VS[0-9]+[a-f0-9]{32}") || amount==null || !amount.matches("[0-9]{1,20}")
                || !Objects.toString(p.get("vnp_ResponseCode"),"").matches("[0-9]{2}")
                || !Objects.toString(p.get("vnp_TransactionStatus"),"").matches("[0-9]{2}")) return loi("99","Invalid data");
        boolean thanhCong="00".equals(p.get("vnp_ResponseCode")) && "00".equals(p.get("vnp_TransactionStatus"));
        if(thanhCong && !Objects.toString(p.get("vnp_TransactionNo"),"").matches("[0-9]{1,100}")) return loi("99","Invalid transaction");
        var ids=jdbc.queryForList("""
            SELECT gd.id_hoa_don FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.ma_yeu_cau=? AND pt.nha_cung_cap=N'VNPAY_SANDBOX' AND ISNULL(gd.xoa_mem,0)=0
            """,ma);
        if(ids.size()!=1) return loi("01","Order not found");
        int id=((Number)ids.get(0).get("id_hoa_don")).intValue();
        // Cùng thứ tự khóa với hủy/sửa/xác nhận đơn: hóa đơn trước, giao dịch sau.
        var dons=jdbc.queryForList("SELECT tong_tien_sau_giam,trang_thai_hien_tai FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND ISNULL(xoa_mem,0)=0",id);
        if(dons.isEmpty()) return loi("01","Order not found");
        var gd=jdbc.queryForList("SELECT id,so_tien,trang_thai FROM giao_dich_thanh_toan WITH (UPDLOCK,HOLDLOCK) WHERE id_hoa_don=? AND ma_yeu_cau=? AND ISNULL(xoa_mem,0)=0",id,ma);
        if(gd.size()!=1) return loi("01","Order not found");
        var g=gd.get(0);var don=dons.get(0);
        BigDecimal tien=new BigDecimal(amount).movePointLeft(2);
        if(tien.signum()<=0 || tien.compareTo((BigDecimal)g.get("so_tien"))!=0 || tien.compareTo((BigDecimal)don.get("tong_tien_sau_giam"))!=0)
            return loi("04","Invalid amount");
        if("thanh_cong".equals(g.get("trang_thai"))) return loi("02","Order already confirmed");
        if(!thanhCong && !"cho_thanh_toan".equals(g.get("trang_thai"))) return loi("02","Order already confirmed");
        // Thông báo thành công đến muộn vẫn ghi nhận tiền, giữ nguyên đơn đã hủy để đối soát.
        String tt=thanhCong?"thanh_cong":("24".equals(p.get("vnp_ResponseCode"))?"da_huy":"that_bai");
        jdbc.update("""
            UPDATE giao_dich_thanh_toan SET trang_thai=?,ma_giao_dich_ngoai=?,du_lieu_phan_hoi=?,
                thoi_gian_cap_nhat=SYSDATETIME(),ghi_chu=? WHERE id=?
            """,tt,p.get("vnp_TransactionNo"),"ResponseCode="+p.get("vnp_ResponseCode")+";TransactionStatus="+p.get("vnp_TransactionStatus"),
                thanhCong?"VNPAY Sandbox xác nhận thanh toán thành công":"VNPAY Sandbox: giao dịch chưa thành công",g.get("id"));
        if(thanhCong) {
            jdbc.update("UPDATE hoa_don SET ngay_thanh_toan=COALESCE(ngay_thanh_toan,SYSDATETIME()),ngay_cap_nhat=SYSDATETIME() WHERE id=?",id);
            int trangThai=((Number)don.get("trang_thai_hien_tai")).intValue();
            jdbc.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu) VALUES(?,?,?)",id,trangThai,
                trangThai==6?"VNPAY Sandbox đã thanh toán cho đơn đã hủy. Cửa hàng cần đối soát hoàn tiền.":"VNPAY Sandbox xác nhận đã thanh toán. Chờ cửa hàng xác nhận đơn.");
            if(chuong!=null) chuong.donThayDoi(id);
        }
        return loi("00","Confirm success");
    }
}
