package com.example.victorsport.Service;

import java.math.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TraHangService {
    public record Quyen(Integer khach,boolean vangLai,Integer nhanVien) {}
    public static final List<String> LY_DO=List.of("Sai size","Sai màu","Giao nhầm sản phẩm","Sản phẩm bị lỗi","Không đúng mô tả","Không còn nhu cầu");
    public static final List<String> NGAN_HANG=List.of("Vietcombank","VietinBank","BIDV","Agribank","Techcombank","MB Bank","ACB","VPBank","TPBank","Sacombank","SHB","MSB","HDBank","OCB","NCB","Eximbank","Nam A Bank");
    public static final Map<String,String> TRANG_THAI;
    static {
        var m=new LinkedHashMap<String,String>();
        m.put("CHO_XAC_NHAN","Chờ xác nhận");m.put("DA_XAC_NHAN","Đã duyệt yêu cầu");m.put("DANG_GUI_HANG","Đang gửi hàng về");
        m.put("DA_NHAN_HANG","Cửa hàng đã nhận hàng");m.put("DANG_KIEM_TRA","Đang kiểm tra");m.put("DA_DUYET_HOAN_TRA","Đã duyệt hoàn tiền");
        m.put("DA_HOAN_TIEN","Đã hoàn tiền");m.put("TU_CHOI","Đã từ chối");m.put("DA_HUY","Đã hủy yêu cầu");TRANG_THAI=Collections.unmodifiableMap(m);
    }
    private final JdbcTemplate jdbc;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private ChuongTrucTuyenService chuong;
    public TraHangService(JdbcTemplate jdbc){this.jdbc=jdbc;}
    private void admin(Quyen q){
        if(q==null || q.nhanVien()==null || jdbc.queryForList("""
            SELECT nv.id FROM nhan_vien nv JOIN quyen_han q ON q.id=nv.id_quyen_han
            WHERE nv.id=? AND nv.trang_thai=1 AND ISNULL(nv.xoa_mem,0)=0 AND q.trang_thai=1
              AND ISNULL(q.xoa_mem,0)=0 AND (q.ten_quyen_han=N'Quản trị viên' OR EXISTS (
                    SELECT 1 FROM quyen_han_chuc_nang qcn JOIN chuc_nang cn ON cn.id=qcn.id_chuc_nang
                    WHERE qcn.id_quyen_han=q.id AND ISNULL(qcn.xoa_mem,0)=0
                      AND cn.ma_chuc_nang='ORDER_WRITE' AND ISNULL(cn.xoa_mem,0)=0 AND ISNULL(cn.trang_thai,1)=1))
            """,q.nhanVien()).isEmpty())throw new IllegalArgumentException("Bạn không có quyền xử lý trả hàng và hoàn tiền.");
    }
    private Map<String,Object> don(int id,Quyen q,boolean khoa){
        if(id<=0 || q==null)throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
        String loc="";var args=new ArrayList<Object>();args.add(id);
        if(q.nhanVien()!=null)admin(q);
        else if(q.khach()!=null){
            loc=" AND hd.id_khach_hang=? AND EXISTS(SELECT 1 FROM khach_hang kh WHERE kh.id=hd.id_khach_hang AND ISNULL(kh.xoa_mem,0)=0 AND kh.trang_thai=1)";
            args.add(q.khach());
        }else if(q.vangLai())loc=" AND hd.id_khach_hang IS NULL AND hd.loai_don IN (1,2)";
        else throw new IllegalArgumentException("Không tìm thấy đơn hàng của bạn.");
        var ds=jdbc.queryForList("""
            SELECT hd.*,thu.da_thu,nhan.ngay_nhan,
                CASE WHEN nhan.ngay_nhan>=DATEADD(DAY,-15,SYSDATETIME()) AND nhan.ngay_nhan<=SYSDATETIME() THEN 1 ELSE 0 END AS con_han
            FROM hoa_don hd
            """+(khoa?" WITH (UPDLOCK,HOLDLOCK) ":"")+"""
            OUTER APPLY(SELECT ISNULL(SUM(gd.so_tien),0) AS da_thu FROM giao_dich_thanh_toan gd
                WHERE gd.id_hoa_don=hd.id AND gd.trang_thai=N'thanh_cong' AND ISNULL(gd.xoa_mem,0)=0) thu
            OUTER APPLY(SELECT COALESCE((SELECT MIN(ls.thoi_gian) FROM lich_su_hoa_don ls WHERE ls.id_hoa_don=hd.id
                AND ls.trang_thai=5 AND ISNULL(ls.xoa_mem,0)=0),CASE WHEN hd.loai_don=0 THEN hd.ngay_thanh_toan END) AS ngay_nhan) nhan
            WHERE hd.id=? AND ISNULL(hd.xoa_mem,0)=0
            """+loc,args.toArray());
        if(ds.isEmpty())throw new IllegalArgumentException("Không tìm thấy đơn hàng của bạn.");
        return new HashMap<>(ds.get(0));
    }
    private List<Map<String,Object>> dongDon(int id){
        return jdbc.queryForList("""
            SELECT ct.*,sp.ten_san_pham,sp.ma_san_pham,ms.ten_mau_sac,kt.ten_kich_thuoc,
                ISNULL((SELECT SUM(rt.so_tien_hoan) FROM chi_tiet_tra_hang rt JOIN yeu_cau_tra_hang ry ON ry.id=rt.id_yeu_cau
                    WHERE rt.id_hoa_don_chi_tiet=ct.id AND ry.trang_thai='DA_HOAN_TIEN' AND rt.duoc_duyet=1),0) AS tien_da_hoan,
                CASE mau.doi_tuong WHEN 'NAM' THEN N'Nam' WHEN 'NU' THEN N'Nữ' ELSE N'Unisex' END AS gioi_tinh
            FROM hoa_don_chi_tiet ct JOIN chi_tiet_san_pham mau ON mau.id=ct.id_chi_tiet_san_pham
            JOIN san_pham sp ON sp.id=mau.id_san_pham LEFT JOIN mau_sac ms ON ms.id=mau.id_mau_sac
            LEFT JOIN kich_thuoc kt ON kt.id=mau.id_kich_thuoc
            WHERE ct.id_hoa_don=? AND ISNULL(ct.xoa_mem,0)=0 ORDER BY ct.id
            """,id);
    }
    private static int n(Object v){return v instanceof Number x?x.intValue():0;}
    private static BigDecimal tien(Object v){return v instanceof Number?new BigDecimal(v.toString()):BigDecimal.ZERO;}
    /** Phân bổ theo giá dòng đã chốt; làm tròn lũy kế để trả từng phần vẫn khớp tổng tiền thực trả. */
    public static Map<Integer,BigDecimal> phanBo(List<Map<String,Object>> dong,BigDecimal thucTra){
        BigDecimal tong=BigDecimal.ZERO;
        for(var d:dong){if(n(d.get("so_luong"))<=0 || tien(d.get("don_gia")).signum()<0)throw new IllegalArgumentException("Dữ liệu số lượng/giá đơn hàng không hợp lệ.");tong=tong.add(tien(d.get("don_gia")).multiply(BigDecimal.valueOf(n(d.get("so_luong")))));}
        if(tong.signum()<=0 || thucTra==null || thucTra.signum()<0 || thucTra.compareTo(tong)>0)throw new IllegalArgumentException("Không tính được tiền hàng thực trả.");
        thucTra=thucTra.setScale(0,RoundingMode.DOWN);
        var result=new LinkedHashMap<Integer,BigDecimal>();BigDecimal luyKe=BigDecimal.ZERO,truoc=BigDecimal.ZERO;
        for(var d:dong){luyKe=luyKe.add(tien(d.get("don_gia")).multiply(BigDecimal.valueOf(n(d.get("so_luong")))));var moi=luyKe.multiply(thucTra).divide(tong,0,RoundingMode.DOWN);result.put(n(d.get("id")),moi.subtract(truoc));truoc=moi;}
        return result;
    }
    public static BigDecimal tienTra(BigDecimal nganSach,int daTra,int traMoi,int daMua){
        if(daMua<=0 || daTra<0 || traMoi<=0 || (long)daTra+traMoi>daMua)throw new IllegalArgumentException("Số lượng trả vượt số lượng còn được trả.");
        var tong=BigDecimal.valueOf(daMua);
        return nganSach.multiply(BigDecimal.valueOf((long)daTra+traMoi)).divide(tong,0,RoundingMode.DOWN)
            .subtract(nganSach.multiply(BigDecimal.valueOf(daTra)).divide(tong,0,RoundingMode.DOWN));
    }
    private BigDecimal tienHoanDong(Map<String,Object> d,BigDecimal nganSach,int sl){
        int daTra=n(d.get("so_luong_da_tra")),daMua=n(d.get("so_luong"));
        BigDecimal phanMoi=tienTra(nganSach,daTra,sl,daMua);
        if(!d.containsKey("tien_da_hoan"))return phanMoi;
        BigDecimal mucLuyKe=nganSach.multiply(BigDecimal.valueOf((long)daTra+sl)).divide(BigDecimal.valueOf(daMua),0,RoundingMode.DOWN);
        return mucLuyKe.subtract(tien(d.get("tien_da_hoan"))).setScale(0,RoundingMode.DOWN).max(BigDecimal.ZERO);
    }
    private List<Map<String,Object>> tienHoanVnd(int yc,Map<String,Object> y,List<Map<String,Object>> dong,boolean luu){
        var ketQua=new ArrayList<Map<String,Object>>();
        boolean dangXuLy=Set.of("CHO_XAC_NHAN","DA_XAC_NHAN","DANG_GUI_HANG","DA_NHAN_HANG","DANG_KIEM_TRA","DA_DUYET_HOAN_TRA").contains(String.valueOf(y.get("trang_thai")));
        dangXuLy=dangXuLy&&(tien(y.get("so_tien_hoan")).stripTrailingZeros().scale()>0||dong.stream().anyMatch(d->tien(d.get("so_tien_hoan")).stripTrailingZeros().scale()>0));
        BigDecimal luyKe=BigDecimal.ZERO,truoc=BigDecimal.ZERO;
        for(var cu:dong){var d=new HashMap<String,Object>(cu);
            if(dangXuLy){BigDecimal goc=tien(cu.get("so_tien_hoan")),moi;
                if(duocDuyet(cu)){luyKe=luyKe.add(goc);var moc=luyKe.setScale(0,RoundingMode.DOWN);moi=moc.subtract(truoc);truoc=moc;}
                else moi=goc.setScale(0,RoundingMode.DOWN);
                d.put("so_tien_hoan",moi);
                if(luu&&moi.compareTo(goc)!=0)jdbc.update("UPDATE chi_tiet_tra_hang SET so_tien_hoan=? WHERE id=? AND id_yeu_cau=?",moi,d.get("id"),yc);
            }ketQua.add(d);
        }
        if(dangXuLy){if(luu&&truoc.compareTo(tien(y.get("so_tien_hoan")))!=0)jdbc.update("UPDATE yeu_cau_tra_hang SET so_tien_hoan=?,ngay_cap_nhat=SYSDATETIME() WHERE id=?",truoc,yc);y.put("so_tien_hoan",truoc);}
        return ketQua;
    }
    private BigDecimal nganSach(Map<String,Object> hd){
        return tien(hd.get("tong_tien_sau_giam")).subtract(tien(hd.get("phi_van_chuyen")));
    }
    private void duDieuKien(Map<String,Object> hd){
        if(n(hd.get("trang_thai_hien_tai"))!=5)throw new IllegalArgumentException("Chỉ trả hàng khi đơn đã nhận hoặc hoàn tất.");
        if(tien(hd.get("da_thu")).compareTo(tien(hd.get("tong_tien_sau_giam")))<0)throw new IllegalArgumentException("Đơn hàng chưa được thanh toán đủ.");
        if(n(hd.get("con_han"))!=1)throw new IllegalArgumentException("Chỉ tạo yêu cầu trong 15 ngày kể từ khi nhận hàng.");
    }
    private List<Map<String,Object>> danhSachDon(int id){
        var ds=jdbc.queryForList("SELECT * FROM yeu_cau_tra_hang WHERE id_hoa_don=? ORDER BY id DESC",id);for(var d:ds)d.put("ten_trang_thai",TRANG_THAI.get(d.get("trang_thai")));return ds;
    }
    public Map<String,Object> form(int id,Quyen q){
        var hd=don(id,q,false);var dong=dongDon(id);var ds=danhSachDon(id);
        String loi="";try{duDieuKien(hd);}catch(IllegalArgumentException ex){loi=ex.getMessage();}
        if(ds.stream().anyMatch(d->!Set.of("TU_CHOI","DA_HUY","DA_HOAN_TIEN").contains(d.get("trang_thai"))))loi="Đơn đang có yêu cầu trả hàng được xử lý.";
        var budgets=phanBo(dong,nganSach(hd));boolean conHang=false;
        for(var d:dong){int con=n(d.get("so_luong"))-n(d.get("so_luong_da_tra"));d.put("con_tra",con);d.put("gia_tri_hoan",con>0?tienHoanDong(d,budgets.get(n(d.get("id"))),con):BigDecimal.ZERO);conHang|=con>0;}
        if(!conHang)loi="Các sản phẩm trong đơn đã được trả hết.";
        return new LinkedHashMap<>(Map.of("don",hd,"dong",dong,"cacYeuCau",ds,"loiDieuKien",loi,"lyDo",LY_DO,"nganHang",NGAN_HANG));
    }
    public static Map<Integer,Integer> soLuong(List<Integer> ids,Map<String,String> form,String prefix){
        if(ids==null || ids.isEmpty() || ids.size()>50)throw new IllegalArgumentException("Chọn từ 1 đến 50 dòng sản phẩm cần trả.");
        var m=new LinkedHashMap<Integer,Integer>();
        for(Integer id:ids){if(id==null || id<=0 || m.containsKey(id))throw new IllegalArgumentException("Dòng sản phẩm không hợp lệ hoặc bị trùng.");m.put(id,soNguyen(form.get(prefix+id),1,Integer.MAX_VALUE));}return m;
    }
    private static int soNguyen(String s,int min,int max){
        try{if(s==null || !s.matches("[0-9]{1,10}"))throw new NumberFormatException();long n=Long.parseLong(s);if(n<min||n>max)throw new NumberFormatException();return (int)n;}
        catch(NumberFormatException ex){throw new IllegalArgumentException("Số lượng phải là số nguyên hợp lệ, từ "+min+" đến "+max+".");}
    }
    private static String chuoi(String v,int min,int max,String nhan){v=v==null?"":v.trim();if(v.length()<min||v.length()>max)throw new IllegalArgumentException(nhan+" phải có từ "+min+" đến "+max+" ký tự.");return v;}
    private List<byte[]> anhHopLe(List<MultipartFile> files){
        var list=files==null?List.<MultipartFile>of():files.stream().filter(f->f!=null&&!f.isEmpty()).toList();
        if(list.isEmpty()||list.size()>5)throw new IllegalArgumentException("Vui lòng chọn 1–5 ảnh JPG/PNG làm minh chứng.");
        var bytes=new ArrayList<byte[]>();
        for(var f:list){
            if(f.getSize()>5*1024*1024)throw new IllegalArgumentException("Mỗi ảnh tối đa 5 MB.");
            try(var in=javax.imageio.ImageIO.createImageInputStream(f.getInputStream())){
                var readers=javax.imageio.ImageIO.getImageReaders(in);if(!readers.hasNext())throw new IllegalArgumentException("Ảnh minh chứng không hợp lệ.");
                var reader=readers.next();
                try{reader.setInput(in);String fmt=reader.getFormatName().toLowerCase(Locale.ROOT);int w=reader.getWidth(0),h=reader.getHeight(0);
                    if(!Set.of("png","jpeg","jpg").contains(fmt)||w<=0||h<=0||(long)w*h>16000000)throw new IllegalArgumentException("Ảnh phải là JPG/PNG, tối đa 16 triệu điểm ảnh.");
                    var image=reader.read(0);var out=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",out);
                    if(out.size()>5*1024*1024)throw new IllegalArgumentException("Ảnh sau kiểm tra vượt 5 MB. Vui lòng chọn ảnh nhỏ hơn.");bytes.add(out.toByteArray());
                }finally{reader.dispose();}
            }catch(java.io.IOException ex){throw new IllegalArgumentException("Chưa đọc được ảnh minh chứng.");}
        }return bytes;
    }
    @Transactional
    public int tao(int id,Quyen q,Map<Integer,Integer> chon,Map<String,String> f,List<MultipartFile> files){
        var hd=don(id,q,true);String key=chuoi(f.get("maTao"),36,36,"Mã tạo yêu cầu");
        try{UUID.fromString(key);}catch(IllegalArgumentException ex){throw new IllegalArgumentException("Mã tạo yêu cầu không hợp lệ.");}
        var cu=jdbc.queryForList("SELECT id FROM yeu_cau_tra_hang WHERE id_hoa_don=? AND ma_tao=?",id,key);if(!cu.isEmpty())return n(cu.get(0).get("id"));
        duDieuKien(hd);
        if(!jdbc.queryForList("SELECT id FROM yeu_cau_tra_hang WHERE id_hoa_don=? AND trang_thai NOT IN ('TU_CHOI','DA_HUY','DA_HOAN_TIEN')",id).isEmpty())throw new IllegalArgumentException("Đơn đang có yêu cầu trả hàng được xử lý.");
        if(chon==null||chon.isEmpty()||chon.size()>50)throw new IllegalArgumentException("Chọn ít nhất một sản phẩm cần trả.");
        String lyDo=f.get("lyDo"),cach=f.get("hinhThuc"),method=f.get("phuongThuc");
        if(!LY_DO.contains(lyDo)||!Set.of("GUI_HANG","TAI_CUA_HANG").contains(Objects.toString(cach,""))||!Set.of("CASH","BANK").contains(Objects.toString(method,"")))throw new IllegalArgumentException("Lý do, hình thức trả hoặc phương thức hoàn tiền không hợp lệ.");
        if(n(hd.get("loai_don"))==0&&!"TAI_CUA_HANG".equals(cach))throw new IllegalArgumentException("Đơn mua tại quầy cần mang hàng đến cửa hàng để trả.");
        String ghi=chuoi(f.get("ghiChu"),0,1000,"Ghi chú"),bank=null,stk=null,ten=null;
        if("BANK".equals(method)){
            bank=f.get("nganHang");stk=chuoi(f.get("soTaiKhoan"),6,34,"Số tài khoản");ten=chuoi(f.get("chuTaiKhoan"),2,100,"Chủ tài khoản").replaceAll("\\s+"," ").toUpperCase(Locale.ROOT);
            if(!NGAN_HANG.contains(bank)||!stk.matches("[0-9]{6,34}")||!ten.matches("[\\p{L} ]{2,100}"))throw new IllegalArgumentException("Ngân hàng, số tài khoản hoặc tên chủ tài khoản không hợp lệ.");
        }
        var dong=dongDon(id);var budgets=phanBo(dong,nganSach(hd));var amounts=new LinkedHashMap<Integer,BigDecimal>();BigDecimal tong=BigDecimal.ZERO;
        for(var d:dong){int row=n(d.get("id"));if(!chon.containsKey(row))continue;Integer sl=chon.get(row);if(sl==null)throw new IllegalArgumentException("Số lượng trả không hợp lệ.");
            BigDecimal amount=tienHoanDong(d,budgets.get(row),sl);amounts.put(row,amount);tong=tong.add(amount);}
        if(amounts.size()!=chon.size())throw new IllegalArgumentException("Sản phẩm trả không thuộc đơn hàng này.");
        if(tong.add(tien(hd.get("tong_tien_da_hoan"))).compareTo(nganSach(hd))>0)throw new IllegalArgumentException("Số tiền vượt phần tiền hàng còn được hoàn.");
        var anh=anhHopLe(files);
        Integer yc=jdbc.queryForObject("""
            INSERT INTO yeu_cau_tra_hang(ma_tao,id_hoa_don,ly_do,ghi_chu,hinh_thuc_tra,phuong_thuc_hoan,ngan_hang,so_tai_khoan,chu_tai_khoan,so_tien_hoan,nguoi_tao_khach,nguoi_tao_nv)
            OUTPUT INSERTED.id VALUES(?,?,?,?,?,?,?,?,?,?,?,?)
            """,Integer.class,key,id,lyDo,ghi,cach,method,bank,stk,ten,tong,q.khach(),q.nhanVien());
        if(yc==null)throw new IllegalStateException("Chưa tạo được yêu cầu trả hàng.");
        for(var e:amounts.entrySet())jdbc.update("INSERT INTO chi_tiet_tra_hang(id_yeu_cau,id_hoa_don_chi_tiet,so_luong,so_tien_hoan) VALUES(?,?,?,?)",yc,e.getKey(),chon.get(e.getKey()),e.getValue());
        for(var a:anh)jdbc.update("INSERT INTO anh_minh_chung_tra_hang(id_yeu_cau,du_lieu) VALUES(?,?)",yc,a);
        lichSu(yc,"CHO_XAC_NHAN","Đã gửi yêu cầu trả hàng và hoàn tiền.",q);bao(id);return yc;
    }
    private int idDon(int yc){var ds=jdbc.queryForList("SELECT id_hoa_don FROM yeu_cau_tra_hang WHERE id=?",yc);if(ds.isEmpty())throw new IllegalArgumentException("Không tìm thấy yêu cầu.");return n(ds.get(0).get("id_hoa_don"));}
    public int idHoaDon(int yc){return idDon(yc);}
    public Map<String,Object> chiTiet(int yc,Quyen q){
        int id=idDon(yc);var hd=don(id,q,false);var y=new HashMap<>(jdbc.queryForMap("SELECT * FROM yeu_cau_tra_hang WHERE id=?",yc));y.put("ten_trang_thai",TRANG_THAI.get(y.get("trang_thai")));
        var m=new LinkedHashMap<String,Object>();m.put("maPhieuHoan",maPhieuHoan(yc));m.put("yeuCau",y);m.put("don",hd);m.put("dongTra",tienHoanVnd(yc,y,dongTra(yc),false));m.put("anh",jdbc.queryForList("SELECT id FROM anh_minh_chung_tra_hang WHERE id_yeu_cau=? ORDER BY id",yc));
        m.put("lichSuTra",jdbc.queryForList("SELECT * FROM lich_su_tra_hang WHERE id_yeu_cau=? ORDER BY id",yc));m.put("hoanTien",jdbc.queryForList("SELECT * FROM giao_dich_hoan_tien WHERE id_yeu_cau=?",yc));return m;
    }
    private List<Map<String,Object>> dongTra(int yc){return jdbc.queryForList("""
        SELECT ct.*,hdct.id_chi_tiet_san_pham,hdct.so_luong AS da_mua,hdct.so_luong_da_tra,sp.ten_san_pham,ms.ten_mau_sac,kt.ten_kich_thuoc,
            CASE mau.doi_tuong WHEN 'NAM' THEN N'Nam' WHEN 'NU' THEN N'Nữ' ELSE N'Unisex' END AS gioi_tinh
        FROM chi_tiet_tra_hang ct JOIN hoa_don_chi_tiet hdct ON hdct.id=ct.id_hoa_don_chi_tiet
        JOIN chi_tiet_san_pham mau ON mau.id=hdct.id_chi_tiet_san_pham JOIN san_pham sp ON sp.id=mau.id_san_pham
        LEFT JOIN mau_sac ms ON ms.id=mau.id_mau_sac LEFT JOIN kich_thuoc kt ON kt.id=mau.id_kich_thuoc
        WHERE ct.id_yeu_cau=? ORDER BY hdct.id_chi_tiet_san_pham,ct.id
        """,yc);}
    public byte[] anh(int yc,int anh,Quyen q){don(idDon(yc),q,false);var ds=jdbc.queryForList("SELECT du_lieu FROM anh_minh_chung_tra_hang WHERE id=? AND id_yeu_cau=?",anh,yc);if(ds.isEmpty())throw new IllegalArgumentException("Không tìm thấy ảnh.");return (byte[])ds.get(0).get("du_lieu");}
    private Map<String,Object> khoa(int yc,Quyen q){don(idDon(yc),q,true);return new HashMap<>(jdbc.queryForMap("SELECT * FROM yeu_cau_tra_hang WITH (UPDLOCK,HOLDLOCK) WHERE id=?",yc));}
    private void chuyen(int yc,String tu,String den,String note,Quyen q){
        if(jdbc.update("UPDATE yeu_cau_tra_hang SET trang_thai=?,ngay_cap_nhat=SYSDATETIME() WHERE id=? AND trang_thai=?",den,yc,tu)!=1)throw new IllegalArgumentException("Trạng thái đã thay đổi. Vui lòng tải lại trang.");
        lichSu(yc,den,note,q);bao(idDon(yc));
    }
    @Transactional public void khachXuLy(int yc,Quyen q,String thaoTac,String hang,String ma){
        var y=khoa(yc,q);String tt=y.get("trang_thai").toString();
        if("huy".equals(thaoTac)){
            if(!Set.of("CHO_XAC_NHAN","DA_XAC_NHAN").contains(tt))throw new IllegalArgumentException("Chỉ hủy yêu cầu trước khi gửi hoặc bàn giao hàng.");
            chuyen(yc,tt,"DA_HUY","Khách hàng hủy yêu cầu trả hàng.",q);
        }else if("gui-hang".equals(thaoTac)){
            if(!"DA_XAC_NHAN".equals(tt)||!"GUI_HANG".equals(y.get("hinh_thuc_tra")))throw new IllegalArgumentException("Yêu cầu phải được duyệt và chọn gửi hàng về.");
            hang=chuoi(hang,2,100,"Hãng vận chuyển");ma=chuoi(ma,3,100,"Mã vận đơn");if(!ma.matches("[A-Za-z0-9_-]{3,100}"))throw new IllegalArgumentException("Mã vận đơn không hợp lệ.");
            jdbc.update("UPDATE yeu_cau_tra_hang SET hang_van_chuyen=?,ma_van_don=? WHERE id=?",hang,ma,yc);
            chuyen(yc,tt,"DANG_GUI_HANG","Khách đã gửi hàng về: "+hang+" · "+ma,q);
        }else throw new IllegalArgumentException("Thao tác không hợp lệ.");
    }
    @Transactional public void quanTri(int yc,Quyen q,String action,Map<String,String> f){
        admin(q);var y=khoa(yc,q);String tt=y.get("trang_thai").toString();String note=chuoi(f.get("ghiChu"),0,1000,"Ghi chú xử lý");
        if("hoan-tien".equals(action)&&"DA_HOAN_TIEN".equals(tt))return;
        tienHoanVnd(yc,y,dongTra(yc),true);
        String den;
        switch(action){
            case "duyet" -> {can(tt,"CHO_XAC_NHAN");note=duyetDong(yc,f,false,note);den="DA_XAC_NHAN";}
            case "tu-choi" -> {if(!Set.of("CHO_XAC_NHAN","DANG_KIEM_TRA").contains(tt))throw new IllegalArgumentException("Không thể từ chối ở bước hiện tại.");note=chuoi(note,10,1000,"Lý do từ chối");den="TU_CHOI";}
            case "nhan-hang" -> {if(!"DANG_GUI_HANG".equals(tt)&&!("DA_XAC_NHAN".equals(tt)&&"TAI_CUA_HANG".equals(y.get("hinh_thuc_tra"))))throw new IllegalArgumentException("Chỉ nhận hàng đã gửi về hoặc khách mang tới cửa hàng sau khi được duyệt.");den="DA_NHAN_HANG";}
            case "kiem-tra" -> {can(tt,"DA_NHAN_HANG");den="DANG_KIEM_TRA";}
            case "duyet-hoan" -> {
                can(tt,"DANG_KIEM_TRA");note=chuoi(note,10,700,"Kết quả kiểm tra");note=duyetDong(yc,f,true,note);den="DA_DUYET_HOAN_TRA";
            }
            case "hoan-tien" -> {can(tt,"DA_DUYET_HOAN_TRA");hoan(yc,y,q,f);den="DA_HOAN_TIEN";note="Đã ghi nhận hoàn "+y.get("so_tien_hoan")+" đ bằng "+("BANK".equals(y.get("phuong_thuc_hoan"))?"chuyển khoản":"tiền mặt")+".";}
            default -> throw new IllegalArgumentException("Thao tác không hợp lệ.");
        }
        chuyen(yc,tt,den,note.isBlank()?TRANG_THAI.get(den):note,q);
    }
    private boolean duocDuyet(Map<String,Object> d){Object v=d.get("duoc_duyet");return v==null||Boolean.TRUE.equals(v)||v instanceof Number num&&num.intValue()==1;}
    private String duyetDong(int yc,Map<String,String> f,boolean kiemTra,String note){
        var rows=dongTra(yc);var ids=new HashSet<Integer>();var chon=new LinkedHashSet<Integer>();var nhapKho=new HashMap<Integer,Integer>();var hangLoi=new HashMap<Integer,Integer>();
        for(var d:rows)ids.add(n(d.get("id")));
        for(var e:f.entrySet())if(e.getKey().startsWith("duyet_")){
            int id=soNguyen(e.getKey().substring(6),1,Integer.MAX_VALUE);
            if(!ids.contains(id)||!"1".equals(e.getValue()))throw new IllegalArgumentException("Sản phẩm được chọn duyệt không hợp lệ.");chon.add(id);
        }
        if(chon.isEmpty())throw new IllegalArgumentException("Tick chọn ít nhất một sản phẩm được duyệt trả. Nếu không chấp nhận sản phẩm nào, hãy dùng Từ chối.");
        BigDecimal tong=BigDecimal.ZERO;var duyet=new ArrayList<String>();var bo=new ArrayList<String>();int stt=0;
        for(var d:rows){stt++;int id=n(d.get("id"));boolean eligible=!kiemTra||duocDuyet(d);
            if(chon.contains(id)){
                if(!eligible)throw new IllegalArgumentException("Không thể duyệt lại sản phẩm đã bị từ chối ở bước tiếp nhận.");
                tong=tong.add(tien(d.get("so_tien_hoan")));duyet.add(String.valueOf(stt));
                if(kiemTra){int sl=n(d.get("so_luong"));int dat=soNguyen(f.get("nhap_"+id),0,sl),loi=soNguyen(f.get("loi_"+id),0,sl);
                    if((long)dat+loi!=sl)throw new IllegalArgumentException("Tổng số lượng đạt và hàng lỗi phải bằng số lượng trả của từng sản phẩm.");
                    nhapKho.put(id,dat);hangLoi.put(id,loi);
                }
            }else if(eligible)bo.add(String.valueOf(stt));
        }
        note=chuoi(note,bo.isEmpty()?0:10,700,bo.isEmpty()?"Ghi chú xử lý":"Lý do không duyệt các sản phẩm bỏ chọn");
        for(var d:rows){int id=n(d.get("id"));boolean ok=chon.contains(id);
            jdbc.update("UPDATE chi_tiet_tra_hang SET duoc_duyet=? WHERE id=? AND id_yeu_cau=?",ok?1:0,id,yc);
            if(kiemTra&&ok){int dat=nhapKho.get(id);jdbc.update("UPDATE chi_tiet_tra_hang SET so_luong_nhap=?,so_luong_loi=? WHERE id=? AND id_yeu_cau=?",dat,hangLoi.get(id),id,yc);}
            else jdbc.update("UPDATE chi_tiet_tra_hang SET so_luong_nhap=0,so_luong_loi=0 WHERE id=? AND id_yeu_cau=?",id,yc);
        }
        jdbc.update("UPDATE yeu_cau_tra_hang SET so_tien_hoan=?,ngay_cap_nhat=SYSDATETIME() WHERE id=?",tong,yc);
        return "Duyệt sản phẩm STT: "+String.join(", ",duyet)+"."+(bo.isEmpty()?"":" Không duyệt STT: "+String.join(", ",bo)+".")+(note.isBlank()?"":" "+note);
    }
    private static void can(String tt,String expected){if(!expected.equals(tt))throw new IllegalArgumentException("Không được bỏ qua bước xử lý. Vui lòng tải lại trang.");}
    private String maPhieuHoan(int yc){return "PC-TH"+String.format(Locale.ROOT,"%06d",yc);}
    private void hoan(int yc,Map<String,Object> y,Quyen q,Map<String,String> f){
        BigDecimal amount;try{amount=new BigDecimal(Objects.toString(f.get("soTien"),""));}catch(NumberFormatException e){throw new IllegalArgumentException("Nhập số tiền đã hoàn hợp lệ.");}
        if(amount.signum()<0||amount.stripTrailingZeros().scale()>0||amount.compareTo(tien(y.get("so_tien_hoan")))!=0)throw new IllegalArgumentException("Số tiền đã hoàn phải đúng số tiền yêu cầu được duyệt.");
        int id=n(y.get("id_hoa_don"));var hd=don(id,q,true);BigDecimal daHoan=tien(hd.get("tong_tien_da_hoan"));
        if(n(hd.get("trang_thai_hien_tai"))!=5 || tien(hd.get("da_thu")).compareTo(tien(hd.get("tong_tien_sau_giam")))<0)
            throw new IllegalArgumentException("Chỉ hoàn tiền cho đơn đã nhận và đã thanh toán đủ.");
        if(daHoan.add(amount).compareTo(nganSach(hd))>0||daHoan.add(amount).compareTo(tien(hd.get("da_thu")))>0)throw new IllegalArgumentException("Số tiền hoàn vượt tiền hàng thực thu còn lại.");
        String ma=chuoi(f.get("maGiaoDich"),0,100,"Mã phiếu chi / giao dịch hoàn tiền");if(ma.isBlank())ma=maPhieuHoan(yc);
        if(!ma.matches("[A-Za-z0-9_./-]{3,100}"))throw new IllegalArgumentException("Mã giao dịch hoàn tiền không hợp lệ.");
        if(!jdbc.queryForList("SELECT id FROM giao_dich_hoan_tien WHERE id_yeu_cau=? OR ma_giao_dich=?",yc,ma).isEmpty())throw new IllegalArgumentException("Yêu cầu hoặc mã giao dịch này đã được ghi nhận hoàn tiền.");
        var cacDong=dongTra(yc);var dongDuyet=cacDong.stream().filter(this::duocDuyet).toList();
        if(dongDuyet.isEmpty()||dongDuyet.stream().map(d->tien(d.get("so_tien_hoan"))).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(amount)!=0)
            throw new IllegalArgumentException("Tiền hoàn không khớp các sản phẩm được duyệt.");
        for(var d:dongDuyet){
            int sl=n(d.get("so_luong")),dat=n(d.get("so_luong_nhap")),loi=n(d.get("so_luong_loi"));
            if(dat<0||loi<0||(long)dat+loi!=sl)throw new IllegalArgumentException("Chưa kiểm tra đủ số lượng hàng trả.");
            if(jdbc.update("UPDATE hoa_don_chi_tiet SET so_luong_da_tra=so_luong_da_tra+? WHERE id=? AND id_hoa_don=? AND so_luong_da_tra<=so_luong-?",sl,d.get("id_hoa_don_chi_tiet"),id,sl)!=1)throw new IllegalArgumentException("Số lượng trả đã thay đổi hoặc đã được hoàn trước đó.");
            if(dat>0 && jdbc.update("UPDATE chi_tiet_san_pham SET so_luong=ISNULL(so_luong,0)+? WHERE id=? AND ISNULL(so_luong,0)<=2147483647-?",dat,d.get("id_chi_tiet_san_pham"),dat)!=1)throw new IllegalArgumentException("Không thể nhập lại tồn kho.");
            if(loi>0)jdbc.update("INSERT INTO san_pham_hoan_tra(id_chi_tiet_tra,id_chi_tiet_san_pham,so_luong,ghi_chu) VALUES(?,?,?,?)",d.get("id"),d.get("id_chi_tiet_san_pham"),loi,y.get("ly_do"));
        }
        jdbc.update("INSERT INTO giao_dich_hoan_tien(id_yeu_cau,id_hoa_don,so_tien,phuong_thuc,ma_giao_dich,id_nhan_vien) VALUES(?,?,?,?,?,?)",yc,id,amount,y.get("phuong_thuc_hoan"),ma,q.nhanVien());
        if(jdbc.update("UPDATE hoa_don SET tong_tien_da_hoan=tong_tien_da_hoan+CAST(? AS DECIMAL(18,2)),ngay_cap_nhat=SYSDATETIME() WHERE id=? AND tong_tien_da_hoan<=?",amount,id,nganSach(hd).subtract(amount))!=1)throw new IllegalArgumentException("Tiền hoàn đã thay đổi. Vui lòng kiểm tra lại.");
        jdbc.update("UPDATE yeu_cau_tra_hang SET ngay_hoan_tien=SYSDATETIME() WHERE id=?",yc);
        jdbc.update("INSERT INTO lich_su_hoa_don(id_hoa_don,trang_thai,ghi_chu,nguoi_cap_nhat,nguoi_thuc_hien,loai_nguoi_thuc_hien) VALUES(?,5,?,?,?,'nhan_vien')",id,"Hoàn tiền theo yêu cầu TH"+yc+": "+amount+" đ.",q.nhanVien(),q.nhanVien());
    }
    private void lichSu(int yc,String tt,String note,Quyen q){jdbc.update("INSERT INTO lich_su_tra_hang(id_yeu_cau,trang_thai,noi_dung,id_khach,id_nhan_vien) VALUES(?,?,?,?,?)",yc,tt,note,q.khach(),q.nhanVien());}
    private void bao(int id){if(chuong!=null){chuong.donThayDoi(id);chuong.duLieuThayDoi("tra-hang,hoa-don,ton-kho,thong-ke");}}
    public Map<String,Object> danhSach(Quyen q,String tuKhoa,String tt,int trang){
        admin(q);tuKhoa=chuoi(tuKhoa,0,100,"Từ khóa");if(!tt.isEmpty()&&!TRANG_THAI.containsKey(tt))throw new IllegalArgumentException("Trạng thái lọc không hợp lệ.");
        String loc=" WHERE (y.ma_yeu_cau LIKE ? OR hd.ma_hoa_don LIKE ? OR hd.so_dien_thoai_khach_hang LIKE ?)";var args=new ArrayList<Object>(List.of("%"+tuKhoa+"%","%"+tuKhoa+"%","%"+tuKhoa+"%"));if(!tt.isEmpty()){loc+=" AND y.trang_thai=?";args.add(tt);}
        int tong=Objects.requireNonNullElse(jdbc.queryForObject("SELECT COUNT(*) FROM yeu_cau_tra_hang y JOIN hoa_don hd ON hd.id=y.id_hoa_don"+loc,Integer.class,args.toArray()),0);
        int pages=Math.max(1,(tong+9)/10);trang=Math.max(1,Math.min(trang,pages));args.add((trang-1)*10);
        var rows=jdbc.queryForList("SELECT y.*,hd.ma_hoa_don,hd.ten_khach_hang FROM yeu_cau_tra_hang y JOIN hoa_don hd ON hd.id=y.id_hoa_don"+loc+" ORDER BY y.ngay_tao DESC,y.id DESC OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY",args.toArray());for(var d:rows){d.put("ten_trang_thai",TRANG_THAI.get(d.get("trang_thai")));if(!Set.of("DA_HOAN_TIEN","DA_HUY","TU_CHOI").contains(String.valueOf(d.get("trang_thai"))))d.put("so_tien_hoan",tien(d.get("so_tien_hoan")).setScale(0,RoundingMode.DOWN));}
        return Map.of("danhSach",rows,"tong",tong,"trang",trang,"soTrang",pages);
    }
    public List<Map<String,Object>> hangLoi(Quyen q){admin(q);return jdbc.queryForList("""
        SELECT TOP 100 loi.*,sp.ten_san_pham,mau.ma_chi_tiet_san_pham,kt.ten_kich_thuoc,ms.ten_mau_sac,y.ma_yeu_cau,y.id AS id_yeu_cau
        FROM san_pham_hoan_tra loi JOIN chi_tiet_tra_hang ct ON ct.id=loi.id_chi_tiet_tra JOIN yeu_cau_tra_hang y ON y.id=ct.id_yeu_cau
        JOIN chi_tiet_san_pham mau ON mau.id=loi.id_chi_tiet_san_pham JOIN san_pham sp ON sp.id=mau.id_san_pham
        LEFT JOIN kich_thuoc kt ON kt.id=mau.id_kich_thuoc LEFT JOIN mau_sac ms ON ms.id=mau.id_mau_sac ORDER BY loi.id DESC
        """);}
}
