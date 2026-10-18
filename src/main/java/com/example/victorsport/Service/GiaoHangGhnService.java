package com.example.victorsport.Service;

import com.example.victorsport.Dto.BaoGiaGhn;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class GiaoHangGhnService {
    private final JdbcTemplate jdbc;
    private final GhnService ghn;
    public GiaoHangGhnService(JdbcTemplate jdbc,GhnService ghn){this.jdbc=jdbc;this.ghn=ghn;}
    public boolean dangBat(){return ghn.dangBat();}
    public BaoGiaGhn baoGia(Map<Integer,Integer> gio,String ten,String sdt,String diaChi,String phuongThuc,
                            BigDecimal tienHang,int tinh,int xa) {
        String tenTinh=timTen(ghn.tinhMoi(),tinh),tenXa=timTen(ghn.xaMoi(tinh),xa);
        long soDoi=gio.values().stream().mapToLong(Integer::longValue).sum();
        if(soDoi<1 || soDoi>1000) throw new IllegalArgumentException("Số lượng giao hàng không hợp lệ.");
        int[] kt=ghn.kichThuoc((int)soDoi);
        diaChi=diaChi.trim()+", "+tenXa+", "+tenTinh;
        if(diaChi.length()>255) throw new IllegalArgumentException("Tổng địa chỉ giao hàng tối đa 255 ký tự.");
        var body=body(ten,sdt,diaChi,tenTinh,tenXa,kt[0],kt[1],kt[2],kt[3],tienHang,
                "COD".equals(phuongThuc)?tienHang:BigDecimal.ZERO);
        var data=ghn.xemTruoc(body);
        return new BaoGiaGhn(gio,ten,sdt,diaChi,phuongThuc,tienHang,ghn.docPhi(data),tinh,xa,tenTinh,tenXa,
                kt[0],kt[1],kt[2],kt[3],Instant.now().plusSeconds(300));
    }
    private String timTen(List<Map<String,Object>> ds,int id) {
        return ds.stream().filter(r->String.valueOf(id).equals(r.get("id"))).map(r->r.get("ten").toString()).findFirst()
                .orElseThrow(()->new IllegalArgumentException("Địa chỉ GHN không khớp. Vui lòng chọn lại tỉnh và phường/xã."));
    }
    private Map<String,Object> body(String ten,String sdt,String diaChi,String tinh,String xa,
                                    int nang,int dai,int rong,int cao,BigDecimal giaTri,BigDecimal cod) {
        if(cod.signum()<0 || cod.compareTo(new BigDecimal("50000000"))>0)
            throw new IllegalArgumentException("Số tiền thu hộ GHN vượt giới hạn 50 triệu đồng.");
        var body=new LinkedHashMap<String,Object>();
        body.put("to_name",ten);body.put("to_phone",sdt);body.put("to_address",diaChi);
        body.put("to_province_name",tinh);body.put("to_ward_name",xa);body.put("is_new_to_address",true);
        body.put("payment_type_id",1);body.put("required_note","CHOXEMHANGKHONGTHU");body.put("service_type_id",2);
        body.put("content","Giày thể thao VictorSport");body.put("weight",nang);body.put("length",dai);body.put("width",rong);body.put("height",cao);
        body.put("insurance_value",giaTri.min(new BigDecimal("5000000")).intValueExact());body.put("cod_amount",cod.intValueExact());
        return body;
    }
    // Gọi trong cùng giao dịch tạo hóa đơn, chưa gửi yêu cầu tạo đơn cho GHN.
    public void luu(int id,BaoGiaGhn q) {
        jdbc.update("""
            INSERT INTO van_don_ghn(id_hoa_don,ma_don_shop,ten_tinh,ten_xa,khoi_luong,dai,rong,cao,phi_bao_gia)
            VALUES (?,?,?,?,?,?,?,?,?)
            """,id,"VS-"+id+"-"+UUID.randomUUID().toString().substring(0,12),q.tenTinh(),q.tenXa(),q.khoiLuong(),q.dai(),q.rong(),q.cao(),q.phi());
    }
    @Transactional
    public String taoVanDon(int id) {
        // Khóa cả hóa đơn và vận đơn để hai lần bấm không tạo hai vận đơn.
        var hd=jdbc.queryForList("SELECT * FROM hoa_don WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND loai_don IN (1,2) AND ISNULL(xoa_mem,0)=0",id);
        if(hd.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn online.");
        var dong=hd.get(0);
        var ds=jdbc.queryForList("SELECT * FROM van_don_ghn WITH (UPDLOCK,HOLDLOCK) WHERE id_hoa_don=?",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Đơn này chưa có địa chỉ và báo giá GHN. Chỉ đơn đặt qua luồng GHN mới tạo được vận đơn.");
        var vd=ds.get(0);
        if(vd.get("ma_van_don")!=null) return vd.get("ma_van_don").toString();
        if(((Number)dong.get("trang_thai_hien_tai")).intValue()!=4)
            throw new IllegalArgumentException("Vui lòng xác nhận đơn trước khi tạo vận đơn GHN.");
        var thu=jdbc.queryForObject("SELECT ISNULL(SUM(so_tien),0) FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND ISNULL(xoa_mem,0)=0",BigDecimal.class,id);
        BigDecimal tong=(BigDecimal)dong.get("tong_tien_sau_giam"),conLai=tong.subtract(thu==null?BigDecimal.ZERO:thu).max(BigDecimal.ZERO);
        var pt=jdbc.queryForList("""
            SELECT TOP 1 pt.nha_cung_cap FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND gd.trang_thai<>N'da_huy' AND ISNULL(gd.xoa_mem,0)=0 ORDER BY gd.id DESC
            """,id);
        boolean cod=!pt.isEmpty() && "COD".equals(pt.get(0).get("nha_cung_cap"));
        if(!cod && conLai.signum()>0) throw new IllegalArgumentException("Đơn QR chưa được xác nhận thanh toán đủ.");
        var body=body(dong.get("ten_khach_hang").toString(),dong.get("so_dien_thoai_khach_hang").toString(),dong.get("dia_chi_khach_hang").toString(),
                vd.get("ten_tinh").toString(),vd.get("ten_xa").toString(),((Number)vd.get("khoi_luong")).intValue(),((Number)vd.get("dai")).intValue(),
                ((Number)vd.get("rong")).intValue(),((Number)vd.get("cao")).intValue(),(BigDecimal)dong.get("tong_tien"),cod?conLai:BigDecimal.ZERO);
        body.put("client_order_code",vd.get("ma_don_shop"));
        var data=ghn.taoDon(body);
        String ma=Objects.toString(data.get("order_code"),"");
        if(!ma.matches("[a-zA-Z0-9_-]{1,50}")) throw new IllegalStateException("GHN chưa trả mã vận đơn hợp lệ. Bạn có thể thử lại với cùng mã đơn shop.");
        jdbc.update("UPDATE van_don_ghn SET ma_van_don=?,trang_thai='ready_to_pick',phi_thuc_te=?,ngay_cap_nhat=SYSDATETIME() WHERE id_hoa_don=?",ma,ghn.docPhi(data),id);
        return ma;
    }
    @Transactional
    public void dongBo(int id) {
        var ds=jdbc.queryForList("SELECT ma_van_don FROM van_don_ghn WITH (UPDLOCK,HOLDLOCK) WHERE id_hoa_don=?",id);
        if(ds.isEmpty() || ds.get(0).get("ma_van_don")==null) throw new IllegalArgumentException("Đơn chưa có mã vận đơn GHN.");
        String ma=ds.get(0).get("ma_van_don").toString();
        var data=ghn.chiTietDon(ma);
        if(!ma.equals(data.get("order_code"))) throw new IllegalStateException("GHN trả thông tin không khớp vận đơn.");
        String trangThai=Objects.toString(data.get("status"),"");
        if(!trangThai.matches("[a-z_]{1,60}")) throw new IllegalStateException("Trạng thái GHN không hợp lệ.");
        // Trạng thái hãng vận chuyển độc lập với xác nhận nhận hàng và đối soát COD.
        jdbc.update("UPDATE van_don_ghn SET trang_thai=?,ngay_cap_nhat=SYSDATETIME() WHERE id_hoa_don=?",trangThai,id);
    }
}
