package uk.gov.ch.bris.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
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
import org.bson.types.Binary;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.FaultDetail;
import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentResponse;
import uk.gov.ch.bris.constants.MongoStatus;
import uk.gov.ch.bris.constants.ServiceConstants;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.domain.BrisMessageType;
import uk.gov.ch.bris.domain.ValidationError;
import uk.gov.ch.bris.producer.SenderImpl;
import uk.gov.ch.bris.service.BRISIncomingMessageService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.StructuredLogger;

public class IncomingMessageProcessorImpl implements IncomingMessageProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger();

    static {
        ((StructuredLogger) LOGGER).setNamespace(ServiceConstants.LOGGER_SERVICE_NAME);
    }

    @Inject
    private BRISIncomingMessageService brisIncomingMessageService;

    @Value("${TEST_MODE}")
    private static int TEST_MODE;

    @Autowired
    private SenderImpl kafkaProducer;

    private Map<Class<?>, URL> businessRegisterClassMap;

    public IncomingMessageProcessorImpl(Map<Class<?>, URL> businessRegisterClassMap) {
        this.businessRegisterClassMap = businessRegisterClassMap;
    }

    /**
     * Save incoming message to mongoDB
     * Send relevant messageId to kafka incoming topic
     * @param deliveryBody
     * @throws FaultResponse
     */
    public void processIncomingMessage(DeliveryBody deliveryBody) throws FaultResponse {

        BRISIncomingMessage message = saveIncomingMessage(deliveryBody);

        if (!kafkaProducer.sendMessage(message.getId())) {
            LOGGER.debug("Could not send message to kafka. Setting status to " + MongoStatus.FAILED + " for message with id " + message.getId(), new HashMap<String, Object>());

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

            String invalidMessage = null;
            //Incase of validation ERROR
            if (brisMessageType.getValidationXML() != null) {
                invalidMessage = message; // keep track of original message if its invalid so we can still store in mongodb
                message = brisMessageType.getValidationXML();
            }

            // extract messageId from Message
            String messageId = brisMessageType.getMessageObjectType().getMessageHeader().getMessageID().getValue();
            String correlationId = brisMessageType.getMessageObjectType().getMessageHeader().getCorrelationID().getValue();

            // create brisIncomingMessage Object
            brisIncomingMessage = new BRISIncomingMessage(messageId, correlationId, message, MongoStatus.PENDING);

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
     * @throws JAXBException
     */
    private BrisMessageType validateSchema(String xmlMessage) throws FaultResponse,JAXBException {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        FaultDetail faultDetail = new FaultDetail();
        BrisMessageType brisMessageType = null;

        try {
            LOGGER.debug("Validating schema for xml message " + xmlMessage, new HashMap<String, Object>());
            brisMessageType = getSchema(xmlMessage);

            Schema schema = factory.newSchema(brisMessageType.getUrl());
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlMessage)));
        } catch (SAXException e) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("message", "XSD Validation Error caught validating schema brisMessageType=" + brisMessageType);

            LOGGER.error(e, data);

            brisMessageType.setClassName(ValidationError.class.getSimpleName());
            brisMessageType.setValidationXML(getXMLValidationMessage(brisMessageType.getMessageObjectType()));

        } catch (JAXBException e) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("message", "JAXBException caught validating schema. FaultResponse will be thrown");

            LOGGER.error(e, data);
            throw new FaultResponse("JAXBException", faultDetail, e);
        } catch (IOException e) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("message", "IOException caught validating schema. FaultResponse will be thrown");

            LOGGER.error(e, data);
            throw new FaultResponse("IO exception", faultDetail, e);
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
        MessageObjectType messageObjectType = (MessageObjectType)getJaxbContext().createUnmarshaller().unmarshal(reader);
        validateMessageID(messageObjectType);

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
                LOGGER.debug("Invalid messageLength for messageId. messageLength=" + messageLength + ", messageId=" + messageObjectType.getMessageHeader().getMessageID().getValue(), new HashMap<String, Object>());
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
        Class<?> clazz = messageObjectType.getClass();
        BrisMessageType brisMessageType = new BrisMessageType();
        brisMessageType.setUrl(businessRegisterClassMap.get(clazz));
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
