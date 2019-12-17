# Haystack Exporter Plugin

This plugins adds the [Haystack OpenCensus Exporter](https://github.com/ExpediaDotCom/haystack-opencensus-exporter-java)
as a plugin to the inspectIT Ocelot Java agent.
By loading and configuring this plugin, you can directly export traces to haystack.

The plugin offers the following configuration options, 
which can be configured just like any configuration option of inspectIT Ocelot:
```
inspectit:
  plugins:
    haystack:
      enabled: (true / false, defaults to true)
      host: (string, defaults to null)
      port: (int, defaults to 35000)
      service-name: (string, defaults to ${inspectit.service-name})
```

After you have configured your host, e.g. by providing the following environment variable:
```
INSPECTIT_PLUGINS_HAYSTACK_HOST=...
```
the haystack exporter will be started.