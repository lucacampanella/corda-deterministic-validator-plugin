package com.github.lucacampanella.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.taskdefs.Jar;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;

public class CordaDeterministicValidatorPlugin implements Plugin<Project> {

        //private static final String BUILD_VERSION = JarExecPathFinderUtils.getBuildVersion();

        //private static final GradleVersion MIN_GRADLE_VERSION = new GradleVersion("4.10");

        @Override
        public void apply(Project project) {

            final String gradleVersion = project.getGradle().getGradleVersion();
            project.getLogger().info("Gradle version: " + gradleVersion);
//            project.getLogger().info("MIN_GRADLE_VERSION.compareTo(new GradleVersion(gradleVersion)) = {}",
//                    MIN_GRADLE_VERSION.compareTo(new GradleVersion(gradleVersion)));
//            if(MIN_GRADLE_VERSION.compareTo(new GradleVersion(gradleVersion)) > 0) {
//                throw new GradleException("Flows doc builder plugin doesn't support version " + gradleVersion + "\n" +
//                        "Minimum supported version: " + MIN_GRADLE_VERSION + ", please upgrade your gradle");
//            }

//            project.getLogger().info("Corda flows doc builder plugin: ");
//           // project.getLogger().info("Version: {}", BUILD_VERSION);
//            project.getLogger().trace(System.getProperty("user.dir"));

        }
    }
