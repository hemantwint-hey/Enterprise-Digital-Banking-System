package org.example.enterprisedigitalbankingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class EnterpriseDigitalBankingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseDigitalBankingSystemApplication.class, args);
    }

}
