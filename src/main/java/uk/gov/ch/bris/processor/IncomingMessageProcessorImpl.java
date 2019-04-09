package uk.gov.ch.bris.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.util.XMLCatalogResolver;
import org.bson.types.Binary;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.FaultDetail;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.reception.request.v1_4.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.reception.response.v1_4.BRBranchDisclosureReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.request.v1_4.BRBranchDisclosureSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.response.v1_4.BRBranchDisclosureSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.company.details.request.v1_4.BRCompanyDetailsRequest;
import eu.europa.ec.bris.jaxb.br.company.details.response.v2_0.BRCompanyDetailsResponse;
import eu.europa.ec.bris.jaxb.br.components.aggregate.v1_4.MessageObjectType;
import eu.europa.ec.bris.jaxb.br.connection.request.v1_4.BRConnectivityRequest;
import eu.europa.ec.bris.jaxb.br.connection.response.v1_4.BRConnectivityResponse;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.reception.request.v1_4.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.reception.response.v1_4.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.submission.request.v1_4.BRCrossBorderMergerSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.crossborder.merger.notification.submission.response.v1_4.BRCrossBorderMergerSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.jaxb.br.document.retrieval.request.v1_4.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.jaxb.br.document.retrieval.response.v1_4.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.jaxb.br.error.v1_4.BRBusinessError;
import eu.europa.ec.bris.jaxb.br.generic.notification.v2_0.BRNotification;
import eu.europa.ec.bris.jaxb.br.led.update.full.request.v1_4.BRFullUpdateLEDRequest;
import eu.europa.ec.bris.jaxb.br.led.update.full.response.v1_4.BRFullUpdateLEDAcknowledgment;
import eu.europa.ec.bris.jaxb.br.led.update.request.v1_4.BRUpdateLEDRequest;
import eu.europa.ec.bris.jaxb.br.led.update.response.v1_4.BRUpdateLEDStatus;
import eu.europa.ec.bris.jaxb.br.subscription.request.v1_4.BRManageSubscriptionRequest;
import eu.europa.ec.bris.jaxb.br.subscription.response.v1_4.BRManageSubscriptionStatus;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.TestDataType;
import eu.europa.ec.digit.message.container.jaxb.v1_0.MessageContainer;
import eu.europa.ec.digit.message.container.jaxb.v1_0.MessageInfo;
import uk.gov.ch.bris.constants.MongoStatus;
import uk.gov.ch.bris.constants.ServiceConstants;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.domain.BrisMessageHeaderType;
import uk.gov.ch.bris.domain.BrisMessageType;
import uk.gov.ch.bris.domain.ValidationError;
import uk.gov.ch.bris.error.ErrorCode;
import uk.gov.ch.bris.producer.Sender;
import uk.gov.ch.bris.service.BRISIncomingMessageService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class IncomingMessageProcessorImpl implements IncomingMessageProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceConstants.LOGGER_SERVICE_NAME);
    
    private static final String UNEXPECTED_OBJECT = "Unexpected object found";

    @Inject
    private BRISIncomingMessageService brisIncomingMessageService;

    @Value("${TEST_MODE}")
    private static int TEST_MODE;

    @Inject
    private Sender kafkaProducer;

    private Map<Class<?>, URL> businessRegisterClassMap;

    public IncomingMessageProcessorImpl(Map<Class<?>, URL> businessRegisterClassMap) {
        this.businessRegisterClassMap = businessRegisterClassMap;
    }

    /**
     * Save incoming message to mongoDB
     * Send relevant messageId to kafka incoming topic
     * @param deliveryHeader
     * @param deliveryBody
     * @throws FaultResponse
     */
    @Override
    public void processIncomingMessage(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse {

        BRISIncomingMessage message = saveIncomingMessage(deliveryHeader, deliveryBody);

        if (!kafkaProducer.sendMessage(message.getId())) {
            LOGGER.debug("Could not send message to kafka. Setting status to " + MongoStatus.FAILED + " for message with id " + message.getId());

            try {
                // Set status to FAILED in MongoDB so that the message will be processed manually
                message.setStatus(MongoStatus.FAILED);
                brisIncomingMessageService.save(message);
            } catch (Exception exc) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("id", message.getId());
                data.put("message", "Exception caught updating status to FAILED for message with id" + message.getId());

                LOGGER.error(exc, data);
            }
        }
    }


    /**
     * Save incoming message to mongoDB
     * @param deliveryHeader
     * @param deliveryBody
     * @return BRISIncomingMessage
     * @throws FaultResponse
     */
    private BRISIncomingMessage saveIncomingMessage(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse {

        BRISIncomingMessage brisIncomingMessage;

        try {
            String message = getXMLmessagefromDeliveryBody(deliveryBody);

            // validate xmlMessage with the schema
            BrisMessageType brisMessageType = validateSchema(message);

            String invalidMessage = null;
            //Incase of validation ERROR
            if (brisMessageType.getValidationXML() != null) {
                invalidMessage = message; // keep track of original message if its invalid so we can still store in mongodb
                message = brisMessageType.getValidationXML();
            }

            // extract messageId from Message
            String messageId = brisMessageType.getMessageHeader().getMessageId();
            String correlationId = brisMessageType.getMessageHeader().getCorrelationId();

            // create brisIncomingMessage Object
            brisIncomingMessage = new BRISIncomingMessage(messageId, correlationId, message, MongoStatus.PENDING);
            
            brisIncomingMessage.setSender(deliveryHeader.getAddressInfo().getSender().getId());
            brisIncomingMessage.setReceiver(deliveryHeader.getAddressInfo().getReceiver().getId());

            // keep a record of the invalid xml in mongodb if we have a validation error
            if (invalidMessage != null) {
                Map<String, Object> data = new HashMap<String, Object>();
                LOGGER.debug("Validation error occurred, storing original message as invalid_message field for message with messageId=" + messageId, data);
                brisIncomingMessage.setInvalidMessage(invalidMessage);
            }

            // save brisIncomingMessage Object in Mongo DB
            brisIncomingMessage.setMessageType(brisMessageType.getClassName());
            brisIncomingMessage.setCreatedOn(getDateTime().toDateTimeISO());

            if (TEST_MODE == 1) {
                brisIncomingMessage = attachBinary(brisIncomingMessage, deliveryBody);
            }
            brisIncomingMessageService.save(brisIncomingMessage);

        } catch (FaultResponse e) {
            throw e;
        } catch (Exception e) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("message", "Exception : Sending FaultResponse");

            LOGGER.error(e, data);
            throw new FaultResponse("Exception" + e.getMessage(), new FaultDetail(), e);
        }

        return brisIncomingMessage;
    }

    /**
     * Attach Binary Data to BRIS Incoming Message
     * @param brisIncomingMessage
     * @param deliveryBody
     * @return brisIncomingMessage
     */
    private BRISIncomingMessage attachBinary(BRISIncomingMessage brisIncomingMessage, DeliveryBody deliveryBody) {

        if ((BRRetrieveDocumentResponse.class.getSimpleName().equals(brisIncomingMessage.getMessageType()))){

            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DataHandler dh =  deliveryBody.getAttachment().getValue();
                dh.writeTo(output);
                brisIncomingMessage.setData(new Binary(output.toByteArray()));
            } catch (IOException e) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("message", "IOException ... Unable to Extract binary data from DeliveryBody");

                LOGGER.error(e, data);
            } catch (Exception e) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("message", "Exception   ... Unable to Extract binary data from DeliveryBody");

                LOGGER.error(e, data);
            }
        }
        return brisIncomingMessage;
    }

    /**
     * Generate DateTime in ISO-8601 string format
     * @return DateTime
     */
    protected DateTime getDateTime() {
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
            context = JAXBContext.newInstance(MessageContainer.class, BRBranchDisclosureReceptionNotification.class,
                    BRBranchDisclosureReceptionNotificationAcknowledgement.class,
                    BRBranchDisclosureSubmissionNotification.class,
                    BRBranchDisclosureSubmissionNotificationAcknowledgement.class, BRCompanyDetailsRequest.class,
                    BRCompanyDetailsResponse.class, BRConnectivityRequest.class, BRConnectivityResponse.class,
                    BRCrossBorderMergerReceptionNotification.class,
                    BRCrossBorderMergerReceptionNotificationAcknowledgement.class,
                    BRCrossBorderMergerSubmissionNotification.class,
                    BRCrossBorderMergerSubmissionNotificationAcknowledgement.class, BRRetrieveDocumentRequest.class,
                    BRRetrieveDocumentResponse.class, BRFullUpdateLEDRequest.class, BRFullUpdateLEDAcknowledgment.class,
                    BRUpdateLEDRequest.class, BRUpdateLEDStatus.class, BRManageSubscriptionRequest.class,
                    BRManageSubscriptionStatus.class, ValidationError.class, BRBusinessError.class);
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
     * @throws JAXBException
     */
    private BrisMessageType validateSchema(String xmlMessage) throws FaultResponse,JAXBException {
        URL catalogURL = getClass().getResource("/catalog/bris-uri-catalog.xml");
        XMLCatalogResolver resolver = new XMLCatalogResolver(new String[] {catalogURL.toString()});
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(resolver);
        
        BrisMessageType brisMessageType = null;

        try {
            LOGGER.debug("Validating schema for xml message " + xmlMessage);
            brisMessageType = getSchema(xmlMessage);

            Schema schema = factory.newSchema(brisMessageType.getUrl());
            Validator validator = schema.newValidator();
            
            String messageToValidate = xmlMessage;
            if (brisMessageType.getContentString() != null) {
                // When it the message comes in a MessageContainer, we need to validate its content
                messageToValidate = brisMessageType.getContentString();
            }
            validator.validate(new StreamSource(new StringReader(messageToValidate)));
        } catch (SAXException e) {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "XSD Validation Error caught validating schema brisMessageType=" + brisMessageType);

            LOGGER.error(e, data);

            brisMessageType.setClassName(ValidationError.class.getSimpleName());
            brisMessageType.setValidationXML(getXMLValidationMessage(brisMessageType.getMessageHeader()));

        } catch (JAXBException e) {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "JAXBException caught validating schema. FaultResponse will be thrown");

            LOGGER.error(e, data);
            throw new FaultResponse("JAXBException", new FaultDetail(), e);
        } catch (IOException e) {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "IOException caught validating schema. FaultResponse will be thrown");

            LOGGER.error(e, data);
            throw new FaultResponse("IO exception", new FaultDetail(), e);
        }

        return brisMessageType;
    }

    /**
     *
     * @param xmlMessage
     * @return BrisMessageType
     * @throws FaultResponse
     * @throws JAXBException
     */
    private BrisMessageType getSchema(String xmlMessage) throws FaultResponse, JAXBException {
        StringReader reader = new StringReader(xmlMessage);
        Object messageObject = getJaxbContext().createUnmarshaller().unmarshal(reader);

        if (messageObject instanceof MessageContainer) {
            return validateCreateBrisMessageType((MessageContainer) messageObject);
        }

        if (messageObject instanceof MessageObjectType) {
            return validateCreateBrisMessageType((MessageObjectType) messageObject);
        }

        final String errorMessage = "Error unmarshalling xml message. Unexpected object found "
                + messageObject.getClass().getName();
        Map<String, Object> data = new HashMap<>();
        data.put("message", errorMessage + ". FaultResponse will be thrown");

        LOGGER.error(UNEXPECTED_OBJECT, data);
        throw new FaultResponse(UNEXPECTED_OBJECT, new FaultDetail());
    }
    
    

    /**
     * Validates for message id in message header
     * @param messageId
     * @throws FaultResponse
     */

    private void validateMessageID(String messageId) throws FaultResponse {

        if(messageId!=null){

            int messageLength=messageId.length();
            if(messageLength < 1 || messageLength > 64 ){
                LOGGER.debug("Invalid messageLength for messageId. messageLength=" + messageLength + ", messageId=" + messageId);
                FaultDetail faultDetail = new FaultDetail();
                faultDetail.setResponseCode("GEN000");
                faultDetail.setMessage("Validation error: Error while processing messageID ");
                throw new FaultResponse("Parsing exception", faultDetail);
            }
        }
    }
    
    /**
     * Validate message and create BrisMessageType for v1.4 messages
     * @param messageObject
     * @return BrisMessageType
     * @throws FaultResponse 
     */
    private BrisMessageType validateCreateBrisMessageType(MessageObjectType messageObject) throws FaultResponse {
        BrisMessageHeaderType header = createBrisMessageHeaderType(messageObject);
        validateMessageID(header.getMessageId());
        
        BrisMessageType brisMessageType = createBrisMessageType(messageObject);
        brisMessageType.setMessageHeader(header);
        return brisMessageType;
    }
    
    /**
     * Validate message and create BrisMessageType for v2.0 messages
     * @param messageContainer
     * @return BrisMessageType
     * @throws FaultResponse 
     */
    private BrisMessageType validateCreateBrisMessageType(MessageContainer messageContainer) throws FaultResponse {
        BrisMessageHeaderType header = createBrisMessageHeaderType(messageContainer);
        validateMessageID(header.getMessageId());
        
        Object messageObject;
        Object messageContent;
        String xmlMessage;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            messageContainer.getContainerBody().getMessageContent().getValue().writeTo(output);
            xmlMessage = new String(output.toByteArray(), StandardCharsets.UTF_8);
            JAXBContext jaxbContext = JAXBContext.newInstance(BRNotification.class,
                    eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.ObjectFactory.class,
                    eu.europa.ec.bris.jaxb.br.generic.notification.template.br.removal.v2_0.ObjectFactory.class,
                    BRCompanyDetailsResponse.class);
            messageContent = jaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlMessage));
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.ERR_BR_5108;
            FaultDetail faultDetail = new FaultDetail();
            faultDetail.setResponseCode(errorCode.name());
            faultDetail.setMessage(errorCode.getDescription());
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Error reading MessageContent");
            LOGGER.error(e, data);
            
            throw new FaultResponse(UNEXPECTED_OBJECT, faultDetail, e);
        }
        
        if (messageContent instanceof BRNotification) {
            // Use the template for notification objects
            BRNotification notification = (BRNotification) messageContent;
            messageObject = notification.getNotificationTemplate().getValue();
        } else {
            messageObject = messageContent;
        }
        
        BrisMessageType brisMessageType = createBrisMessageType(messageObject);
        brisMessageType.setMessageHeader(header);
        brisMessageType.setContentString(xmlMessage);
        return brisMessageType;
    }

    private BrisMessageType createBrisMessageType(Object messageObject) {
        Class<?> clazz = messageObject.getClass();
        BrisMessageType brisMessageType = new BrisMessageType();
        brisMessageType.setUrl(businessRegisterClassMap.get(clazz));
        brisMessageType.setClassName(clazz.getSimpleName());
        return brisMessageType;
    }
    
    private BrisMessageHeaderType createBrisMessageHeaderType(MessageContainer messageContainer) {
        BrisMessageHeaderType header = new BrisMessageHeaderType();

        header.setMessageId(messageContainer.getContainerHeader().getMessageInfo().getMessageID());
        header.setCorrelationId(messageContainer.getContainerHeader().getMessageInfo().getCorrelationID());
        header.setBusinessRegisterId(messageContainer.getContainerHeader().getAddressInfo().getSender().getCode());
        header.setBusinessRegisterCountry(
                messageContainer.getContainerHeader().getAddressInfo().getSender().getCountryCode());
        
        MessageInfo.TestData testData = messageContainer.getContainerHeader().getMessageInfo().getTestData();
        if (testData != null) {
            header.getTestData().setCaseId(testData.getTestCaseID());
            header.getTestData().setConditionId(testData.getTestStepID());
            header.getTestData().setExecutionId(testData.getTestExecutionID());
            header.getTestData().setPackageId(testData.getTestPackageID());
            header.getTestData().setSessionId(testData.getTestSessionID());
        }
        
        return header;
    }
    
    private BrisMessageHeaderType createBrisMessageHeaderType(MessageObjectType messageObjectType) {
        BrisMessageHeaderType header = new BrisMessageHeaderType();
        header.setMessageId(messageObjectType.getMessageHeader().getMessageID().getValue());
        header.setCorrelationId(messageObjectType.getMessageHeader().getCorrelationID().getValue());
        header.setBusinessRegisterId(messageObjectType.getMessageHeader().getBusinessRegisterReference().getBusinessRegisterID().getValue());
        header.setBusinessRegisterCountry(messageObjectType.getMessageHeader().getBusinessRegisterReference().getBusinessRegisterCountry().getValue());
        
        TestDataType testData = messageObjectType.getMessageHeader().getTestData();
        if (testData != null) {
            header.getTestData().setCaseId(testData.getTestCaseID().getValue());
            header.getTestData().setConditionId(testData.getTestConditionID().getValue());
            header.getTestData().setExecutionId(testData.getTestExecutionID().getValue());
            header.getTestData().setPackageId(testData.getTestPackageID().getValue());
            header.getTestData().setSessionId(testData.getTestSessionID().getValue());
        }
        
        return header;
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
            xmlMessage = IOUtils.toString(dataHandler.getInputStream(), CharEncoding.UTF_8);
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
     * @param messageHeader
     * @return String - ValidationError xml string
     * @throws JAXBException
     */
    private String getXMLValidationMessage(BrisMessageHeaderType messageHeader) throws JAXBException {
        ValidationError validationError = new ValidationError();
        validationError.setHeader(messageHeader);
        JAXBContext jaxbContext =getJaxbContext() ;
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(validationError, sw);
        return sw.toString();
    }

    protected BRISIncomingMessageService getBrisIncomingMessageService() {
        return brisIncomingMessageService;
    }

    protected void setBrisIncomingMessageService(BRISIncomingMessageService brisIncomingMessageService) {
        this.brisIncomingMessageService = brisIncomingMessageService;
    }

    protected Sender getKafkaProducer() {
        return kafkaProducer;
    }

    protected void setKafkaProducer(Sender kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }
}
