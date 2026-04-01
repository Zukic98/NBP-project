package ba.unsa.etf.suds.ba.unsa.etf.suds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ba.unsa.etf.suds") 
public class SudsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SudsApplication.class, args);
    }
}