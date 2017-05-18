package uk.gov.ch.bris.client;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageHeaderType;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.AddressType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.BranchEUIDsType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.BusinessRegisterReferenceType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.BusinessRegisterType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.CompanyAlternateIdentifiersType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.LegislationReferencesType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.NotificationCompanyType;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.NotificationContextType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterNameType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CompanyEUIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CompanyNameType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CompanyRegistrationNumberType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CorrelationIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CountryType;
import eu.europa.ec.bris.v140.jaxb.components.basic.DateTimeType;
import eu.europa.ec.bris.v140.jaxb.components.basic.DocumentIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.EffectiveDateType;
import eu.europa.ec.bris.v140.jaxb.components.basic.LegalFormCodeType;
import eu.europa.ec.bris.v140.jaxb.components.basic.MessageIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.PaymentReferenceType;
import eu.europa.ec.bris.v140.jaxb.components.basic.ProceedingType;

public class BranchDisclosureReceptionNotificationDetailsHelper {

    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRBranchDisclosureReceptionNotification newInstance(
        String correlationId,
        String messageId,
        String companyRegistrationNumber,
        String businessRegisterId,
        String countryCode,
        String documentId) {
    	
        BRBranchDisclosureReceptionNotification request = new BRBranchDisclosureReceptionNotification();
    	request.setMessageHeader(getMessageHeader(correlationId, messageId));
    	
    	request.setNotificationContext(setNotificationContextType());
        request.setProceeding(setProceedingType("proceedingTypeValue"));
        request.setDisclosureCompany(setNotificationCompanyType());
        request.setRecipientOrganisation(setBusinessRegisterType());
        
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
        BusinessRegisterNameType businessRegisterNameType = new BusinessRegisterNameType();
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
    
    public static PaymentReferenceType setPaymentReferenceType(String paymentReference) {
        PaymentReferenceType paymentReferenceType = new PaymentReferenceType();
        paymentReferenceType.setValue(paymentReference);
        return paymentReferenceType;
    }

    private static NotificationContextType setNotificationContextType() {
        NotificationContextType notificationContextType = new NotificationContextType();
        
        LegislationReferencesType legislationReferencesType = new LegislationReferencesType();
        BusinessRegisterType issuingOrganisation = new BusinessRegisterType();
        DateTimeType issuanceDateTime = new DateTimeType();
        EffectiveDateType effectiveDate = new EffectiveDateType();
        
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValue(getXMLGregorianCalendarNow());
        effectiveDate.setValue(getXMLGregorianCalendarNow());
        
        BusinessRegisterIDType businessRegisterIDType = new BusinessRegisterIDType();
        
        //BusinessRegisterID
        businessRegisterIDType.setValue("EW");
        
        BusinessRegisterNameType businessRegisterNameType = new BusinessRegisterNameType();
        businessRegisterNameType.setValue("Companies House");
        
        //BusinessRegisterCountry Country
        CountryType countryType=new CountryType();
        countryType.setValue("UK");
        
        legislationReferencesType.getLegislationReference();
        
        //set BusinessRegisterID
        // set BusinessRegisterCountry
        issuingOrganisation.setBusinessRegisterID(businessRegisterIDType);
        issuingOrganisation.setBusinessRegisterCountry(countryType);
        
        notificationContextType.setIssuingOrganisation(issuingOrganisation);
        notificationContextType.setIssuanceDateTime(dateTimeType);
        notificationContextType.setEffectiveDate(effectiveDate);
        
        return notificationContextType;
    }
    
    
    private static ProceedingType setProceedingType(String proceedingTypeValue) {
        ProceedingType proceedingType = new ProceedingType();
        proceedingType.setValue(proceedingTypeValue);
        return proceedingType;
    }
    
    private static NotificationCompanyType setNotificationCompanyType() {
        BusinessRegisterNameType businessRegisterNameType = new BusinessRegisterNameType();
        businessRegisterNameType.setValue("");
        
        CompanyAlternateIdentifiersType companyAlternateIdentifiersType = new CompanyAlternateIdentifiersType();
        //companyAlternateIdentifiersType.getCompanyAlternateID().
        
        CompanyEUIDType companyEUIDType = new CompanyEUIDType();
        companyEUIDType.setValue("");
        
        LegalFormCodeType legalFormCodeType = new LegalFormCodeType();
        legalFormCodeType.setValue("");
        
        CompanyNameType companyNameType = new CompanyNameType();
        companyNameType.setValue("");        
        
        AddressType addressType = new AddressType();
        //addressType.setAddressLine1("");
        
        NotificationCompanyType notificationCompanyType = new NotificationCompanyType();
        notificationCompanyType.setBusinessRegisterName(businessRegisterNameType);
        notificationCompanyType.setCompanyAlternateIdentifiers(companyAlternateIdentifiersType); 
        notificationCompanyType.setCompanyEUID(companyEUIDType);
        notificationCompanyType.setCompanyLegalForm(legalFormCodeType); 
        notificationCompanyType.setCompanyName(companyNameType);
        notificationCompanyType.setCompanyRegisteredOffice(addressType);
        
        return notificationCompanyType;
    }
    
    
    private static BusinessRegisterType setBusinessRegisterType() {
        BusinessRegisterType businessRegisterType = new BusinessRegisterType();
        BusinessRegisterIDType businessRegisterIDType=new BusinessRegisterIDType();
        
        //BusinessRegisterID
        businessRegisterIDType.setValue("EW");
        
        //BusinessRegisterCountry Country
        CountryType countryType = new CountryType();
        countryType.setValue("UK");
        
        businessRegisterType.setBusinessRegisterCountry(countryType);
        businessRegisterType.setBusinessRegisterID(businessRegisterIDType); 
        
        return businessRegisterType;
    }
    
    private static BranchEUIDsType setBranchEUIDsType() {
        BranchEUIDsType branchEUIDsType = new BranchEUIDsType();
        branchEUIDsType.getBranchEUID();
        return branchEUIDsType;
    }
    
    
    /* ---- Getters and Setters ---- */
    
    private static XMLGregorianCalendar getXMLGregorianCalendarNow() {
        XMLGregorianCalendar now = null;
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

            System.out.println("" + now);
        } catch (DatatypeConfigurationException exception) {

            exception.printStackTrace();
        }
        return now;
    }
    
}
