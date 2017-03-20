package uk.gov.ch.bris.config;

import javax.xml.ws.Endpoint;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import plugin.domibus.eu.bris.wsdl.endpoint.delivery.envelope._1.DeliveryEnvelopeInterface;
import plugin.domibus.eu.bris.wsdl.endpoint.delivery.envelope._1.DeliveryEnvelopeService;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;

@Configuration
public class SimpleBootCxfConfiguration {

    public static final String SERVICE_URL = "/DeliverySoapService_1.0";

    @Autowired
    private SpringBus springBus;

    @Bean
    public DeliveryEnvelopeInterface deliveryEnvelopeService() {
        return new DeliveryEnvelopeServiceEndpoint();
    }
    
    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus, deliveryEnvelopeService());
        endpoint.setServiceName(deliveryEnvelopeServiceClient().getServiceName());
        endpoint.setWsdlLocation(deliveryEnvelopeServiceClient().getWSDLDocumentLocation().toString());
        endpoint.publish(SERVICE_URL);
        return endpoint;
    }

    @Bean
    public DeliveryEnvelopeService deliveryEnvelopeServiceClient() {
        return new DeliveryEnvelopeService();
    }

    /*
    @Bean
    public CustomFaultBuilder weatherFaultBuilder() {
        return new WeatherFaultBuilder();
    }
    */
}
