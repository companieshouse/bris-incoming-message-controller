package uk.gov.ch.bris.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Service;

@Service
public class MetricsCounterService {

    private final CounterService counterService;

    @Autowired
    public MetricsCounterService(CounterService counterService) {
        this.counterService = counterService;
    }

    public void incrementConsumedMessageCounter() {
        this.counterService.increment("consumed.invoked");
    }

    public void incrementWriteToIncomingMongoDBCounter() {
        this.counterService.increment("mongodb.incoming.invoked");
    }
    
    public void incrementWriteToOutgoingMongoDBCounter() {
        this.counterService.increment("mongodb.outgoing.invoked");
    }
    
    public void incrementWriteToKafkaIncomingCounter() {
        this.counterService.increment("kafka.incoming.invoked");
    }
    
    public void incrementWriteToKafkaOutgoingCounter() {
        this.counterService.increment("kafka.outgoing.invoked");
    }
    
}
