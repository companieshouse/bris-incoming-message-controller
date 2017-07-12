package uk.gov.ch.bris.processor;

import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;

public interface IncomingMessageProcessor {

    /**
     * Process incoming message
     * @param deliveryBody
     * @throws FaultResponse
     */
    public void processIncomingMessage(DeliveryBody deliveryBody) throws FaultResponse;
}
