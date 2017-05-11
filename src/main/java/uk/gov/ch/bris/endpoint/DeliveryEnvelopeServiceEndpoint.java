package uk.gov.ch.bris.endpoint;


import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.SAXException;

import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeInterface;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.Acknowledgement;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryBody;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryHeader;
import eu.domibus.plugin.bris.jaxb.delivery.DeliveryMessageInfoType;
import eu.domibus.plugin.bris.jaxb.delivery.FaultDetail;
import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;
import uk.gov.ch.bris.constants.ResourcePathConstants;
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
     * @return
     * @throws FaultResponse
     */
    @Override
	public Acknowledgement submit(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse {
        
        loger.info("deliveryHeader.getDeliveryMessageInfo().getMessageID() :"+deliveryHeader.getDeliveryMessageInfo().getMessageID());
        
        String xmlMessage = getXMLmessagefromDeliveryBody(deliveryBody);
		loger.info("xmlMessage :"+xmlMessage);
		kafkaProducer.sendMessage(brisIncomingTopic, xmlMessage);
		
		Acknowledgement acknowledgement = new Acknowledgement();
    	DeliveryMessageInfoType messageInfo = new DeliveryMessageInfoType();
        messageInfo.setMessageID(deliveryHeader.getDeliveryMessageInfo().getMessageID());
        messageInfo.setTimestamp(getXMLGregorianCalendarNow());
		acknowledgement.setDeliveryMessageInfo(messageInfo);
		
		return acknowledgement;
	}



    /**
     *
     * @param deliveryBody
     * @return
     * @throws FaultResponse
     */
	private String getXMLmessagefromDeliveryBody(DeliveryBody deliveryBody) throws FaultResponse{
		//WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
		String xmlMessage = "";
		FaultDetail faultDetail= new FaultDetail();
		DataHandler dataHandler = new DataHandler(deliveryBody.getMessageContent().getValue().getDataSource());

		try {
			xmlMessage = IOUtils.toString(dataHandler.getInputStream());
		} catch (IOException e) {
			faultDetail.setResponseCode("BR-TECH-ERR-0002");
			faultDetail.setMessage("IOException oocured while extracting business message"+e.getLocalizedMessage());
			throw new FaultResponse("IO exception",faultDetail,e);
		}catch (Exception e) {
			faultDetail.setResponseCode("BR-TECH-ERR-0001");
			faultDetail.setMessage("Exception oocured while extracting message: "+e.getLocalizedMessage());
			throw new FaultResponse("Exception oocured while extracting business message: ",faultDetail,e);
		}
		return  xmlMessage;
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
