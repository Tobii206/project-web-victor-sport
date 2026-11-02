package com.example.victorsport.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class AnhDaiDienKhachService {
    private static final String URL="/image/avatars/";
    private final JdbcTemplate jdbc;
    private final Path thuMuc;
    public AnhDaiDienKhachService(JdbcTemplate jdbc,@Value("${khach-hang.avatar-directory:image/avatars}") String thuMuc) {
        this.jdbc=jdbc;this.thuMuc=Path.of(thuMuc).toAbsolutePath().normalize();
    }
    @Transactional
    public void doiAnh(int id,MultipartFile tep) {
        luuAnh(id,tep,false);
    }
    @Transactional
    public void doiAnhNhanVien(int id,MultipartFile tep) {
        luuAnh(id,tep,true);
    }
    private void luuAnh(int id,MultipartFile tep,boolean nhanVien) {
        if(tep==null || tep.isEmpty() || tep.getSize()>5L*1024*1024)
            throw new IllegalArgumentException("Chọn một ảnh JPG hoặc PNG, tối đa 5 MB.");
        String ten=Objects.toString(tep.getOriginalFilename(),"").toLowerCase(Locale.ROOT);
        if(!ten.matches(".*\\.(jpg|jpeg|png)$")) throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG hoặc PNG.");
        String bang=nhanVien?"nhan_vien":"khach_hang",cotAnh=nhanVien?"anh_nhan_vien":"anh_dai_dien";
        String chuAnh=(nhanVien?"nv-":"")+id;
        var ds=jdbc.queryForList("SELECT "+cotAnh+" AS anh_dai_dien FROM "+bang+" WITH (UPDLOCK,HOLDLOCK) WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",id);
        if(ds.isEmpty()) throw new IllegalArgumentException("Tài khoản không còn hoạt động.");
        String anhCu=Objects.toString(ds.get(0).get("anh_dai_dien"),"");
        Path moi=null;
        try {
            BufferedImage anh;
            try(var raw=tep.getInputStream();var input=ImageIO.createImageInputStream(raw)) {
                if(input==null) throw new IllegalArgumentException("Ảnh không hợp lệ.");
                var readers=ImageIO.getImageReaders(input);
                if(!readers.hasNext()) throw new IllegalArgumentException("Ảnh không hợp lệ.");
                var reader=readers.next();
                try {
                    reader.setInput(input);
                    String format=reader.getFormatName();
                    int w=reader.getWidth(0),h=reader.getHeight(0);
                    if(!Set.of("png","jpeg","jpg").contains(format.toLowerCase(Locale.ROOT)) || w<1 || h<1 || (long)w*h>16000000L)
                        throw new IllegalArgumentException("Chọn ảnh JPG/PNG hợp lệ, tối đa 16 triệu điểm ảnh.");
                    anh=reader.read(0);
                } finally {reader.dispose();}
            }
            var vuong=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
            var g=vuong.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int canh=Math.min(anh.getWidth(),anh.getHeight()),x=(anh.getWidth()-canh)/2,y=(anh.getHeight()-canh)/2;
                g.drawImage(anh,0,0,256,256,x,y,x+canh,y+canh,null);
            } finally {g.dispose();}
            Files.createDirectories(thuMuc);
            String tenMoi=chuAnh+"-"+UUID.randomUUID()+".png";
            moi=thuMuc.resolve(tenMoi);
            if(!ImageIO.write(vuong,"png",moi.toFile())) throw new IOException("Không lưu được PNG");
            if(jdbc.update("UPDATE "+bang+" SET "+cotAnh+"=?,ngay_cap_nhat=SYSDATETIME(),nguoi_cap_nhat=? WHERE id=? AND trang_thai=1 AND ISNULL(xoa_mem,0)=0",URL+tenMoi,id,id)!=1)
                throw new IllegalArgumentException("Tài khoản đã thay đổi. Vui lòng đăng nhập lại.");
            Path tepMoi=moi;
            if(TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCommit() {xoaAnhCu(chuAnh,anhCu);}
                    @Override public void afterCompletion(int status) {if(status!=STATUS_COMMITTED) xoaTep(tepMoi);}
                });
            } else xoaAnhCu(chuAnh,anhCu);
        } catch(IOException e) {
            xoaTep(moi);throw new IllegalStateException("Chưa lưu được ảnh đại diện. Vui lòng thử lại.",e);
        } catch(RuntimeException e) {xoaTep(moi);throw e;}
    }
    private void xoaAnhCu(String chuAnh,String url) {
        if(!url.startsWith(URL)) return;
        String ten=url.substring(URL.length());
        if(ten.matches(chuAnh+"-[0-9a-fA-F-]{36}\\.png")) xoaTep(thuMuc.resolve(ten));
    }
    private void xoaTep(Path tep) {
        if(tep==null || !tep.toAbsolutePath().normalize().startsWith(thuMuc)) return;
        try {Files.deleteIfExists(tep);} catch(IOException e) { /* Giữ nguyên ảnh đang được tài khoản sử dụng. */ }
    }
}
