# inspectIT Ocelot Plugins
OpenCensus comes with a wide range of exporter which we cannot all include in our Java Agent [inspectIT Ocelot](https://github.com/inspectIT/inspectit-ocelot). For this reason you can use plugins to add any OpenCensus Metrics or Trace exporter you want to Ocelot.

See the [Documentation](http://docs.inspectit.rocks/) on how to configure your Agent to load your plugins.
For building the plugin jar files see the isntructions below.

## Building the Plugins

The plugins depend on two projects of inspectIT Ocelot: The `inspectit-ocelot-sdk` and the `inspectit-ocelot-config` project. As we do not push these yet to the Maven Central repository, you need to build them locally and add them to your local maven repository.
To do so, clone the [Ocelot Repository](https://github.com/inspectIT/inspectit-ocelot) and simply run the following command in its root directory:
```
./gradlew install
```
Afterwards you can now build the plugins from this repository you would like to use.
cd into the project directory you would like to build and run
```
./gradlew jar
```
The jar you can now find in the `/out/lib` folder is ready to be used as plugin for inspectIT Ocelot.