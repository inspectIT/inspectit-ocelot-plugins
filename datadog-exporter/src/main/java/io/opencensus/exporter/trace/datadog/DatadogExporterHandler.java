/*
 * Copyright 2019, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.exporter.trace.datadog;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.exporter.trace.util.TimeLimitedHandler;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

@SuppressWarnings({
  // This library is not supposed to be Android or Java 7 compatible.
  "AndroidJdkLibsChecker",
  "Java7ApiChecker"
})
final class DatadogExporterHandler extends TimeLimitedHandler {

  public static final String DD_METRIC_ = "dd_metric_";
  private static final String EXPORT_SPAN_NAME = "ExportDatadogTraces";
  private static final Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();
  public static final String DD_RESOURCE = "dd_resource";
  public static final String DD_SERVICE = "dd_service";
  public static final String DD_TYPE = "dd_type";

  private final URL agentEndpoint;
  private final String service;
  private final String type;

  DatadogExporterHandler(String agentEndpoint, String service, String type, Duration deadline)
      throws MalformedURLException {
    super(deadline, EXPORT_SPAN_NAME);
    this.agentEndpoint = new URL(agentEndpoint);
    this.service = service;
    this.type = type;
  }

  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
            Functions.returnToString(),
            Functions.returnToString(),
            Functions.returnToString(),
            Functions.returnToString(),
            Functions.throwIllegalArgumentException());
  }

  private Number attributeValueToNumber(AttributeValue attributeValue) {
    return attributeValue.match(
            (input) ->  NumberUtils.isCreatable(input) ? NumberUtils.createNumber(input) : null,
            (input) -> input ? 1 : 0,
            (input) -> input,
            (input) -> input,
            Functions.throwIllegalArgumentException());
  }



  private static long convertSpanId(final SpanId spanId) {
    final byte[] bytes = spanId.getBytes();
    long result = 0;
    for (int i = 0; i < Long.SIZE / Byte.SIZE; i++) {
      result <<= Byte.SIZE;
      result |= (bytes[i] & 0xff);
    }
    if (result < 0) {
      return -result;
    }
    return result;
  }

  private static long timestampToNanos(final Timestamp timestamp) {
    return TimeUnit.SECONDS.toNanos(timestamp.getSeconds()) + timestamp.getNanos();
  }

  private static Integer errorCode(@Nullable final Status status) {
    if (status == null || status.equals(Status.OK) || status.equals(Status.ALREADY_EXISTS)) {
      return 0;
    }

    return 1;
  }

  private Collection<List<DatadogSpan>> getTraces(Collection<SpanData> spanDataList) {
    final ArrayList<DatadogSpan> datadogSpans = new ArrayList<>();
    for (SpanData sd : spanDataList) {
      SpanContext sc = sd.getContext();

      final long startTime = timestampToNanos(sd.getStartTimestamp());
      final Timestamp endTimestamp =
          Optional.ofNullable(sd.getEndTimestamp()).orElseGet(() -> Tracing.getClock().now());
      final long endTime = timestampToNanos(endTimestamp);
      final long duration = endTime - startTime;

      final Long parentId =
          Optional.ofNullable(sd.getParentSpanId())
              .map(DatadogExporterHandler::convertSpanId)
              .orElse(null);

      final Map<String, AttributeValue> attributes = sd.getAttributes().getAttributeMap();
      final HashMap<String, String> meta = new HashMap<>();
      final HashMap<String, Number> metrics = new HashMap<>();

      final DDSpecialAttributes specialAttributes = new DDSpecialAttributes("UNKNOWN",this.service, this.type);
      attributes.entrySet().stream()
              .filter(entry -> entry.getValue() != null)
              .forEach(entry -> {
                if(entry.getKey().equals(DD_RESOURCE)){
                  specialAttributes.resource = attributeValueToString(entry.getValue());
                } else if (entry.getKey().equals(DD_SERVICE)){
                  specialAttributes.service = attributeValueToString(entry.getValue());
                } else if (entry.getKey().equals(DD_TYPE)){
                  specialAttributes.type = attributeValueToString(entry.getValue());
                } else if (entry.getKey().startsWith(DD_METRIC_)){
                  Number numberValue = attributeValueToNumber(entry.getValue());
                  if(null != numberValue){
                    metrics.put(entry.getKey().substring(DD_METRIC_.length()), numberValue);
                  }
                }else {
                  meta.put(entry.getKey(), attributeValueToString(entry.getValue()));
                }
              });

      final DatadogSpan span =
          new DatadogSpan(
              sc.getTraceId().getLowerLong(),
                  convertSpanId(sc.getSpanId()),
              sd.getName(),
                  specialAttributes.resource,
                  specialAttributes.service,
                  specialAttributes.type,
              startTime,
              duration,
              parentId,
              errorCode(sd.getStatus()),
              meta,
                  metrics);
      datadogSpans.add(span);
    }

    return datadogSpans.stream()
        .collect(Collectors.groupingBy(DatadogSpan::getTraceId, Collectors.toList()))
        .values();
  }

  @Override
  public void timeLimitedExport(Collection<SpanData> spanDataList) throws Exception {
    final Collection<List<DatadogSpan>> traces = getTraces(spanDataList);

    final String data = gson.toJson(traces);

    final HttpURLConnection connection = (HttpURLConnection) agentEndpoint.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("X-Datadog-Trace-Count", String.valueOf(traces.size()));
    connection.setDoOutput(true);
    OutputStream outputStream = connection.getOutputStream();
    outputStream.write(data.getBytes(Charset.defaultCharset()));
    outputStream.flush();
    outputStream.close();
    if (connection.getResponseCode() != 200) {
      throw new Exception("Response " + connection.getResponseCode());
    }
  }

  class DDSpecialAttributes {
    public DDSpecialAttributes(String resource, String service, String type) {
      this.resource = resource;
      this.service = service;
      this.type = type;
    }

    public String resource;
    public String service;
    public String type;
  }
}
