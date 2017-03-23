package uk.gov.ch.bris;
/*
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
*/
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"uk.gov.ch.bris"})
public class Application {

    public static void main(String[] args) {
        // needed for streaming, see https://java.net/jira/browse/SAAJ-31
        System.setProperty("saaj.use.mimepull", "true");
                
        SpringApplication.run(Application.class, args);
    }
    
}
