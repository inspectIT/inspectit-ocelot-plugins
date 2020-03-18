package rocks.inspectit.ocelot.datadog;

import io.opencensus.exporter.trace.datadog.DatadogTraceConfiguration;
import io.opencensus.exporter.trace.datadog.DatadogTraceExporter;
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
@OcelotPlugin(value = "datadog", defaultConfig = "default.yml")
public class DatadogExporter implements ConfigurablePlugin<DatadogExporterSettings> {

    /**
     * Whether the exporter has been enabled.
     */
    private boolean enabled = false;

    /**
     * The currently used settings.
     */
    private DatadogExporterSettings activeSettings;

    @Override
    public void start(InspectitConfig inspectitConfig, DatadogExporterSettings datadogExporterSettings) {
        update(inspectitConfig, datadogExporterSettings);
    }

    @Override
    public void update(InspectitConfig inspectitConfig, DatadogExporterSettings settings) {
        boolean enable = inspectitConfig.getTracing().isEnabled()
                && settings.isEnabled()
                && !StringUtils.isEmpty(settings.getUrl());

        //we force a restart if the access token has changed
        if (enabled && (!enable || !Objects.equals(activeSettings, settings))) {
            stop();
        }
        if (!enabled && enable) {
            start(settings);
        }
    }

    @Override
    public Class<DatadogExporterSettings> getConfigurationClass() {
        return DatadogExporterSettings.class;
    }

    /**
     * Starts the exporter.
     *
     * @param settings the settings to use
     */
    private void start(DatadogExporterSettings settings) {
        try {
            log.info("Starting DataDog Exporter");

            DatadogTraceConfiguration config = DatadogTraceConfiguration.builder()
                    .setAgentEndpoint(settings.getUrl())
                    .setService(settings.getServiceName())
                    .setType("java")
                    .build();

            DatadogTraceExporter.createAndRegister(config);
        } catch (Throwable t) {
            log.error("Error creating DataDog exporter", t);
        }
        enabled = true;
        activeSettings = settings;
    }

    /**
     * Stops the exporter.
     */
    private void stop() {
        log.info("Stopping DataDog Exporter");
        try {
            DatadogTraceExporter.unregister();
        } catch (Throwable t) {
            log.error("Error disabling DataDog exporter", t);
        }
        enabled = false;
    }

}
