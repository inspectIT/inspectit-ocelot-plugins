package rocks.inspectit.ocelot.datadog;

import lombok.Data;

/**
 * The plugin configuration.
 */
@Data
public class DatadogExporterSettings {

    /**
     * Whether this exporter is enabled or not.
     */
    private boolean enabled;

    /**
     * The url of teh DD agent.
     */
    private String url;

    /**
     * The service name under which traces are published, defaults to ${inspectit.service-name}
     */
    private String serviceName;
}
