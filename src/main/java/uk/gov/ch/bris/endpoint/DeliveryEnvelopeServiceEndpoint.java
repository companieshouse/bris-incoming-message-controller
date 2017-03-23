package uk.gov.ch.bris.endpoint;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.GregorianCalendar;


import javax.activation.DataHandler;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import org.apache.cxf.helpers.IOUtils;

import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeInterface;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

public class DeliveryEnvelopeServiceEndpoint implements DeliveryEnvelopeInterface {

    private final Logger loger = LoggerFactory.getLogger(DeliveryEnvelopeServiceEndpoint.class);
    
    @Override
    public Acknowledgement submit(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse {
        System.out.println("MessageContent ... " + deliveryBody.getMessageContent());
        
        String xmlMessage = "";
        //MessageContentType message = new MessageContentType();
        //BRCompanyDetailsRequest br;
        
        DataHandler dataHandler = new DataHandler(deliveryBody.getMessageContent().getValue().getDataSource());
        try {
            xmlMessage = IOUtils.toString(dataHandler.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        loger.info("xmlMessage     ... " + xmlMessage);
        
        
        //System.out.println("ContentType    ... " + deliveryBody.getMessageContent().getContentType());
        //System.out.println("Attachment     ... " + deliveryBody.getAttachment());

        loger.info("@@@@@@@@@@@@@@@@@@");
        loger.info("Inside submit ... ");
        loger.info("@@@@@@@@@@@@@@@@@@");
        
        Acknowledgement ack = new Acknowledgement();
        DeliveryMessageInfoType messageInfo = new DeliveryMessageInfoType();
        
        
        messageInfo.setMessageID(deliveryHeader.getDeliveryMessageInfo().getMessageID());
        messageInfo.setTimestamp(getXMLGregorianCalendarNow());
        ack.setDeliveryMessageInfo(messageInfo);

        String strOK = this.convertXmlToObject(xmlMessage);
        return ack;
    }
    public String convertXmlToObject(String xmlMessage) {
        BRCompanyDetailsRequest brCompanyDetailsRequest = new BRCompanyDetailsRequest();

        JAXBContext jaxbContext;
        try {
            jaxbContext = getJaxbContext();
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBSource source = new JAXBSource(jaxbContext, BRCompanyDetailsRequest.class);
            SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            Schema schema = sf.newSchema(new File("/Users/rkumar/Downloads/A007_SoapCxfApp/src/main/resources/META-INF/xsd/bris/v140/br/BRX-CompanyDetailsRequest.xsd"));
            jaxbUnmarshaller.setSchema(schema);
            //jaxbUnmarshaller.setEventHandler(new BRISErrorHandler());




            StringReader reader = new StringReader(xmlMessage);
            Object obj = jaxbUnmarshaller.unmarshal(reader);



            //jaxbUnmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            if(obj instanceof BRCompanyDetailsRequest) {
                brCompanyDetailsRequest = (BRCompanyDetailsRequest) obj;
            }

            loger.info("Inside convertXmlToObject ... ");
            loger.info("brCompanyDetailsRequest   ... " + brCompanyDetailsRequest.getBusinessRegisterReference().getBusinessRegisterID().getValue());
            loger.info("brCompanyDetailsRequest   ... " + brCompanyDetailsRequest.getBusinessRegisterReference().getBusinessRegisterCountry().getValue());
            loger.info("END ßInside convertXmlToObject ... ");

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "ok";
    }
    
    public XMLGregorianCalendar getXMLGregorianCalendarNow() {
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            return now;
        } catch (DatatypeConfigurationException exception) {
            //throw new BRISProgramException("message.bris.utils.error.program.001", exception);
            return null;
        }
    }

    @Bean
    public JAXBContext getJaxbContext() {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(
                    MessageObjectType.class
            );
        } catch (JAXBException exception) {
            exception.printStackTrace();
        }

        return context;
    }
}
