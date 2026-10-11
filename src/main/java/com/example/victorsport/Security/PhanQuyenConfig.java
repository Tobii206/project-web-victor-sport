package com.example.victorsport.Security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PhanQuyenConfig implements WebMvcConfigurer {
    private final com.example.victorsport.Service.PhanQuyenService phanQuyenService;
    public PhanQuyenConfig(com.example.victorsport.Service.PhanQuyenService phanQuyenService) {
        this.phanQuyenService = phanQuyenService;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PhanQuyenInterceptor(phanQuyenService))
                .addPathPatterns("/pos", "/pos/**", "/quan-ly", "/quan-ly/**");
    }
}
