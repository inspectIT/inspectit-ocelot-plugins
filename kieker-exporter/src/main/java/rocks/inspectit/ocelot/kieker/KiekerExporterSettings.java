package rocks.inspectit.ocelot.kieker;

import rocks.inspectit.ocelot.kieker.exporterComponent.ExporterConfig;
import lombok.Data;

/**
 * The plugin configuration.
 */
@Data
public class KiekerExporterSettings {
    /**
     * Whether this exporter is enabled or not.
     */
    private boolean enabled;

    /**
     * The URL of the JMS Apache MQ Service where Traces shall be send to
     */
    private String jmsConnectionUrl;

    /**
     * The Queue name where traces will be send to
     */
    private String jmsQueueName;

}
