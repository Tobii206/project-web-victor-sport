package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

public class DiaChiForm implements java.io.Serializable {
    @NotBlank(message="Vui lòng nhập tên địa chỉ.")
    @Size(max=100,message="Tên địa chỉ tối đa 100 ký tự.")
    private String tenDiaChi;
    @NotBlank(message="Vui lòng chọn tỉnh / thành phố.")
    @Size(max=100,message="Tỉnh / thành phố tối đa 100 ký tự.")
    private String thanhPho;
    @NotBlank(message="Vui lòng chọn phường / xã.")
    @Size(max=100,message="Phường / xã tối đa 100 ký tự.")
    private String phuong;
    @NotBlank(message="Vui lòng nhập địa chỉ chi tiết.")
    @Size(max=255,message="Địa chỉ chi tiết tối đa 255 ký tự.")
    private String diaChiCuThe;
    private boolean macDinh;

    public String getTenDiaChi(){return tenDiaChi;}
    public void setTenDiaChi(String value){tenDiaChi=value==null?null:value.trim();}
    public String getThanhPho(){return thanhPho;}
    public void setThanhPho(String value){thanhPho=value==null?null:value.trim();}
    public String getPhuong(){return phuong;}
    public void setPhuong(String value){phuong=value==null?null:value.trim();}
    public String getDiaChiCuThe(){return diaChiCuThe;}
    public void setDiaChiCuThe(String value){diaChiCuThe=value==null?null:value.trim();}
    public boolean isMacDinh(){return macDinh;}
    public void setMacDinh(boolean value){macDinh=value;}
}
