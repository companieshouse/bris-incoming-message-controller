package uk.gov.ch.bris.repository;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.MongoRepository;

import uk.gov.ch.bris.domain.BRISIncomingMessage;

/**
 * Spring Data MongoDB repository for the BRISIncomingMessage entity.
 */
@ComponentScan
public interface BRISIncomingMessageRepository extends MongoRepository<BRISIncomingMessage,String> {
    
    BRISIncomingMessage findOneById(String id);
    List<BRISIncomingMessage> findAll();
    
    @SuppressWarnings("unchecked")
    BRISIncomingMessage save(BRISIncomingMessage saved);
    
    void delete(BRISIncomingMessage b);
    void deleteAll();
    
}
