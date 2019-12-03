# Kieker Exporter Plugin

This plugin contains a Kieker OpenCensus Exporter and adds it as a plugin to the inspectIT Ocelot Java agent.
By loading and configuring this plugin, you can export traces to a ApacheMQ JMS queue where Kieker is able to retrieve them.

The plugin offers the following configuration options, which can be configured just like any configuration option of inspectIT Ocelot:
```
inspectit:
  plugins:
    kieker:
      enabled: (true / false, defaults to true)
      jms-connection-url: (string, defaults to null)
      jms-queue-name: (string, defaults to ${inspectit.service-name})
```

After you have configured your connection url, e.g. by providing the following environment variable:
```
INSPECTIT_PLUGINS_KIEKER_JMSCONNECTIONURL=...
```
the exporter will be started.

## Instrumentation

Traces which shall be sent require the two attributes "eoi" and "ess". 

Within /resources is a config.yml file with a sample configuration for the InspectIT Ocelot Java agent.
The given configuration can be adjusted so that traces will receive the needed attributes.