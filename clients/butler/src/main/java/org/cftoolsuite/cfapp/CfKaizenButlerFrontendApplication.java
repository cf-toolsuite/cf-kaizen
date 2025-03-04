package org.cftoolsuite.cfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
public class CfKaizenButlerFrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CfKaizenButlerFrontendApplication.class, args);
    }

}
