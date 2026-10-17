package com.example.victorsport.Service;

import com.example.victorsport.Dto.DiaChiForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class DiaChiKhachHangService {
    private final JdbcTemplate jdbc;
    public DiaChiKhachHangService(JdbcTemplate jdbc){this.jdbc=jdbc;}

    private void kiemTraKhach(int kh,boolean khoa) {
        String sql="SELECT id FROM khach_hang " + (khoa?"WITH (UPDLOCK,HOLDLOCK) ":"")
                + "WHERE id=? AND ISNULL(xoa_mem,0)=0 AND ISNULL(trang_thai,1)=1";
        if(jdbc.queryForList(sql,kh).isEmpty()) throw new IllegalArgumentException("Tài khoản không còn hoạt động.");
    }

    public List<Map<String,Object>> danhSach(int kh) {
        kiemTraKhach(kh,false);
        return jdbc.queryForList("""
                SELECT *,CONCAT_WS(N', ',NULLIF(dia_chi_cu_the,N''),NULLIF(phuong,N''),
                    NULLIF(quan,N''),NULLIF(thanh_pho,N'')) AS dia_chi_day_du
                FROM dia_chi_khach_hang WHERE id_khach_hang=? AND ISNULL(xoa_mem,0)=0
                ORDER BY mac_dinh DESC,id DESC
                """,kh);
    }

    public Map<String,Object> chiTiet(int kh,int id) {
        for(var dc:danhSach(kh)) if(((Number)dc.get("id")).intValue()==id) return dc;
        throw new IllegalArgumentException("Không tìm thấy địa chỉ của bạn.");
    }

    @Transactional
    public void luu(int kh,Integer id,DiaChiForm form) {
        kiemTraKhach(kh,true);
        if(id!=null) chiTiet(kh,id);
        String dayDu=form.getDiaChiCuThe()+", "+form.getPhuong()+", "+form.getThanhPho();
        if(dayDu.length()>255) throw new IllegalArgumentException("Tổng địa chỉ nhận hàng tối đa 255 ký tự.");
        boolean macDinh=form.isMacDinh() || danhSach(kh).isEmpty();
        if(macDinh) jdbc.update("UPDATE dia_chi_khach_hang SET mac_dinh=0 WHERE id_khach_hang=? AND ISNULL(xoa_mem,0)=0",kh);
        if(id==null) jdbc.update("""
                INSERT INTO dia_chi_khach_hang(id_khach_hang,ten_dia_chi,thanh_pho,phuong,quan,dia_chi_cu_the,mac_dinh)
                VALUES (?,?,?,?,NULL,?,?)
                """,kh,form.getTenDiaChi(),form.getThanhPho(),form.getPhuong(),form.getDiaChiCuThe(),macDinh);
        else jdbc.update("""
                UPDATE dia_chi_khach_hang SET ten_dia_chi=?,thanh_pho=?,phuong=?,quan=NULL,dia_chi_cu_the=?,
                    mac_dinh=CASE WHEN ?=1 THEN 1 ELSE mac_dinh END
                WHERE id=? AND id_khach_hang=? AND ISNULL(xoa_mem,0)=0
                """,form.getTenDiaChi(),form.getThanhPho(),form.getPhuong(),form.getDiaChiCuThe(),macDinh,id,kh);
    }

    @Transactional
    public void datMacDinh(int kh,int id) {
        kiemTraKhach(kh,true);chiTiet(kh,id);
        jdbc.update("UPDATE dia_chi_khach_hang SET mac_dinh=CASE WHEN id=? THEN 1 ELSE 0 END WHERE id_khach_hang=? AND ISNULL(xoa_mem,0)=0",id,kh);
    }

    @Transactional
    public void xoa(int kh,int id) {
        kiemTraKhach(kh,true);chiTiet(kh,id);
        jdbc.update("UPDATE dia_chi_khach_hang SET xoa_mem=1,mac_dinh=0 WHERE id=? AND id_khach_hang=? AND ISNULL(xoa_mem,0)=0",id,kh);
        var conLai=danhSach(kh);
        if(!conLai.isEmpty() && !Boolean.TRUE.equals(conLai.get(0).get("mac_dinh")))
            jdbc.update("UPDATE dia_chi_khach_hang SET mac_dinh=1 WHERE id=? AND id_khach_hang=?",conLai.get(0).get("id"),kh);
    }
}
