package uk.gov.ch.bris.endpoint;


import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;

import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeInterface;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import uk.gov.ch.bris.constants.ServiceConstants;
import uk.gov.ch.bris.processor.IncomingMessageProcessor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Endpoint Controller class which handles all Business Register requests from ECP.
 * It extracts business message and enques to Kafka topic.
 *
 */

public class DeliveryEnvelopeServiceEndpoint implements DeliveryEnvelopeInterface {

    /*
        logger instance for debug/log any messages.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceConstants.LOGGER_SERVICE_NAME);

    @Autowired
    private IncomingMessageProcessor messageProcessor;

    /**
     * Service handles all delivery submission messages from BR-ECP
     *
     * @param deliveryHeader
     * @param deliveryBody
     * @return acknowledgement
     * @throws FaultResponse
     */
    @Override
    public Acknowledgement submit(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse {
        LOGGER.debug("deliveryHeader.getDeliveryMessageInfo().getMessageID() :"+deliveryHeader.getDeliveryMessageInfo().getMessageID());

        messageProcessor.processIncomingMessage(deliveryBody);
        Acknowledgement acknowledgement = new Acknowledgement();
        DeliveryMessageInfoType messageInfo = new DeliveryMessageInfoType();
        messageInfo.setMessageID(deliveryHeader.getDeliveryMessageInfo().getMessageID());
        messageInfo.setTimestamp(getXMLGregorianCalendarNow());
        acknowledgement.setDeliveryMessageInfo(messageInfo);

        return acknowledgement;
    }



    /**
     *
     * @return
     */
    private XMLGregorianCalendar getXMLGregorianCalendarNow() {
        XMLGregorianCalendar now=null;

        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        } catch (DatatypeConfigurationException exception) {
            Map<String, Object> data = new HashMap<String, Object>();

            data.put("message", "Datatype Configuration Exception: unable to create new XML Gregorian Calendar instance");

            LOGGER.error(exception, data);
        }
        return now;
    }


}
