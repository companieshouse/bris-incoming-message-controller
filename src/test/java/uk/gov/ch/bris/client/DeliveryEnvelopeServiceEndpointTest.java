package uk.gov.ch.bris.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.domibus.plugin.bris.jaxb.aggregate.AttachmentType;
import eu.domibus.plugin.bris.jaxb.aggregate.MessageContentType;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import eu.domibus.plugin.bris.jaxb.submission.SubmissionBody;
import eu.domibus.plugin.bris.jaxb.submission.SubmissionHeader;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.reception.request.v1_4.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.reception.response.v1_4.BRBranchDisclosureReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.request.v1_4.BRBranchDisclosureSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.response.v1_4.BRBranchDisclosureSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.company.details.request.v1_4.BRCompanyDetailsRequest;
import eu.europa.ec.bris.jaxb.br.company.details.response.v1_4.BRCompanyDetailsResponse;
import eu.europa.ec.bris.jaxb.br.connection.request.v1_4.BRConnectivityRequest;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.reception.request.v1_4.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.reception.response.v1_4.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.submission.request.v1_4.BRCrossBorderMergerSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.submission.response.v1_4.BRCrossBorderMergerSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.document.retrieval.request.v1_4.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.jaxb.br.document.retrieval.response.v1_4.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.jaxb.br.error.v1_4.BRBusinessError;
import eu.europa.ec.bris.jaxb.br.led.update.full.response.v1_4.BRFullUpdateLEDAcknowledgment;
import eu.europa.ec.bris.jaxb.br.led.update.request.v2_0.BRUpdateLEDRequest;
import eu.europa.ec.bris.jaxb.br.led.update.response.v1_4.BRUpdateLEDStatus;
import eu.europa.ec.bris.jaxb.br.subscription.request.v1_4.BRManageSubscriptionRequest;
import eu.europa.ec.bris.jaxb.br.subscription.response.v1_4.BRManageSubscriptionStatus;
import eu.europa.ec.digit.message.container.jaxb.v1_0.MessageContainer;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;
import uk.gov.ch.bris.processor.IncomingMessageProcessor;

@ExtendWith(MockitoExtension.class)
public class DeliveryEnvelopeServiceEndpointTest {

    public static String MESSAGE_ID = UUID.randomUUID().toString();
    public static String CORRELATION_ID = MESSAGE_ID; // "COR-000123";
    public final static String DOC_ID = "G8VtZ7UJymaKplxBHLB8XWYOlQHlemtIRIOV5CvTfqY";

    @Mock
    IncomingMessageProcessor messageProcessor;
    
    @InjectMocks
    protected DeliveryEnvelopeServiceEndpoint deliveryEnvelopeServiceEndpoint;
    
    protected static JAXBContext jaxbContext;

    @BeforeAll
    public static void setup() throws JAXBException {
        jaxbContext = jaxbContext();
    }
    
    @Test
    public void sendDocumentDetailsRequestMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRRetrieveDocumentRequest request = RetrieveDocumentDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "03977902", "EW", "UK", DOC_ID);

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendDocumentDetailsResponseMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRRetrieveDocumentResponse request = RetrieveDocumentResponseHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "03977902",
                "EW", "UK", DOC_ID);

        AttachmentType attachment = new AttachmentType();
        attachment.setFileName("filename");
        attachment.setReference("a1");

        DataHandler fileDataHandler = new DataHandler(new ByteArrayDataSource("get".getBytes(), "application/pdf"));
        attachment.setValue(fileDataHandler);
        body.setAttachment(attachment);

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendCompanyDetailsRequestMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRCompanyDetailsRequest request = CompanyDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "03977902", "EW",
                "UK");
        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }
    
    @Test
    public void sendCompanyDetailsRequestMessageWithNOCompanyReg() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);
        BRCompanyDetailsRequest request = CompanyDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "", "EW",
                "UK");
        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendMesssageWithoutMessageId() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(null);
        BRCompanyDetailsRequest request = CompanyDetailsHelper.newInstance(CORRELATION_ID, null, "0006400", "EW",
                "UK");
        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertNull(ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendConnectionDetailsRequestMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRConnectivityRequest request = ConnectionDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "03977902",
                "EW", "UK");

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendBranchDisclosureReceptionNotificationDetailsRequestMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRBranchDisclosureReceptionNotification request = BranchDisclosureReceptionNotificationDetailsHelper
                .newInstance(CORRELATION_ID, MESSAGE_ID, "03977902", "EW", "UK", DOC_ID);

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);

    }

    @Test
    public void sendBranchDisclosureSubmissionNotificationDetailsRequestMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRBranchDisclosureSubmissionNotification request = BranchDisclosureSubmissionNotificationDetailsHelper
                .newInstance(CORRELATION_ID, MESSAGE_ID, "03977902", "EW", "UK", DOC_ID);

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);

    }

    @Test
    public void sendFullUpdateLEDAcknowledgmentMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRFullUpdateLEDAcknowledgment request = FullUpdateLEDAckDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID,
                "03977902", "EW", "UK");

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendUpdateLEDStatusMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRUpdateLEDStatus request = UpdateLEDStatusHelper.newInstance(CORRELATION_ID, MESSAGE_ID,
                "03977902", "EW", "UK");

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    @Test
    public void sendBusinessErrorMessage() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        BRBusinessError request = BusinessErrorDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "03977902", "EW",
                "UK", "ERR_BR_0100");

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }
    
    @Test
    public void sendMessageContainer() throws Exception {
        DeliveryBody body = new DeliveryBody();
        DeliveryHeader header = createDeliveryHeader(MESSAGE_ID);

        MessageContainer request = MessageContainerHelper.newAddBRNotification(CORRELATION_ID, MESSAGE_ID, "new-register", "name", "ES");

        Acknowledgement ack = callDeliveryEnvelopeService(header, body, request);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
        assertNotNull(ack.getDeliveryMessageInfo().getTimestamp());
        verify(messageProcessor).processIncomingMessage(header, body);
    }

    private Acknowledgement callDeliveryEnvelopeService(DeliveryHeader header, DeliveryBody body, Object request) throws Exception {
        try (Reader requestStream = marshal(request).getReader()) {
            String xmlMessage = "<ns30:MessageContainer xmlns=\"http://ec.europa.eu/bris/v1_4/common/BasicComponents\" xmlns:ns2=\"http://ec.europa.eu/bris/v1_4/common/AggregateComponents\" xmlns:ns4=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureReceptionNotificationRequest\" xmlns:ns3=\"http://ec.europa.eu/bris/v1_4/br/AggregateComponents\" xmlns:ns6=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureSubmissionNotificationRequest\" xmlns:ns5=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureReceptionNotificationResponse\" xmlns:ns30=\"http://eu.europa.ec/digit/message/container/1_0\" xmlns:ns8=\"http://ec.europa.eu/bris/v1_4/br/BusinessError\" xmlns:ns7=\"http://ec.europa.eu/bris/v1_4/br/BranchDisclosureSubmissionNotificationResponse\" xmlns:ns13=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Document\" xmlns:ns9=\"http://ec.europa.eu/bris/v1_4/br/CompanyDetailsRequest\" xmlns:ns12=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/CompanyItem\" xmlns:ns11=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Addresses\" xmlns:ns10=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Company\" xmlns:ns17=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerReceptionNotificationResponse\" xmlns:ns16=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerReceptionNotificationRequest\" xmlns:ns15=\"http://ec.europa.eu/bris/v2_0/br/CompanyDetailsResponse\" xmlns:ns14=\"http://ec.europa.eu/bris/v1_5/common/AggregateComponents/Particular\" xmlns:ns19=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerSubmissionNotificationResponse\" xmlns:ns18=\"http://ec.europa.eu/bris/v1_4/br/CrossBorderMergerSubmissionNotificationRequest\" xmlns:xmime=\"http://www.w3.org/2005/05/xmlmime\" xmlns:ns20=\"http://ec.europa.eu/bris/v1_4/br/SubscriptionRequest\" xmlns:ns24=\"http://ec.europa.eu/bris/v1_4/br/UpdateLEDRequest\" xmlns:ns23=\"http://ec.europa.eu/bris/v1_4/br/RetrieveDocumentResponse\" xmlns:ns22=\"http://ec.europa.eu/bris/v1_4/br/RetrieveDocumentRequest\" xmlns:ns21=\"http://ec.europa.eu/bris/v1_4/br/SubscriptionResponse\" xmlns:ns28=\"http://eu.domibus.plugin/bris/common/aggregate/components/1.0\" xmlns:ns26=\"http://eu.domibus.plugin/bris/delivery/components/1.0\" xmlns:ns25=\"http://ec.europa.eu/bris/v1_4/br/UpdateLEDResponse\" xmlns:ns29=\"http://eu.domibus.plugin/bris/submission/components/1.0\"><ns30:ContainerHeader><ns30:AddressInfo><ns30:Sender><ns30:Code>EW</ns30:Code><ns30:CountryCode>UK</ns30:CountryCode></ns30:Sender></ns30:AddressInfo><ns30:MessageInfo><ns30:MessageID>65d60e6d-9aa1-4572-b57d-5e23c85de468</ns30:MessageID><ns30:CorrelationID>65d60e6d-9aa1-4572-b57d-5e23c85de468</ns30:CorrelationID></ns30:MessageInfo></ns30:ContainerHeader><ns30:ContainerBody><ns30:MessageContent>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI/Pjxuczk6QlItTm90aWZpY2F0aW9uIHhtbG5zPSJodHRwOi8vZWMuZXVyb3BhLmV1L2JyaXMvdjFfNC9jb21tb24vQmFzaWNDb21wb25lbnRzIiB4bWxuczpuczI9Imh0dHA6Ly9lYy5ldXJvcGEuZXUvYnJpcy92Ml8wL2JyL0dlbmVyaWNBY2tub3dsZWRnZW1lbnQiIHhtbG5zOm5zND0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YxXzUvY29tbW9uL0FnZ3JlZ2F0ZUNvbXBvbmVudHMvQWRkcmVzc2VzIiB4bWxuczpuczM9Imh0dHA6Ly9lYy5ldXJvcGEuZXUvYnJpcy92MV81L2NvbW1vbi9BZ2dyZWdhdGVDb21wb25lbnRzL0NvbXBhbnkiIHhtbG5zOm5zNj0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YxXzUvY29tbW9uL0FnZ3JlZ2F0ZUNvbXBvbmVudHMvRG9jdW1lbnQiIHhtbG5zOm5zNT0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YxXzUvY29tbW9uL0FnZ3JlZ2F0ZUNvbXBvbmVudHMvQ29tcGFueUl0ZW0iIHhtbG5zOm5zOD0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YyXzAvYnIvQ29tcGFueURldGFpbHNSZXNwb25zZSIgeG1sbnM6bnM3PSJodHRwOi8vZWMuZXVyb3BhLmV1L2JyaXMvdjFfNS9jb21tb24vQWdncmVnYXRlQ29tcG9uZW50cy9QYXJ0aWN1bGFyIiB4bWxuczpuczEzPSJodHRwOi8vZWMuZXVyb3BhLmV1L2JyaXMvdjFfNS9jb21tb24vQWdncmVnYXRlQ29tcG9uZW50cy9CdXNpbmVzc1JlZ2lzdGVyUmVmZXJlbmNlIiB4bWxuczpuczk9Imh0dHA6Ly9lYy5ldXJvcGEuZXUvYnJpcy92Ml8wL2JyL0dlbmVyaWNOb3RpZmljYXRpb24iIHhtbG5zOm5zMTI9Imh0dHA6Ly9lYy5ldXJvcGEuZXUvYnJpcy92Ml8wL2JyL0NoYW5nZUJ1c2luZXNzUmVnaXN0ZXJDb2RlTm90aWZpY2F0aW9uVGVtcGxhdGUiIHhtbG5zOm5zMTE9Imh0dHA6Ly9lYy5ldXJvcGEuZXUvYnJpcy92MV81L2NvbW1vbi9BZ2dyZWdhdGVDb21wb25lbnRzL0xlZ2FsRm9ybSIgeG1sbnM6bnMxMD0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YxXzUvY29tbW9uL0FnZ3JlZ2F0ZUNvbXBvbmVudHMvQnVzaW5lc3NSZWdpc3RlciIgeG1sbnM6bnMxND0iaHR0cDovL2VjLmV1cm9wYS5ldS9icmlzL3YyXzAvYnIvQ2hhbmdlQ29tcGFueUVVSUROb3RpZmljYXRpb25UZW1wbGF0ZSI+PG5zMTU6QWRkQnVzaW5lc3NSZWdpc3Rlck5vdGlmaWNhdGlvblRlbXBsYXRlIHhtbG5zOm5zMTU9Imh0dHA6Ly9lYy5ldXJvcGEuZXUvYnJpcy92Ml8wL2JyL0FkZEJ1c2luZXNzUmVnaXN0ZXJOb3RpZmljYXRpb25UZW1wbGF0ZSI+PE5vdGlmaWNhdGlvbkRhdGVUaW1lPjIwMTktMDQtMDhUMTU6MTg6MzkuNzMyKzAxOjAwPC9Ob3RpZmljYXRpb25EYXRlVGltZT48bnMxMDpCdXNpbmVzc1JlZ2lzdGVyPjxCdXNpbmVzc1JlZ2lzdGVyQ29kZT5Db21wYW5pZXMgSG91c2U8L0J1c2luZXNzUmVnaXN0ZXJDb2RlPjxCdXNpbmVzc1JlZ2lzdGVyQ291bnRyeT5VSzwvQnVzaW5lc3NSZWdpc3RlckNvdW50cnk+PC9uczEwOkJ1c2luZXNzUmVnaXN0ZXI+PC9uczE1OkFkZEJ1c2luZXNzUmVnaXN0ZXJOb3RpZmljYXRpb25UZW1wbGF0ZT48L25zOTpCUi1Ob3RpZmljYXRpb24+</ns30:MessageContent></ns30:ContainerBody></ns30:MessageContainer>";
            DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(xmlMessage, "text/plain; charset=UTF-8"));
            
            MessageContentType message = new MessageContentType();
            message.setValue(dataHandler);

            body.setMessageContent(message);
            
            // method under test
            return deliveryEnvelopeServiceEndpoint.submit(header, body);
        } 
    }
    
    private DeliveryHeader createDeliveryHeader(String messageId) {
        DeliveryHeader header = new DeliveryHeader();
        DeliveryMessageInfoType deliveryMessageInfoType = new DeliveryMessageInfoType();
        deliveryMessageInfoType.setMessageID(messageId);
        header.setDeliveryMessageInfo(deliveryMessageInfoType);
        return header;
    }

    private StreamSource marshal(Object message) throws JAXBException {
        StringBuilderWriter writer = new StringBuilderWriter();
        jaxbContext.createMarshaller().marshal(message, writer);
        return new StreamSource(new StringReader(writer.toString()));
    }

    private static JAXBContext jaxbContext() throws JAXBException {
        return JAXBContext.newInstance(BRBranchDisclosureReceptionNotification.class,
                BRBranchDisclosureReceptionNotificationAcknowledgement.class,
                BRBranchDisclosureSubmissionNotification.class,
                BRBranchDisclosureSubmissionNotificationAcknowledgement.class, BRBusinessError.class,
                BRConnectivityRequest.class, BRCompanyDetailsRequest.class, BRCompanyDetailsResponse.class,
                BRCrossBorderMergerReceptionNotification.class,
                BRCrossBorderMergerReceptionNotificationAcknowledgement.class,
                BRCrossBorderMergerSubmissionNotification.class,
                BRCrossBorderMergerSubmissionNotificationAcknowledgement.class, BRManageSubscriptionRequest.class,
                BRManageSubscriptionStatus.class, BRRetrieveDocumentRequest.class, BRRetrieveDocumentResponse.class,
                BRUpdateLEDRequest.class, BRUpdateLEDStatus.class, BRFullUpdateLEDAcknowledgment.class, Acknowledgement.class, DeliveryBody.class,
                SubmissionBody.class, SubmissionHeader.class, MessageContainer.class);
    }

}
