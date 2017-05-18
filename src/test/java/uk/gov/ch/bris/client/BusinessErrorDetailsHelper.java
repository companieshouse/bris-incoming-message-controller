package uk.gov.ch.bris.client;


import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageHeaderType;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.BusinessRegisterReferenceType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.FaultErrorType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterNameType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CompanyRegistrationNumberType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CorrelationIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CountryType;
import eu.europa.ec.bris.v140.jaxb.components.basic.DateTimeType;
import eu.europa.ec.bris.v140.jaxb.components.basic.FaultErrorCodeType;
import eu.europa.ec.bris.v140.jaxb.components.basic.FaultErrorDescriptionType;
import eu.europa.ec.bris.v140.jaxb.components.basic.FaultErrorSourceType;
import eu.europa.ec.bris.v140.jaxb.components.basic.MessageIDType;
import uk.gov.ch.bris.api.error.BusinessError;
import uk.gov.ch.bris.error.ErrorCode;


public class BusinessErrorDetailsHelper {

    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRBusinessError newInstance(
        String correlationId,
        String messageId,
        String companyRegistrationNumber,
        String businessRegisterId,
        String countryCode,
        String strErrorCode)   {

        BRBusinessError request = new BRBusinessError();
        try{
          //Current Time
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            
            DateTimeType dateTimeType = new DateTimeType();
            dateTimeType.setValue(now);
            
            request = new BRBusinessError();
        	request.setMessageHeader(getMessageHeader(correlationId, messageId));
        	
            ErrorCode errorCode = ErrorCode.valueOf(strErrorCode);
            BusinessError businessError = new BusinessError(errorCode);
            
        	FaultErrorType errorType = new FaultErrorType();
            FaultErrorCodeType faultErrorCodeType = new FaultErrorCodeType();
            FaultErrorDescriptionType faultErrorDescriptionType = new FaultErrorDescriptionType();
            FaultErrorSourceType faultErrorSourceType = new FaultErrorSourceType();
            
            faultErrorCodeType.setValue(businessError.getErrorCode().name());
            errorType.setFaultErrorCode(faultErrorCodeType);
            
            faultErrorDescriptionType.setValue(businessError.getErrorCode().getDescription());
            errorType.setFaultErrorDescription(faultErrorDescriptionType);
            
            faultErrorSourceType.setValue("BR");
            errorType.setFaultErrorSource(faultErrorSourceType);
            
            request.getFaultError().add(errorType);
            
        } catch(DatatypeConfigurationException dce) {    	    
    	} catch(Exception ex) {    	    
    	}
        
    	return request;
    }

    public static BRBusinessError newInstance(
            String correlationId,
            String messageId) {

        BRBusinessError request = new BRBusinessError();
        request.setMessageHeader(getMessageHeader(correlationId, messageId));
        return request;
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

    /* ---- Getters and Setters ---- */
}
