package com.sunflower.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAsync
@EnableSwagger2
@SpringBootApplication(scanBasePackages = {"com.sunflower.server"})
public class SteyrServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteyrServerApplication.class);
    }
}
