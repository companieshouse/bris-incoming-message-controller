package uk.gov.ch.bris.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ch.bris.producer.SenderImpl;
import uk.gov.ch.bris.service.KafkaProducerService;
import uk.gov.ch.bris.service.KafkaProducerServiceImpl;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

@Configuration
public class SenderConfig {

    @Bean
    public SenderImpl sender() {
        return new SenderImpl();
    }

    @Bean
    public KafkaProducerService kafkaProducerService() {
        uk.gov.companieshouse.kafka.producer.ProducerConfig config = new uk.gov.companieshouse.kafka.producer.ProducerConfig();
        config.setAcks(Acks.WAIT_FOR_LOCAL);
        config.setRetries(10);
        ProducerConfigHelper.assignBrokerAddresses(config);
        return new KafkaProducerServiceImpl(config);
    }
}
