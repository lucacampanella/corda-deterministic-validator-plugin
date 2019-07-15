package com.github.lucacampanella.cordadeterministicvalidatorplugin;

import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySubstitutions;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Optional;

public class CordaDeterministicValidatorPlugin implements Plugin<Project> {

    private static final String CORDA_RELAESE_GROUP = "net.corda";
    private static final String DEFAULT_CORDA_VERSION = "4.0";
    private static final String DETERMINISTIC_IDENTIFIER = "-deterministic";

    @Override
    public void apply(Project project) {

        final String gradleVersion = project.getGradle().getGradleVersion();
        final Logger logger = project.getLogger();
        logger.info("Gradle version: {}", gradleVersion);


        final JavaCompile javaDeterministicCompileInternal = project.getTasks()
                .create("javaDeterministicCompile", JavaCompile.class);
        final Configuration deterministicImplementation = project.getConfigurations().create("deterministicImplementation");

        project.afterEvaluate(prog -> {

            final JavaCompile javaCompileTask = (JavaCompile) project.getTasks().getByName("compileJava");

        deterministicImplementation.extendsFrom(project.getConfigurations().getByName("compileClasspath"));

        substitute(deterministicImplementation, "corda-serialization", DEFAULT_CORDA_VERSION);
        substitute(deterministicImplementation, "corda-core", DEFAULT_CORDA_VERSION);

        javaDeterministicCompileInternal.setClasspath(deterministicImplementation);
        javaDeterministicCompileInternal.setSource(javaCompileTask.getSource());

        final File outputDir = project.file("build/deterministic");
        outputDir.mkdirs();
        javaDeterministicCompileInternal.setDestinationDir(outputDir);
    });

   }

   private static Optional<Dependency> getDependency(Configuration configuration, String cordaDepName) {
       return configuration.getAllDependencies()
               .stream()
               .filter(dep -> dep.getName().contains(cordaDepName) &&
                       dep.getGroup().equals(CORDA_RELAESE_GROUP)).findAny();
   }

    private void substitute(Configuration configuration, String cordaDepName, String defaultVers) {
        final DependencySubstitutions depSub = configuration.getResolutionStrategy().getDependencySubstitution();

        String cordaVersion = defaultVers;
        final Optional<Dependency> cordaSerializationDeterministic = getDependency(configuration,
                cordaDepName + DETERMINISTIC_IDENTIFIER);
        if(cordaSerializationDeterministic.isPresent()) {
            cordaVersion = cordaSerializationDeterministic.get().getVersion();
            depSub.substitute(depSub.module(CORDA_RELAESE_GROUP + ":" + cordaDepName + DETERMINISTIC_IDENTIFIER + ":"))
                    .with(depSub.module(CORDA_RELAESE_GROUP + ":" + cordaDepName + DETERMINISTIC_IDENTIFIER + ":" + cordaVersion));
        }
        else {
            final Optional<Dependency> cordaSerialization = getDependency(configuration,
                    cordaDepName);
            if(cordaSerialization.isPresent()) {
                cordaVersion = cordaSerialization.get().getVersion();
            }
        }

        depSub.substitute(depSub.module(CORDA_RELAESE_GROUP + ":" + cordaDepName))
                .with(depSub.module(CORDA_RELAESE_GROUP + ":" + cordaDepName + DETERMINISTIC_IDENTIFIER + ":" + cordaVersion));
    }
}
