package com.example.victorsport.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

@Service
public class ChuongTrucTuyenService {
    private record Phien(String kenh,int chu,SseEmitter emitter,BooleanSupplier conQuyen) {}
    private final Map<UUID,Phien> cacPhien=new ConcurrentHashMap<>();
    private final ScheduledExecutorService hangDoi=Executors.newSingleThreadScheduledExecutor(r->{
        var t=new Thread(r,"chuong-truc-tuyen");t.setDaemon(true);return t;
    });
    private final JdbcTemplate jdbc;
    public ChuongTrucTuyenService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @PostConstruct public void batDau() {
        hangDoi.scheduleAtFixedRate(()->cacPhien.forEach((ma,phien)->gui(ma,phien,false)),25,25,TimeUnit.SECONDS);
    }
    protected SseEmitter taoEmitter() { return new SseEmitter(30*60*1000L); }
    public SseEmitter ketNoi(boolean admin,int chu,BooleanSupplier conQuyen) {
        return ketNoi(admin?"admin":"khach",chu,conQuyen);
    }
    public SseEmitter ketNoi(String kenh,int chu,BooleanSupplier conQuyen) {
        var emitter=taoEmitter();var ma=UUID.randomUUID();var phien=new Phien(kenh,chu,emitter,conQuyen);
        cacPhien.put(ma,phien);
        emitter.onCompletion(()->cacPhien.remove(ma));emitter.onTimeout(()->{cacPhien.remove(ma);emitter.complete();});
        emitter.onError(e->cacPhien.remove(ma));
        // Đồng bộ ngay sau kết nối, kể cả khi vừa bị mất mạng.
        gui(ma,phien,true);
        return emitter;
    }
    private void gui(UUID ma,Phien phien,boolean thayDoi) {
        try {
            if(!phien.conQuyen.getAsBoolean()) {cacPhien.remove(ma);phien.emitter.complete();return;}
            phien.emitter.send(thayDoi?SseEmitter.event().name("cap-nhat").data("don-hang")
                :SseEmitter.event().comment("giu-ket-noi"));
        } catch(Exception e) {cacPhien.remove(ma);phien.emitter.complete();}
    }
    private void sauCommit(Runnable viec) {
        Runnable gui=()->hangDoi.execute(()->{
            try {viec.run();}
            catch(Exception e) {org.slf4j.LoggerFactory.getLogger(getClass()).warn("Chưa gửi được tín hiệu chuông; sẽ đồng bộ khi kết nối lại.");}
        });
        if(TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {gui.run();}
            });
        } else gui.run();
    }
    public void donThayDoi(int idHoaDon) {
        sauCommit(()->{
            var ds=jdbc.queryForList("SELECT id_khach_hang FROM hoa_don WHERE id=? AND loai_don IN(1,2) AND ISNULL(xoa_mem,0)=0",idHoaDon);
            if(ds.isEmpty()) return;
            Object kh=ds.get(0).get("id_khach_hang");
            cacPhien.forEach((ma,p)->{if(p.kenh.equals("admin") || p.kenh.equals("khach") && kh instanceof Number n && p.chu==n.intValue()) gui(ma,p,true);});
        });
    }
    public void daDoc(boolean admin,int chu) {
        sauCommit(()->cacPhien.forEach((ma,p)->{if(p.kenh.equals(admin?"admin":"khach") && p.chu==chu) gui(ma,p,true);}));
    }
    public void duLieuThayDoi(String nhom) {
        sauCommit(()->cacPhien.forEach((ma,p)->{
            String guiNhom=nhom;
            if(p.kenh.equals("cong-khai")) {
                guiNhom=String.join(",",Arrays.stream(nhom.split(",")).filter(n->Set.of("san-pham","ton-kho","uu-dai").contains(n)).toList());
            }
            if(guiNhom.isEmpty()) return;
            try {
                if(!p.conQuyen.getAsBoolean()) {cacPhien.remove(ma);p.emitter.complete();return;}
                p.emitter.send(SseEmitter.event().name("du-lieu").data(guiNhom));
            } catch(Exception e) {cacPhien.remove(ma);p.emitter.complete();}
        }));
    }
    @PreDestroy public void dong() {
        hangDoi.shutdownNow();cacPhien.values().forEach(p->p.emitter.complete());cacPhien.clear();
    }
}
