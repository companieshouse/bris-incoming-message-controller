package uk.gov.ch.bris.processor;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.aggregate.AddressInfoType;
import eu.domibus.plugin.bris.jaxb.aggregate.MessageContentType;
import eu.domibus.plugin.bris.jaxb.aggregate.ReceiverType;
import eu.domibus.plugin.bris.jaxb.aggregate.SenderType;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.europa.ec.bris.jaxb.br.company.details.request.v1_4.BRCompanyDetailsRequest;
import eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.AddBusinessRegisterNotificationTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.notification.v2_0.BRNotification;
import eu.europa.ec.digit.message.container.jaxb.v1_0.MessageContainer;
import uk.gov.ch.bris.client.CompanyDetailsHelper;
import uk.gov.ch.bris.client.MessageContainerHelper;
import uk.gov.ch.bris.config.IncomingMessageProcessorConfig;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.domain.ValidationError;
import uk.gov.ch.bris.producer.Sender;
import uk.gov.ch.bris.service.BRISIncomingMessageService;

@ExtendWith(MockitoExtension.class)
public class IncomingMessageProcessorImplTest {

    private static Map<Class<?>, URL> businessRegisterClassMap;
    private static JAXBContext jaxbContext;

    @Mock
    private BRISIncomingMessageService brisIncomingMessageService;

    @Mock
    private Sender kafkaProducer;

    private IncomingMessageProcessorImpl processor;

    @Captor
    ArgumentCaptor<BRISIncomingMessage> messageCaptor;


    private DeliveryHeader deliveryHeader;
    
    @BeforeAll
    public static void beforeAll() throws JAXBException {
        businessRegisterClassMap = new IncomingMessageProcessorConfig().getBusinessRegisterClassMap();
        
        jaxbContext = JAXBContext
                .newInstance(BRCompanyDetailsRequest.class, MessageContainer.class, BRNotification.class,
                        eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.ObjectFactory.class);
    }

    @BeforeEach
    public void beforeEach() {
        processor = new IncomingMessageProcessorImpl(businessRegisterClassMap);
        processor.setBrisIncomingMessageService(brisIncomingMessageService);
        processor.setKafkaProducer(kafkaProducer);

        deliveryHeader = createDeliveryHeader("SNDR", "RCVR");
    }

    @Test
    public void testProcessIncomingMessageObjectTypeSuccessful() throws Exception {
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String coNum = "00006400";
        final String registerId = "EW";
        final String country = "UK";
        final String xmlMessage = marshal(
                CompanyDetailsHelper.newInstance(correlationId, messageId, coNum, registerId, country));
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertEquals(xmlMessage, message.getMessage());
        assertEquals("PENDING", message.getStatus());
        assertEquals(deliveryHeader.getAddressInfo().getSender().getId(), message.getSender());
        assertEquals(deliveryHeader.getAddressInfo().getReceiver().getId(), message.getReceiver());
        assertNull(message.getData()); // No binary
        assertNull(message.getInvalidMessage());
        assertNotNull(message.getCreatedOn());
        assertEquals(BRCompanyDetailsRequest.class.getSimpleName(), message.getMessageType());

        verify(kafkaProducer).sendMessage(message.getId());
    }
    
    @Test
    public void testProcessIncomingMessageContainerSuccessful() throws Exception {
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String businessRegisterId = "29290";
        final String businessRegisterName = "name";
        final String countryCode = "ES";
        final String xmlMessage = marshal(MessageContainerHelper.newAddBRNotification(correlationId, messageId,
                businessRegisterId, businessRegisterName, countryCode));
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertEquals(xmlMessage, message.getMessage());
        assertEquals("PENDING", message.getStatus());
        assertEquals(deliveryHeader.getAddressInfo().getSender().getId(), message.getSender());
        assertEquals(deliveryHeader.getAddressInfo().getReceiver().getId(), message.getReceiver());
        assertNull(message.getData()); // No binary
        assertNull(message.getInvalidMessage());
        assertNotNull(message.getCreatedOn());
        assertEquals(AddBusinessRegisterNotificationTemplateType.class.getSimpleName(), message.getMessageType());

        verify(kafkaProducer).sendMessage(message.getId());
    }
    
    @Test
    public void testProcessIncomingMessageValidationError() throws Exception {
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String coNum = "?"; // Invalid company number
        final String registerId = "EW";
        final String country = "UK";
        final String xmlMessage = marshal(
                CompanyDetailsHelper.newInstance(correlationId, messageId, coNum, registerId, country));
        final String expectedMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<validationError xmlns:ns4=\"http://ec.europa.eu/bris/v1_4/common/AggregateComponents\" xmlns:ns3=\"http://ec.europa.eu/bris/v1_4/common/BasicComponents\" xmlns:ns6=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureReceptionNotificationRequest\" xmlns:ns31=\"http://ec.europa.eu/bris/v1_4/br/BusinessError\" xmlns:ns5=\"http://ec.europa.eu/bris/v1_4/br/AggregateComponents\" xmlns:ns30=\"http://eu.europa.ec/digit/message/container/1_0\" xmlns:ns8=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureSubmissionNotificationRequest\" xmlns:ns7=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureReceptionNotificationResponse\" xmlns:ns13=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/CompanyItem\" xmlns:ns9=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureSubmissionNotificationResponse\" xmlns:ns12=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Addresses\" xmlns:ns11=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Company\" xmlns:ns10=\"http://ec.europa.eu/bris/v1_4/br/CompanyDetailsRequest\" xmlns:ns17=\"http://ec.europa.eu/bris/v1_4/br/ConnectionRequest\" xmlns:ns16=\"http://ec.europa.eu/bris/v2_0/br/CompanyDetailsResponse\" xmlns:ns15=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Particular\" xmlns:ns14=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Document\" xmlns:ns19=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerReceptionNotificationRequest\" xmlns:ns18=\"http://ec.europa.eu/bris/v1_4/br/ConnectionResponse\" xmlns:xmime=\"http://www.w3.org/2005/05/xmlmime\" xmlns:ns20=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerReceptionNotificationResponse\" xmlns:ns24=\"http://ec.europa.eu/bris/v1_4/br/RetrieveDocumentResponse\" xmlns:ns23=\"http://ec.europa.eu/bris/v1_4/br/RetrieveDocumentRequest\" xmlns:ns22=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerSubmissionNotificationResponse\" xmlns:ns21=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerSubmissionNotificationRequest\" xmlns:ns28=\"http://ec.europa.eu/bris/v1_4/br/UpdateLEDResponse\" xmlns:ns27=\"http://ec.europa.eu/bris/v1_4/br/UpdateLEDRequest\" xmlns:ns26=\"http://ec.europa.eu/bris/v1_4/br/FullUpdateLEDResponse\" xmlns:ns25=\"http://ec.europa.eu/bris/v1_4/br/FullUpdateLEDRequest\" xmlns:ns29=\"http://ec.europa.eu/bris/v1_4/br/SubscriptionRequest\">\n" + 
                "    <header>\n" + 
                "        <businessRegisterCountry>"+country+"</businessRegisterCountry>\n" + 
                "        <businessRegisterId>"+registerId+"</businessRegisterId>\n" + 
                "        <correlationId>"+correlationId+"</correlationId>\n" + 
                "        <messageId>"+messageId+"</messageId>\n" + 
                "        <testData/>\n" + 
                "    </header>\n" + 
                "</validationError>\n";
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);
        
        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertEquals(expectedMessage, message.getMessage()); // validation xml
        assertEquals("PENDING", message.getStatus());
        assertEquals(deliveryHeader.getAddressInfo().getSender().getId(), message.getSender());
        assertEquals(deliveryHeader.getAddressInfo().getReceiver().getId(), message.getReceiver());
        assertNull(message.getData()); // No binary
        assertEquals(xmlMessage, message.getInvalidMessage());
        assertNotNull(message.getCreatedOn());
        assertEquals(ValidationError.class.getSimpleName(), message.getMessageType());

        verify(kafkaProducer).sendMessage(message.getId());
    }
    
    @Test
    public void testProcessIncomingMessageKafkaError() throws Exception {
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String coNum = "00006400";
        final String registerId = "EW";
        final String country = "UK";
        final String xmlMessage = marshal(
                CompanyDetailsHelper.newInstance(correlationId, messageId, coNum, registerId, country));
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(false);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService, Mockito.times(2)).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getAllValues().get(1); // Only interested in second call
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertEquals(xmlMessage, message.getMessage());
        assertEquals("FAILED", message.getStatus());
        assertEquals(deliveryHeader.getAddressInfo().getSender().getId(), message.getSender());
        assertEquals(deliveryHeader.getAddressInfo().getReceiver().getId(), message.getReceiver());
        assertNull(message.getData()); // No binary
        assertNull(message.getInvalidMessage());
        assertNotNull(message.getCreatedOn());
        assertEquals(BRCompanyDetailsRequest.class.getSimpleName(), message.getMessageType());

        verify(kafkaProducer).sendMessage(message.getId());
    }
    
    @Test
    public void testProcessIncomingMessageFaultResponse() throws Exception{
        final String xmlMessage =  "Invalid message";
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        assertThrows(FaultResponse.class, () -> processor.processIncomingMessage(deliveryHeader, deliveryBody));

        verify(kafkaProducer, Mockito.never()).sendMessage(Mockito.any());
        verify(brisIncomingMessageService, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testProcessIncomingMessageContainerEmptyContent() throws Exception{
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String businessRegisterId = "29290";
        final String businessRegisterName = "name";
        final String countryCode = "ES";
        MessageContainer messageContainer = MessageContainerHelper.newAddBRNotification(correlationId, messageId,
                businessRegisterId, businessRegisterName, countryCode);
        messageContainer.getContainerBody().setMessageContent(null);
        final String xmlMessage = marshal(messageContainer);
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        FaultResponse fault = assertThrows(FaultResponse.class, () -> processor.processIncomingMessage(deliveryHeader, deliveryBody));

        assertNotNull(fault.getFaultInfo());
        assertEquals("ERR_BR_5108",fault.getFaultInfo().getResponseCode());
        assertEquals("Message Container failed validation.",fault.getFaultInfo().getMessage());
        
        verify(kafkaProducer, Mockito.never()).sendMessage(Mockito.any());
        verify(brisIncomingMessageService, Mockito.never()).save(Mockito.any());
    }

    private static DeliveryHeader createDeliveryHeader(String senderId, String receiverId) {
        SenderType sender = new SenderType();
        sender.setId(senderId);
        ReceiverType receiver = new ReceiverType();
        receiver.setId(receiverId);

        AddressInfoType addressInfo = new AddressInfoType();
        addressInfo.setReceiver(receiver);
        addressInfo.setSender(sender);

        DeliveryHeader header = new DeliveryHeader();
        header.setAddressInfo(addressInfo);
        return header;
    }

    private static DeliveryBody createDeliveryBody(String xmlMessage) throws IOException {
        MessageContentType content = new MessageContentType();
        content.setValue(new DataHandler(new ByteArrayDataSource(xmlMessage, "text/plain; charset=UTF-8")));

        DeliveryBody deliveryBody = new DeliveryBody();
        deliveryBody.setMessageContent(content);
        return deliveryBody;
    }
    
    private static String marshal(Object o) throws JAXBException {
        StringWriter writer = new StringWriter();
        jaxbContext.createMarshaller().marshal(o, writer);
        return writer.toString();
    }
}
