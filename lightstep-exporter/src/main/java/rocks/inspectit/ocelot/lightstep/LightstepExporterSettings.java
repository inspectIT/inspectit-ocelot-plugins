package rocks.inspectit.ocelot.lightstep;

import lombok.Data;

@Data
public class LightstepExporterSettings {

    private boolean enabled;

    /**
     * The access token for lightstep
     */
    private String accessToken;

    /**
     * The service name under which traces are published, defaults to inspectit.service-name
     */
    private String serviceName;
}
