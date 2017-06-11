package uk.gov.ch.bris.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.ch.bris.service.KafkaProducerService;
import uk.gov.ch.bris.transformer.IncomingMessage;
import uk.gov.companieshouse.kafka.message.Message;

public class SenderImpl implements Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderImpl.class);

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
        } catch (JsonProcessingException jpe) {
            LOGGER.error("Unable to create kafka message id " + messageId, jpe);

        }

        return successful;
    }

}
