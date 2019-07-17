package com.github.lucacampanella.cordadeterministicvalidatorplugin;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySubstitutions;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class CordaDeterministicValidatorPlugin implements Plugin<Project> {

    private static final String CORDA_RELAESE_GROUP = "net.corda";
    private static final String DEFAULT_CORDA_VERSION = "4.0";
    private static final String DETERMINISTIC_IDENTIFIER = "-deterministic";

    private static final String BOOTH_CLASSPATH_ARG = "-bootclasspath";

    @Override
    public void apply(Project project) {

        final String gradleVersion = project.getGradle().getGradleVersion();
        final Logger logger = project.getLogger();
        logger.info("Gradle version: {}", gradleVersion);

        project.getRepositories().maven(mavenArtifactRepository ->
                mavenArtifactRepository.setUrl("https://ci-artifactory.corda.r3cev.com/artifactory/corda-dev"));


        final Configuration deterministicJdkConf = project.getConfigurations().create("deterministicJdk");
        deterministicJdkConf.getDependencies().add(
                project.getDependencies().create("net.corda:deterministic-rt:latest.integration:api")
        );

        final Copy installJdkTask = project.getTasks()
                .create("installJdk", Copy.class);

        final File jdkHome = project.file(project.getRootDir().getPath() + "/jdk");
        final File rtJar = project.file(jdkHome.getPath() + "/jre/lib/rt.jar");

        installJdkTask.getOutputs().dir(jdkHome.getPath());
        installJdkTask.from(deterministicJdkConf).rename("deterministic-rt-(.*).jar", "rt.jar");
        installJdkTask.into(project.file(jdkHome.getPath() +  "/jre/lib").getPath());
        installJdkTask.doLast(task -> {
            final String eol = System.getProperty("line.separator");
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(project.file(jdkHome.getPath() + "/release"), false);
                fileWriter.write("JAVA_VERSION=\"1.8.0_172\"" + eol);
            } catch (IOException e) {
                throw new GradleException("Could not write to file", e);
            }
            // false to overwrite.
            project.file(jdkHome.getPath() + "/bin").mkdir();

            try {
                final File javac = project.file(jdkHome.getPath() + "/bin/javac");
                fileWriter = new FileWriter(javac, false);
                fileWriter.write("#!/bin/sh\necho \"javac 1.8.0_172\"\n");
                final boolean setExecRes = javac.setExecutable(true, false);
                if(!setExecRes) {
                    throw new GradleException("Could not set javac as an executable");
                }

            } catch (IOException e) {
                throw new GradleException("Could not write to file", e);
            }
        });



        final JavaCompile javaCompileTask = (JavaCompile) project.getTasks().getByName("compileJava");
        final JavaCompile javaDeterministicCompileTask = project.getTasks()
                .create("compileDeterministicJava", JavaCompile.class);
        final Configuration deterministicImplementation = project.getConfigurations().create("deterministicImplementation");
        javaDeterministicCompileTask.dependsOn(installJdkTask);

        project.afterEvaluate(prog -> {

            setupCompilerArgs(rtJar, javaCompileTask, javaDeterministicCompileTask);

            logger.info("javaDeterministicCompileTask.getOptions().getAllCompilerArgs() = {}",
                    javaDeterministicCompileTask.getOptions().getAllCompilerArgs());

            deterministicImplementation.extendsFrom(project.getConfigurations().getByName("compileClasspath"));

            substitute(deterministicImplementation, "corda-serialization", DEFAULT_CORDA_VERSION);
            substitute(deterministicImplementation, "corda-core", DEFAULT_CORDA_VERSION);

            javaDeterministicCompileTask.setClasspath(deterministicImplementation);
            javaDeterministicCompileTask.setSource(javaCompileTask.getSource());
            javaDeterministicCompileTask.getOptions()
                    .setAnnotationProcessorPath(javaCompileTask.getOptions().getAnnotationProcessorPath());

            final File outputDir = project.file("build/deterministic");
            outputDir.mkdirs();
            javaDeterministicCompileTask.setDestinationDir(outputDir);
        });

    }

    private void setupCompilerArgs(File rtJar, JavaCompile javaCompileTask, JavaCompile javaDeterministicCompileTask) {
        LinkedHashSet<String> compilerArgsSet = new LinkedHashSet<>(javaCompileTask.getOptions().getCompilerArgs());

        final List<String> compilerArgs = javaDeterministicCompileTask.getOptions().getCompilerArgs();

        if(compilerArgsSet.contains(BOOTH_CLASSPATH_ARG)) {
            final int indexOfBoothClasspath = compilerArgs.indexOf(BOOTH_CLASSPATH_ARG);
            compilerArgs.remove(indexOfBoothClasspath);
            compilerArgs.remove(indexOfBoothClasspath);
        }
        compilerArgs.add(BOOTH_CLASSPATH_ARG);
        compilerArgs.add(rtJar.getPath());

        compilerArgsSet.addAll(compilerArgs);
        compilerArgsSet.add("-parameters");
        javaDeterministicCompileTask.getOptions().setCompilerArgs(new ArrayList<>(compilerArgsSet));
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
                .with(depSub.module(CORDA_RELAESE_GROUP + ":" + cordaDepName + DETERMINISTIC_IDENTIFIER + ":"
                        + cordaVersion));
    }
}
