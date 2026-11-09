package com.example.victorsport.Service;

import com.example.victorsport.Dto.DotGiamGiaForm;
import com.example.victorsport.Dto.SanPhamGiamGiaForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class QuanLyDotGiamGiaService {
    private final JdbcTemplate jdbc;
    public QuanLyDotGiamGiaService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Integer> cacMucUuTien() {
        return jdbc.queryForList("""
                SELECT DISTINCT muc_uu_tien FROM dot_giam_gia
                WHERE ISNULL(xoa_mem,0)=0 AND muc_uu_tien IS NOT NULL AND muc_uu_tien>=0
                ORDER BY muc_uu_tien
                """, Integer.class);
    }

    public List<Map<String,Object>> tim(String tuKhoa, String trangThai) {
        String sql = "SELECT d.*, (SELECT COUNT(*) FROM chi_tiet_dot_giam_gia ct WHERE ct.id_dot_giam_gia=d.id AND ISNULL(ct.xoa_mem,0)=0 AND ct.trang_thai=1) AS so_mau FROM dot_giam_gia d WHERE ISNULL(d.xoa_mem,0)=0 AND (d.ten_dot_giam_gia COLLATE Vietnamese_100_CI_AI LIKE ? OR d.ma_dot_giam_gia LIKE ?)";
        List<Object> args = new ArrayList<>();
        args.add("%" + tuKhoa.trim() + "%"); args.add("%" + tuKhoa.trim() + "%");
        var homNay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if ("0".equals(trangThai) || "1".equals(trangThai)) {
            sql += " AND d.trang_thai=? AND d.ngay_ket_thuc>=?";args.add(Integer.valueOf(trangThai));args.add(homNay);
            if("1".equals(trangThai)) {sql+=" AND d.ngay_bat_dau<=?";args.add(homNay);}
        } else if("2".equals(trangThai)) {sql+=" AND d.ngay_ket_thuc<?";args.add(homNay);}
        else if("3".equals(trangThai)) {
            sql+=" AND d.trang_thai=1 AND d.ngay_bat_dau>? AND d.ngay_ket_thuc>=?";args.add(homNay);args.add(homNay);
        }
        return jdbc.queryForList(sql + " ORDER BY d.id DESC", args.toArray()).stream().map(d->themTrangThai(d,homNay)).toList();
    }

    public static String tenTrangThai(Map<String,Object> data,LocalDate homNay) {
        Object ngay=data.get("ngay_ket_thuc");
        if(ngay!=null && LocalDate.parse(ngay.toString().substring(0,10)).isBefore(homNay)) return "Hết hạn";
        Object trangThai=data.get("trang_thai");
        boolean bat=Boolean.TRUE.equals(trangThai) || trangThai instanceof Number n && n.intValue()==1;
        if(!bat) return "Ngừng áp dụng";
        Object batDau=data.get("ngay_bat_dau");
        if(batDau!=null && LocalDate.parse(batDau.toString().substring(0,10)).isAfter(homNay)) return "Sắp mở";
        return "Cho phép áp dụng";
    }
    private Map<String,Object> themTrangThai(Map<String,Object> data,LocalDate homNay) {
        var result=new LinkedHashMap<>(data);String ten=tenTrangThai(data,homNay);
        result.put("ten_trang_thai",ten);result.put("het_han","Hết hạn".equals(ten));result.put("sap_mo","Sắp mở".equals(ten));return result;
    }

    public Map<String,Object> lay(int id) {
        var ds = jdbc.queryForList("SELECT * FROM dot_giam_gia WHERE id=? AND ISNULL(xoa_mem,0)=0", id);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đợt giảm giá.");
        return themTrangThai(ds.get(0),LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    @Transactional
    public int luu(Integer id, DotGiamGiaForm f) {
        return luuVaKiemTraThongBao(id, f).id();
    }

    public record KetQuaLuu(int id, boolean guiEmail) {}

    @Transactional
    public KetQuaLuu luuVaKiemTraThongBao(Integer id, DotGiamGiaForm f) {
        KiemTraUuDai.kiemTra(f.getLoai_giam_gia(), f.getGia_tri_giam_gia(), f.getNgay_bat_dau(), f.getNgay_ket_thuc());
        LocalDate homNay = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        boolean choPhep = Integer.valueOf(1).equals(f.getTrang_thai());
        if (id == null) {
            int moi = jdbc.queryForObject("""
                INSERT INTO dot_giam_gia (ten_dot_giam_gia,loai_giam_gia,gia_tri_giam_gia,ngay_bat_dau,ngay_ket_thuc,muc_uu_tien,trang_thai,so_tien_giam_toi_da)
                OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?,?)
                """, Integer.class, f.getTen_dot_giam_gia(), f.getLoai_giam_gia(), f.getGia_tri_giam_gia(),
                    f.getNgay_bat_dau(), f.getNgay_ket_thuc(), f.getMuc_uu_tien(), f.getTrang_thai(), f.getSo_tien_giam_toi_da());
            return new KetQuaLuu(moi, choPhep && !f.getNgay_ket_thuc().isBefore(homNay));
        }
        // Đọc trạng thái cũ và cập nhật trong cùng khóa để hai lần lưu không gửi trùng.
        Object trangThaiCu = khoa(id).get("trang_thai");
        boolean daBat = Boolean.TRUE.equals(trangThaiCu) || trangThaiCu instanceof Number n && n.intValue() == 1;
        jdbc.update("""
                UPDATE dot_giam_gia SET ten_dot_giam_gia=?,loai_giam_gia=?,gia_tri_giam_gia=?,ngay_bat_dau=?,ngay_ket_thuc=?,muc_uu_tien=?,trang_thai=?,so_tien_giam_toi_da=? WHERE id=?
                """, f.getTen_dot_giam_gia(), f.getLoai_giam_gia(), f.getGia_tri_giam_gia(),
                    f.getNgay_bat_dau(), f.getNgay_ket_thuc(), f.getMuc_uu_tien(), f.getTrang_thai(), f.getSo_tien_giam_toi_da(), id);
        boolean batLaiTrongThoiGian = !daBat && choPhep && !homNay.isBefore(f.getNgay_bat_dau())
                && !homNay.isAfter(f.getNgay_ket_thuc());
        return new KetQuaLuu(id, batLaiTrongThoiGian);
    }

    private Map<String,Object> khoa(int id) {
        var ds = jdbc.queryForList("SELECT * FROM dot_giam_gia WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND ISNULL(xoa_mem,0)=0", id);
        if (ds.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đợt giảm giá.");
        return ds.get(0);
    }

    public List<Map<String,Object>> cacMau() {
        return jdbc.queryForList("""
                SELECT ct.id,ct.ma_chi_tiet_san_pham,sp.ten_san_pham,kt.ten_kich_thuoc,ms.ten_mau_sac,fc.ten_form_chan
                FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id=ct.id_san_pham
                JOIN kich_thuoc kt ON kt.id=ct.id_kich_thuoc JOIN mau_sac ms ON ms.id=ct.id_mau_sac JOIN form_chan fc ON fc.id=ct.id_form_chan
                WHERE ISNULL(ct.xoa_mem,0)=0 AND ct.trang_thai=1 AND ISNULL(sp.xoa_mem,0)=0 AND sp.trang_thai_kinh_doanh=1
                  AND ISNULL(kt.xoa_mem,0)=0 AND kt.trang_thai=1 AND ISNULL(ms.xoa_mem,0)=0 AND ms.trang_thai=1
                  AND ISNULL(fc.xoa_mem,0)=0 AND fc.trang_thai=1
                ORDER BY sp.ten_san_pham,ct.id
                """);
    }

    public List<Map<String,Object>> mauDaChon(int id) {
        return jdbc.queryForList("""
                SELECT c.*,ct.ma_chi_tiet_san_pham,sp.ten_san_pham,kt.ten_kich_thuoc,ms.ten_mau_sac,fc.ten_form_chan
                FROM chi_tiet_dot_giam_gia c JOIN chi_tiet_san_pham ct ON ct.id=c.id_chi_tiet_san_pham
                JOIN san_pham sp ON sp.id=ct.id_san_pham JOIN kich_thuoc kt ON kt.id=ct.id_kich_thuoc
                JOIN mau_sac ms ON ms.id=ct.id_mau_sac JOIN form_chan fc ON fc.id=ct.id_form_chan
                WHERE c.id_dot_giam_gia=? AND ISNULL(c.xoa_mem,0)=0 AND c.trang_thai=1 ORDER BY c.id
                """, id);
    }

    @Transactional
    public void luuMau(int id, SanPhamGiamGiaForm f, int nhanVien) {
        khoa(id);
        if (jdbc.queryForList("""
                SELECT ct.id FROM chi_tiet_san_pham ct JOIN san_pham sp ON sp.id=ct.id_san_pham
                WHERE ct.id=? AND ISNULL(ct.xoa_mem,0)=0 AND ct.trang_thai=1 AND ISNULL(sp.xoa_mem,0)=0 AND sp.trang_thai_kinh_doanh=1
                """, f.getId_chi_tiet_san_pham()).isEmpty()) throw new IllegalArgumentException("Mẫu giày không còn được bán.");
        var cu = jdbc.queryForList("SELECT id FROM chi_tiet_dot_giam_gia WHERE id_dot_giam_gia=? AND id_chi_tiet_san_pham=?", id, f.getId_chi_tiet_san_pham());
        if (cu.isEmpty()) jdbc.update("""
                INSERT INTO chi_tiet_dot_giam_gia (id_dot_giam_gia,id_chi_tiet_san_pham,ghi_chu,nguoi_tao)
                VALUES (?,?,?,?)
                """, id, f.getId_chi_tiet_san_pham(), f.getGhi_chu(), nhanVien);
        else jdbc.update("""
                UPDATE chi_tiet_dot_giam_gia SET ghi_chu=?,trang_thai=1,xoa_mem=0,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME()
                WHERE id_dot_giam_gia=? AND id_chi_tiet_san_pham=?
                """, f.getGhi_chu(), nhanVien, id, f.getId_chi_tiet_san_pham());
    }

    @Transactional
    public void boMau(int id, int mau, int nhanVien) {
        khoa(id);
        if (jdbc.update("""
                UPDATE chi_tiet_dot_giam_gia SET xoa_mem=1,trang_thai=0,nguoi_cap_nhat=?,ngay_cap_nhat=SYSDATETIME()
                WHERE id_dot_giam_gia=? AND id=? AND ISNULL(xoa_mem,0)=0
                """, nhanVien, id, mau) != 1) throw new IllegalArgumentException("Không tìm thấy mẫu trong đợt giảm giá.");
    }

    public void ngung(int id) {
        if (jdbc.update("UPDATE dot_giam_gia SET trang_thai=0 WHERE id=? AND ISNULL(xoa_mem,0)=0", id) != 1)
            throw new IllegalArgumentException("Không tìm thấy đợt giảm giá.");
    }
}
