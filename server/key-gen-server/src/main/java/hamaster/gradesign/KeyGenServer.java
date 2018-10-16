package hamaster.gradesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAutoConfiguration
//@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "hamaster.gradesign.repo")
@ComponentScan(basePackages = {"hamaster.gradesign", "hamaster.gradesign.idmgmt", "hamaster.gradesign.key", "hamaster.gradesign.controller"})
@EntityScan(basePackages = "hamaster.gradesign.entity")
public class KeyGenServer {
    public static void main(String[] args) {
        SpringApplication.run(KeyGenServer.class, args);
    }
}
