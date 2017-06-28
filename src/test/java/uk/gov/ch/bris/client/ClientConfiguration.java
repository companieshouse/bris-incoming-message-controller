package uk.gov.ch.bris.client;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsResponse;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;

@Configuration
public class ClientConfiguration {

    /*---- Constants ---- */

    /* ---- Constructors ---- */

    /* ---- Configuration Beans ---- */

    @Bean
    public JAXBContext jaxbContext() {

        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(
                    BRCompanyDetailsRequest.class,
                    BRCompanyDetailsResponse.class,
                    BRBusinessError.class,
                    DeliveryBody.class,
                    Acknowledgement.class
                    );
        } catch (JAXBException exc) {
            throw new RuntimeException(exc);
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
