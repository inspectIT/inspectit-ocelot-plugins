package rocks.inspectit.ocelot.kieker;

import rocks.inspectit.ocelot.kieker.exporterComponent.ExporterConfig;
import rocks.inspectit.ocelot.kieker.exporterComponent.KiekerTraceExporter;

import rocks.inspectit.ocelot.config.model.InspectitConfig;
import rocks.inspectit.ocelot.sdk.ConfigurablePlugin;
import rocks.inspectit.ocelot.sdk.OcelotPlugin;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

/**
 * inspectIT Ocelot plugin for enabling the export of gathered spans to a JMS endpoint in Kieker Span format.
 */
@Slf4j
@OcelotPlugin(value = "kieker", defaultConfig = "default.yml")
public class KiekerExporter implements ConfigurablePlugin<KiekerExporterSettings> {
    /**
     * Whether the exporter has been enabled.
     */
    private boolean enabled = false;

    /**
     * The currently used settings.
     */
    private KiekerExporterSettings activeSettings;

    @Override
    public void start(InspectitConfig inspectitConfig, KiekerExporterSettings kiekerExporterSettings) {
        update(inspectitConfig, kiekerExporterSettings);
    }

    @Override
    public void update(InspectitConfig inspectitConfig, KiekerExporterSettings settings) {
        boolean enable = inspectitConfig.getTracing().isEnabled()
                && settings.isEnabled()
                && !StringUtils.isEmpty(settings.getJmsConnectionUrl());

        //we force a restart if the access token has changed
        if (enabled && (!enable || !Objects.equals(activeSettings, settings))) {
            stop();
        }
        if (!enabled && enable) {
            start(settings);
        }
    }

    @Override
    public Class<KiekerExporterSettings> getConfigurationClass() {
        return KiekerExporterSettings.class;
    }

    /**
     * Starts the exporter.
     *
     * @param settings - the settings to use
     */
    private void start(KiekerExporterSettings settings) {
        try {
            log.info("Starting Kieker Exporter");
            ExporterConfig config = new ExporterConfig(settings.getJmsConnectionUrl(), settings.getJmsQueueName());
            KiekerTraceExporter.createAndRegister(config);
        } catch (Throwable t) {
            log.error("Error creating Kieker exporter", t);
        }
        enabled = true;
        activeSettings = settings;
    }

    /**
     * Stops the exporter.
     */
    private void stop() {
        log.info("Stopping Kieker Exporter");
        try {
            KiekerTraceExporter.unregister();
        } catch (Throwable t) {
            log.error("Error disabling Kieker exporter", t);
        }
        enabled = false;
    }
}