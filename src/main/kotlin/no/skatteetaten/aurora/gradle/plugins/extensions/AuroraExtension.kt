package no.skatteetaten.aurora.gradle.plugins.extensions

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.util.ConfigureUtil.configure

@Suppress("unused")
open class AuroraExtension(private val project: Project) {
    val versions: VersionsConfiguration
        get() = project.getVersionsExtension()
    val features: FeaturesConfiguration
        get() = project.getFeaturesExtension()

    fun versions(config: Closure<VersionsConfiguration>) = configure(config, project.getVersionsExtension())
    fun versions(configuration: Action<VersionsConfiguration>) = configuration.execute(project.getVersionsExtension())
    fun features(config: Closure<FeaturesConfiguration>) = configure(config, project.getFeaturesExtension())
    fun features(configuration: Action<FeaturesConfiguration>) = configuration.execute(project.getFeaturesExtension())

    val useAuroraDefaults: AuroraExtension
        get() = configureAuroraDefaults()

    fun useAuroraDefaults(): AuroraExtension = configureAuroraDefaults()

    private fun configureAuroraDefaults(): AuroraExtension {
        useVersions()
        useSonar()
        useGradleLogger()
        useKotlin {
            useKtLint()
        }
        useSpringBoot {
            useWebFlux()
            useCloudContract()
        }

        return this
    }

    val useGradleLogger: AuroraExtension
        get() = configureGradleLogger()

    fun useGradleLogger(): AuroraExtension = configureGradleLogger()

    private fun configureGradleLogger(): AuroraExtension {
        if (!project.plugins.hasPlugin("com.adarshr.test-logger")) {
            project.plugins.apply("com.adarshr.test-logger")
        }

        return this
    }

    val useSonar: AuroraExtension
        get() = configureSonar()

    fun useSonar(): AuroraExtension = configureSonar()

    private fun configureSonar(): AuroraExtension {
        if (!project.plugins.hasPlugin("org.sonarqube")) {
            project.plugins.apply("org.sonarqube")
        }

        return this
    }

    val useAsciiDoctor: AuroraExtension
        get() = configureAsciiDoctor()

    fun useAsciiDoctor(): AuroraExtension = configureAsciiDoctor()

    private fun configureAsciiDoctor(): AuroraExtension {
        if (!project.plugins.hasPlugin("org.asciidoctor.convert")) {
            project.plugins.apply("org.asciidoctor.convert")
        }

        return this
    }

    val useVersions: AuroraExtension
        get() = configureVersions()

    fun useVersions(): AuroraExtension = configureVersions()

    private fun configureVersions(): AuroraExtension {
        if (!project.plugins.hasPlugin("com.github.ben-manes.versions")) {
            project.plugins.apply("com.github.ben-manes.versions")
        }

        return this
    }

    val usePitest: AuroraExtension
        get() = configurePitest()

    fun usePitest(): AuroraExtension = configurePitest()

    private fun configurePitest(): AuroraExtension {
        if (!project.plugins.hasPlugin("info.solidsoft.pitest")) {
            project.plugins.apply("info.solidsoft.pitest")
        }

        return this
    }

    val useKotlin: UseKotlin
        get() = configureKotlin()

    fun useKotlin(): UseKotlin = configureKotlin()

    fun useKotlin(configuration: Closure<UseKotlin>): UseKotlin {
        val useKotlinExt = configureKotlin()

        configure(configuration, useKotlinExt)

        return useKotlinExt
    }

    fun useKotlin(configuration: Action<UseKotlin>): UseKotlin {
        val useKotlinExt = configureKotlin()

        configuration.execute(useKotlinExt)

        return useKotlinExt
    }

    private fun configureKotlin(): UseKotlin {
        if (!project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            project.plugins.apply("org.jetbrains.kotlin.jvm")
        }

        if (project.hasSpringBootButNotKotlinSpringPlugin()) {
            project.plugins.apply("org.jetbrains.kotlin.plugin.spring")
        }

        return project.getUseKotlinExtension()
    }

    val useSpringBoot: UseSpringBoot
        get() = configureSpringBoot()

    fun useSpringBoot() = configureSpringBoot()

    fun useSpringBoot(configuration: Closure<UseSpringBoot>): UseSpringBoot {
        val useSpringBootExt = configureSpringBoot()

        configure(configuration, useSpringBootExt)

        return useSpringBootExt
    }

    fun useSpringBoot(configuration: Action<UseSpringBoot>): UseSpringBoot {
        val useSpringBootExt = configureSpringBoot()

        configuration.execute(useSpringBootExt)

        return useSpringBootExt
    }

    private fun configureSpringBoot(): UseSpringBoot {
        if (!project.plugins.hasPlugin("org.springframework.boot")) {
            project.plugins.apply("org.springframework.boot")
        }

        if (project.hasKotlinButNotKotlinSpringPlugin()) {
            project.plugins.apply("org.jetbrains.kotlin.plugin.spring")
        }

        return project.getUseSpringBootExtension()
    }

    private fun Project.hasKotlinButNotKotlinSpringPlugin(): Boolean =
        plugins.hasPlugin("org.jetbrains.kotlin.jvm") && !plugins.hasPlugin("org.jetbrains.kotlin.plugin.spring")

    private fun Project.hasSpringBootButNotKotlinSpringPlugin(): Boolean =
        plugins.hasPlugin("org.springframework.boot") && !plugins.hasPlugin("org.jetbrains.kotlin.plugin.spring")

    private fun Project.getUseKotlinExtension(): UseKotlin {
        val extension = project.extensions.getByType(AuroraExtension::class.java)

        return (extension as ExtensionAware).extensions.getByType(UseKotlin::class.java)
    }
}

fun Project.getUseSpringBootExtension(): UseSpringBoot {
    val extension = project.extensions.getByType(AuroraExtension::class.java)

    return (extension as ExtensionAware).extensions.getByType(UseSpringBoot::class.java)
}

fun Project.getVersionsExtension(): VersionsConfiguration {
    val extension = project.extensions.getByType(AuroraExtension::class.java)

    return (extension as ExtensionAware).extensions.getByType(VersionsConfiguration::class.java)
}

fun Project.getFeaturesExtension(): FeaturesConfiguration {
    val extension = project.extensions.getByType(AuroraExtension::class.java)

    return (extension as ExtensionAware).extensions.getByType(FeaturesConfiguration::class.java)
}
