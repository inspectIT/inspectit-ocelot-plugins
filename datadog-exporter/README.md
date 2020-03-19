# Datadog Exporter Plugin

This plugins adds an OpenCensus exporter for [Datadog](https://www.datadoghq.com/) as a plugin to the inspectIT Ocelot Java agent.
By loading and configuring this plugin, you can export traces to a Datadog Agent.

The plugin offers the following configuration options, which can be configured just like any configuration option of inspectIT Ocelot:
```
inspectit:
  plugins:
    lightstep:
      enabled: (true / false, defaults to true)
      url: (string, defaults to http://localhost:8126/v0.3/traces)
      service-name: (string, defaults to ${inspectit.service-name})
```