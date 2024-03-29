
package uk.gov.ch.bris.client;

import eu.europa.ec.bris.jaxb.br.components.aggregate.v1_4.MessageHeaderType;
import eu.europa.ec.bris.jaxb.br.document.retrieval.response.v1_4.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_4.BusinessRegisterReferenceType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.AttachmentReferenceType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterNameType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyRegistrationNumberType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CorrelationIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CountryType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.DocumentIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.MessageIDType;

public class RetrieveDocumentResponseHelper {

    /* ---- Constants ---- */

    /* ---- Instance Variables ---- */

    /* ---- Constructors ---- */

    /* ---- Business Methods ---- */

    public static BRRetrieveDocumentResponse newInstance(String correlationId, String messageId,
            String companyRegistrationNumber, String businessRegisterId, String countryCode, String documentId) {

        BRRetrieveDocumentResponse response = new BRRetrieveDocumentResponse();

        response.setMessageHeader(getMessageHeader(correlationId, messageId));
        response.setBusinessRegisterReference(businessRegisterReference(countryCode, businessRegisterId));
        response.setCompanyRegistrationNumber(companyRegistrationNumber(companyRegistrationNumber));
        response.setDocumentID(setDocumentId(documentId));
        response.setAttachmentReference(setAttachmentReferenceType(documentId));

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

    private static AttachmentReferenceType setAttachmentReferenceType(String documentId) {
        AttachmentReferenceType attachmentReferenceType = new AttachmentReferenceType();
        attachmentReferenceType.setValue(documentId);
        return attachmentReferenceType;
    }

    /* ---- Getters and Setters ---- */
}
