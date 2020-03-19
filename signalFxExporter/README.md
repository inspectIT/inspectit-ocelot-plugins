# SignalFx Exporter Plugin

This plugin adds the 
[SignalFx OpenCensus Exporter](https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/signalfx) 
as a plugin to the inspectIT Ocelot Java agent.
By loading and configuring this plugin, you can export metrics to SignalFx 
e.g. using [SignalFx Smart Agent](https://docs.signalfx.com/en/latest/apm/apm-instrument/). 
Instructions for the smart agent can be found [here](https://docs.signalfx.com/en/latest/integrations/agent/).

The plugin offers the following configuration options, 
which can be configured just like any configuration option of inspectIT Ocelot:
```
inspectit:
  plugins:
    signalfx:
      enabled: (true / false, defaults to true)
      token: (string, defaults to null)
      endpoint: (uri, defaults to https://ingest.signalfx.com)
      reporting-interval: (int, defaults to ${inspectit.metrics.frequency})
```
The default endpoint configuration will result in the usage of the SignalFx `us0` [realm](https://developers.signalfx.com/).
You can change this by configuring your endpoint: `https://ingest.<REALM>.signalfx.com`

After you have configured your [access-token](https://docs.signalfx.com/en/latest/admin-guide/tokens.html#working-with-access-tokens),
 e.g. by providing the following environment variable:
```
INSPECTIT_PLUGINS_SIGNALFX_TOKEN=...
```
the SignalFx exporter will be started.

## Restrictions
Unlike the other plugins, the SignalFx exporter cannot be restarted (e.g. when changing the configuration). 
In order to restart the SignalFx exporter plugin, the agent/ application has to be restarted.