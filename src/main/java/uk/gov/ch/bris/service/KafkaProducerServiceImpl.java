package uk.gov.ch.bris.service;

import java.util.HashMap;

import javax.annotation.PreDestroy;

import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.StructuredLogger;

public class KafkaProducerServiceImpl implements KafkaProducerService {

    private CHKafkaProducer producer; 

    private static final String BRIS_INCOMING_TOPIC = System.getenv("BRIS_INCOMING_TOPIC");
    private final static Logger log = LoggerFactory.getLogger();  

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
        ((StructuredLogger) log).setNamespace("bris.incoming.controller");
        
        kafkaMessage.setTopic(BRIS_INCOMING_TOPIC);
        log.debug("Sending kafka message value " + kafkaMessage + " to topic " + kafkaMessage.getTopic(), new HashMap<String, Object>());
        producer.send(kafkaMessage);
    }
    
    @PreDestroy
    public void close() {
        ((StructuredLogger) log).setNamespace("bris.incoming.controller");
        
        log.debug("Closing kafka producer", new HashMap<String, Object>());
        producer.close();
    }

}
