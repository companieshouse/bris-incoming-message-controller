package uk.gov.ch.bris.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.repository.BRISIncomingMessageRepository;

@ComponentScan
@Service
public class BRISIncomingMessageService {

    final Logger logger = LoggerFactory.getLogger(BRISIncomingMessageService.class);

    @Autowired
    private BRISIncomingMessageRepository brisIncomingMessageRepository;

    public List<BRISIncomingMessage> findAll() {
        List<BRISIncomingMessage> brisIncomingMessageEntries = brisIncomingMessageRepository.findAll();
        return brisIncomingMessageEntries;
    }
    
    public BRISIncomingMessage findById(String id) {
        return brisIncomingMessageRepository.findOneById(id);
    }

    public BRISIncomingMessage save(BRISIncomingMessage brisIncomingMessage) {
        return brisIncomingMessageRepository.save(brisIncomingMessage);
    }

    public void delete(BRISIncomingMessage brisIncomingMessage) {
        brisIncomingMessageRepository.delete(brisIncomingMessage);
    }

    /**
     * Removes all BRISIncomingMessage entities from database.
     */
    public void deleteAll() {
        brisIncomingMessageRepository.deleteAll();
    }
    
    public BRISIncomingMessage findByMessageId(String messageId) {
        return brisIncomingMessageRepository.findOneByMessageId(messageId);
    }
    
    

}
