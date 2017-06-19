package uk.gov.ch.bris.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureSubmissionNotification;
import eu.europa.ec.bris.v140.jaxb.br.branch.disclosure.BRBranchDisclosureSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.detail.BRCompanyDetailsResponse;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentRequest;
import eu.europa.ec.bris.v140.jaxb.br.company.document.BRRetrieveDocumentResponse;
import eu.europa.ec.bris.v140.jaxb.br.connection.BRConnectivityRequest;
import eu.europa.ec.bris.v140.jaxb.br.connection.BRConnectivityResponse;
import eu.europa.ec.bris.v140.jaxb.br.error.BRBusinessError;
import eu.europa.ec.bris.v140.jaxb.br.led.BRUpdateLEDRequest;
import eu.europa.ec.bris.v140.jaxb.br.led.BRUpdateLEDStatus;
import eu.europa.ec.bris.v140.jaxb.br.led.full.BRFullUpdateLEDAcknowledgment;
import eu.europa.ec.bris.v140.jaxb.br.led.full.BRFullUpdateLEDRequest;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerReceptionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerSubmissionNotification;
import eu.europa.ec.bris.v140.jaxb.br.merger.BRCrossBorderMergerSubmissionNotificationAcknowledgement;
import eu.europa.ec.bris.v140.jaxb.br.subscription.BRManageSubscriptionRequest;
import eu.europa.ec.bris.v140.jaxb.br.subscription.BRManageSubscriptionStatus;
import uk.gov.ch.bris.constants.ResourcePathConstants;
import uk.gov.ch.bris.processor.IncomingMessageProcessor;
import uk.gov.ch.bris.processor.IncomingMessageProcessorImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class IncomingMessageProcessorConfig {

    private static final Logger log = LoggerFactory.getLogger();
    
    /**
     * Bean config for incoming message processor
     * Load schemas for message types into map used by message processor class
     * @return IncomingMessageProcessor
     */
    @Bean
    public IncomingMessageProcessor messageProcessor() {
        
        Map<Class<?>, URL>businessRegisterClassMap = new HashMap<>();

        ClassLoader classLoader = getClass().getClassLoader();
        
        //Branch Disclosure - reception
        businessRegisterClassMap.put(BRBranchDisclosureReceptionNotification.class, getURL(classLoader, ResourcePathConstants.BRANCH_DISCLOSURE_RECEPTION_NOTIFICATION_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRBranchDisclosureReceptionNotificationAcknowledgement.class, getURL(classLoader, ResourcePathConstants.BRANCH_DISCLOSURE_RECEPTION_NOTIFICATION_RESPONSE_SCHEMA));
        //Branch Disclosure - submission
        businessRegisterClassMap.put(BRBranchDisclosureSubmissionNotification.class, getURL(classLoader, ResourcePathConstants.BRANCH_DISCLOSURE_SUBMISSION_NOTIFICATION_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRBranchDisclosureSubmissionNotificationAcknowledgement.class, getURL(classLoader, ResourcePathConstants.BRANCH_DISCLOSURE_SUBMISSION_NOTIFICATION_RESPONSE_SCHEMA));
        
        //Business Error
        businessRegisterClassMap.put(BRBusinessError.class, getURL(classLoader, ResourcePathConstants.BUSINESS_ERROR_SCHEMA));
        
        //Company details
        businessRegisterClassMap.put(BRCompanyDetailsRequest.class, getURL(classLoader, ResourcePathConstants.COMPANY_DETAILS_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRCompanyDetailsResponse.class, getURL(classLoader, ResourcePathConstants.COMPANY_DETAILS_RESPONSE_SCHEMA));
        
        //Connectivity
        businessRegisterClassMap.put(BRConnectivityRequest.class, getURL(classLoader, ResourcePathConstants.CONNECTION_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRConnectivityResponse.class, getURL(classLoader, ResourcePathConstants.CONNECTION_RESPONSE_SCHEMA));
        
        //Cross Border Merger - reception
        businessRegisterClassMap.put(BRCrossBorderMergerReceptionNotification.class, getURL(classLoader, ResourcePathConstants.CROSS_BORDER_RECEPTION_NOTIFICATION_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRCrossBorderMergerReceptionNotificationAcknowledgement.class, getURL(classLoader, ResourcePathConstants.CROSS_BORDER_RECEPTION_NOTIFICATION_RESPONSE_SCHEMA));      
        //Cross Border Merger - submission
        businessRegisterClassMap.put(BRCrossBorderMergerSubmissionNotification.class, getURL(classLoader, ResourcePathConstants.CROSS_BORDER_SUBMISSION_NOTIFICATION_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRCrossBorderMergerSubmissionNotificationAcknowledgement.class, getURL(classLoader, ResourcePathConstants.CROSS_BORDER_SUBMISSION_NOTIFICATION_RESPONSE_SCHEMA));
        
        //Full Update LED
        businessRegisterClassMap.put(BRFullUpdateLEDRequest.class, getURL(classLoader, ResourcePathConstants.FULL_UPDATE_LED_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRFullUpdateLEDAcknowledgment.class, getURL(classLoader, ResourcePathConstants.FULL_UPDATE_LED_RESPONSE_SCHEMA));
        
        //Document
        businessRegisterClassMap.put(BRRetrieveDocumentRequest.class, getURL(classLoader, ResourcePathConstants.RETRIEVE_DOCUMENT_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRRetrieveDocumentResponse.class, getURL(classLoader, ResourcePathConstants.RETRIEVE_DOCUMENT_RESPONSE_SCHEMA));
        
        //Subscription
        businessRegisterClassMap.put(BRManageSubscriptionRequest.class, getURL(classLoader, ResourcePathConstants.SUBSCRIPTION_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRManageSubscriptionStatus.class, getURL(classLoader, ResourcePathConstants.SUBSCRIPTION_RESPONSE_SCHEMA));
        
        //Update LED
        businessRegisterClassMap.put(BRUpdateLEDRequest.class, getURL(classLoader, ResourcePathConstants.UPDATE_LED_REQUEST_SCHEMA));
        businessRegisterClassMap.put(BRUpdateLEDStatus.class, getURL(classLoader, ResourcePathConstants.UPDATE_LED_RESPONSE_SCHEMA));

        log.debug("Creating class map for BR Messages types: " + businessRegisterClassMap, new HashMap<String, Object>());
        
        return new IncomingMessageProcessorImpl(businessRegisterClassMap);
    }
    
    /**
     * Get URL for schema 
     * @param classLoader
     * @param schemaName
     * @return URL
     */
    private URL getURL(ClassLoader classLoader, final String schemaName) {
        URL url = classLoader.getResource(ResourcePathConstants.XSD_PATH + schemaName);
        if (url == null) {
            throw new RuntimeException("Unable to find schema " + ResourcePathConstants.XSD_PATH + schemaName);
        }
        return url;
    }

}
