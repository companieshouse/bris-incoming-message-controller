package uk.gov.ch.bris.producer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.xml.sax.SAXException;

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
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerSubmissionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.subscription.BRManageSubscriptionRequest;
import eu.europa.ec.bris.v140.jaxb.br.subscription.BRManageSubscriptionStatus;
import uk.gov.ch.bris.constants.ResourcePathConstants;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.domain.BrisMessageType;
import uk.gov.ch.bris.service.BRISIncomingMessageService;

public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    @Inject
    private BRISIncomingMessageService brisIncomingMessageService;
    
    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    public void sendMessage(String topic, String message) throws FaultResponse {
        FaultDetail faultDetail = new FaultDetail();
        
        DateTime dateTimeResult =  null;
        String messageId = "";
        String correlationId = "";
        String id = "";
        try {
            // validate xmlMessage with the schema
            BrisMessageType brisMessageType = validateSchema(message);

            // extract messageId from Message
            messageId = this.extractMessageId(message);
            correlationId  = this.extractCorrelationId(message);
            dateTimeResult = getDateTime();
            
            // create brisIncomingMessage Object
            BRISIncomingMessage brisIncomingMessage = new BRISIncomingMessage(messageId, correlationId, message, "PENDING"); 
            
            // save brisIncomingMessage Object in Mongo DB
            brisIncomingMessage.setMessageType(brisMessageType.getClassName());
            brisIncomingMessage.setCreatedOn(dateTimeResult.toDateTimeISO());
            brisIncomingMessage = brisIncomingMessageService.save(brisIncomingMessage);
            
            id = brisIncomingMessage.getId();
            brisIncomingMessage = brisIncomingMessageService.save(brisIncomingMessage);
            
            LOGGER.info("Listing brisIncomingMessage with id: " + id + " messageId: " + brisIncomingMessage.getMessageId() + " correlationId: " + brisIncomingMessage.getCorrelationId());
        } catch (Exception e) {
            throw new FaultResponse("Exception", faultDetail, e);
        }
        
        // the KafkaTemplate provides asynchronous send methods returning a
        // Future
        ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(topic, id);
        
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

    public DateTime getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dt = new Date();
        String strDt = sdf.format(dt);
        
        DateTimeFormatter parser = ISODateTimeFormat.dateTime();
        DateTime dateTimeResult = parser.parseDateTime(strDt);
        
        return dateTimeResult;
    }
    
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

    @Bean
    public Marshaller marshaller() throws JAXBException {
        return getJaxbContext().createMarshaller();
    }

    @Bean
    public Unmarshaller unmarshaller() throws JAXBException {
        return getJaxbContext().createUnmarshaller();
    }
    
    /**
    *
    * @param xmlMessage
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
    * @return
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
    * @param
    * @return
    */
   private BrisMessageType getXSDResource(Class clazz) {
       
       Map<Class, URL> map = new HashMap<>();
       
       map.put(BRCompanyDetailsRequest.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.COMPANY_DETAILS_SCHEMA));
       map.put(BRBranchDisclosureReceptionNotification.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.XSD_PATH + ResourcePathConstants.BRANCH_DISCLOSURE_NOTIFICATION_SCHEMA));
       map.put(BRCrossBorderMergerReceptionNotification.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.CRS_BORDER_MERGER_NOTIFICATION_SCHEMA));
       map.put(BRRetrieveDocumentRequest.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.XSD_PATH + ResourcePathConstants.RETRIEVE_DOCUMENT_SCHEMA));
       map.put(BRConnectivityRequest.class, clazz.getClassLoader().getResource(ResourcePathConstants.XSD_PATH + ResourcePathConstants.CONNECTION_REQ_SCHEMA));
       
       BrisMessageType brisMessageType = new BrisMessageType();
       brisMessageType.setUrl(map.get(clazz));
       brisMessageType.setClassName(clazz.getSimpleName());
       
       return brisMessageType;
   }
   
}
