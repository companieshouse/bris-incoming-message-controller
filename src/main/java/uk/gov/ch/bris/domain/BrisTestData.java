package uk.gov.ch.bris.domain;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates test data coming from any bris version (1.4, 2.0)
 */
@XmlRootElement
public class BrisTestData {
    private String caseId;
    private String conditionId;
    private String executionId;
    private String packageId;
    private String sessionId;
    
    public BrisTestData() {
        super();
    }

    public BrisTestData(String caseId, String conditionId, String executionId, String packageId, String sessionId) {
        this();
        this.caseId = caseId;
        this.conditionId = conditionId;
        this.executionId = executionId;
        this.packageId = packageId;
        this.sessionId = sessionId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
