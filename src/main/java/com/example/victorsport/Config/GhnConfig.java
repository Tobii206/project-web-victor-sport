package com.example.victorsport.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ghn")
public class GhnConfig {
    private boolean enabled;
    private String token = "";
    private int shopId;
    private int fromDistrictId;
    private String fromWardCode = "";
    private int weightPerPair=1000;
    private int length=35;
    private int width=25;
    private int heightPerPair=15;
    public int getWeightPerPair(){return weightPerPair;}
    public void setWeightPerPair(int v){weightPerPair=v;}
    public int getLength(){return length;}
    public void setLength(int v){length=v;}
    public int getWidth(){return width;}
    public void setWidth(int v){width=v;}
    public int getHeightPerPair(){return heightPerPair;}
    public void setHeightPerPair(int v){heightPerPair=v;}
    private String fromName="VictorSport",fromPhone="0325327602",fromAddress="",fromProvinceName="",fromWardName="";
    public String getFromName(){return fromName;}
    public void setFromName(String v){fromName=v==null?"":v.trim();}
    public String getFromPhone(){return fromPhone;}
    public void setFromPhone(String v){fromPhone=v==null?"":v.trim();}
    public String getFromAddress(){return fromAddress;}
    public void setFromAddress(String v){fromAddress=v==null?"":v.trim();}
    public String getFromProvinceName(){return fromProvinceName;}
    public void setFromProvinceName(String v){fromProvinceName=v==null?"":v.trim();}
    public String getFromWardName(){return fromWardName;}
    public void setFromWardName(String v){fromWardName=v==null?"":v.trim();}
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled=enabled; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token=token; }
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId=shopId; }
    public int getFromDistrictId() { return fromDistrictId; }
    public void setFromDistrictId(int id) { fromDistrictId=id; }
    public String getFromWardCode() { return fromWardCode; }
    public void setFromWardCode(String code) { fromWardCode=code; }
    public boolean sanSang() { return enabled && token!=null && !token.isBlank() && shopId>0; }
}
