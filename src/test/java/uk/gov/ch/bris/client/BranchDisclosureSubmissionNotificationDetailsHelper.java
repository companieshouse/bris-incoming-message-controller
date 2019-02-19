package uk.gov.ch.bris.client;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.europa.ec.bris.jaxb.br.branch.disclosure.notification.submission.request.v1_4.BRBranchDisclosureSubmissionNotification;
import eu.europa.ec.bris.jaxb.br.components.aggregate.v1_4.MessageHeaderType;
import eu.europa.ec.bris.jaxb.br.error.v1_4.BRBusinessError;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.AddressType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.BranchEUIDsType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.BusinessRegisterReferenceType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.BusinessRegisterType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.CompanyAlternateIdentifiersType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.LegislationReferencesType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.NotificationCompanyType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.NotificationContextType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.AddressLine1Type;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.AddressLine2Type;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.AddressLine3Type;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterNameType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CityType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyAlternateIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyEUIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyNameType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyRegistrationNumberType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CorrelationIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CountryType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.DateTimeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.DocumentIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.EffectiveDateType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.LegalFormCodeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.MessageIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.PaymentReferenceType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.PostalCodeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.ProceedingType;

public class BranchDisclosureSubmissionNotificationDetailsHelper {

    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRBranchDisclosureSubmissionNotification newInstance(String correlationId, String messageId,
            String companyRegistrationNumber, String businessRegisterId, String countryCode, String documentId) {

        BRBranchDisclosureSubmissionNotification request = new BRBranchDisclosureSubmissionNotification();

        request.setMessageHeader(getMessageHeader(correlationId, messageId));

        request.setNotificationContext(setNotificationContextType());
        request.setProceeding(setProceedingType("WINDING_UP_OPENING"));
        request.setDisclosureCompany(setNotificationCompanyType());

        return request;
    }

    public static BRBusinessError newInstance(String correlationId, String messageId) {

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

        // ***** START --BusinessRegisterReference *******************//
        BusinessRegisterReferenceType businessRegisterReferenceType = new BusinessRegisterReferenceType();
        BusinessRegisterNameType businessRegisterNameType = new BusinessRegisterNameType();
        businessRegisterNameType.setValue("Companies House");

        BusinessRegisterIDType businessRegisterIDType = new BusinessRegisterIDType();

        // BusinessRegisterID
        businessRegisterIDType.setValue("EW");

        // BusinessRegisterCountry Country
        CountryType countryType = new CountryType();
        countryType.setValue("UK");

        // set BusinessRegisterID
        businessRegisterReferenceType.setBusinessRegisterID(businessRegisterIDType);

        // set BusinessRegisterCountry
        businessRegisterReferenceType.setBusinessRegisterCountry(countryType);
        // TODO BusinessRegisterName??

        // set BusinessRegisterReference to CompanyDetailsResponse
        messageHeaderType.setBusinessRegisterReference(businessRegisterReferenceType);
        return messageHeaderType;
    }

    private static BusinessRegisterReferenceType businessRegisterReference(String countryCode,
            String businessRegisterId) {
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

        // BusinessRegisterID
        businessRegisterIDType.setValue("EW");

        BusinessRegisterNameType businessRegisterNameType = new BusinessRegisterNameType();
        businessRegisterNameType.setValue("Companies House");

        // BusinessRegisterCountry Country
        CountryType countryType = new CountryType();
        countryType.setValue("UK");

        legislationReferencesType.getLegislationReference();

        // set BusinessRegisterID
        // set BusinessRegisterCountry
        issuingOrganisation.setBusinessRegisterID(businessRegisterIDType);
        issuingOrganisation.setBusinessRegisterCountry(countryType);
        issuingOrganisation.setBusinessRegisterName(businessRegisterNameType);

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
        businessRegisterNameType.setValue("EW");

        CompanyAlternateIDType companyAlternateIDType = new CompanyAlternateIDType();
        companyAlternateIDType.setValue("34EDED");

        CompanyAlternateIdentifiersType companyAlternateIdentifiersType = new CompanyAlternateIdentifiersType();
        companyAlternateIdentifiersType.getCompanyAlternateID().add(companyAlternateIDType);

        CompanyEUIDType companyEUIDType = new CompanyEUIDType();
        companyEUIDType.setValue("FRIG.2010012341-Z<");

        LegalFormCodeType legalFormCodeType = new LegalFormCodeType();
        legalFormCodeType.setValue("LF-NL-001");

        CompanyNameType companyNameType = new CompanyNameType();
        companyNameType.setValue("CompanyName");

        AddressType addressType = new AddressType();
        AddressLine1Type addressLine1 = new AddressLine1Type();
        AddressLine2Type addressLine2 = new AddressLine2Type();
        AddressLine3Type addressLine3 = new AddressLine3Type();
        PostalCodeType postalCode = new PostalCodeType();
        CityType city = new CityType();
        CountryType countryType = new CountryType();

        addressLine1.setValue("1A Broadway Parade");
        addressLine2.setValue("Pinner Road");
        addressLine3.setValue("Middx");
        postalCode.setValue("HA27SY");
        city.setValue("HARROW");
        countryType.setValue("UK");

        addressType.setAddressLine1(addressLine1);
        addressType.setAddressLine2(addressLine2);
        addressType.setAddressLine3(addressLine3);
        addressType.setCity(city);
        addressType.setPostalCode(postalCode);
        addressType.setCountry(countryType);

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
        BusinessRegisterIDType businessRegisterIDType = new BusinessRegisterIDType();

        // BusinessRegisterID
        businessRegisterIDType.setValue("EW");

        BusinessRegisterNameType businessRegisterNameType = new BusinessRegisterNameType();
        businessRegisterNameType.setValue("Companies House");

        // BusinessRegisterCountry Country
        CountryType countryType = new CountryType();
        countryType.setValue("UK");

        businessRegisterType.setBusinessRegisterCountry(countryType);
        businessRegisterType.setBusinessRegisterID(businessRegisterIDType);
        businessRegisterType.setBusinessRegisterName(businessRegisterNameType);

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

        } catch (DatatypeConfigurationException exception) {
            throw new RuntimeException(exception);
        }

        return now;
    }

}
