package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

@Service
public class BaoMatTaiKhoanService {
    private final JdbcTemplate jdbc;
    public BaoMatTaiKhoanService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    private String cot(boolean nhanVien) { return nhanVien?"id_nhan_vien":"id_khach_hang"; }
    public void taoNeuChuaCo(int id,boolean nhanVien) {
        String cot=cot(nhanVien);
        try { jdbc.update("INSERT INTO bao_mat_tai_khoan("+cot+") SELECT ? WHERE NOT EXISTS (SELECT 1 FROM bao_mat_tai_khoan WITH (UPDLOCK,HOLDLOCK) WHERE "+cot+"=?)",id,id); }
        catch(DuplicateKeyException e) { /* Phiên khác đã tạo cùng tài khoản. */ }
    }
    public boolean biKhoa(int id,boolean nhanVien) {
        taoNeuChuaCo(id,nhanVien);
        Integer dem=jdbc.queryForObject("SELECT COUNT(*) FROM bao_mat_tai_khoan WHERE "+cot(nhanVien)+"=? AND bi_khoa=1",Integer.class,id);
        return dem!=null && dem>0;
    }
    public int ghiSai(int id,boolean nhanVien) {
        taoNeuChuaCo(id,nhanVien);
        Integer dem=jdbc.queryForObject("UPDATE bao_mat_tai_khoan SET so_lan_sai=CASE WHEN so_lan_sai<5 THEN so_lan_sai+1 ELSE 5 END,bi_khoa=CASE WHEN so_lan_sai>=4 THEN 1 ELSE bi_khoa END OUTPUT INSERTED.so_lan_sai WHERE "+cot(nhanVien)+"=?",Integer.class,id);
        return dem==null?1:dem;
    }
    public void dangNhapDung(int id,boolean nhanVien) {
        jdbc.update("UPDATE bao_mat_tai_khoan SET so_lan_sai=0 WHERE "+cot(nhanVien)+"=? AND bi_khoa=0",id);
        if(biKhoa(id,nhanVien)) throw new IllegalArgumentException("Tài khoản đã bị khóa. Vui lòng liên hệ quản lý.");
    }
    public void moKhoa(int id,boolean nhanVien) {
        taoNeuChuaCo(id,nhanVien);
        jdbc.update("UPDATE bao_mat_tai_khoan SET so_lan_sai=0,bi_khoa=0,ma_xac_minh_hash=NULL,han_xac_minh=NULL WHERE "+cot(nhanVien)+"=?",id);
    }
}
