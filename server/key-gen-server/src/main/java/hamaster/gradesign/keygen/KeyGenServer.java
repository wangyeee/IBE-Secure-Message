package hamaster.gradesign.keygen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@EnableJpaRepositories(basePackages = "hamaster.gradesign.keygen.repo")
@ComponentScan(basePackages = {"hamaster.gradesign.keygen", "hamaster.gradesign.keygen.idmgmt",
        "hamaster.gradesign.keygen.key", "hamaster.gradesign.keygen.controller"})
@EntityScan(basePackages = "hamaster.gradesign.keygen.entity")
public class KeyGenServer {
    public static void main(String[] args) {
        SpringApplication.run(KeyGenServer.class, args);
    }
}
