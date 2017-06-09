package uk.gov.ch.bris.producer;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.FaultDetail;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.xml.sax.SAXException;
import uk.gov.ch.bris.constants.ResourcePathConstants;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.domain.BrisMessageType;
import uk.gov.ch.bris.service.BRISIncomingMessageService;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    @Inject
    private BRISIncomingMessageService brisIncomingMessageService;
    
    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Value("${TEST_MODE}")
    private int TEST_MODE;


    /**
     * Save incoming message to mongoDB
     * Send relevant messageId to kafka incoming topic
     * @param topic
     * @param deliveryBody
     * @return void
     * @throws FaultResponse 
     */
    public void sendMessage(String topic, DeliveryBody deliveryBody) throws FaultResponse {

        String message = getXMLmessagefromDeliveryBody(deliveryBody);
        BRISIncomingMessage brisIncomingMessage = null;
        FaultDetail faultDetail = new FaultDetail();
        DateTime dateTimeResult =  null;
        String strErrorMessage = "";
        String messageId = "";
        String correlationId = "";
        String id = "";
        String jsonIncomingId = "";

        try {
            // validate xmlMessage with the schema
            BrisMessageType brisMessageType = validateSchema(message);
            
            // extract messageId from Message
            messageId = this.extractMessageId(message);
            correlationId  = this.extractCorrelationId(message);
            dateTimeResult = getDateTime();

            // check if messageId/correlationId already exists in mongodb
            if(null == brisIncomingMessageService.findByMessageId(messageId)) {
                // create brisIncomingMessage Object
                 brisIncomingMessage = new BRISIncomingMessage(messageId, correlationId, message, "PENDING");
                
                // save brisIncomingMessage Object in Mongo DB
                brisIncomingMessage.setMessageType(brisMessageType.getClassName());
                brisIncomingMessage.setCreatedOn(dateTimeResult.toDateTimeISO());
                brisIncomingMessage = brisIncomingMessageService.save(brisIncomingMessage);

                id = brisIncomingMessage.getId();

                brisIncomingMessage = TEST_MODE==0?attachBinary(brisIncomingMessage,deliveryBody):brisIncomingMessage;
                
                brisIncomingMessage = brisIncomingMessageService.save(brisIncomingMessage);
                
                jsonIncomingId = "{\"incoming_id\":\"" + id + "\"}";
                LOGGER.info("Listing brisIncomingMessage with id: " + id + " messageId: " + brisIncomingMessage.getMessageId() + " correlationId: " + brisIncomingMessage.getCorrelationId());
                
            } else {
                strErrorMessage = "The provided MessageID value of this BRIS message " + messageId +  " already exists";
                Exception ex = new Exception(strErrorMessage);
                faultDetail.setResponseCode("GEN007");
                faultDetail.setMessage(strErrorMessage);
                throw new FaultResponse(strErrorMessage, faultDetail, ex);   
            }
            
        } catch (Exception e) {
            throw new FaultResponse("Exception", faultDetail, e);
        }
        
        // the KafkaTemplate provides asynchronous send methods returning a
        // Futurez
        ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(topic, jsonIncomingId);

        // you can register a callback with the listener to receive the result
        // of the send asynchronously
        future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                LOGGER.info("sent message='{}' with offset={}", message, result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("unable to send message='{}'", message, ex);
            }
        });

        // alternatively, to block the sending thread, to await the result,
        // invoke the future's get() method
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
     * Extract MessageId from the message
     * @param xmlMessage
     * @return messageId
     * @throws FaultResponse 
     */
    public String extractMessageId(String xmlMessage) throws FaultResponse {
        FaultDetail faultDetail = new FaultDetail();
        String messageId = "";
        
        try {
            Pattern p = Pattern.compile(".*\\<*<MessageID> *(.*) *\\</MessageID>*");
            Matcher m = p.matcher(xmlMessage);
            m.find();
            messageId = m.group(1);
            LOGGER.info("brCompanyDetailsRequest   ... ");
            LOGGER.info("brCompanyDetailsRequest messageId      ... " + messageId);
        } catch (Exception e) {
            faultDetail.setResponseCode("GEN000");
            faultDetail.setMessage("Parsing exception" + e.getLocalizedMessage());
            throw new FaultResponse("Exception", faultDetail, e);
        }
        
        return messageId;
    }
    
    /**
     * Extract CorrelationId from the message
     * @param xmlMessage
     * @return correlationId
     * @throws FaultResponse 
     */
    public String extractCorrelationId(String xmlMessage) throws FaultResponse {
        FaultDetail faultDetail = new FaultDetail();
        String correlationId = "";
        
        try {
            Pattern p = Pattern.compile(".*\\<*<CorrelationID> *(.*) *\\</CorrelationID>*");
            Matcher m = p.matcher(xmlMessage);
            m.find();
            correlationId = m.group(1);
            LOGGER.info("brCompanyDetailsRequest correlationId  ... " + correlationId);
        } catch (Exception e) {
            faultDetail.setResponseCode("GEN000");
            faultDetail.setMessage("Parsing exception" + e.getLocalizedMessage());
            throw new FaultResponse("Exception", faultDetail, e);
        }
        
        return correlationId;
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
            context = JAXBContext.newInstance(BRBranchDisclosureReceptionNotification.class,
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
                    SubmissionHeader.class);
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
    private BrisMessageType validateSchema(String xmlMessage) throws FaultResponse {
       SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
       FaultDetail faultDetail = new FaultDetail();
       try {
           BrisMessageType brisMessageType = getSchema(xmlMessage);

           Schema schema = factory.newSchema(brisMessageType.getUrl());
           Validator validator = schema.newValidator();
           validator.validate(new StreamSource(new StringReader(xmlMessage)));

           return brisMessageType;
       } catch (SAXException e) {
           e.printStackTrace();
           faultDetail.setResponseCode("GEN000");
           faultDetail.setMessage("Parsing exception" + e.getLocalizedMessage());
           throw new FaultResponse("Parsing exception", faultDetail, e);
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
   private BrisMessageType getSchema(String xmlMessage) {
       JAXBContext jaxbContext;
       Object obj = null;
       try {
           jaxbContext = getJaxbContext();
           Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
           JAXBSource source = new JAXBSource(jaxbContext, MessageObjectType.class);
           StringReader reader = new StringReader(xmlMessage);
           obj = jaxbUnmarshaller.unmarshal(reader);

       } catch (JAXBException e) {
           e.printStackTrace();
       }

       return getXSDResource(obj.getClass());

   }

   /**
    *
    * @param clazz
    * @return brisMessageType
    */
   private BrisMessageType getXSDResource(Class clazz) {
       
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
     * Pause for parameterized time
     * @param timeMillis
     */
    private void pauseAction(int timeMillis) {
        try {
            //Pause for parameterized time in seconds
            Thread.sleep(timeMillis);
        } catch (Exception ex) {
            LOGGER.error("Error while calling pause Action", Sender.class.getSimpleName(), ex);
        }
    }

}
