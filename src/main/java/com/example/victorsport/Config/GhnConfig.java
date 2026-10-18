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
