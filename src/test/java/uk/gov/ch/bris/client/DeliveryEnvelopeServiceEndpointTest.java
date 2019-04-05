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
import eu.europa.ec.bris.jaxb.br.led.update.request.v1_4.BRUpdateLEDRequest;
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
            String xmlMessage = IOUtils.toString(requestStream);
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
