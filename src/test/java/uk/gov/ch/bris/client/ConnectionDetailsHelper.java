
package uk.gov.ch.bris.client;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.europa.ec.bris.jaxb.br.components.aggregate.v1_4.MessageHeaderType;
import eu.europa.ec.bris.jaxb.br.connection.request.v1_4.BRConnectivityRequest;
import eu.europa.ec.bris.jaxb.br.error.v1_4.BRBusinessError;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.BusinessRegisterReferenceType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterNameType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyRegistrationNumberType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CorrelationIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CountryType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.DateTimeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.MessageIDType;

public class ConnectionDetailsHelper {

    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRConnectivityRequest newInstance(String correlationId, String messageId,
            String companyRegistrationNumber, String businessRegisterId, String countryCode) throws DatatypeConfigurationException {

        BRConnectivityRequest request = new BRConnectivityRequest();

        // Current Time
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValue(now);

        request = new BRConnectivityRequest();
        request.setMessageHeader(getMessageHeader(correlationId, messageId));
        request.setSendingDateTime(dateTimeType);

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

    /* ---- Getters and Setters ---- */
}
