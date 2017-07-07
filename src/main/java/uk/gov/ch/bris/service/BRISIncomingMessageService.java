package uk.gov.ch.bris.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import uk.gov.ch.bris.constants.ServiceConstants;
import uk.gov.ch.bris.domain.BRISIncomingMessage;
import uk.gov.ch.bris.repository.BRISIncomingMessageRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Service for the BRISIncomingMessage entity.
 */
@ComponentScan
@Service
public class BRISIncomingMessageService {

    @Autowired
    private BRISIncomingMessageRepository brisIncomingMessageRepository;

    final static Logger LOGGER = LoggerFactory.getLogger(ServiceConstants.LOGGER_SERVICE_NAME);

    /**
     * Find all BRISIncomingMessage entities from database.
     */
    public List<BRISIncomingMessage> findAll() {
        List<BRISIncomingMessage> brisIncomingMessageEntries = brisIncomingMessageRepository.findAll();
        return brisIncomingMessageEntries;
    }

    /**
     * Find one BRISIncomingMessage entity from database using the id
     */
    public BRISIncomingMessage findById(String id) {
        return brisIncomingMessageRepository.findOneById(id);
    }

    /**
     * Saves single BRISIncomingMessage entity from database.
     */
    public BRISIncomingMessage save(BRISIncomingMessage brisIncomingMessage) {
        LOGGER.debug("Saving incoming message " + brisIncomingMessage);
        return brisIncomingMessageRepository.save(brisIncomingMessage);
    }

    /**
     * Removes single BRISIncomingMessage entity from database.
     */
    public void delete(BRISIncomingMessage brisIncomingMessage) {
        brisIncomingMessageRepository.delete(brisIncomingMessage);
    }

    /**
     * Removes all BRISIncomingMessage entities from database.
     */
    public void deleteAll() {
        brisIncomingMessageRepository.deleteAll();
    }

    /**
     * Find one BRISIncomingMessage entity from database using the messageId
     */
    public BRISIncomingMessage findByMessageId(String messageId) {
        return brisIncomingMessageRepository.findOneByMessageId(messageId);
    }



}
