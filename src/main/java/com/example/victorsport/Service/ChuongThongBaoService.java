package com.example.victorsport.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ChuongThongBaoService {
    @org.springframework.beans.factory.annotation.Autowired
    private ChuongTrucTuyenService trucTuyen;
    private final JdbcTemplate jdbc;
    public ChuongThongBaoService(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    // Dùng lịch sử đơn đã lưu trong giao dịch, tránh báo đơn bị rollback.
    private String dieuKien(boolean admin) {
        return " FROM lich_su_hoa_don ls JOIN hoa_don hd ON hd.id=ls.id_hoa_don "
            + " WHERE ISNULL(ls.xoa_mem,0)=0 AND ISNULL(hd.xoa_mem,0)=0 AND hd.loai_don IN (1,2)"
            + " AND ls.trang_thai IN (1,2,3,4,5,6)" + (admin?"":" AND hd.id_khach_hang=?");
    }
    private String cotChu(boolean admin) { return admin?"id_nhan_vien":"id_khach_hang"; }
    private String chuaDoc(boolean admin) {
        return " AND NOT EXISTS (SELECT 1 FROM thong_bao_da_doc d WHERE d.id_lich_su=ls.id AND d."+cotChu(admin)+"=?)";
    }
    public Map<String,Object> danhSach(boolean admin,int chu) {
        String sql="SELECT TOP 20 ls.id,hd.id AS id_hoa_don,hd.ma_hoa_don,ls.trang_thai,ls.ghi_chu,"
            +"CONVERT(VARCHAR(19),ls.thoi_gian,126) AS thoi_gian,"
            +"CASE WHEN EXISTS (SELECT 1 FROM thong_bao_da_doc d WHERE d.id_lich_su=ls.id AND d."+cotChu(admin)+"=?) THEN 1 ELSE 0 END AS da_doc"
            +dieuKien(admin)+" ORDER BY ls.id DESC";
        var ds=jdbc.queryForList(sql,admin?new Object[]{chu}:new Object[]{chu,chu});
        for(var dong:ds) dong.put("duong_dan",(admin?"/quan-ly/hoa-don/":"/don-hang/")+dong.get("id_hoa_don"));
        Integer dem=jdbc.queryForObject("SELECT COUNT(*)"+dieuKien(admin)+chuaDoc(admin),Integer.class,
            admin?new Object[]{chu}:new Object[]{chu,chu});
        return Map.of("danhSach",ds,"chuaDoc",dem==null?0:dem);
    }
    @Transactional
    public void danhDau(boolean admin,int chu,int id,boolean tatCa) {
        if(id<=0) throw new IllegalArgumentException("Thông báo không hợp lệ.");
        // Giới hạn theo ID đã hiển thị để không đọc nhầm thông báo vừa đến.
        String sql="INSERT INTO thong_bao_da_doc(id_lich_su,"+cotChu(admin)+") SELECT ls.id,?"
            +dieuKien(admin)+" AND ls.id"+(tatCa?"<=?":"=?")
            +" AND NOT EXISTS (SELECT 1 FROM thong_bao_da_doc d WITH (UPDLOCK,HOLDLOCK) WHERE d.id_lich_su=ls.id AND d."+cotChu(admin)+"=?)";
        jdbc.update(sql,admin?new Object[]{chu,id,chu}:new Object[]{chu,chu,id,chu});
        if(trucTuyen!=null) trucTuyen.daDoc(admin,chu);
    }
}
