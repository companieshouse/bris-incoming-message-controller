package uk.gov.ch.bris.healthcheck;

public class HealthCheckResponse {

    private final String status;

    public HealthCheckResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
