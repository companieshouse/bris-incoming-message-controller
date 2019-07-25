package uk.gov.ch.bris.processor;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import javax.xml.datatype.DatatypeFactory;

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
import eu.europa.ec.bris.jaxb.br.subscription.request.v1_4.BRManageSubscriptionRequest;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyEUIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.DateTimeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.ManageSubscriptionCodeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.ManageSubscriptionIDType;
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
                        eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.ObjectFactory.class, BRManageSubscriptionRequest.class);
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
    public void testProcessIncomingMessageContainerAddBusinessRegisterNotificationSuccessful() throws Exception {
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
    public void testProcessIncomingMessageContainerManageSubscriptionSuccessful() throws Exception {
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String businessRegisterId = "RMC";
        final String countryCode = "BE";
        CompanyEUIDType companyEUIDType = new CompanyEUIDType();
        companyEUIDType.setValue("FRIG.2010012341-00");
        CompanyEUIDType branchId = new CompanyEUIDType();
        branchId.setValue("FRIG.2010012341-00");
        ManageSubscriptionCodeType subscriptionCode = new ManageSubscriptionCodeType();
        subscriptionCode.setValue("ADD");
        ManageSubscriptionIDType subscriptionId = new ManageSubscriptionIDType();
        subscriptionId.setValue("002");
        DateTimeType subscriptionDateTime = new DateTimeType();
        subscriptionDateTime.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar());
        final String xmlMessage = marshal(CompanyDetailsHelper.newInstance(correlationId, messageId, companyEUIDType,
                branchId, businessRegisterId, subscriptionDateTime, countryCode, subscriptionId, subscriptionCode));
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
        assertEquals(BRManageSubscriptionRequest.class.getSimpleName(), message.getMessageType());

        verify(kafkaProducer).sendMessage(message.getId());
    }

    @Test
    public void testProcessIncomingMessageContainsOldMessageVersion() throws Exception {
        final String messageId = "f1da1193-2651-4218-a569-78a3997d5a01";
        final String correlationId = "5d3873305c4b5b14821018a3";
        final String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "  <BR-ManageSubscriptionStatus modelVersion=\"1.4.0\" xmlns=\"http://ec.europa.eu/bris/v1_4/br/SubscriptionResponse\" xmlns:p6=\"http://ec.europa.eu/bris/v1_4/br/AggregateComponents\" xmlns:bac=\"http://ec.europa.eu/bris/v1_4/common/AggregateComponents\" xmlns:bbc=\"http://ec.europa.eu/bris/v1_4/common/BasicComponents\" xmlns:ns40=\"http://eu.europa.ec/digit/message/container/1_0\" >\n" + 
                "        <p6:MessageHeader>\n" + 
                "            <bbc:CorrelationID>5d3873305c4b5b14821018a3</bbc:CorrelationID>\n" + 
                "            <bbc:MessageID>f1da1193-2651-4218-a569-78a3997d5a01</bbc:MessageID>\n" + 
                "            <bac:BusinessRegisterReference>\n" + 
                "                <bbc:BusinessRegisterID>EW</bbc:BusinessRegisterID>\n" + 
                "                <bbc:BusinessRegisterCountry>UK</bbc:BusinessRegisterCountry>\n" + 
                "            </bac:BusinessRegisterReference>\n" + 
                "            <bac:TestData>\n" + 
                "                <bbc:TestSessionID>f1da1193-2651-4218-a569-78a3997d5a01</bbc:TestSessionID>\n" + 
                "                <bbc:TestPackageID>TP-003</bbc:TestPackageID>\n" + 
                "                <bbc:TestCaseID>TC-IN-BR-SL-SBRN-001</bbc:TestCaseID>\n" + 
                "                <bbc:TestConditionID>TC-IN-BR-SL-SBRN-001</bbc:TestConditionID>\n" + 
                "                <bbc:TestExecutionID>f1da1193-2651-4218-a569-78a3997d5a01</bbc:TestExecutionID>\n" + 
                "            </bac:TestData>\n" + 
                "        </p6:MessageHeader>\n" + 
                "        <Status>\n" + 
                "                <bbc:ManageSubscriptionStatusCode>PROCESSED</bbc:ManageSubscriptionStatusCode>\n" + 
                "                <bbc:SubscriptionStatusCode>SUBSCRIBED</bbc:SubscriptionStatusCode>\n" + 
                "                <bbc:ManageSubscriptionID>1</bbc:ManageSubscriptionID>\n" + 
                "                <bbc:ManageSubscriptionProcessedDateTime>2019-07-24T17:03:15.048+02:00</bbc:ManageSubscriptionProcessedDateTime>\n" + 
                "                <bbc:CompanyEUID>FR1104.1234</bbc:CompanyEUID>\n" + 
                "        </Status>\n" + 
                "    </BR-ManageSubscriptionStatus>";
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertTrue(message.getMessage().contains("<errorCode>ERR_BR_5107</errorCode>"));
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
    public void testProcessIncomingMessageContainsInvalidXMLMessage() throws Exception {
        final String messageId = "M-0000337385";
        final String correlationId = "C-0000337385";
        final String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "    <MessageContainer xmlns=\"http://eu.europa.ec/digit/message/container/1_0\" xmlns:xmime=\"http://www.w3.org/2005/05/xmlmime\">\n"
                + "        <ContainerHeader>\n" 
                + "            <AddressInfo>\n" 
                + "                <Sender>\n"
                + "                    <Id>BRIS_EM_01_ACC_GW</Id>\n" 
                + "                </Sender>\n"
                + "                <Receiver>\n" 
                + "                    <Id>BRIS_UK_01_ACC_GW</Id>\n"
                + "                    <Code>EW</Code>\n" 
                + "                    <CountryCode>UK</CountryCode>\n"
                + "                </Receiver>\n" 
                + "            </AddressInfo>\n" 
                + "            <MessageInfo>\n"
                + "                <Timestamp>2019-07-12T10:47:37.883+02:00</Timestamp>\n"
                + "                <MessageID>M-0000337385</MessageID>\n"
                + "                <CorrelationID>C-0000337385</CorrelationID>\n" 
                + "</MessageInfo>\n" 
                + "        </ContainerHeader>\n"
                + "        <ContainerBody>\n" 
                + "            <MessageContent>PEJSLU1hbmFnZVN1YnNjcmlwdGlvblN0YXR1c0pBWEIKICAgIHhtbG5zPSJodHRwOi8vZWMuZXVyb3BhLmV1L2JyaXMvdjJfMC9ici9TdWJzY3JpcHRpb25SZXNwb25zZSIKICAgIHhtbG5zOmJiYz0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YxXzQvY29tbW9uL0Jhc2ljQ29tcG9uZW50cyI+CiAgICAgICAgICAgIDxTdGF0dXM+CiAgICAgICAgICAgICAgICA8YmJjOk1hbmFnZVN1YnNjcmlwdGlvblN0YXR1c0NvZGU+UFJPQ0VTU0VEPC9iYmM6TWFuYWdlU3Vic2NyaXB0aW9uU3RhdHVzQ29kZT4KICAgICAgICAgICAgICAgIDxiYmM6TWFuYWdlU3Vic2NyaXB0aW9uSUQ+MTwvYmJjOk1hbmFnZVN1YnNjcmlwdGlvbklEPgogICAgICAgICAgICAgICAgPGJiYzpNYW5hZ2VTdWJzY3JpcHRpb25Qcm9jZXNzZWREYXRlVGltZT4yMDE5LTA3LTEyVDEwOjQ3OjM3Ljg4MyswMjowMDwvYmJjOk1hbmFnZVN1YnNjcmlwdGlvblByb2Nlc3NlZERhdGVUaW1lPgogICAgICAgICAgICAgICAgPGJiYzpDb21wYW55RVVJRD5GUjExMDQuMTIzNDwvYmJjOkNvbXBhbnlFVUlEPgogICAgICAgICAgICA8L1N0YXR1cz4KPC9CUi1NYW5hZ2VTdWJzY3JpcHRpb25TdGF0dXNKQVhCPg==</MessageContent>\n" 
                + "        </ContainerBody>\n"
                + "    </MessageContainer>";
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertTrue(message.getMessage().contains("<errorCode>ERR_BR_5108</errorCode>"));
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
    public void testProcessIncomingMessageManageSubsciptionStatus() throws Exception {
        final String messageId = "720fb698-1c8e-450e-b9d4-d185350ae2f7";
        final String correlationId = "5d35cf585c4b5b14821018a1";
        final String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "    <MessageContainer xmlns=\"http://eu.europa.ec/digit/message/container/1_0\" xmlns:xmime=\"http://www.w3.org/2005/05/xmlmime\">\n" +
                "<ContainerHeader><AddressInfo><Sender><Id>BRIS_EM_01_ACC_GW</Id></Sender><Receiver><Id>BRIS_UK_01_ACC_GW</Id><Code>EW</Code><CountryCode>UK</CountryCode></Receiver>" +
                "</AddressInfo><MessageInfo><Timestamp>2019-07-22T16:59:38.356+02:00</Timestamp><MessageID>720fb698-1c8e-450e-b9d4-d185350ae2f7</MessageID><CorrelationID>5d35cf585c4b5b14821018a1</CorrelationID>" +
                "<TestData><TestPackageID>TP-004</TestPackageID><TestCaseID>TC-FN-BR-SL-003_02</TestCaseID><TestStepID>TC-FN-BR-SL-003_02</TestStepID><TestExecutionID>720fb698-1c8e-450e-b9d4-d185350ae2f7</TestExecutionID><TestSessionID>720fb698-1c8e-450e-b9d4-d185350ae2f7</TestSessionID></TestData></MessageInfo>" +
                "</ContainerHeader><ContainerBody><MessageContent>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxCUi1NYW5hZ2VTdWJzY3JpcHRpb25TdGF0dXMNCiAgICB4bWxucz0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YyXzAvYnIvU3Vic2NyaXB0aW9uUmVzcG9uc2UiDQoJeG1sbnM6YmJjPSJodHRwOi8vZWMuZXVyb3BhLmV1L2JyaXMvdjFfNC9jb21tb24vQmFzaWNDb21wb25lbnRzIj4NCg0KPCEtLSBUQy1GTi1CUi1TTC0wMDNfMDItLT4NCg0KDQoNCgkJCTxTdGF0dXM+DQoJCQkJPGJiYzpNYW5hZ2VTdWJzY3JpcHRpb25TdGF0dXNDb2RlPlBST0NFU1NFRDwvYmJjOk1hbmFnZVN1YnNjcmlwdGlvblN0YXR1c0NvZGU+DQoJCQkJPGJiYzpTdWJzY3JpcHRpb25TdGF0dXNWYWx1ZT5TVUJTQ1JJQkVEPC9iYmM6U3Vic2NyaXB0aW9uU3RhdHVzVmFsdWU+DQoNCgkJCQkJPGJiYzpNYW5hZ2VTdWJzY3JpcHRpb25JRD5YPC9iYmM6TWFuYWdlU3Vic2NyaXB0aW9uSUQ+DQoNCgkJCQk8YmJjOk1hbmFnZVN1YnNjcmlwdGlvblByb2Nlc3NlZERhdGVUaW1lPjIwMTktMDctMjJUMTY6NTk6MzguMzU2KzAyOjAwPC9iYmM6TWFuYWdlU3Vic2NyaXB0aW9uUHJvY2Vzc2VkRGF0ZVRpbWU+DQoJCQkJPGJiYzpDb21wYW55RVVJRD5GUjExMDQuMTIzNDwvYmJjOkNvbXBhbnlFVUlEPg0KCQkJPC9TdGF0dXM+DQoNCjwvQlItTWFuYWdlU3Vic2NyaXB0aW9uU3RhdHVzPg0K</MessageContent>"+ 
                "     </ContainerBody></MessageContainer>";
        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertEquals("PENDING", message.getStatus());
        assertEquals(deliveryHeader.getAddressInfo().getSender().getId(), message.getSender());
        assertEquals(deliveryHeader.getAddressInfo().getReceiver().getId(), message.getReceiver());
        assertNull(message.getData()); // No binary

        assertNotNull(message.getCreatedOn());


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

        DeliveryBody deliveryBody = createDeliveryBody(xmlMessage);

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);
        
        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertTrue(message.getMessage().contains("<errorCode>ERR_BR_5102</errorCode>"));
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

        when(kafkaProducer.sendMessage(Mockito.any())).thenReturn(true);

        processor.processIncomingMessage(deliveryHeader, deliveryBody);

        verify(brisIncomingMessageService).save(messageCaptor.capture());

        final BRISIncomingMessage message = messageCaptor.getValue();
        assertNotNull(message);

        assertEquals(messageId, message.getMessageId());
        assertEquals(correlationId, message.getCorrelationId());
        assertTrue(message.getMessage().contains("<errorCode>ERR_BR_5108</errorCode>"));
        assertEquals("PENDING", message.getStatus());
        assertEquals(deliveryHeader.getAddressInfo().getSender().getId(), message.getSender());
        assertEquals(deliveryHeader.getAddressInfo().getReceiver().getId(), message.getReceiver());
        assertNull(message.getData()); // No binary
        assertEquals(xmlMessage, message.getInvalidMessage());
        assertNotNull(message.getCreatedOn());
        assertEquals(ValidationError.class.getSimpleName(), message.getMessageType());

        verify(kafkaProducer).sendMessage(message.getId());
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
