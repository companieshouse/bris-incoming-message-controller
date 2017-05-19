package uk.gov.ch.bris.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Reader;
import java.io.StringReader;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.aggregate.MessageContentType;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import eu.domibus.plugin.bris.jaxb.submission.SubmissionBody;
import eu.domibus.plugin.bris.jaxb.submission.SubmissionHeader;
import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureSubmissionNotification;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsResponse;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.v140.jaxb.br.connection.BRConnectivityRequest;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import eu.europa.ec.bris.v140.jaxb.br.fault.BRFaultResponse;
import eu.europa.ec.bris.v140.jaxb.br.led.BRUpdateLEDRequest;
import eu.europa.ec.bris.v140.jaxb.br.led.BRUpdateLEDStatus;
import eu.europa.ec.bris.v140.jaxb.br.led.full.BRFullUpdateLEDAcknowledgment;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerSubmissionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.subscription.BRManageSubscriptionRequest;
import eu.europa.ec.bris.v140.jaxb.br.subscription.BRManageSubscriptionStatus;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class DeliveryWsApplicationTests {
    
    @Autowired
    protected DeliveryEnvelopeServiceEndpoint deliveryEnvelopeServiceEndpoint = null;

    public static String MESSAGE_ID = UUID.randomUUID().toString();
    public static String CORRELATION_ID = MESSAGE_ID; //"COR-000123";
    public final static String DOC_ID = "G8VtZ7UJymaKplxBHLB8XWYOlQHlemtIRIOV5CvTfqY";
    
    @Autowired
    protected Marshaller marshaller = null;
    
    @Test
    public void sendDocumentDetailsRequestMessage() {
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        
        MessageObjectType request = RetrieveDocumentDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "03977902",
                "EW",
                "UK",
                DOC_ID);
        
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        
    }
    
    @Test
    public void sendCompanyDetailsRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        //CORRELATION_ID = "51d38d70-0a84-4719-8dc2-e059113a1004";
        
        //"00006400", "03977902"
        MessageObjectType request = CompanyDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "03977902",
                "EW",
                "UK");
        Acknowledgement ack=null;
       
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
    }
    
    
    
    @Test
    public void sendInvalidRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        //CORRELATION_ID = "51d38d70-0a84-4719-8dc2-e059113a1004";
        
        //"00006400", "03977902"
        MessageObjectType request = CompanyDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "03977902aaa",
                "EW",
                "UK");
        Acknowledgement ack=null;
       
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());

    }
    
    @Test
    public void sendConnectionDetailsRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        
        //"00006400", "03977902"
        MessageObjectType request = ConnectionDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "03977902",
                "EW",
                "UK");
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        
    }
    
    //@Test
    public void sendBranchDisclosureReceptionNotificationDetailsRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        
        //"00006400", "03977902"
        MessageObjectType request = BranchDisclosureReceptionNotificationDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "03977902",
                "EW",
                "UK",
                DOC_ID);
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        
    }
    
    //@Test
    public void sendBranchDisclosureSubmissionNotificationDetailsRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        
        //"00006400", "03977902"
        MessageObjectType request = BranchDisclosureSubmissionNotificationDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "03977902",
                "EW",
                "UK",
                DOC_ID);
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        
    }

    //@Test
    public void sendFullUpdateLEDAcknowledgmentMessage() {
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
    
        MessageObjectType request = FullUpdateLEDAckDetailsHelper.newInstance(
            CORRELATION_ID,
            MESSAGE_ID,
            "03977902",
            "EW",
            "UK");
        
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
    }
    
    //@Test
    public void sendUpdateLEDStatusMessage() {
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
    
        MessageObjectType request = FullUpdateLEDAckDetailsHelper.newInstance(
            CORRELATION_ID,
            MESSAGE_ID,
            "03977902",
            "EW",
            "UK");
        
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
    }
    
    //@Test
    public void sendBusinessErrorMessage() {
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
    
        MessageObjectType request = BusinessErrorDetailsHelper.newInstance(
            CORRELATION_ID,
            MESSAGE_ID,
            "03977902",
            "EW",
            "UK",
            "ERR_BR_0100");
        
        Acknowledgement ack=null;
        
        ack = callDeliveryEnvelopeService(body, message, request, ack);
        
        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
    }
    
    private Acknowledgement callDeliveryEnvelopeService(DeliveryBody body, MessageContentType message,
            MessageObjectType request, Acknowledgement ack) {
        try {
            Reader requestStream = marshal(request).getReader();
            String xmlMessage = IOUtils.toString(requestStream);
            DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(xmlMessage, "text/plain; charset=UTF-8"));
            message.setValue(dataHandler);
            
            body.setMessageContent(message);
            
            // set attachment
            /* AttachmentType attachment = new AttachmentType();
            attachment.setFileName("filename");
            attachment.setReference("a1");
            File testFile = new File("test1.pdf");
            FileInputStream fileStream = new FileInputStream(testFile);
            DataHandler fileDataHandler = new DataHandler(new ByteArrayDataSource(fileStream, "application/pdf"));
            attachment.setValue(fileDataHandler);
            body.setAttachment(attachment);*/
            
            // method under test
            DeliveryHeader deliveryHeader = new DeliveryHeader();
            DeliveryMessageInfoType deliveryMessageInfoType = new DeliveryMessageInfoType();
            deliveryMessageInfoType.setMessageID(MESSAGE_ID);
            deliveryMessageInfoType.setTimestamp(getXMLGregorianCalendarNow());
            deliveryHeader.setDeliveryMessageInfo(deliveryMessageInfoType);
            
            ack = deliveryEnvelopeServiceEndpoint.submit(deliveryHeader, body);
            
            //cleanup
            requestStream.close();
            
        }catch (FaultResponse faultResponse) {
            faultResponse.printStackTrace();
        }catch (Exception  exception) {
            exception.printStackTrace();
        }
        return ack;
    }
    
    /**
     *
     * @return
     */
    private XMLGregorianCalendar getXMLGregorianCalendarNow() {
        XMLGregorianCalendar now=null;
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        } catch (DatatypeConfigurationException exception) {

            exception.printStackTrace();
        }
        return now;
    }

    private StreamSource marshal(Object message) throws JAXBException {
        StringBuilderWriter writer = new StringBuilderWriter();
        marshaller.marshal(message, writer);
        return new StreamSource(new StringReader(writer.toString()));
    }

    @Bean
    public JAXBContext jaxbContext() {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(                    
                    BRBranchDisclosureReceptionNotification.class,
                    BRBranchDisclosureReceptionNotificationAcknowledgement.class,
                    BRBranchDisclosureSubmissionNotification.class,
                    BRBranchDisclosureSubmissionNotificationAcknowledgement.class, 
                    BRBusinessError.class,
                    BRCompanyDetailsRequest.class, 
                    BRCompanyDetailsResponse.class,
                    BRCrossBorderMergerReceptionNotification.class,
                    BRCrossBorderMergerReceptionNotificationAcknowledgement.class,
                    BRCrossBorderMergerSubmissionNotification.class,
                    BRCrossBorderMergerSubmissionNotificationAcknowledgement.class, 
                    BRFaultResponse.class,
                    BRManageSubscriptionRequest.class, 
                    BRManageSubscriptionStatus.class,
                    BRRetrieveDocumentRequest.class, 
                    BRRetrieveDocumentResponse.class, 
                    BRUpdateLEDRequest.class,
                    BRUpdateLEDStatus.class, 
                    Acknowledgement.class, 
                    DeliveryBody.class, 
                    SubmissionBody.class,
                    SubmissionHeader.class
            );
        } catch (JAXBException exception) {
            exception.printStackTrace();
        }
        return context;
    }


}
