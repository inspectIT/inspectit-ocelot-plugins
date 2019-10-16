package rocks.inspectit.ocelot.lightstep;

import com.lightstep.opencensus.exporter.LightStepTraceExporter;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import rocks.inspectit.ocelot.config.model.InspectitConfig;
import rocks.inspectit.ocelot.sdk.ConfigurablePlugin;
import rocks.inspectit.ocelot.sdk.OcelotPlugin;

import java.util.Objects;

/**
 * inspectIT Ocelot plugin for enabling the export of gathered spans to a Lightstep backend.
 */
@Slf4j
@OcelotPlugin(value = "lightstep", defaultConfig = "default.yml")
public class LightstepExporter implements ConfigurablePlugin<LightstepExporterSettings> {

    /**
     * Whether the exporter has been enabled.
     */
    private boolean enabled = false;

    /**
     * The currently used settings.
     */
    private LightstepExporterSettings activeSettings;

    @Override
    public void start(InspectitConfig inspectitConfig, LightstepExporterSettings lightstepExporterSettings) {
        update(inspectitConfig, lightstepExporterSettings);
    }

    @Override
    public void update(InspectitConfig inspectitConfig, LightstepExporterSettings settings) {
        boolean enable = inspectitConfig.getTracing().isEnabled()
                && settings.isEnabled()
                && !StringUtils.isEmpty(settings.getAccessToken());

        //we force a restart if the access token has changed
        if (enabled && (!enable || !Objects.equals(activeSettings, settings))) {
            stop();
        }
        if (!enabled && enable) {
            start(settings);
        }
    }

    @Override
    public Class<LightstepExporterSettings> getConfigurationClass() {
        return LightstepExporterSettings.class;
    }

    /**
     * Starts the exporter.
     *
     * @param settings the settings to use
     */
    private void start(LightstepExporterSettings settings) {
        try {
            log.info("Starting Lightstep Exporter");

            Options options = new Options.OptionsBuilder()
                    .withComponentName(settings.getServiceName())
                    .withAccessToken(settings.getAccessToken())
                    .build();

            JRETracer tracer = new JRETracer(options);

            LightStepTraceExporter.createAndRegister(tracer);
        } catch (Throwable t) {
            log.error("Error creating Lightstep exporter", t);
        }
        enabled = true;
        activeSettings = settings;
    }

    /**
     * Stops the exporter.
     */
    private void stop() {
        log.info("Stopping Lightstep Exporter");
        try {
            LightStepTraceExporter.unregister();
        } catch (Throwable t) {
            log.error("Error disabling Lightstep exporter", t);
        }
        enabled = false;
    }

}
