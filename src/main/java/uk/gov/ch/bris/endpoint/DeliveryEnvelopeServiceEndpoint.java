package uk.gov.ch.bris.endpoint;


import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeInterface;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import uk.gov.ch.bris.producer.Sender;



/**
 * Endpoint Controller class which handles all Business Register requests from ECP.
 * It extracts business message and enques to Kafka topic.
 *
 */

public class DeliveryEnvelopeServiceEndpoint implements DeliveryEnvelopeInterface {

    /*
        logger instance for debug/log any messages.
     */
    private final Logger loger = LoggerFactory.getLogger(DeliveryEnvelopeServiceEndpoint.class);

    @Autowired
    private Sender kafkaProducer;

    @Value("${kafka.producer.topic}")
    private String brisIncomingTopic;

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
        
        loger.info("deliveryHeader.getDeliveryMessageInfo().getMessageID() :"+deliveryHeader.getDeliveryMessageInfo().getMessageID());

        kafkaProducer.sendMessage(brisIncomingTopic, deliveryBody);
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

            exception.printStackTrace();
        }
        return now;
    }


}
