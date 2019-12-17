package rocks.inspectit.ocelot.haystack;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * The plugin configuration.
 */
@Data
public class HaystackExporterSettings {

    /**
     * Whether this exporter is enabled or not.
     */
    private boolean enabled;

    /**
     * The hostname of the Haystack agent.
     */
    private String host;

    /**
     * The port of the Haystack agent.
     */
    @Min(0)
    @Max(65535)
    private int port;

    /**
     * The service name under which traces are published. Defaults to inspectit.service-name;
     */
    private String serviceName;
}
