package uk.gov.ch.bris.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import uk.gov.ch.bris.constants.ServiceConstants;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;

@Configuration
public class SimpleBootCxfConfiguration {
    
    private Map<String, String> env = new HashMap<String, String>();
    
    @Autowired
    private SpringBus springBus;

    @Bean
    public DeliveryEnvelopeInterface deliveryEnvelopeService() {
        return new DeliveryEnvelopeServiceEndpoint();
    }
    
    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        ServletRegistrationBean<CXFServlet> servletRegistrationBean = new ServletRegistrationBean<>(new CXFServlet(), ServiceConstants.SERVLET_MAPPING_URL_PATH + "/*");
        // If necessary add custom Title to CXF´s ServiceList
        return servletRegistrationBean;
    }
    
    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus, deliveryEnvelopeService());
        endpoint.setServiceName(deliveryEnvelopeServiceClient().getServiceName());
        endpoint.publish(ServiceConstants.DELIVERY_SERVICE_URL + ServiceConstants.UNDERSCORE + getVersionFromEnvironment());
        return endpoint;
    }
    
    @Bean
    public DeliveryEnvelopeService deliveryEnvelopeServiceClient() {
        DeliveryEnvelopeService deliveryEnvelopeService = null;
        URL baseUrl = DeliveryEnvelopeService.class.getClassLoader().getResource("Delivery-Envelope.wsdl");
        
        try {
             deliveryEnvelopeService = new DeliveryEnvelopeService(baseUrl,
                    new QName("http://eu.domibus.plugin/bris/wsdl/endpoint/delivery/envelope/1.0", "DeliveryEnvelopeService"));


        } catch (Exception e){
            e.printStackTrace();

        }
        return deliveryEnvelopeService;
    }

    private String getVersionFromEnvironment() {
        env = System.getenv();
        String strVersion = env.entrySet().stream()
                .filter(env -> "VERSION".equals(env.getKey()))
                .map(env->env.getValue())
                .collect(Collectors.joining());
        
        if("".equals(strVersion)) {
            strVersion = "1.0";
        }
        
        return strVersion;
    }
    
}
