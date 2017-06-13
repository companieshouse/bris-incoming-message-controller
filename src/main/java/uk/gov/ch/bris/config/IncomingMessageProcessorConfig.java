package uk.gov.ch.bris.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ch.bris.processor.IncomingMessageProcessor;
import uk.gov.ch.bris.processor.IncomingMessageProcessorImpl;

@Configuration
public class IncomingMessageProcessorConfig {

    /**
     * Bean config for incoming message processor
     * @return IncomingMessageProcessor
     */
    @Bean
    public IncomingMessageProcessor messageProcessor() {
        return new IncomingMessageProcessorImpl();
    }

}
