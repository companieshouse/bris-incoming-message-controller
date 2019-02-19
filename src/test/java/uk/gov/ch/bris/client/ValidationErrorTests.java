package uk.gov.ch.bris.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.reception.request.v1_4.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.reception.response.v1_4.BRBranchDisclosureReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.request.v1_4.BRBranchDisclosureSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.response.v1_4.BRBranchDisclosureSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.company.details.request.v1_4.BRCompanyDetailsRequest;
import eu.europa.ec.bris.jaxb.br.company.details.response.v1_4.BRCompanyDetailsResponse;
import eu.europa.ec.bris.jaxb.br.components.aggregate.v1_4.MessageObjectType;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.reception.request.v1_4.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.reception.response.v1_4.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.submission.request.v1_4.BRCrossBorderMergerSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.submission.response.v1_4.BRCrossBorderMergerSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.document.retrieval.request.v1_4.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.jaxb.br.document.retrieval.response.v1_4.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.jaxb.br.error.v1_4.BRBusinessError;
import eu.europa.ec.bris.jaxb.br.led.update.request.v1_4.BRUpdateLEDRequest;
import eu.europa.ec.bris.jaxb.br.led.update.response.v1_4.BRUpdateLEDStatus;
import eu.europa.ec.bris.jaxb.br.subscription.request.v1_4.BRManageSubscriptionRequest;
import eu.europa.ec.bris.jaxb.br.subscription.response.v1_4.BRManageSubscriptionStatus;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class ValidationErrorTests {

    @Autowired
    protected DeliveryEnvelopeServiceEndpoint deliveryEnvelopeServiceEndpoint = null;

    public static String MESSAGE_ID = UUID.randomUUID().toString();
    public static String CORRELATION_ID = MESSAGE_ID; // "COR-000123";
    public final static String DOC_ID = "G8VtZ7UJymaKplxBHLB8XWYOlQHlemtIRIOV5CvTfqY";

    @Autowired
    protected Marshaller marshaller = null;



    @Test
    public void sendMessageWithNOCompanyReg() {
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        MessageObjectType request = CompanyDetailsHelper.newInstance(CORRELATION_ID, MESSAGE_ID, "", "EW",
                "UK");
        Acknowledgement ack = null;

        ack = callDeliveryEnvelopeService(body, message, request, ack);

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());
    }

    @Test
    public void sendMesssagewithoutMessageId() {
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();
        MESSAGE_ID = UUID.randomUUID().toString();
        CORRELATION_ID = MESSAGE_ID;
        MessageObjectType request = CompanyDetailsHelper.newInstance(CORRELATION_ID, null, "0006400", "EW",
                "UK");
        Acknowledgement ack = null;

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

            // method under test
            DeliveryHeader deliveryHeader = new DeliveryHeader();
            DeliveryMessageInfoType deliveryMessageInfoType = new DeliveryMessageInfoType();
            deliveryMessageInfoType.setMessageID(MESSAGE_ID);
            deliveryMessageInfoType.setTimestamp(getXMLGregorianCalendarNow());
            deliveryHeader.setDeliveryMessageInfo(deliveryMessageInfoType);

            ack = deliveryEnvelopeServiceEndpoint.submit(deliveryHeader, body);

            // cleanup
            requestStream.close();

        } catch (FaultResponse faultResponse) {
            faultResponse.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return ack;
    }

    /**
     *
     * @return
     */
    private XMLGregorianCalendar getXMLGregorianCalendarNow() {
        XMLGregorianCalendar now = null;
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
            context = JAXBContext.newInstance(BRBranchDisclosureReceptionNotification.class,
                    BRBranchDisclosureReceptionNotificationAcknowledgement.class,
                    BRBranchDisclosureSubmissionNotification.class,
                    BRBranchDisclosureSubmissionNotificationAcknowledgement.class, BRBusinessError.class,
                    BRCompanyDetailsRequest.class, BRCompanyDetailsResponse.class,
                    BRCrossBorderMergerReceptionNotification.class,
                    BRCrossBorderMergerReceptionNotificationAcknowledgement.class,
                    BRCrossBorderMergerSubmissionNotification.class,
                    BRCrossBorderMergerSubmissionNotificationAcknowledgement.class,
                    BRManageSubscriptionRequest.class, BRManageSubscriptionStatus.class,
                    BRRetrieveDocumentRequest.class, BRRetrieveDocumentResponse.class, BRUpdateLEDRequest.class,
                    BRUpdateLEDStatus.class, Acknowledgement.class, DeliveryBody.class, SubmissionBody.class,
                    SubmissionHeader.class);
        } catch (JAXBException exception) {
            exception.printStackTrace();
        }
        return context;
    }

}
