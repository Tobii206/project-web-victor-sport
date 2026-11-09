package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class QuanLySanPhamService {
    public static final List<Map<String, String>> DOI_TUONG =
            List.of(Map.of("ma", "NAM", "ten", "Nam"), Map.of("ma", "NU", "ten", "Nữ"), Map.of("ma", "UNISEX", "ten", "Unisex"));

    public static boolean doiTuongHopLe(String ma) {
        return DOI_TUONG.stream().anyMatch(muc -> muc.get("ma").equals(ma));
    }
    private final JdbcTemplate jdbc;
    // Tên bảng và cột cố định, không lấy tên bảng từ dữ liệu người nhập.
    private static final String[][] DANH_MUC = {
        {"id_thuong_hieu", "thuong_hieu", "ten_thuong_hieu", "Thương hiệu"},
        {"id_xuat_xu", "xuat_xu", "ten_xuat_xu", "Xuất xứ"},
        {"id_vi_tri_thi_dau", "vi_tri_thi_dau", "ten_vi_tri", "Nhu cầu / vị trí thi đấu"},
        {"id_phong_cach_choi", "phong_cach_choi", "ten_phong_cach", "Phong cách"},
        {"id_co_giay", "co_giay", "ten_co_giay", "Cổ giày"},
        {"id_chat_lieu", "chat_lieu", "ten_chat_lieu", "Chất liệu"},
        {"id_mau_sac", "mau_sac", "ten_mau_sac", "Màu sắc"},
        {"id_kich_thuoc", "kich_thuoc", "ten_kich_thuoc", "Kích thước"},
        {"id_form_chan", "form_chan", "ten_form_chan", "Form chân"}
    };
    public QuanLySanPhamService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> danhMuc() {
        List<Map<String, Object>> ds = new ArrayList<>();
        for (String[] dm : DANH_MUC) {
            ds.add(Map.of("truong", dm[0], "nhan", dm[3], "luaChon", jdbc.queryForList(
                "SELECT id, " + dm[2] + " AS ten FROM " + dm[1] + " WHERE ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1 ORDER BY " + dm[2])));
        }
        return ds;
    }

    public List<Map<String, Object>> tim(Map<String, String> loc) {
        String sql = """
            SELECT sp.*, th.ten_thuong_hieu, cl.ten_chat_lieu,
                ISNULL(t.ton,0) AS ton_kho, ISNULL(t.so_mau,0) AS so_mau, t.gia,
                (SELECT TOP 1 a.duong_dan_anh FROM anh_chi_tiet_san_pham a
                 JOIN chi_tiet_san_pham ct ON ct.id=a.id_chi_tiet_san_pham
                 WHERE ct.id_san_pham=sp.id AND ISNULL(a.xoa_mem,0)=0
                   AND EXISTS (SELECT 1 FROM chi_tiet_san_pham con WHERE con.id_san_pham=ct.id_san_pham
                     AND con.id_mau_sac=ct.id_mau_sac AND ISNULL(con.xoa_mem,0)=0)
                 ORDER BY a.la_anh_dai_dien DESC,a.id) AS anh
            FROM san_pham sp JOIN thuong_hieu th ON th.id=sp.id_thuong_hieu
            LEFT JOIN chat_lieu cl ON cl.id=sp.id_chat_lieu
            OUTER APPLY (SELECT SUM(so_luong) AS ton, COUNT(*) AS so_mau,
                MIN(gia_ban) AS gia FROM chi_tiet_san_pham
                WHERE id_san_pham=sp.id AND ISNULL(xoa_mem,0)=0) t
            WHERE ISNULL(sp.xoa_mem,0)=0
            """;
        List<Object> args = new ArrayList<>();
        String tu = loc.getOrDefault("tuKhoa", "").trim();
        if (!tu.isEmpty()) {
            sql += """
                AND (sp.ten_san_pham COLLATE Vietnamese_100_CI_AI LIKE ? OR sp.ma_san_pham LIKE ?
                    OR EXISTS (SELECT 1 FROM chi_tiet_san_pham ct WHERE ct.id_san_pham=sp.id
                        AND ISNULL(ct.xoa_mem,0)=0 AND ct.ma_chi_tiet_san_pham = ?))
                """;
            args.add("%" + tu + "%");
            args.add("%" + tu + "%");
            args.add(tu);
        }
        for (String ten : new String[]{"id_thuong_hieu", "id_chat_lieu", "id_vi_tri_thi_dau"}) {
            Integer id = soNguyen(loc.get(ten), "Bộ lọc", false);
            if (id != null) { sql += " AND sp." + ten + "=?"; args.add(id); }
        }
        String trangThai = loc.getOrDefault("trang_thai_kinh_doanh", "");
        String doiTuong = loc.get("doi_tuong");
        if ("chua-phan-loai".equals(doiTuong)) sql += " AND sp.doi_tuong IS NULL";
        else if (doiTuongHopLe(doiTuong)) {
            sql += " AND sp.doi_tuong=?";
            args.add(doiTuong);
        }
        if (trangThai.equals("0") || trangThai.equals("1")) { sql += " AND sp.trang_thai_kinh_doanh=?"; args.add(Integer.valueOf(trangThai)); }
        if ("het".equals(loc.get("tonKho"))) sql += " AND ISNULL(t.ton,0)=0";
        if ("con".equals(loc.get("tonKho"))) sql += " AND ISNULL(t.ton,0)>0";
        String sapXep = loc.getOrDefault("sapXep", "");
        if (sapXep.equals("gia-tang")) sql += " ORDER BY t.gia, sp.id DESC";
        else if (sapXep.equals("gia-giam")) sql += " ORDER BY t.gia DESC, sp.id DESC";
        else if (sapXep.equals("ten")) sql += " ORDER BY sp.ten_san_pham, sp.id";
        else sql += " ORDER BY sp.id DESC";
        return jdbc.queryForList(sql, args.toArray());
    }

    public Map<String, Object> lay(int id) {
        var ds = jdbc.queryForList("SELECT * FROM san_pham WHERE id=? AND ISNULL(xoa_mem,0)=0", id);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
        return ds.get(0);
    }
    public List<Map<String, Object>> layMau(int id) {
        return jdbc.queryForList("""
            SELECT ct.*, ms.ten_mau_sac, kt.ten_kich_thuoc, fc.ten_form_chan
            FROM chi_tiet_san_pham ct JOIN mau_sac ms ON ms.id=ct.id_mau_sac
            JOIN kich_thuoc kt ON kt.id=ct.id_kich_thuoc JOIN form_chan fc ON fc.id=ct.id_form_chan
            WHERE ct.id_san_pham=? AND ISNULL(ct.xoa_mem,0)=0 ORDER BY ct.id DESC
            """, id);
    }
    public List<Map<String, Object>> layAnh(int id) {
        return layAnhTheoMau(jdbc, id);
    }

    static List<Map<String, Object>> layAnhTheoMau(JdbcTemplate jdbc, int id) {
        return jdbc.queryForList("""
            SELECT a.*, ct.id_mau_sac
            FROM anh_chi_tiet_san_pham a
            JOIN chi_tiet_san_pham ct ON ct.id=a.id_chi_tiet_san_pham
            WHERE ct.id_san_pham=? AND ISNULL(a.xoa_mem,0)=0
            ORDER BY ct.id_mau_sac, ISNULL(a.la_anh_dai_dien,0) DESC, a.id
            """, id);
    }
    public static List<Map<String,Object>> nhomTheoMau(List<Map<String,Object>> cacMau,List<Map<String,Object>> cacAnh) {
        var nhom=new LinkedHashMap<Integer,Map<String,Object>>();
        for(var mau:cacMau) {
            if(!(mau.get("id_mau_sac") instanceof Number soMau)) continue;
            int idMau=soMau.intValue();
            var n=nhom.computeIfAbsent(idMau,key->{
                var moi=new LinkedHashMap<String,Object>();moi.put("id_mau_sac",key);moi.put("ten_mau_sac",mau.get("ten_mau_sac"));
                moi.put("idMau",mau.get("id"));moi.put("cacMau",new ArrayList<Map<String,Object>>());
                moi.put("cacAnh", new ArrayList<Map<String,Object>>());
                return moi;
            });
            @SuppressWarnings("unchecked") var sizes=(List<Map<String,Object>>)n.get("cacMau");sizes.add(mau);
            if(((Number)mau.get("id")).intValue()<((Number)n.get("idMau")).intValue()) n.put("idMau",mau.get("id"));
        }
        for (var anh : cacAnh) {
            if (!(anh.get("id_mau_sac") instanceof Number id) || !nhom.containsKey(id.intValue())) continue;
            var nhomMau = nhom.get(id.intValue());
            nhomMau.putIfAbsent("anh", anh);
            @SuppressWarnings("unchecked")
            var anhCungMau = (List<Map<String, Object>>) nhomMau.get("cacAnh");
            anhCungMau.add(anh);
        }
        return new ArrayList<>(nhom.values());
    }
    private void khoaSanPham(int id) {
        if (jdbc.queryForList("SELECT id FROM san_pham WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND ISNULL(xoa_mem,0)=0", id).isEmpty())
            throw new IllegalArgumentException("Sản phẩm đã bị xóa. Vui lòng tải lại trang.");
    }
    private Integer chonDanhMuc(Map<String, String> data, int viTri, boolean batBuoc) {
        String[] dm = DANH_MUC[viTri];
        Integer id = soNguyen(data.get(dm[0]), dm[3], batBuoc);
        if (id != null && jdbc.queryForObject("SELECT COUNT(*) FROM " + dm[1] + " WHERE id=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1", Integer.class, id) == 0)
            throw new IllegalArgumentException(dm[3] + " không còn hoạt động. Vui lòng chọn lại.");
        return id;
    }
    @Transactional
    public int luu(Integer id, Map<String, String> d, int nhanVien) {
        String ten = chu(d.get("ten_san_pham"), "Tên sản phẩm", 255, true);
        String ngan = chu(d.get("mo_ta_ngan"), "Mô tả ngắn", 500, false);
        String chiTiet = chu(d.get("mo_ta_chi_tiet"), "Mô tả chi tiết", 20000, false);
        BigDecimal giaChung = tien(d.get("gia_ban"), "Giá sản phẩm", true);
        String doiTuong = chu(d.get("doi_tuong"), "Giới tính", 6, false);
        if (!doiTuong.isEmpty() && !doiTuongHopLe(doiTuong))
            throw new IllegalArgumentException("Giới tính sản phẩm không hợp lệ.");
        String maDoiTuong = doiTuong.isEmpty() ? null : doiTuong;
        List<Object> args = new ArrayList<>();
        for (int i=0;i<6;i++) args.add(chonDanhMuc(d, i, i==0));
        args.add(ten); args.add(ngan); args.add(chiTiet); args.add(trangThai(d.get("trang_thai_kinh_doanh"))); args.add(nhanVien);
        if (id == null) {
            args.add(giaChung);
            args.add(maDoiTuong);
            return jdbc.queryForObject("""
                INSERT INTO san_pham (id_thuong_hieu,id_xuat_xu,id_vi_tri_thi_dau,id_phong_cach_choi,id_co_giay,id_chat_lieu,
                    ten_san_pham,mo_ta_ngan,mo_ta_chi_tiet,trang_thai_kinh_doanh,nguoi_tao,gia_ban,doi_tuong)
                OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, Integer.class, args.toArray());
        }
        khoaSanPham(id);
        args.add(giaChung);
        args.add(d.containsKey("doi_tuong") ? 1 : 0);
        args.add(maDoiTuong);
        args.add(id);
        jdbc.update("""
            UPDATE san_pham SET id_thuong_hieu=?,id_xuat_xu=?,id_vi_tri_thi_dau=?,id_phong_cach_choi=?,id_co_giay=?,id_chat_lieu=?,
                ten_san_pham=?,mo_ta_ngan=?,mo_ta_chi_tiet=?,trang_thai_kinh_doanh=?,nguoi_cap_nhat=?,gia_ban=?,
                doi_tuong=CASE WHEN ?=1 THEN ? ELSE doi_tuong END,ngay_cap_nhat=SYSDATETIME() WHERE id=?
            """, args.toArray());
        jdbc.update("UPDATE chi_tiet_san_pham SET gia_ban=?,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME() WHERE id_san_pham=? AND ISNULL(xoa_mem,0)=0", giaChung, nhanVien, id);
        return id;
    }
    @Transactional
    public void xoa(int id, int nhanVien) {
        khoaSanPham(id);
        jdbc.update("UPDATE san_pham SET xoa_mem=1,trang_thai_kinh_doanh=0,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME() WHERE id=?", nhanVien,id);
    }
    @Transactional
    public int luuMau(int sanPham, Integer id, Map<String, String> d, int nhanVien) {
        khoaSanPham(sanPham);
        Integer mau = chonDanhMuc(d,6,true), size = chonDanhMuc(d,7,true), form = chonDanhMuc(d,8,true);
        int ton = soNguyen(d.get("so_luong"), "Số lượng", true);
        if (ton<0) throw new IllegalArgumentException("Tồn kho không được âm.");
        BigDecimal gia = jdbc.queryForObject("SELECT gia_ban FROM san_pham WHERE id=? AND ISNULL(xoa_mem,0)=0", BigDecimal.class, sanPham);
        if (gia == null) throw new IllegalArgumentException("Vui lòng lưu giá trong Thông tin sản phẩm trước khi thêm mẫu.");
        int soMauTrung = jdbc.queryForObject("SELECT COUNT(*) FROM chi_tiet_san_pham WHERE id_san_pham=? AND id_mau_sac=? AND id_kich_thuoc=? AND id_form_chan=? AND ISNULL(xoa_mem,0)=0 AND id<>?", Integer.class,sanPham,mau,size,form,id==null?0:id);
        if (soMauTrung > 0) {
            if (id != null || soMauTrung > 1)
                throw new IllegalArgumentException("Mẫu có cùng màu, size và form chân đã tồn tại.");
            int idMau = jdbc.queryForObject("""
                SELECT id FROM chi_tiet_san_pham
                WHERE id_san_pham=? AND id_mau_sac=? AND id_kich_thuoc=? AND id_form_chan=? AND ISNULL(xoa_mem,0)=0
                """, Integer.class, sanPham, mau, size, form);
            int soDong = jdbc.update("""
                UPDATE chi_tiet_san_pham
                SET so_luong=so_luong+?,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME()
                WHERE id=? AND CAST(so_luong AS BIGINT)+?<=2147483647
                """, ton, nhanVien, idMau, ton);
            if (soDong != 1) throw new IllegalArgumentException("Số lượng nhập vượt giới hạn tồn kho.");
            return idMau;
        }
        String ghiChu = chu(d.get("ghi_chu"),"Ghi chú",255,false);
        int trangThai = trangThai(d.get("trang_thai"));
        if (id==null) return jdbc.queryForObject("""
            INSERT INTO chi_tiet_san_pham (id_san_pham,id_mau_sac,id_kich_thuoc,id_form_chan,so_luong,gia_ban,trang_thai,ghi_chu,nguoi_tao)
            OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?,?,?)
            """,Integer.class,sanPham,mau,size,form,ton,gia,trangThai,ghiChu,nhanVien);
        int tonCu = soNguyen(d.get("so_luong_cu"),"Tồn kho trước khi sửa",true);
        giuAnhKhiDoiMau(sanPham,id,mau);
        int soDong = jdbc.update("""
            UPDATE chi_tiet_san_pham SET id_mau_sac=?,id_kich_thuoc=?,id_form_chan=?,so_luong=?,gia_ban=?,trang_thai=?,ghi_chu=?,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME()
            WHERE id=? AND id_san_pham=? AND ISNULL(xoa_mem,0)=0 AND so_luong=?
            """,mau,size,form,ton,gia,trangThai,ghiChu,nhanVien,id,sanPham,tonCu);
        if (soDong!=1) throw new IllegalArgumentException("Mẫu giày hoặc tồn kho đã thay đổi. Vui lòng tải lại trang rồi sửa.");
        return id;
    }
    @Transactional
    public int themNhanhMau(int sanPham, List<Integer> sizes, Map<String, String> duLieu, int nhanVien) {
        if (sizes == null || sizes.isEmpty() || sizes.size() > 30 || sizes.stream().anyMatch(size -> size == null || size <= 0))
            throw new IllegalArgumentException("Vui lòng chọn từ 1 đến 30 size hợp lệ.");
        Map<String, String> mau = new HashMap<>(duLieu);
        var sizeKhongTrung = new LinkedHashSet<>(sizes);
        for (Integer size : sizeKhongTrung) {
            mau.put("id_kich_thuoc", size.toString());
            luuMau(sanPham, null, mau, nhanVien);
        }
        return sizeKhongTrung.size();
    }

    @Transactional
    public void xoaMau(int sanPham,int id,int nhanVien) {
        khoaSanPham(sanPham);
        if (jdbc.update("UPDATE chi_tiet_san_pham SET xoa_mem=1,trang_thai=0,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME() WHERE id=? AND id_san_pham=? AND ISNULL(xoa_mem,0)=0",nhanVien,id,sanPham)!=1)
            throw new IllegalArgumentException("Không tìm thấy mẫu giày.");
    }
    private void giuAnhKhiDoiMau(int sanPham,int id,int mauMoi) {
        var cu=jdbc.queryForList("SELECT id_mau_sac FROM chi_tiet_san_pham WHERE id=? AND id_san_pham=? AND ISNULL(xoa_mem,0)=0",id,sanPham);
        if(cu.isEmpty() || ((Number)cu.get(0).get("id_mau_sac")).intValue()==mauMoi) return;
        int mauCu=((Number)cu.get(0).get("id_mau_sac")).intValue();
        var khac=jdbc.queryForList("SELECT TOP 1 id FROM chi_tiet_san_pham WHERE id_san_pham=? AND id_mau_sac=? AND id<>? ORDER BY ISNULL(xoa_mem,0),id",sanPham,mauCu,id);
        if(khac.isEmpty()) jdbc.update("UPDATE anh_chi_tiet_san_pham SET xoa_mem=1,la_anh_dai_dien=0 WHERE id_chi_tiet_san_pham=? AND ISNULL(xoa_mem,0)=0",id);
        else jdbc.update("UPDATE anh_chi_tiet_san_pham SET id_chi_tiet_san_pham=? WHERE id_chi_tiet_san_pham=? AND ISNULL(xoa_mem,0)=0",khac.get(0).get("id"),id);
    }
    private static final String ANH_CUNG_MAU="id_chi_tiet_san_pham IN (SELECT id FROM chi_tiet_san_pham WHERE id_san_pham=? AND id_mau_sac=?) AND ISNULL(xoa_mem,0)=0";

    @Transactional
    public void themAnh(int sanPham,int mau,String duongDan) {
        khoaSanPham(sanPham);
        var ds=jdbc.queryForList("SELECT id_mau_sac FROM chi_tiet_san_pham WHERE id=? AND id_san_pham=? AND ISNULL(xoa_mem,0)=0",mau,sanPham);
        if (ds.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy mẫu giày để thêm ảnh.");
        int mauSac=((Number)ds.get(0).get("id_mau_sac")).intValue();
        var hienTai=jdbc.queryForList("SELECT id,duong_dan_anh FROM anh_chi_tiet_san_pham WHERE "+ANH_CUNG_MAU+" ORDER BY ISNULL(la_anh_dai_dien,0) DESC,id",sanPham,mauSac);
        if (hienTai.stream().anyMatch(anh -> duongDan.equals(anh.get("duong_dan_anh")))) return;
        if (hienTai.size() >= 12) throw new IllegalArgumentException("Mỗi màu lưu tối đa 12 ảnh.");
        jdbc.update("INSERT INTO anh_chi_tiet_san_pham (id_chi_tiet_san_pham,duong_dan_anh,la_anh_dai_dien) VALUES (?,?,?)",
                mau, duongDan, hienTai.isEmpty() ? 1 : 0);
    }
    @Transactional
    public void suaAnh(int sanPham,int id,boolean xoa) {
        khoaSanPham(sanPham);
        var ds = jdbc.queryForList("SELECT a.*,ct.id_mau_sac FROM anh_chi_tiet_san_pham a JOIN chi_tiet_san_pham ct ON ct.id=a.id_chi_tiet_san_pham WHERE a.id=? AND ct.id_san_pham=? AND ISNULL(a.xoa_mem,0)=0 AND EXISTS (SELECT 1 FROM chi_tiet_san_pham con WHERE con.id_san_pham=ct.id_san_pham AND con.id_mau_sac=ct.id_mau_sac AND ISNULL(con.xoa_mem,0)=0)",id,sanPham);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy ảnh.");
        int mau = ((Number)ds.get(0).get("id_mau_sac")).intValue();
        if (xoa) {
            jdbc.update("UPDATE anh_chi_tiet_san_pham SET xoa_mem=1,la_anh_dai_dien=0 WHERE id=?",id);
        } else {
            jdbc.update("UPDATE anh_chi_tiet_san_pham SET la_anh_dai_dien=0 WHERE "+ANH_CUNG_MAU+" AND id<>?",sanPham,mau,id);
            jdbc.update("UPDATE anh_chi_tiet_san_pham SET la_anh_dai_dien=1 WHERE id=?",id);
        }
    }
    private String chu(String s,String nhan,int max,boolean batBuoc) {
        String value=s==null?"":s.trim();
        if (batBuoc && value.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập "+nhan.toLowerCase()+".");
        if (value.length()>max) throw new IllegalArgumentException(nhan+" quá dài (tối đa "+max+" ký tự).");
        return value;
    }
    private Integer soNguyen(String s,String nhan,boolean batBuoc) {
        if (s==null || s.isBlank()) {if (!batBuoc) return null;throw new IllegalArgumentException("Vui lòng nhập/chọn "+nhan.toLowerCase()+".");}
        try {return Integer.valueOf(s);} catch(NumberFormatException e) {throw new IllegalArgumentException(nhan+" phải là số nguyên hợp lệ.");}
    }
    private int trangThai(String s) {
        if (!"0".equals(s) && !"1".equals(s)) throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        return Integer.parseInt(s);
    }
    private BigDecimal tien(String s,String nhan,boolean batBuoc) {
        if (s==null || s.isBlank()) {if (!batBuoc) return null;throw new IllegalArgumentException("Vui lòng nhập "+nhan.toLowerCase()+".");}
        try {BigDecimal n=new BigDecimal(s);if(n.signum()<0 || n.scale()>2 || n.compareTo(new BigDecimal("9999999999999999.99"))>0) throw new NumberFormatException();return n;}
        catch(NumberFormatException e) {throw new IllegalArgumentException(nhan+" phải không âm và có tối đa 2 số thập phân.");}
    }
}
