package uk.gov.ch.bris.client;

import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsResponse;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.StructuredLogger;

@Configuration
public class ClientConfiguration {
    
    /*---- Constants ---- */

    /* ---- Instance Variables ---- */
    private final static Logger log = LoggerFactory.getLogger();
            
    /* ---- Constructors ---- */

    /* ---- Configuration Beans ---- */

    @Bean
	public JAXBContext jaxbContext() {
        ((StructuredLogger) log).setNamespace("bris.incoming.controller");
        
	  	JAXBContext context = null;
	   	try {
	   		context = JAXBContext.newInstance(
	   			BRCompanyDetailsRequest.class,
	   			BRCompanyDetailsResponse.class,
                BRBusinessError.class,
	   			DeliveryBody.class,
                Acknowledgement.class
	   		);
	   	} catch (JAXBException exception) {
	   	    Map<String, Object> data = new HashMap<String, Object>();
            data.put("message", "JAXBException: Couldn't create JAXBContext");
            
            log.error(exception, data);
	   	}
	   	return context;
	}

    @Bean
    public Marshaller marshaller() throws JAXBException {
        return jaxbContext().createMarshaller();
    }

    @Bean
    public Unmarshaller unmarshaller() throws JAXBException {
        return jaxbContext().createUnmarshaller();
    }

    /* ---- Getters and Setters ---- */

}
