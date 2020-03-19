package rocks.inspectit.ocelot.kieker.exporterComponent;


import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;

import kieker.common.record.controlflow.OperationExecutionRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/*
 * Kieker Exporter Handler
 *
 * convert spans from OpenCensus to Kiekers span (OperationExecutionRecord) format
 * Spans must have two attributes named eoi (execution Order) and ess (stack depth)
 */
@Slf4j
public class ExporterHandler extends SpanExporter.Handler {
    private static JMSSender jmsSender;

    public ExporterHandler(JMSSender newSender) {
        jmsSender = newSender;
    }

    /**
     * will be called with a list of spans after the handler has correctly been registered
     * @param spanDataList
     */
    @Override
    public void export(Collection<SpanData> spanDataList) {
        for (SpanData sd : spanDataList) {

            Map attributes = sd.getAttributes().getAttributeMap();
            if(!((Map) attributes).containsKey("eoi") || !attributes.containsKey("ess")) {
                log.info("Span is missing an attribute named eoi and/or ess to export to Kieker");
                return;
            }

            // Getting execution order/ stack depth attribute value
            AttributeValue valEoi = (AttributeValue) attributes.get("eoi");
            AttributeValue valEss = (AttributeValue) attributes.get("ess");
            int eoi = valEoi.match(Integer::valueOf, bool -> -1, Long::intValue, Double::intValue, obj -> -1);
            int ess = valEss.match(Integer::valueOf, bool -> -1, Long::intValue, Double::intValue, obj -> -1);

            // Checking if attribute values are valid
            if (eoi == -1 || ess == -1) {
                log.info("eoi and/or ess Atributes of Span can't be converted into int");
                return;
            }

            JMSSender.send(createOperationExecutionRecord(sd, eoi, ess));
        }
    }

    /**
     * converts the given Span into Kieker OperationExecutionRecord
     *
     * @param sd - The Span to be added
     * @param eoi - Execution Order of the Span
     * @param ess - Execution Stack Size of the Span
     * @return OperationExecutionRecord
     */
    private static OperationExecutionRecord createOperationExecutionRecord(SpanData sd, int eoi, int ess) {
        // create Long for usage as Kieker-trace ID
        Integer hash = sd.getContext().getTraceId().hashCode();
        Long kiekerTraceId = hash.longValue();

        return new OperationExecutionRecord(
                sd.getName(),
                OperationExecutionRecord.NO_SESSION_ID,
                kiekerTraceId,
                Long.parseLong(String.format("%s", sd.getStartTimestamp().getSeconds())),
                Long.parseLong(String.format("%s", sd.getEndTimestamp().getSeconds())),
                OperationExecutionRecord.NO_HOSTNAME,
                eoi,
                ess
        );
    }
}
