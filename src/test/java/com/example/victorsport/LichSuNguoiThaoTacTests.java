package com.example.victorsport;

import com.example.victorsport.Service.HoaDonService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LichSuNguoiThaoTacTests {
    @Test void hienDungNhanVienKhachHangVangLaiVaHeThongKhongTuGanNguoiCu() {
        var j=mock(JdbcTemplate.class);
        var nv=new HashMap<String,Object>(Map.of("loai_tac_nhan","nhan_vien","ten_nhan_vien","Nguyễn Văn A","ma_nhan_vien","NV0001","ten_quyen_han","Nhân viên","ghi_chu","Đã gửi hàng cho shipper."));
        var kh=new HashMap<String,Object>(Map.of("loai_tac_nhan","khach_hang","nguoi_thuc_hien",7,"id_khach_hang",7,"ten_khach_hang","Trần Văn B","ma_khach_hang","KH0007"));
        var guest=new HashMap<String,Object>(Map.of("loai_tac_nhan","khach_hang","ten_khach_tren_don","Lê Văn C"));
        var system=new HashMap<String,Object>(Map.of("loai_tac_nhan","he_thong"));
        var old=new HashMap<String,Object>(Map.of("loai_tac_nhan","khong_ro","ghi_chu","Quản trị viên xác nhận đơn hàng."));
        when(j.queryForList(contains("FROM lich_su_hoa_don"),eq(10))).thenReturn(List.of(nv,kh,guest,system,old));
        var rows=new HoaDonService(j).layLichSu(10);
        assertEquals("Nguyễn Văn A",rows.get(0).get("nguoi_thao_tac"));assertEquals("NV0001",rows.get(0).get("ma_nguoi_thao_tac"));
        assertEquals("Nhân viên",rows.get(0).get("vai_tro_thao_tac"));
        assertEquals("Trần Văn B",rows.get(1).get("nguoi_thao_tac"));assertEquals("KH0007",rows.get(1).get("ma_nguoi_thao_tac"));
        assertEquals("Khách vãng lai",rows.get(2).get("vai_tro_thao_tac"));assertEquals("Lê Văn C",rows.get(2).get("nguoi_thao_tac"));
        assertEquals("Hệ thống",rows.get(3).get("nguoi_thao_tac"));assertEquals("Chưa ghi nhận người thực hiện",rows.get(4).get("nguoi_thao_tac"));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void lichSuDonGhepDuCacMocTraHangDungThuTuVaKhongLapHoanTien(){
        var j=mock(JdbcTemplate.class);
        var don=new HashMap<String,Object>(Map.of("thoi_gian",new java.sql.Timestamp(1000),"ghi_chu","Khách đặt hàng online"));
        var hoanCu=new HashMap<String,Object>(Map.of("thoi_gian",new java.sql.Timestamp(9000),"ghi_chu","Hoàn tiền theo yêu cầu TH21: 100 đ."));
        var hoanKhac=new HashMap<String,Object>(Map.of("thoi_gian",new java.sql.Timestamp(10000),"ghi_chu","Hoàn tiền theo yêu cầu TH22: 50 đ."));
        when(j.queryForList(contains("FROM lich_su_hoa_don"),eq(10))).thenReturn(List.of(don,hoanCu,hoanKhac));
        var states=List.of("CHO_XAC_NHAN","DA_XAC_NHAN","DANG_GUI_HANG","DA_NHAN_HANG","DANG_KIEM_TRA","DA_DUYET_HOAN_TRA","DA_HOAN_TIEN");
        var tra=new ArrayList<Map<String,Object>>();int index=0;
        for(String state:states){var row=new HashMap<String,Object>();row.put("id_yeu_cau",21);row.put("ma_yeu_cau","TH000021");row.put("trang_thai_tra_hang",state);row.put("thoi_gian",new java.sql.Timestamp(2000+index++*1000));row.put("noi_dung","Nội dung xử lý "+state);
            row.put("loai_tac_nhan",state.equals("CHO_XAC_NHAN")?"khach_hang":"nhan_vien");row.put("nguoi_thuc_hien",state.equals("CHO_XAC_NHAN")?7:9);
            row.put("ten_nhan_vien","Nhân viên xử lý");row.put("ma_nhan_vien","NV0009");row.put("ten_quyen_han","Nhân viên");row.put("ten_khach_hang","Khách tạo yêu cầu");row.put("ma_khach_hang","KH0007");tra.add(row);}
        when(j.queryForList(contains("FROM lich_su_tra_hang"),eq(10))).thenReturn(tra);
        var rows=new HoaDonService(j).layLichSu(10);assertEquals(9,rows.size());
        assertEquals(states,rows.stream().filter(r->r.containsKey("id_yeu_cau")).map(r->r.get("trang_thai_tra_hang")).toList());
        assertEquals("Khách tạo yêu cầu",rows.get(1).get("nguoi_thao_tac"));assertEquals("NV0009",rows.get(7).get("ma_nguoi_thao_tac"));
        assertTrue(rows.get(7).get("ghi_chu").toString().contains("Đã hoàn tiền"));
        assertFalse(rows.stream().anyMatch(r->r.get("ghi_chu").toString().startsWith("Hoàn tiền theo yêu cầu TH21:")));
        assertTrue(rows.get(8).get("ghi_chu").toString().startsWith("Hoàn tiền theo yêu cầu TH22:"));
        for(int i=1;i<rows.size();i++)assertTrue(((java.util.Date)rows.get(i-1).get("thoi_gian")).getTime()<=((java.util.Date)rows.get(i).get("thoi_gian")).getTime());
        verify(j,never()).update(anyString(),any(Object[].class));
    }
}
