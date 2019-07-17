# corda-deterministic-validator-plugin
[![Build Status](https://travis-ci.org/lucacampanella/corda-deterministic-validator-plugin.svg?branch=master)](https://travis-ci.org/lucacampanella/corda-deterministic-validator-plugin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A gradle plugin to build your code against the deterministic version of Corda R3 and check you only use deterministic calls in contract calls

### Installing

#### As a Gradle plugin

To apply the plugin, in the `build.gradle` 
(see [gradle website](https://plugins.gradle.org/plugin/com.github.lucacampanella.cordadeterministicvalidatorplugin.corda-deterministic-validator-plugin)):

Using legacy plugin application:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.lucacampanella::cordadeterministicvalidatorplugin:+"
  }
}

apply plugin: "com.github.lucacampanella.cordadeterministicvalidatorplugin.corda-deterministic-validator-plugin"
```

Be careful: in any case the plugin should be applied after the Java plugin.

## Usage
Run `./gradlew compileDeterministicJava`

The plugin creates a new `JavaCompile` task, called `compileDeterministicJava` which copies the default task, but
compiles it with the deterministic Corda JDK and the deterministic Corda modules. 
(See [Corda docs](https://docs.corda.net/deterministic-modules.html))

To do this it creates a new configuration, called `deterministicImplementation`, which extends from `classpathCompile`.
By default the version of the deterministic Corda modules is `4.0`, but this can be changed by explicitly 
writing the dependencies on this configuration.
Example:
```
dependencies {
    deterministicImplementation "net.corda:corda-core-deterministic:$your_preferred_version"
    deterministicImplementation "net.corda:corda-serialization-deterministic:$your_preferred_version"
}
```

The `compileDeterministicJava` task copies the compiler args (`options.compilerArgs`), from `compileJava` task.
It also adds `-parameters` and the arguments needed for deterministic compilation. If you want to add compiler 
arguments only to the `compileDeterministicJava`, just do so using the Gradle DSL.
The tasks also copies the annotation processors configurations.