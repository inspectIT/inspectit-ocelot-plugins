package rocks.inspectit.ocelot.kieker.exporterComponent;

import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;

import javax.jms.JMSException;

/*
* Exporter towards Kieker
* initializes and registers Handler and JMSSender
*/
public class KiekerTraceExporter {
    private static final String REGISTER_NAME = KiekerTraceExporter.class.getName();
    private static ExporterConfig config;
    private static JMSSender jmsSender;
    private static SpanExporter.Handler handler;

    public static void createAndRegister(ExporterConfig newConfig) throws JMSException {
        config = newConfig;
        jmsSender = new JMSSender(config.getJmsConnectionUrl(), config.getJmsQueueName());
        handler = new ExporterHandler(jmsSender);
        createAndRegister();
    }

    private static void createAndRegister() {
        Tracing.getExportComponent().getSpanExporter().registerHandler(REGISTER_NAME, handler);
    }

    public static void unregister() {
        Tracing.getExportComponent().getSpanExporter().unregisterHandler(REGISTER_NAME);
    }
}
