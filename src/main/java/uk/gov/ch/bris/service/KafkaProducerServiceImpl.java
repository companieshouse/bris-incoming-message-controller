package uk.gov.ch.bris.service;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

public class KafkaProducerServiceImpl implements KafkaProducerService {

    private CHKafkaProducer producer; 

    private static final String BRIS_INCOMING_TOPIC = System.getenv("BRIS_INCOMING_TOPIC");
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);    

    /**
     * 
     * @param config producer config
     */
    public KafkaProducerServiceImpl(ProducerConfig config) {
        this.producer = new CHKafkaProducer(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Message kafkaMessage) {
        kafkaMessage.setTopic(BRIS_INCOMING_TOPIC);
        LOGGER.info("Sending kafka message value " + kafkaMessage + " to topic " + kafkaMessage.getTopic());       
        producer.send(kafkaMessage);
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("Closing kafka producer");
        producer.close();
    }

}
