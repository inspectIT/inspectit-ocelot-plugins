package rocks.inspectit.ocelot.signalFx;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Duration;

/**
 * The plugin configuration.
 */
@Data
public class SignalFxExporterSettings {

    /**
     * Whether this exporter is enabled or not.
     */
    private boolean enabled;

    /**
     * The SignalFx token.
     */
    private String token;

    /**
     * The SignalFx endpoint.
     */
    private String endpoint;

    /**
     * The reporting interval in seconds.
     */
    @NotNull
    private Duration reportingInterval;
}
