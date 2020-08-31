package no.skatteetaten.aurora.gradle.plugins.functional

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import no.skatteetaten.aurora.gradle.plugins.taskStatus
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT
import java.io.File

@Execution(CONCURRENT)
class AuroraPluginSpringBootKotlinFunctionalTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        buildFile = testProjectDir.resolve("build.gradle")
        buildFile.createNewFile()
        buildFile.writeText(
            """
            plugins {
                id 'no.skatteetaten.gradle.aurora'
                id 'org.springframework.boot' version '2.3.3.RELEASE'
                id 'org.jetbrains.kotlin.jvm' version '1.3.72'
                id 'org.jetbrains.kotlin.plugin.spring' version '1.3.72'
            }
 
            repositories {
                mavenCentral()
            }
        """
        )
    }

    @Test
    fun `build test`() {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("build")
            .withPluginClasspath()
            .build()

        result.taskStatus()
    }

    @Test
    fun sunshineTest() {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("aurora")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("----- Aurora Plugin Report -----")
        assertThat(result.output).contains("com.fasterxml.jackson.module:jackson-module-kotlin")
        assertThat(result.output).contains("org.jetbrains.kotlin:kotlin-reflect")
        assertThat(result.output).contains("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        assertThat(result.output).contains("io.github.microutils:kotlin-logging")
        result.taskStatus(taskName = ":aurora")
    }

    @Test
    fun `mvc test`() {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("aurora")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("no.skatteetaten.aurora.springboot:aurora-spring-boot-mvc-starter")
        assertThat(result.output).doesNotContain("no.skatteetaten.aurora.springboot:aurora-spring-boot-webflux-starter")
        result.taskStatus(taskName = ":aurora")
    }

    @Test
    fun `webflux test`() {
        val gradleProps = testProjectDir.resolve("gradle.properties")
        gradleProps.createNewFile()
        gradleProps.writeText("""aurora.useWebFlux=true""")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("aurora")
            .withPluginClasspath()
            .build()

        assertThat(result.output).doesNotContain("no.skatteetaten.aurora.springboot:aurora-spring-boot-mvc-starter")
        assertThat(result.output).contains("no.skatteetaten.aurora.springboot:aurora-spring-boot-webflux-starter")
        assertThat(result.output).contains("webflux enabled and webmvc + tomcat excluded")
        result.taskStatus(taskName = ":aurora")
    }

    @Test
    fun `bootjar test`() {
        val gradleProps = testProjectDir.resolve("gradle.properties")
        gradleProps.createNewFile()
        gradleProps.writeText("""aurora.useBootJar=true""")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("aurora")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("no.skatteetaten.aurora.springboot:aurora-spring-boot-mvc-starter")
        assertThat(result.output).doesNotContain("no.skatteetaten.aurora.springboot:aurora-spring-boot-webflux-starter")
        assertThat(result.output).contains("bootJar enabled")
        assertThat(result.output).doesNotContain("bootJar disabled")
        result.taskStatus(taskName = ":aurora")
    }

    @Test
    fun `devtools test`() {
        val gradleProps = testProjectDir.resolve("gradle.properties")
        gradleProps.createNewFile()
        gradleProps.writeText(
            """
            aurora.useBootJar=true
            aurora.springDevTools=true
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("aurora")
            .withPluginClasspath()
            .build()

        assertThat(result.output).contains("no.skatteetaten.aurora.springboot:aurora-spring-boot-mvc-starter")
        assertThat(result.output).contains("org.springframework.boot:spring-boot-devtools")
        assertThat(result.output).doesNotContain("no.skatteetaten.aurora.springboot:aurora-spring-boot-webflux-starter")
        assertThat(result.output).contains("bootJar enabled")
        assertThat(result.output).doesNotContain("bootJar disabled")
        result.taskStatus(taskName = ":aurora")
    }
}
