package rocks.inspectit.ocelot.kieker.exporterComponent;

import lombok.Data;

/**
 * Exporter Configuration Object
 */
@Data
public class ExporterConfig {

    public ExporterConfig(String newConnectionUrl, String newQueueName) {
        jmsConnectionUrl = newConnectionUrl;
        jmsQueueName = newQueueName;
    }

    /**
     * The URL of the JMS Apache MQ Service where Traces shall be send to
     */
    private String jmsConnectionUrl;

    /**
     * The Queue name where traces will be send to
     */
    private String jmsQueueName;
}
