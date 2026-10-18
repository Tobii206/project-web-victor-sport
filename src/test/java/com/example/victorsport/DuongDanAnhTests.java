package com.example.victorsport;

import com.example.victorsport.Config.GiaoDienConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.*;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DuongDanAnhTests {
    @Test void docAnhTuImageVaLogoVaGiuDuongDanAnhCu() throws Exception {
        Path anh=Path.of("image","kiem-tra-"+UUID.randomUUID()+".png");
        byte[] noiDung={1,2,3,4};
        try(var context=new AnnotationConfigWebApplicationContext()) {
            Files.write(anh,noiDung);
            context.setServletContext(new MockServletContext());
            context.register(CauHinh.class,GiaoDienConfig.class);context.refresh();
            var mvc=MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(get("/image/"+anh.getFileName())).andExpect(status().isOk()).andExpect(content().bytes(noiDung));
            mvc.perform(get("/uploads/products/"+anh.getFileName())).andExpect(status().isOk()).andExpect(content().bytes(noiDung));
            for(String ten:new String[]{"logo.png","logo-nobackgroud.png","favicon-victorsport.svg"})
                mvc.perform(get("/logo/"+ten)).andExpect(status().isOk()).andExpect(content().bytes(Files.readAllBytes(Path.of("logo",ten))));
            mvc.perform(get("/image/../pom.xml")).andExpect(status().isNotFound());
        } finally {Files.deleteIfExists(anh);}
    }
    @Configuration @EnableWebMvc static class CauHinh {}
}
