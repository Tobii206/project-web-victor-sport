package com.example.victorsport;

import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FreeshipNoiThanhTests {
    @Test void chiHienPhieuNoiThanhKhiGiaoHaNoi() {
        var service=spy(new SanPhamOnlineService(mock(JdbcTemplate.class)));
        var noiThanh=Map.<String,Object>of("id",1,"ten_phieu_giam_gia","Freeship nội thành");
        var toanQuoc=Map.<String,Object>of("id",2,"ten_phieu_giam_gia","Freeship toàn quốc");
        var gia=new BigDecimal("1000000");var phi=new BigDecimal("50000");
        doReturn(List.of(noiThanh,toanQuoc)).when(service).layPhieuKhaDung(gia,true,phi);
        assertEquals(List.of(toanQuoc),service.layPhieuKhaDung(gia,true,phi,"Hải Phòng"));
        assertEquals(List.of(noiThanh,toanQuoc),service.layPhieuKhaDung(gia,true,phi,"Thành phố Hà Nội"));
        assertEquals(List.of(toanQuoc),service.layPhieuKhaDung(gia,true,phi,null));
        assertFalse(service.laHaNoi("Hà Nội, Hải Phòng"));
    }
    @Test void guiTrucTiepPhieuNoiThanhNgoaiHaNoiKhongDuocTruLuotHayTaoHoaDon() {
        var jdbc=mock(JdbcTemplate.class);var service=spy(new SanPhamOnlineService(jdbc));
        var dong=new HashMap<String,Object>();dong.put("id",9);dong.put("so_luong",3);dong.put("so_luong_mua",1);
        dong.put("gia_ban",new BigDecimal("1000000"));dong.put("thanh_tien_goc",new BigDecimal("1000000"));dong.put("thanh_tien",new BigDecimal("1000000"));
        doReturn(List.of(dong)).when(service).layGioHang(anyMap());
        when(jdbc.update(contains("UPDATE chi_tiet_san_pham"),any(),any(),any(),any())).thenReturn(1);
        when(jdbc.queryForList(contains("FROM phieu_giam_gia WITH"),eq(3),any()))
            .thenReturn(List.of(Map.of("ten_phieu_giam_gia","Freeship nội thành","loai_phieu_giam_gia",true,"gia_tri_giam_gia",new BigDecimal("30000"))));
        var loi=assertThrows(IllegalArgumentException.class,()->service.datHang(Map.of(9,1),null,"An","0900000001","Số 10, Hải Phòng",false,"COD",3,new BigDecimal("1000000")));
        assertTrue(loi.getMessage().contains("chỉ áp dụng"));
        verify(jdbc,never()).update(contains("so_luong_su_dung=so_luong_su_dung-1"),anyInt());
        verify(jdbc,never()).queryForObject(contains("INSERT INTO hoa_don"),eq(Integer.class),any(Object[].class));
    }
}
