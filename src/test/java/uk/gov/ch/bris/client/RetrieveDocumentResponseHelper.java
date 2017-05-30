
package uk.gov.ch.bris.client;


import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageHeaderType;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.BusinessRegisterReferenceType;
import eu.europa.ec.bris.v140.jaxb.components.basic.AttachmentReferenceType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterNameType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CompanyRegistrationNumberType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CorrelationIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CountryType;
import eu.europa.ec.bris.v140.jaxb.components.basic.DocumentIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.MessageIDType;


public class RetrieveDocumentResponseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveDocumentResponseHelper.class);
    
    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRRetrieveDocumentResponse newInstance(
        String correlationId,
        String messageId,
        String companyRegistrationNumber,
        String businessRegisterId,
        String countryCode,
        String documentId) {
        
        BRRetrieveDocumentResponse response = new BRRetrieveDocumentResponse();
        
        try {
            response.setMessageHeader(getMessageHeader(correlationId, messageId));
            response.setBusinessRegisterReference(businessRegisterReference(countryCode, businessRegisterId));
            response.setCompanyRegistrationNumber(companyRegistrationNumber(companyRegistrationNumber));
            response.setDocumentID(setDocumentId(documentId));
            response.setAttachmentReference(setAttachmentReferenceType(documentId));
        
        } catch(Exception ex) {
            LOGGER.error("unable to create new instance", "", ex);
        }
    
    	return response;
    }
    


    private static MessageHeaderType getMessageHeader(String correlationId, String messageId) {
        MessageHeaderType messageHeaderType = new MessageHeaderType();
        CorrelationIDType correlationIDType = new CorrelationIDType();
        correlationIDType.setValue(correlationId);
        messageHeaderType.setCorrelationID(correlationIDType);
        MessageIDType messageIDType = new MessageIDType();
        messageIDType.setValue(messageId);
        messageHeaderType.setMessageID(messageIDType);
        
        //***** START --BusinessRegisterReference *******************//
        BusinessRegisterReferenceType businessRegisterReferenceType=new BusinessRegisterReferenceType();
        BusinessRegisterNameType businessRegisterNameType=new BusinessRegisterNameType();
        businessRegisterNameType.setValue("Companies House");
        
        BusinessRegisterIDType businessRegisterIDType=new BusinessRegisterIDType();
        
        //BusinessRegisterID
        businessRegisterIDType.setValue("EW");
        
        //BusinessRegisterCountry Country
        CountryType countryType=new CountryType();
        countryType.setValue("UK");
        
        //set BusinessRegisterID
        businessRegisterReferenceType.setBusinessRegisterID(businessRegisterIDType);
        
        // set BusinessRegisterCountry
        businessRegisterReferenceType.setBusinessRegisterCountry(countryType);
        // TODO BusinessRegisterName??
        
        // set BusinessRegisterReference to CompanyDetailsResponse
        messageHeaderType.setBusinessRegisterReference(businessRegisterReferenceType);
        return messageHeaderType;
    }

    private static BusinessRegisterReferenceType businessRegisterReference(String countryCode, String businessRegisterId) {
    	BusinessRegisterReferenceType businessRegisterReference = new BusinessRegisterReferenceType();
    	businessRegisterReference.setBusinessRegisterCountry(country(countryCode));
    	businessRegisterReference.setBusinessRegisterID(businessRegisterId(businessRegisterId));
    	return businessRegisterReference;
    }

    private static CompanyRegistrationNumberType companyRegistrationNumber(String companyRegNumber) {
    	CompanyRegistrationNumberType companyRegistrationNumber = new CompanyRegistrationNumberType();
    	companyRegistrationNumber.setValue(companyRegNumber);
    	return companyRegistrationNumber;
    }

    private static CountryType country(String countryCode) {
    	CountryType country = new CountryType();
    	country.setValue(countryCode);
    	return country;
    }

    private static BusinessRegisterIDType businessRegisterId(String identifier) {
    	BusinessRegisterIDType businessRegisterId = new BusinessRegisterIDType();
    	businessRegisterId.setValue(identifier);
    	return businessRegisterId;
    }
    
    private static DocumentIDType setDocumentId(String documentId) {
        DocumentIDType documentIdType = new DocumentIDType();
        documentIdType.setValue(documentId);
        return documentIdType;
    }
    
    private static AttachmentReferenceType setAttachmentReferenceType(String documentId) {
        AttachmentReferenceType attachmentReferenceType = new AttachmentReferenceType();
        attachmentReferenceType.setValue(documentId);
        return attachmentReferenceType;
    }
    

    /* ---- Getters and Setters ---- */
}