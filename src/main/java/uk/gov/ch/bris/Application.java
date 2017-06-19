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

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.StructuredLogger;


@SpringBootApplication(scanBasePackages={"uk.gov.ch.bris"})
public class Application {
    
    private final static Logger log = LoggerFactory.getLogger();

	public static void main(String[] args) {
		// needed for streaming, see https://java.net/jira/browse/SAAJ-31
		System.setProperty("saaj.use.mimepull", "true");
		
		((StructuredLogger) log).setNamespace("bris.incoming.controller");
				
		SpringApplication.run(Application.class, args);
	}
	
}