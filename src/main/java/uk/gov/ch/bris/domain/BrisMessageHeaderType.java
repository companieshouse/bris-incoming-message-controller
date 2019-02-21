package uk.gov.ch.bris.domain;

/**
 * Encapsulates header details coming from any bris version (1.4, 2.0)
 */
public class BrisMessageHeaderType {

    private String messageId;
    private String correlationId;
    private String businessRegisterId;
    private String businessRegisterCountry;
    private BrisTestData testData = new BrisTestData();

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getBusinessRegisterId() {
        return businessRegisterId;
    }

    public void setBusinessRegisterId(String businessRegisterId) {
        this.businessRegisterId = businessRegisterId;
    }

    public String getBusinessRegisterCountry() {
        return businessRegisterCountry;
    }

    public void setBusinessRegisterCountry(String businessRegisterCountry) {
        this.businessRegisterCountry = businessRegisterCountry;
    }

    public BrisTestData getTestData() {
        return testData;
    }

    public void setTestData(BrisTestData testData) {
        this.testData = testData;
    }
}
