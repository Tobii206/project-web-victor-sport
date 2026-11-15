package com.example.victorsport.Config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
public class TraHangConfig implements ApplicationRunner {
    private final DataSource dataSource;
    public TraHangConfig(DataSource dataSource){this.dataSource=dataSource;}
    @Override public void run(ApplicationArguments args){
        var script=new ResourceDatabasePopulator(new ClassPathResource("db/tra-hang.sql"));
        script.setSqlScriptEncoding("UTF-8");script.setSeparator("GO");script.execute(dataSource);
    }
}
