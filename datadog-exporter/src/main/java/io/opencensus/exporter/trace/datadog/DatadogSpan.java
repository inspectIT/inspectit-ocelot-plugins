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

import java.util.Map;

@SuppressWarnings("unused")
class DatadogSpan {
  // Required. The unique integer (64-bit unsigned) ID of the trace containing this span.
  private final long traceId;
  // Required. The span integer (64-bit unsigned) ID.
  private final long spanId;
  // Required. The span name. The span name must not be longer than 100 characters.
  private final String name;
  // Required. The resource you are tracing. The resource name must not be longer than 5000
  // characters.
  // A resource is a particular action for a service.
  private final String resource;
  // Required. The service you are tracing. The service name must not be longer than 100 characters.
  private final String service;
  // Required. The type of request.
  private final String type;
  // Required. The start time of the request in nanoseconds from the unix epoch.
  private final long start;
  // Required. The duration of the request in nanoseconds.
  private final long duration;
  // Optional. The span integer ID of the parent span.
  private final Long parentId;
  // Optional. Set this value to 1 to indicate if an error occured. If an error occurs, you
  // should pass additional information, such as the error message, type and stack information
  // in the meta property.
  private final Integer error;
  // Optional. A dictionary of key-value metadata. e.g. tags.
  private final Map<String, String> meta;

  private final Map<String, Number> metrics;

  long getTraceId() {
    return traceId;
  }

  DatadogSpan(
      final long traceId,
      final long spanId,
      final String name,
      final String resource,
      final String service,
      final String type,
      final long start,
      final long duration,
      final Long parentId,
      final Integer error,
      final Map<String, String> meta,
      final Map<String, Number> metrics) {
    this.traceId = traceId;
    this.spanId = spanId;
    this.name = name;
    this.resource = resource;
    this.service = service;
    this.type = type;
    this.start = start;
    this.duration = duration;
    this.parentId = parentId;
    this.error = error;
    this.meta = meta;
    this.metrics = metrics;
  }
}
