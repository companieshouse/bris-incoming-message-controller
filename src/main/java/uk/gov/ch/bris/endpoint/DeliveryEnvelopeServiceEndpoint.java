package uk.gov.ch.bris.endpoint;


import java.io.IOException;
import java.util.GregorianCalendar;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import plugin.domibus.eu.bris.common.aggregate.components._1.MessageContentType;
import plugin.domibus.eu.bris.delivery.components._1.Acknowledgement;
import plugin.domibus.eu.bris.delivery.components._1.DeliveryBody;
import plugin.domibus.eu.bris.delivery.components._1.DeliveryHeader;
import plugin.domibus.eu.bris.delivery.components._1.DeliveryMessageInfoType;
import plugin.domibus.eu.bris.wsdl.endpoint.delivery.envelope._1.DeliveryEnvelopeInterface;
import plugin.domibus.eu.bris.wsdl.endpoint.delivery.envelope._1.FaultResponse;
import uk.gov.ch.bris.controller.DeliveryEnvelopeServiceController;

public class DeliveryEnvelopeServiceEndpoint implements DeliveryEnvelopeInterface {

    @Autowired
    private DeliveryEnvelopeServiceController weatherServiceController;
    
    
    @Override
	public Acknowledgement submit(DeliveryHeader deliveryHeader, DeliveryBody deliveryBody) throws FaultResponse {
    	System.out.println("MessageContent ... " + deliveryBody.getMessageContent());
    	
    	String xmlMessage = "";
    	MessageContentType message = new MessageContentType();
    	
    	DataHandler dataHandler = new DataHandler(deliveryBody.getMessageContent().getValue().getDataSource());
    	try {
			xmlMessage = IOUtils.toString(dataHandler.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    	
    	System.out.println("xmlMessage     ... " + xmlMessage);
    	
		
    	//System.out.println("ContentType    ... " + deliveryBody.getMessageContent().getContentType());
    	//System.out.println("Attachment     ... " + deliveryBody.getAttachment());
    	
    	System.out.println("@@@@@@@@@@@@@@@@@@");
    	System.out.println("Inside submit ... ");
    	System.out.println("@@@@@@@@@@@@@@@@@@");
    	
    	Acknowledgement ack = new Acknowledgement();
    	DeliveryMessageInfoType messageInfo = new DeliveryMessageInfoType();
    	
    	
        messageInfo.setMessageID(deliveryHeader.getDeliveryMessageInfo().getMessageID());
        messageInfo.setTimestamp(getXMLGregorianCalendarNow());
        ack.setDeliveryMessageInfo(messageInfo);
    	
		// TODO Auto-generated method stub
		return ack;
	}
    
    public XMLGregorianCalendar getXMLGregorianCalendarNow() {
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            return now;
        } catch (DatatypeConfigurationException exception) {
            //throw new BRISProgramException("message.bris.utils.error.program.001", exception);
        	return null;
        }
    }
}
