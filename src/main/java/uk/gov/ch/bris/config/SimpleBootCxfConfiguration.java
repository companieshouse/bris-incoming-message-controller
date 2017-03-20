package uk.gov.ch.bris.config;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeInterface;
import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeService;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;

@Configuration
public class SimpleBootCxfConfiguration {

	public static final String SERVLET_MAPPING_URL_PATH = "/ch-bris-api";
    public static final String SERVICE_URL = "/DeliverySoapService_1.0";

    @Autowired
    private SpringBus springBus;

    @Bean
    public DeliveryEnvelopeInterface deliveryEnvelopeService() {
    	return new DeliveryEnvelopeServiceEndpoint();
    }

    @Bean
    public ServletRegistrationBean cxfServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new CXFServlet(), SERVLET_MAPPING_URL_PATH + "/*");
        // If necessary add custom Title to CXF´s ServiceList
        return servletRegistrationBean;
    }
    
    @Bean
    public Endpoint endpoint() {
    	EndpointImpl endpoint = new EndpointImpl(springBus, deliveryEnvelopeService());
        endpoint.setServiceName(deliveryEnvelopeServiceClient().getServiceName());
        endpoint.publish(SERVICE_URL);
        return endpoint;
    }

    @Bean
    public DeliveryEnvelopeService deliveryEnvelopeServiceClient() {
        DeliveryEnvelopeService deliveryEnvelopeService = null;
        URL baseUrl = DeliveryEnvelopeService.class.getClassLoader().getResource("Delivery-Envelope.wsdl");
        
        try {
             deliveryEnvelopeService = new DeliveryEnvelopeService(baseUrl,
                    new QName("http://eu.domibus.plugin/bris/wsdl/endpoint/delivery/envelope/1.0", "DeliveryEnvelopeService"));


        }catch (Exception e){
            e.printStackTrace();

        }
        return deliveryEnvelopeService;
    }

    

}