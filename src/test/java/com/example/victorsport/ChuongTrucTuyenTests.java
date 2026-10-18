package com.example.victorsport;

import com.example.victorsport.Service.ChuongTrucTuyenService;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChuongTrucTuyenTests {
    static class Kenh extends SseEmitter {
        AtomicInteger tin=new AtomicInteger();boolean dong;
        List<String> noiDung=new CopyOnWriteArrayList<>();
        @Override public void send(SseEventBuilder event) {
            noiDung.add(event.build().stream().map(d->String.valueOf(d.getData())).collect(java.util.stream.Collectors.joining()));
            tin.incrementAndGet();
        }
        @Override public void complete() {dong=true;}
    }
    JdbcTemplate jdbc;ChuongTrucTuyenService service;List<Kenh> kenh;
    @BeforeEach void tao() {
        jdbc=mock(JdbcTemplate.class);kenh=new ArrayList<>();
        service=new ChuongTrucTuyenService(jdbc) {
            @Override protected SseEmitter taoEmitter() {var k=new Kenh();kenh.add(k);return k;}
        };
        when(jdbc.queryForList(anyString(),eq(9))).thenReturn(List.of(Map.of("id_khach_hang",7)));
    }
    @AfterEach void dong() {
        if(TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.clearSynchronization();
        service.dong();
    }
    void doi() throws Exception {
        ((ExecutorService)ReflectionTestUtils.getField(service,"hangDoi")).submit(()->{}).get(3,TimeUnit.SECONDS);
    }
    @Test void chiGuiChoAdminVaDungChuDonKeCaNhieuTab() throws Exception {
        service.ketNoi(true,1,()->true);service.ketNoi(false,7,()->true);
        service.ketNoi(false,8,()->true);service.ketNoi(false,7,()->true);
        service.donThayDoi(9);doi();
        assertEquals(List.of(2,2,1,2),kenh.stream().map(k->k.tin.get()).toList());
    }
    @Test void khongGuiTruocKhiCommitVaKhongGuiNeuRollback() throws Exception {
        service.ketNoi(true,1,()->true);
        TransactionSynchronizationManager.initSynchronization();service.donThayDoi(9);doi();
        assertEquals(1,kenh.get(0).tin.get());
        for(var sync:TransactionSynchronizationManager.getSynchronizations()) sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        TransactionSynchronizationManager.clearSynchronization();doi();
        assertEquals(1,kenh.get(0).tin.get());verifyNoInteractions(jdbc);
    }
    @Test void chiDaySauCommitThanhCong() throws Exception {
        service.ketNoi(false,7,()->true);
        TransactionSynchronizationManager.initSynchronization();service.donThayDoi(9);
        var callbacks=TransactionSynchronizationManager.getSynchronizations();
        TransactionSynchronizationManager.clearSynchronization();callbacks.forEach(TransactionSynchronization::afterCommit);doi();
        assertEquals(2,kenh.get(0).tin.get());
    }
    @Test void docDongBoCacTabCuaCungTaiKhoanKhongDungCheoVaiTro() throws Exception {
        service.ketNoi(false,7,()->true);service.ketNoi(false,7,()->true);service.ketNoi(true,7,()->true);
        service.daDoc(false,7);doi();
        assertEquals(List.of(2,2,1),kenh.stream().map(k->k.tin.get()).toList());
    }
    @Test void dangXuatHoacMatQuyenThiDungGui() throws Exception {
        var conQuyen=new AtomicBoolean(true);service.ketNoi(false,7,conQuyen::get);
        conQuyen.set(false);service.donThayDoi(9);doi();
        assertTrue(kenh.get(0).dong);assertEquals(1,kenh.get(0).tin.get());
    }
    @Test void khachVangLaiChiNhanTinHieuDuLieuCongKhai() throws Exception {
        service.ketNoi("cong-khai",0,()->true);service.ketNoi("noi-bo",7,()->true);
        service.duLieuThayDoi("khach-hang,ton-kho,nhan-vien");doi();
        assertTrue(kenh.get(0).noiDung.get(1).contains("ton-kho"));
        assertFalse(kenh.get(0).noiDung.get(1).contains("khach-hang"));
        assertFalse(kenh.get(0).noiDung.get(1).contains("nhan-vien"));
        assertTrue(kenh.get(1).noiDung.get(1).contains("nhan-vien"));
        service.donThayDoi(9);doi();assertEquals(2,kenh.get(0).tin.get());assertEquals(2,kenh.get(1).tin.get());
    }
}
