package de.jeb.japp.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "de.jeb.japp")
@EnableJpaRepositories(basePackages = "de.jeb.japp")
@EntityScan(basePackages = "de.jeb.japp.model")
public class JappApplication {

    public static void main(String[] args) {
        SpringApplication.run(JappApplication.class, args);
    }

}
