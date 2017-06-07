package uk.gov.ch.bris.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ExportMetricReader;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.reader.MetricReader;
import org.springframework.boot.actuate.metrics.reader.MetricRegistryMetricReader;
import org.springframework.boot.actuate.metrics.statsd.StatsdMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

@Configuration
@EnableMetrics(proxyTargetClass = true)
public class MetricsConfig extends MetricsConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsConfig.class);
    
    private String prefix = "bris.incoming.controller";
    
    @Value("${STATSD_HOST}")
    private String host; // = "localhost";
    
    @Value("${STATSD_PORT}")
    private int port; // = 8125;
    
    @Autowired
    private MetricRegistry metricRegistry;
    
    @Bean
    @ExportMetricReader
    public MetricReader metricReader() {
        return new MetricRegistryMetricReader(metricRegistry);
    }
    
    @Bean
    @ExportMetricWriter
    public MetricWriter metricWriter() {
        LOGGER.info("Configuring StatsdMetricWriter to export with prefix {} to {}:{}", prefix, host, port);
        return new StatsdMetricWriter(prefix, host, port);
    }

}