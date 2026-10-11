package com.example.victorsport.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import java.util.Locale;

@Configuration
public class GiaoDienConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
            .addResourceLocations(java.nio.file.Path.of("uploads", "products").toAbsolutePath().toUri().toString() + "/");
    }
    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("vi-VN"));
    }
}
