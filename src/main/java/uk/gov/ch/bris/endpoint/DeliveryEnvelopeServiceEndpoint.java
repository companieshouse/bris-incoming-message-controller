package uk.gov.ch.bris.endpoint;


import eu.domibus.plugin.bris.endpoint.delivery.DeliveryEnvelopeInterface;
import eu.domibus.plugin.bris.endpoint.delivery.FaultResponse;
import eu.domibus.plugin.bris.jaxb.delivery.*;
import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.gov.ch.bris.constants.ResourcePathConstants;

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
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;



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

	/**
	 *
	 * @param xmlMessage
	 * @throws FaultResponse
	 */
	private void validateSchema(String xmlMessage) throws FaultResponse{
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		FaultDetail faultDetail= new FaultDetail();
		try {
			Schema schema = factory.newSchema(getSchema(xmlMessage));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(xmlMessage)));


		} catch (SAXException e) {

			faultDetail.setResponseCode("GEN000");
			faultDetail.setMessage("Parsing exception"+e.getLocalizedMessage());
			throw new FaultResponse("Parsing exception",faultDetail,e);
		} catch (IOException e) {

			throw new FaultResponse("IO exception",faultDetail,e);
		}
	}

	/**
	 *
	 * @param xmlMessage
	 * @return
	 */
	private URL getSchema(String xmlMessage) {
		JAXBContext jaxbContext;
		Object obj=null;
		try {
			jaxbContext = getJaxbContext();
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			JAXBSource source = new JAXBSource(jaxbContext, MessageObjectType.class);
			StringReader reader = new StringReader(xmlMessage);
			obj = jaxbUnmarshaller.unmarshal(reader);



		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return this.getClass().getResource(getXSDPathLocation(obj.getClass().getName()));
	}

	/**
	 *
	 * @param className
	 * @return
	 */
	private String getXSDPathLocation(String className){

		Map<String,String> map=new HashMap<>();
		map.put("eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest", ResourcePathConstants.COMPANY_DETAILS_SCHEMA_FILE_PATH);
		map.put("eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotification", ResourcePathConstants.BRANCH_DISCLOUSER_NOTIFICATION_SCHEMA_FILE_PATH);
		map.put("eu.europa.ec.bris.v140.jaxb.br.connection.BRConnectivityRequest", ResourcePathConstants.CONNECTION_REQ_SCHEMA_FILE_PATH);
		map.put("eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotification", ResourcePathConstants.CRS_BORDER_MERGER_NOTIFICATION_SCHEMA_FILE_PATH);
		map.put("eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentRequest", ResourcePathConstants.RETRIEVE_DOCUMENT_SCHEMA_FILE_PATH);

		return  map.get(className);

	}
	/**
	 *
	 * @return
	 */
	private JAXBContext getJaxbContext() {
		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(
					MessageObjectType.class
			);
		} catch (JAXBException exception) {
			exception.printStackTrace();
		}

		return context;
	}
}
