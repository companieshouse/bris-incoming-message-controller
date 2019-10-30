package uk.gov.ch.bris.processor;

import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;

public interface IncomingMessageProcessor {

    /**
     * Process incoming message
     * @param deliveryHeader
     * @param deliveryBody
     * @throws FaultResponse
     */
    public void processIncomingMessage(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse;
}
