# Lightstep Exporter Plugin

This plugins adds the [Lightstep OpenCensus Exporter](https://github.com/lightstep/lightstep-census-java) as a plugin to the inspectIT Ocelot Java agent.
By loading and configuring this plugin, you can directly export traces to lightstep.

The plugin offers the following configuration options, which can be configured just like any configuration option of inspectIT Ocelot:
```
inspectit:
  plugins:
    lightstep:
      enabled: (true / false, defaults to true)
      access-token: (string, defaults to null)
      service-name: (string, defaults to ${inspectit.service-name})
```

After you have configured your API-token, e.g. by providing the following environment variable:
```
INSPECTIT_PLUGINS_LIGHTSTEP_ACCESSTOKEN=...
```
the lightstep exporter will be started.