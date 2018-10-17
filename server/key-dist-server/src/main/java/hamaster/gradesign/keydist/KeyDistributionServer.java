package hamaster.gradesign.keydist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "hamaster.gradesign.keydist.dao")
@ComponentScan(basePackages = {"hamaster.gradesign.keydist.service", "hamaster.gradesign.keydist.daemon",
        "hamaster.gradesign.keydist.controller", "hamaster.gradesign.keydist.mail",
        "hamaster.gradesign.keydist.client", "hamaster.gradesign.keydist.web"})
@EntityScan(basePackages = "hamaster.gradesign.keydist.entity")
public class KeyDistributionServer {
    public static void main(String[] args) {
        SpringApplication.run(KeyDistributionServer.class, args);
    }
}
