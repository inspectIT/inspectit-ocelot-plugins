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

@OcelotPlugin(value = "lightstep", defaultConfig = "default.yml")
@Slf4j
public class LightstepExporter implements ConfigurablePlugin<LightstepExporterSettings> {

    private boolean isEnabled = false;

    private LightstepExporterSettings activeSettings;

    @Override
    public void start(InspectitConfig inspectitConfig, LightstepExporterSettings lightstepExporterSettings) {
        update(inspectitConfig, lightstepExporterSettings);
    }

    @Override
    public void update(InspectitConfig inspectitConfig, LightstepExporterSettings ls) {

        boolean enable = inspectitConfig.getTracing().isEnabled()
                && ls.isEnabled()
                && !StringUtils.isEmpty(ls.getAccessToken());
        //we force a restart if the access token has changed
        if(isEnabled && (!enable || !Objects.equals(activeSettings, ls))) {
            stop();
        }
        if(!isEnabled && enable) {
            start(ls);
        }
    }

    @Override
    public Class<LightstepExporterSettings> getConfigurationClass() {
        return LightstepExporterSettings.class;
    }

    private void start(LightstepExporterSettings ls) {
        try {
            log.info("Starting Lightstep Exporter");

            JRETracer tracer = new JRETracer(new Options.OptionsBuilder()
                    .withComponentName(ls.getServiceName())
                    .withAccessToken(ls.getAccessToken())
                    .build()
            );
            LightStepTraceExporter.createAndRegister(tracer);
        } catch (Throwable t) {
            log.error("Error creating Lightstep exporter", t);
        }
        isEnabled = true;
        activeSettings = ls;
    }

    private void stop() {
        log.info("Stopping Lightstep Exporter");
        try {
            LightStepTraceExporter.unregister();
        } catch (Throwable t) {
            log.error("Error disabling Lightstep exporter", t);
        }
        isEnabled = false;
    }

}
