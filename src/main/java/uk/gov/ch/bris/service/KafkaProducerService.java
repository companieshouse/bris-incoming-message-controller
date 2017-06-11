package uk.gov.ch.bris.service;

import uk.gov.companieshouse.kafka.message.Message;

public interface KafkaProducerService {

    /**
     * send kafka message
     * @param kafkaMessage
     */
    public void send(Message kafkaMessage);
}
