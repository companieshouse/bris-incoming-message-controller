package uk.gov.ch.bris.healthcheck;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    
    private static final String ALIVE_STATUS = "ALIVE";

    @RequestMapping(value="/healthcheck")
    public HealthCheckResponse healthCheck() {
        return new HealthCheckResponse(ALIVE_STATUS);
    }
}
