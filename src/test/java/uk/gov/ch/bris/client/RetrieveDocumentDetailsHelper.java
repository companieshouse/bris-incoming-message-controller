
package uk.gov.ch.bris.client;

import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageHeaderType;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import eu.europa.ec.bris.v140.jaxb.components.aggregate.BusinessRegisterReferenceType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.BusinessRegisterNameType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CompanyRegistrationNumberType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CorrelationIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.CountryType;
import eu.europa.ec.bris.v140.jaxb.components.basic.DocumentIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.MessageIDType;
import eu.europa.ec.bris.v140.jaxb.components.basic.PaymentReferenceType;

public class RetrieveDocumentDetailsHelper {

    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRRetrieveDocumentRequest newInstance(String correlationId, String messageId,
            String companyRegistrationNumber, String businessRegisterId, String countryCode, String documentId) {
        BRRetrieveDocumentRequest request = new BRRetrieveDocumentRequest();

        request.setMessageHeader(getMessageHeader(correlationId, messageId));
        request.setBusinessRegisterReference(businessRegisterReference(countryCode, businessRegisterId));
        request.setPaymentReference(setPaymentReferenceType("PaymentRef"));
        request.setCompanyRegistrationNumber(companyRegistrationNumber(companyRegistrationNumber));
        request.setDocumentID(setDocumentId(documentId));

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

    /* ---- Getters and Setters ---- */
}
