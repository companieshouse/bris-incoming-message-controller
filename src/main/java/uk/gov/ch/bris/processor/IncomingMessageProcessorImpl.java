package uk.gov.ch.bris.processor;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.FaultDetail;
import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsResponse;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.v140.jaxb.br.connection.BRConnectivityRequest;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import eu.europa.ec.bris.v140.jaxb.br.led.BRUpdateLEDStatus;
import eu.europa.ec.bris.v140.jaxb.br.led.full.BRFullUpdateLEDAcknowledgment;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import org.apache.commons.io.IOUtils;
import org.bson.types.Binary;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;
import uk.gov.ch.bris.constants.ResourcePathConstants;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.domain.BrisMessageType;
import uk.gov.ch.bris.domain.ValidationError;
import uk.gov.ch.bris.producer.SenderImpl;
import uk.gov.ch.bris.service.BRISIncomingMessageService;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IncomingMessageProcessorImpl implements IncomingMessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingMessageProcessorImpl.class);

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FAILED = "FAILED";

    @Inject
    private BRISIncomingMessageService brisIncomingMessageService;

    @Value("${TEST_MODE}")
    private static int TEST_MODE;

    @Autowired
    private SenderImpl kafkaProducer;


    /**
     * Save incoming message to mongoDB
     * Send relevant messageId to kafka incoming topic
     * @param deliveryBody
     * @throws FaultResponse
     */
    public void processIncomingMessage(DeliveryBody deliveryBody) throws FaultResponse {
        BRISIncomingMessage message = saveIncomingMessage(deliveryBody);

        if (!kafkaProducer.sendMessage(message.getId())) {
            LOGGER.warn("Could not send message to kafka. Setting status to " + STATUS_FAILED + " for message with id " + message.getId());
            try {
                // Set status to FAILED in MongoDB so that the message will be processed manually
                message.setStatus(STATUS_FAILED);
                brisIncomingMessageService.save(message);
            } catch (Exception exc) {
                LOGGER.error("Exception caught updating status to FAILED for message with id" + message.getId());
            }
        }
    }


    /**
     * Save incoming message to mongoDB
     * @param deliveryBody
     * @return BRISIncomingMessage
     * @throws FaultResponse
     */
    private BRISIncomingMessage saveIncomingMessage(DeliveryBody deliveryBody) throws FaultResponse {
        BRISIncomingMessage brisIncomingMessage;

        try {
            String message = getXMLmessagefromDeliveryBody(deliveryBody);

            // validate xmlMessage with the schema
            BrisMessageType brisMessageType = validateSchema(message);

            //Incase of validation ERROR
            if (brisMessageType.getValidationXML() != null) {
                message = brisMessageType.getValidationXML();
            }

            // extract messageId from Message
            String messageId = brisMessageType.getMessageObjectType().getMessageHeader().getMessageID().getValue();
            String correlationId = brisMessageType.getMessageObjectType().getMessageHeader().getCorrelationID().getValue();

            // create brisIncomingMessage Object
            brisIncomingMessage = new BRISIncomingMessage(messageId, correlationId, message, STATUS_PENDING);

            // save brisIncomingMessage Object in Mongo DB
            brisIncomingMessage.setMessageType(brisMessageType.getClassName());
            brisIncomingMessage.setCreatedOn(getDateTime().toDateTimeISO());

            if (TEST_MODE == 1) {
                brisIncomingMessage = attachBinary(brisIncomingMessage, deliveryBody);
            }
            brisIncomingMessageService.save(brisIncomingMessage);

        } catch (Exception e) {
            LOGGER.error("Exception " + e, e);
            throw new FaultResponse("Exception" + e.getMessage(), new FaultDetail(), e);
        }

        return brisIncomingMessage;
    }

    /**
     * Attach Binary Data to BRIS Incoming Message
     * @param brisIncomingMessage
     * @param deliveryBody
     * @return brisIncomingMessage
     * @throws FaultResponse
     */
    private BRISIncomingMessage attachBinary(BRISIncomingMessage brisIncomingMessage, DeliveryBody deliveryBody) {


        if ((BRRetrieveDocumentResponse.class.getSimpleName().equals(brisIncomingMessage.getMessageType()))){

            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DataHandler dh =  deliveryBody.getAttachment().getValue();
                dh.writeTo(output);
                brisIncomingMessage.setData(new Binary(output.toByteArray()));
            } catch (IOException e) {
                LOGGER.error("IOException ... Unable to Extract binary data from DeliveryBody: "+e);
            } catch (Exception e) {
                LOGGER.error("Exception   ... Unable to Extract binary data from DeliveryBody: "+e);
            }

        }
        return brisIncomingMessage;
    }

    /**
     * Generate DateTime in ISO-8601 string format
     * @return DateTime
     * @throws FaultResponse
     */
    public DateTime getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dt = new Date();
        String strDt = sdf.format(dt);

        DateTimeFormatter parser = ISODateTimeFormat.dateTime();
        DateTime dateTimeResult = parser.parseDateTime(strDt);

        return dateTimeResult;
    }

    /**
     * Load JAXBContext
     * @return JAXBContext
     */
    @Bean
    public JAXBContext getJaxbContext() {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(MessageObjectType.class,ValidationError.class);
        } catch (JAXBException exception) {
            exception.printStackTrace();
        }

        return context;
    }

    /**
     * Validate Schema for the incoming XML Message
     * @param xmlMessage
     * @return brisMessageType
     * @throws FaultResponse
     */
    private BrisMessageType validateSchema(String xmlMessage) throws FaultResponse,JAXBException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        FaultDetail faultDetail = new FaultDetail();
        BrisMessageType brisMessageType=null;
        try {
            LOGGER.info("Validating schema for xml message " + xmlMessage);
            brisMessageType = getSchema(xmlMessage);

            Schema schema = factory.newSchema(brisMessageType.getUrl());
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlMessage)));

            return brisMessageType;
        } catch (SAXException e) {
            LOGGER.error("XSD Validation Error on: "+brisMessageType.getClassName());
            brisMessageType.setClassName(ValidationError.class.getSimpleName());
            brisMessageType.setValidationXML(getXMLValidationMessage(brisMessageType.getMessageObjectType()));
            return brisMessageType;
        } catch (IOException e) {
            e.printStackTrace();
            throw new FaultResponse("IO exception", faultDetail, e);
        }
    }

    /**
     *
     * @param xmlMessage
     * @return BrisMessageType
     */
    private BrisMessageType getSchema(String xmlMessage) throws FaultResponse {
        JAXBContext jaxbContext;
        MessageObjectType messageObjectType = null;
        try {
            jaxbContext = getJaxbContext();
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlMessage);
            messageObjectType = (MessageObjectType)jaxbUnmarshaller.unmarshal(reader);
            validateMessageID(messageObjectType);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return getXSDResource(messageObjectType);

    }

    /**
     * Validates for message id in message header
     * @param messageObjectType
     * @throws FaultResponse
     */

    private void validateMessageID(MessageObjectType messageObjectType) throws FaultResponse {

        if(messageObjectType.getMessageHeader().getMessageID().getValue()!=null){

            int messageLength=messageObjectType.getMessageHeader().getMessageID().getValue().length();
            if(messageLength < 1 || messageLength > 64 ){
                FaultDetail faultDetail = new FaultDetail();
                faultDetail.setResponseCode("GEN000");
                faultDetail.setMessage("Validation error: Error while processing messageID ");
                throw new FaultResponse("Parsing exception", faultDetail);
            }
        }
    }

    /**
     *
     * @param messageObjectType
     * @return brisMessageType
     */
    private BrisMessageType getXSDResource(MessageObjectType messageObjectType) {

        Class clazz = messageObjectType.getClass();
        Map<Class, URL> map = new HashMap<>();

        map.put(BRCompanyDetailsRequest.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.COMPANY_DETAILS_SCHEMA));
        map.put(BRBranchDisclosureReceptionNotification.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.BRANCH_DISCLOSURE_NOTIFICATION_SCHEMA));
        map.put(BRCrossBorderMergerReceptionNotification.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.CRS_BORDER_MERGER_NOTIFICATION_SCHEMA));
        map.put(BRRetrieveDocumentRequest.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.RETRIEVE_DOCUMENT_SCHEMA));
        map.put(BRConnectivityRequest.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.CONNECTION_REQ_SCHEMA));
        map.put(BRFullUpdateLEDAcknowledgment.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.FULL_UPDATE_LED_ACK_SCHEMA));
        map.put(BRUpdateLEDStatus.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.UPDATE_LED_STATUS_SCHEMA));
        map.put(BRCrossBorderMergerReceptionNotificationAcknowledgement.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.CROSS_BRDR_MERG_NOTIFICATION_RES_SCHEMA));
        map.put(BRBusinessError.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.BR_BUSINESS_ERR_SCHEMA));

        //TODO below are added for testing purpose
        map.put(BRCompanyDetailsResponse.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants. COMPANY_DETAILS_RESPONSE_SCHEMA ));
        map.put(BRRetrieveDocumentResponse.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants. RETRIEVE_DOCUMENT_RESPONSE_SCHEMA ));

        BrisMessageType brisMessageType = new BrisMessageType();
        brisMessageType.setUrl(map.get(clazz));
        brisMessageType.setClassName(clazz.getSimpleName());
        brisMessageType.setMessageObjectType(messageObjectType);

        return brisMessageType;
    }

    /**
     * Get XML message from DeliveryBody
     * @param deliveryBody
     * @return xmlMessage
     * @throws FaultResponse
     */
    private String getXMLmessagefromDeliveryBody(DeliveryBody deliveryBody) throws FaultResponse{
        //WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        String xmlMessage = "";
        FaultDetail faultDetail= new FaultDetail();
        DataHandler dataHandler = new DataHandler(deliveryBody.getMessageContent().getValue().getDataSource());

        try {
            xmlMessage = IOUtils.toString(dataHandler.getInputStream());
        } catch (IOException e) {
            faultDetail.setResponseCode("BR-TECH-ERR-0002");
            faultDetail.setMessage("IOException oocured while extracting business message"+e.getLocalizedMessage());
            throw new FaultResponse("IO exception",faultDetail,e);
        }catch (Exception e) {
            faultDetail.setResponseCode("BR-TECH-ERR-0001");
            faultDetail.setMessage("Exception oocured while extracting message: "+e.getLocalizedMessage());
            throw new FaultResponse("Exception oocured while extracting business message: ",faultDetail,e);
        }
        return  xmlMessage;
    }

    /**
     *  Prepares ValidationError xml string
     * @param messageObjectType
     * @return String - ValidationError xml string
     * @throws JAXBException
     */

    private String getXMLValidationMessage(MessageObjectType messageObjectType) throws JAXBException {
        ValidationError validationError = new ValidationError();
        validationError.setMessageHeader(messageObjectType.getMessageHeader());
        JAXBContext jaxbContext =getJaxbContext() ;
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(validationError, sw);
        return sw.toString();
    }
}
