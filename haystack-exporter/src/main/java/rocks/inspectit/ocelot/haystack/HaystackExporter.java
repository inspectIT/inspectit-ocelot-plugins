package rocks.inspectit.ocelot.haystack;

import com.expedia.www.opencensus.exporter.trace.HaystackTraceExporter;
import com.expedia.www.opencensus.exporter.trace.config.GrpcAgentDispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import rocks.inspectit.ocelot.config.model.InspectitConfig;
import rocks.inspectit.ocelot.sdk.ConfigurablePlugin;
import rocks.inspectit.ocelot.sdk.OcelotPlugin;

import java.util.Objects;

/**
 * inspectIT Ocelot plugin for enabling the export of gathered spans to a Haystack backend.
 */
@Slf4j
@OcelotPlugin(value = "haystack", defaultConfig = "default.yml")
public class HaystackExporter implements ConfigurablePlugin<HaystackExporterSettings> {

    /**
     * Whether the exporter has been enabled.
     */
    private boolean enabled = false;

    /**
     * The currently used settings.
     */
    private HaystackExporterSettings activeSettings;

    @Override
    public void start(InspectitConfig inspectitConfig, HaystackExporterSettings haystackExporterSettings) {
        update(inspectitConfig, haystackExporterSettings);
    }

    @Override
    public void update(InspectitConfig inspectitConfig, HaystackExporterSettings settings) {
        boolean enable = inspectitConfig.getTracing().isEnabled()
                && settings.isEnabled()
                && !StringUtils.isEmpty(settings.getHost());

        //we force a restart if the access token has changed
        if (enabled && (!enable || !Objects.equals(activeSettings, settings))) {
            stop();
        }
        if (!enabled && enable) {
            start(settings);
        }
    }

    @Override
    public Class<HaystackExporterSettings> getConfigurationClass() {
        return HaystackExporterSettings.class;
    }

    /**
     * Starts the exporter.
     *
     * @param settings the settings to use
     */
    private void start(HaystackExporterSettings settings) {
        try {
            log.info("Starting Haystack Exporter");
            HaystackTraceExporter.createAndRegister(new GrpcAgentDispatcherConfig(settings.getHost(), settings.getPort()), settings.getServiceName());
        } catch (Throwable t) {
            log.error("Error creating Haystack exporter", t);
        }
        enabled = true;
        activeSettings = settings;
    }

    /**
     * Stops the exporter.
     */
    private void stop() {
        log.info("Stopping Haystack Exporter");
        try {
            HaystackTraceExporter.unregister();
        } catch (Throwable t) {
            log.error("Error disabling Haystack exporter", t);
        }
        enabled = false;
    }

}
