package uk.gov.ch.bris.client;


import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.aggregate.MessageContentType;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsResponse;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ch.bris.endpoint.DeliveryEnvelopeServiceEndpoint;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;
import java.io.Reader;
import java.io.StringReader;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class DeliveryWsApplicationTests {
    @Autowired
    protected DeliveryEnvelopeServiceEndpoint deliveryEnvelopeServiceEndpoint = null;

    public final static String CORRELATION_ID = "COR-000123";
    public final static String MESSAGE_ID = UUID.randomUUID().toString();


    @Autowired
    protected Marshaller marshaller = null;

    @Test
    public void sendCompanyDetailsRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();

        BRCompanyDetailsRequest request = CompanyDetailsHelper.newInstance(
                CORRELATION_ID,
                MESSAGE_ID,
                "NF004188",
                "EW",
                "UK");
        Acknowledgement ack=null;
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
            //fileStream.close();
        }catch (FaultResponse faultResponse){

            faultResponse.printStackTrace();
        }catch (Exception  exception){
            exception.printStackTrace();

        }

        assertNotNull(ack);
        assertEquals(MESSAGE_ID, ack.getDeliveryMessageInfo().getMessageID());

    }



    @Test
    public void sendInvalidRequestMessage(){
        DeliveryBody body = new DeliveryBody();
        MessageContentType message = new MessageContentType();


        Acknowledgement ack=null;
        try {


            //set null message
            message.setValue(null);
            body.setMessageContent(message);

            // method under test
            DeliveryHeader deliveryHeader = new DeliveryHeader();
            DeliveryMessageInfoType deliveryMessageInfoType = new DeliveryMessageInfoType();
            deliveryMessageInfoType.setMessageID(MESSAGE_ID);
            deliveryMessageInfoType.setTimestamp(getXMLGregorianCalendarNow());
            deliveryHeader.setDeliveryMessageInfo(deliveryMessageInfoType);


            ack = deliveryEnvelopeServiceEndpoint.submit(deliveryHeader, body);



        }catch (FaultResponse faultResponse){

            faultResponse.printStackTrace();
        }catch (Exception  exception){
            exception.printStackTrace();

        }

        assertNull(ack);

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
                    BRCompanyDetailsRequest.class,
                    BRCompanyDetailsResponse.class,
                    BRBusinessError.class,
                    DeliveryBody.class,
                    Acknowledgement.class
            );
        } catch (JAXBException exception) {
            exception.printStackTrace();
        }
        return context;
    }


}
