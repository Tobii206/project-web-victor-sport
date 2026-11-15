package com.example.victorsport.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/** Giả lập hoàn toàn trong dự án, không tạo mã chuyển tiền ngân hàng. */
@Service
public class QrDemoService {
    private final JdbcTemplate jdbc;
    @Value("${thanhtoan.qr-demo.bat:false}") private boolean bat;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private ChuongTrucTuyenService chuong;
    public QrDemoService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public boolean dangBat() { return bat; }
    public void kiemTraCheDo() {
        if(!bat) throw new IllegalArgumentException("Phương thức thanh toán QR đang tắt.");
    }
    public void taoGiaoDich(int id,BigDecimal tien) {
        kiemTraCheDo();
        if(tien==null || tien.signum()<=0 || tien.stripTrailingZeros().scale()>0) throw new IllegalArgumentException("Tổng thanh toán không hợp lệ.");
        jdbc.update("""
            IF NOT EXISTS(SELECT 1 FROM phuong_thuc_thanh_toan WITH (UPDLOCK,HOLDLOCK) WHERE nha_cung_cap=N'QR_DEMO' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0)
            INSERT INTO phuong_thuc_thanh_toan(ten_phuong_thuc_thanh_toan,nha_cung_cap,trang_thai,xoa_mem) VALUES(N'Thanh toán QR',N'QR_DEMO',1,0)
            """);
        Integer pt=jdbc.queryForObject("SELECT TOP 1 id FROM phuong_thuc_thanh_toan WHERE nha_cung_cap=N'QR_DEMO' AND trang_thai=1 AND ISNULL(xoa_mem,0)=0 ORDER BY id",Integer.class);
        jdbc.update("""
            INSERT INTO giao_dich_thanh_toan(id_hoa_don,id_phuong_thuc_thanh_toan,so_tien,trang_thai,ma_yeu_cau,ghi_chu)
            VALUES(?,?,?,N'cho_thanh_toan',?,N'QR mô phỏng')
            """,id,pt,tien,"DEMO"+id+UUID.randomUUID().toString().replace("-",""));
    }
    public Map<String,Object> lay(int id,Integer kh,boolean phien) {
        kiemTraCheDo();
        if(kh==null && !phien) return null;
        var ds=jdbc.queryForList("""
            SELECT TOP 1 hd.id,hd.id_khach_hang,hd.ma_hoa_don,hd.trang_thai_hien_tai,gd.so_tien,gd.trang_thai,gd.ma_yeu_cau,
                DATEDIFF_BIG(MILLISECOND,gd.thoi_gian_tao,SYSDATETIME())/1000 AS da_cho
            FROM hoa_don hd JOIN giao_dich_thanh_toan gd ON gd.id_hoa_don=hd.id
            JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE hd.id=? AND ISNULL(hd.xoa_mem,0)=0 AND ISNULL(gd.xoa_mem,0)=0 AND pt.nha_cung_cap=N'QR_DEMO'
                AND (hd.id_khach_hang=? OR (?=1 AND hd.id_khach_hang IS NULL)) ORDER BY gd.id DESC
            """,id,kh==null?-1:kh,phien?1:0);
        if(ds.isEmpty()) return null;
        var gd=ds.get(0);
        themHan(gd);
        return gd;
    }
    private void themHan(Map<String,Object> gd) {
        int daCho=gd.get("da_cho") instanceof Number n?n.intValue():0;
        gd.put("het_han",daCho>=120);
        gd.put("con_lai",Math.max(0,120-daCho));
        if(daCho>=120 && "cho_thanh_toan".equals(gd.get("trang_thai"))) gd.put("trang_thai","that_bai");
    }
    @org.springframework.scheduling.annotation.Scheduled(initialDelay=1000,fixedDelay=1000)
    public void danhDauHetHan() {
        if(!bat)return;
        try {
            jdbc.update("""
                UPDATE gd SET trang_thai=N'that_bai',thoi_gian_cap_nhat=SYSDATETIME(),ghi_chu=N'Mã QR hết hạn sau 2 phút'
                FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
                WHERE pt.nha_cung_cap=N'QR_DEMO' AND ISNULL(gd.xoa_mem,0)=0 AND gd.trang_thai=N'cho_thanh_toan'
                  AND gd.thoi_gian_tao<=DATEADD(SECOND,-120,SYSDATETIME())
                """);
        } catch(org.springframework.dao.DataAccessException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).debug("Chưa cập nhật hạn QR, sẽ thử lại.");
        }
    }
    public String anhQr(String duongDan) {
        try {
            var matrix=new com.google.zxing.qrcode.QRCodeWriter().encode(
                duongDan,
                com.google.zxing.BarcodeFormat.QR_CODE,280,280);
            var anh=new java.awt.image.BufferedImage(280,280,java.awt.image.BufferedImage.TYPE_INT_RGB);
            for(int y=0;y<280;y++) for(int x=0;x<280;x++) anh.setRGB(x,y,matrix.get(x,y)?0x142d52:0xffffff);
            var out=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(anh,"png",out);
            return "data:image/png;base64,"+Base64.getEncoder().encodeToString(out.toByteArray());
        } catch(com.google.zxing.WriterException | java.io.IOException ex) { throw new IllegalStateException("Không tạo được mã QR.",ex); }
    }
    @Transactional
    public void taoLai(int id,Integer kh,boolean phien,String maCu) {
        kiemTraCheDo();
        if(kh==null && !phien) throw new IllegalArgumentException("Bạn không có quyền xử lý đơn này.");
        if(maCu==null || !maCu.matches("DEMO[0-9]+[a-f0-9]{32}")) throw new IllegalArgumentException("Mã giao dịch không hợp lệ.");
        var dons=jdbc.queryForList("""
            SELECT tong_tien_sau_giam,trang_thai_hien_tai,ngay_thanh_toan FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
            WHERE id=? AND ISNULL(xoa_mem,0)=0 AND (id_khach_hang=? OR (?=1 AND id_khach_hang IS NULL))
            """,id,kh==null?-1:kh,phien?1:0);
        if(dons.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn của bạn.");
        var don=dons.get(0);int tt=((Number)don.get("trang_thai_hien_tai")).intValue();
        if(tt<1 || tt>3 || don.get("ngay_thanh_toan")!=null) throw new IllegalArgumentException("Đơn đã thanh toán, đã gửi hàng hoặc đã hủy. Không thể tạo lại QR.");
        var ds=jdbc.queryForList("""
            SELECT gd.id,gd.so_tien,gd.trang_thai,gd.ma_yeu_cau,DATEDIFF_BIG(MILLISECOND,gd.thoi_gian_tao,SYSDATETIME())/1000 AS da_cho
            FROM giao_dich_thanh_toan gd WITH (UPDLOCK,HOLDLOCK) JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND pt.nha_cung_cap=N'QR_DEMO' AND ISNULL(gd.xoa_mem,0)=0
            """,id);
        if(ds.size()!=1) throw new IllegalArgumentException("Không tìm thấy giao dịch QR hợp lệ.");
        var gd=ds.get(0);
        if("thanh_cong".equals(gd.get("trang_thai"))) throw new IllegalArgumentException("Đơn đã thanh toán thành công.");
        // Hai lần bấm cùng mã chỉ tạo một mã mới, không làm mất hiệu lực mã vừa hiển thị.
        if(!maCu.equals(gd.get("ma_yeu_cau"))) return;
        String hienTai=gd.get("trang_thai").toString();
        if(!Set.of("that_bai","da_huy").contains(hienTai)
                && !("cho_thanh_toan".equals(hienTai) && ((Number)gd.get("da_cho")).intValue()>=120))
            throw new IllegalArgumentException("Mã QR hiện tại vẫn đang có hiệu lực.");
        BigDecimal tien=(BigDecimal)don.get("tong_tien_sau_giam");
        if(tien==null || tien.signum()<=0 || tien.stripTrailingZeros().scale()>0) throw new IllegalArgumentException("Tổng tiền không hợp lệ.");
        var daThu=jdbc.queryForList("SELECT id FROM giao_dich_thanh_toan WHERE id_hoa_don=? AND trang_thai=N'thanh_cong' AND ISNULL(xoa_mem,0)=0",id);
        if(!daThu.isEmpty()) throw new IllegalArgumentException("Đơn đã có thanh toán, không thể tạo lại QR.");
        String maMoi="DEMO"+id+UUID.randomUUID().toString().replace("-","");
        int capNhat=jdbc.update("""
            UPDATE giao_dich_thanh_toan SET trang_thai=N'cho_thanh_toan',ma_yeu_cau=?,so_tien=?,
                thoi_gian_tao=SYSDATETIME(),thoi_gian_cap_nhat=SYSDATETIME(),ghi_chu=N'Chờ thanh toán QR'
            WHERE id=? AND ma_yeu_cau=?
            """,maMoi,tien,gd.get("id"),maCu);
        if(capNhat!=1) throw new IllegalArgumentException("Giao dịch đã thay đổi. Vui lòng tải lại trang.");
        if(chuong!=null) chuong.donThayDoi(id);
    }
    @Transactional
    public String xuLy(int id,Integer kh,boolean phien,String ketQua) {
        if("thanh_cong".equals(ketQua)) throw new IllegalArgumentException("Vui lòng quét QR bằng điện thoại và xác nhận thanh toán.");
        return xuLyNoiBo(id,kh,phien,ketQua,null);
    }
    public Map<String,Object> layTheoMa(String ma) {
        kiemTraCheDo();
        if(ma==null || !ma.matches("DEMO[0-9]+[a-f0-9]{32}")) return null;
        var ds=jdbc.queryForList("""
            SELECT hd.id,hd.ma_hoa_don,hd.trang_thai_hien_tai,gd.so_tien,gd.trang_thai,
                DATEDIFF_BIG(MILLISECOND,gd.thoi_gian_tao,SYSDATETIME())/1000 AS da_cho
            FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            JOIN hoa_don hd ON hd.id=gd.id_hoa_don
            WHERE gd.ma_yeu_cau=? AND pt.nha_cung_cap=N'QR_DEMO' AND ISNULL(gd.xoa_mem,0)=0 AND ISNULL(hd.xoa_mem,0)=0
            """,ma);
        if(ds.size()!=1) return null;
        var gd=new HashMap<String,Object>(ds.get(0));themHan(gd);return gd;
    }
    @Transactional
    public String xuLyQuet(String ma,String ketQua) {
        var gd=layTheoMa(ma);
        if(gd==null) throw new IllegalArgumentException("Mã QR không hợp lệ hoặc đã hết hạn.");
        if(Boolean.TRUE.equals(gd.get("het_han")) && !"thanh_cong".equals(gd.get("trang_thai"))) return "that_bai";
        return xuLyNoiBo(((Number)gd.get("id")).intValue(),null,false,ketQua,ma);
    }
    private String xuLyNoiBo(int id,Integer kh,boolean phien,String ketQua,String maQuet) {
        kiemTraCheDo();
        if(ketQua==null || !Set.of("thanh_cong","that_bai","da_huy").contains(ketQua)) throw new IllegalArgumentException("Kết quả giao dịch không hợp lệ.");
        if(kh==null && !phien && maQuet==null) throw new IllegalArgumentException("Bạn không có quyền xử lý đơn này.");
        var dons=maQuet==null?jdbc.queryForList("""
            SELECT tong_tien_sau_giam,trang_thai_hien_tai FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
            WHERE id=? AND ISNULL(xoa_mem,0)=0 AND (id_khach_hang=? OR (?=1 AND id_khach_hang IS NULL))
            """,id,kh==null?-1:kh,phien?1:0):jdbc.queryForList("""
            SELECT tong_tien_sau_giam,trang_thai_hien_tai FROM hoa_don WITH (UPDLOCK,HOLDLOCK)
            WHERE id=? AND ISNULL(xoa_mem,0)=0 AND EXISTS (
                SELECT 1 FROM giao_dich_thanh_toan gd JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
                WHERE gd.id_hoa_don=hoa_don.id AND gd.ma_yeu_cau=? AND pt.nha_cung_cap=N'QR_DEMO'
                  AND ISNULL(gd.xoa_mem,0)=0 AND gd.thoi_gian_tao>DATEADD(SECOND,-120,SYSDATETIME()))
            """,id,maQuet);
        if(dons.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn của bạn.");
        var don=dons.get(0);
        if(((Number)don.get("trang_thai_hien_tai")).intValue()==6) throw new IllegalArgumentException("Đơn hàng đã hủy.");
        var ds=jdbc.queryForList("""
            SELECT gd.id,gd.so_tien,gd.trang_thai,DATEDIFF_BIG(MILLISECOND,gd.thoi_gian_tao,SYSDATETIME())/1000 AS da_cho
            FROM giao_dich_thanh_toan gd WITH (UPDLOCK,HOLDLOCK) JOIN phuong_thuc_thanh_toan pt ON pt.id=gd.id_phuong_thuc_thanh_toan
            WHERE gd.id_hoa_don=? AND pt.nha_cung_cap=N'QR_DEMO' AND ISNULL(gd.xoa_mem,0)=0
            """,id);
        if(ds.size()!=1) throw new IllegalArgumentException("Không tìm thấy giao dịch QR hợp lệ.");
        var gd=ds.get(0);String hienTai=gd.get("trang_thai").toString();
        if(!"cho_thanh_toan".equals(hienTai)) return hienTai;
        if(((Number)gd.get("da_cho")).intValue()>=120) return "that_bai";
        if(((BigDecimal)gd.get("so_tien")).compareTo((BigDecimal)don.get("tong_tien_sau_giam"))!=0)
            throw new IllegalArgumentException("Tổng tiền đã thay đổi. Vui lòng kiểm tra lại đơn hàng.");
        jdbc.update("UPDATE giao_dich_thanh_toan SET trang_thai=?,thoi_gian_cap_nhat=SYSDATETIME(),ghi_chu=? WHERE id=?",
            ketQua,"thanh_cong".equals(ketQua)?"Đã thanh toán QR":"Thanh toán QR thất bại",gd.get("id"));
        if("thanh_cong".equals(ketQua)) {
            jdbc.update("""
                UPDATE hoa_don SET ngay_thanh_toan=COALESCE(ngay_thanh_toan,SYSDATETIME()),ngay_cap_nhat=SYSDATETIME(),
                    ghi_chu=CASE WHEN ghi_chu IN (N'QR DEMO - giả lập, không thu tiền thật',N'QR mô phỏng',N'Chờ thanh toán online')
                        THEN N'Đã thanh toán QR' ELSE ghi_chu END WHERE id=?
                """,id);
            jdbc.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,loai_nguoi_thuc_hien) VALUES(?,?,N'Đã thanh toán QR','he_thong')",
                id,don.get("trang_thai_hien_tai"));
        }
        if(chuong!=null) chuong.donThayDoi(id);
        return ketQua;
    }
}
