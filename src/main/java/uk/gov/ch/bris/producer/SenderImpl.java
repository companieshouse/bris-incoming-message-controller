package uk.gov.ch.bris.producer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.ch.bris.constants.ServiceConstants;
import uk.gov.ch.bris.service.KafkaProducerService;
import uk.gov.ch.bris.transformer.IncomingMessage;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class SenderImpl implements Sender {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceConstants.LOGGER_SERVICE_NAME);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public boolean sendMessage(String messageId) {
        boolean successful=false;

        Message kafkaMessage = new Message();

        //Create object to represent outgoing message
        IncomingMessage incomingMessage = new IncomingMessage();
        incomingMessage.setIncomingMessageId(messageId);

        //Convert object to byte array to insert into kafka message
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] value = mapper.writeValueAsString(incomingMessage).getBytes();
            kafkaMessage.setValue(value);
            kafkaProducerService.send(kafkaMessage);
            successful=true;
        } catch (JsonProcessingException|InterruptedException|ExecutionException exc) {

            Map<String, Object> data = new HashMap<String, Object>();

            data.put("message", "Unable to create kafka message id " + messageId);

            LOGGER.error(exc, data);

        }

        return successful;
    }

}
