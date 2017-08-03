package uk.gov.ch.bris.service;

import java.util.concurrent.ExecutionException;

import uk.gov.companieshouse.kafka.message.Message;

public interface KafkaProducerService {

    /**
     * send kafka message
     * @param kafkaMessage
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void send(Message kafkaMessage) throws ExecutionException, InterruptedException;
}
