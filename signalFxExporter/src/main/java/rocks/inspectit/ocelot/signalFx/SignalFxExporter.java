package rocks.inspectit.ocelot.signalFx;

import io.opencensus.common.Duration;
import io.opencensus.exporter.stats.signalfx.SignalFxStatsConfiguration;
import io.opencensus.exporter.stats.signalfx.SignalFxStatsExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import rocks.inspectit.ocelot.config.model.InspectitConfig;
import rocks.inspectit.ocelot.sdk.ConfigurablePlugin;
import rocks.inspectit.ocelot.sdk.OcelotPlugin;

import java.net.URI;
import java.util.Objects;

/**
 * inspectIT Ocelot plugin for enabling the export of gathered metrics to a SignalFx.
 */
@Slf4j
@OcelotPlugin(value = "signalfx", defaultConfig = "default.yml")
public class SignalFxExporter implements ConfigurablePlugin<SignalFxExporterSettings> {

    /**
     * Whether the exporter has been enabled.
     */
    private boolean enabled = false;

    /**
     * The currently used settings.
     */
    private SignalFxExporterSettings activeSettings;

    @Override
    public void start(InspectitConfig inspectitConfig, SignalFxExporterSettings signalFxExporterSettings) {
        update(inspectitConfig, signalFxExporterSettings);
    }

    @Override
    public void update(InspectitConfig inspectitConfig, SignalFxExporterSettings settings) {
        boolean enable = inspectitConfig.getTracing().isEnabled()
                && settings.isEnabled()
                && !StringUtils.isEmpty(settings.getToken());

        if (enabled && (!enable || !Objects.equals(activeSettings, settings))) {
            log.info("The SignalFx exporter cannot be stopped during runtime. In order to disable the exporter, the agent has to be restarted.");
        }
        if (!enabled && enable) {
            start(settings);
        }
    }

    @Override
    public Class<SignalFxExporterSettings> getConfigurationClass() {
        return SignalFxExporterSettings.class;
    }

    /**
     * Starts the exporter.
     *
     * @param settings the settings to use
     */
    private void start(SignalFxExporterSettings settings) {
        try {
            log.info("Starting SignalFx exporter");
            SignalFxStatsExporter.create(SignalFxStatsConfiguration.builder()
                    .setIngestEndpoint(new URI(settings.getEndpoint()))
                    .setToken(settings.getToken())
                    .setExportInterval(Duration.fromMillis(settings.getReportingInterval().toMillis()))
                    .build());
        } catch (Throwable t) {
            log.error("Error creating SignalFx exporter", t);
        }
        enabled = true;
        activeSettings = settings;
    }
}
