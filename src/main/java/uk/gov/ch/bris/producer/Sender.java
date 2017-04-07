package uk.gov.ch.bris.producer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.FaultDetail;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.service.BRISIncomingMessageService;

public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    @Inject
    private BRISIncomingMessageService brisIncomingMessageService;
    
    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        DateTime dateTimeResult =  null;
        String messageId = "";
        String id = "";
        try {
            messageId = this.extractMessageId(message);
            dateTimeResult = getDateTime();
            
            BRISIncomingMessage brisIncomingMessage = new BRISIncomingMessage(messageId, message, "PENDING"); 
            brisIncomingMessage.setTimestamp(dateTimeResult.toDateTimeISO());
            
            brisIncomingMessage = brisIncomingMessageService.save(brisIncomingMessage);
            id = brisIncomingMessage.getId();
            
            LOGGER.info("Listing brisIncomingMessage with id: " + id + " messageId: " + messageId);
        } catch (Exception e) {            
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
            System.out.println("brCompanyDetailsRequest   ... " + messageId);
        
        } catch (Exception e) {
            faultDetail.setResponseCode("GEN000");
            faultDetail.setMessage("Parsing exception" + e.getLocalizedMessage());
            throw new FaultResponse("Exception", faultDetail, e);
        }
        
        return messageId;
    }

    public DateTime getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dt = new Date();
        String strDt = sdf.format(dt);
        
        DateTimeFormatter parser = ISODateTimeFormat.dateTime();
        DateTime dateTimeResult = parser.parseDateTime(strDt);
        
        return dateTimeResult;
    }
    
}
