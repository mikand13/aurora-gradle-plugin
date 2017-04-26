package no.skatteetaten.aurora.gradle.plugins

import org.gradle.api.Project

class NexusStagingTools {

  static addNexusStagingTasks(Project p, String stagingProfileId) {
    p.with {
      configurations {
        nexusStaging {
          transitive = false
        }
      }
      dependencies {
        nexusStaging group: 'org.sonatype.nexus.ant', name: 'nexus-staging-ant-tasks', version: '1.6.3', classifier: 'uber'
      }

      ext.nexusStagingInfoId = "target-nexus"
      ext.stagingDirectory = file("${buildDir}/stage")
      ext.repositoryDirectory = file("${buildDir}/repository")

      task('deploy', description: 'Build and deploy artifacts to staging and release staging repository',
          dependsOn: 'stageRemotely')

      task('configure', description: "Configure staging deployment to $nexusUrl") {
        doLast {
          ant.taskdef(uri: 'staging', resource: 'org/sonatype/nexus/ant/staging/antlib.xml', classpath: configurations.nexusStaging.asPath)
          ant.'staging:nexusStagingInfo'(id: nexusStagingInfoId, stagingDirectory: stagingDirectory) {
            'staging:projectInfo'(stagingProfileId: stagingProfileId)
            'staging:connectionInfo'(baseUrl: nexusUrl) {
              'staging:authentication'(username: nexusUsername, password: nexusPassword)
            }
          }
        }
      }

      task('createRepository', dependsOn: [build], description: 'Create a local repository as build output') {
        inputs.dir "${buildDir}/distributions"
        outputs.dir repositoryDirectory
        doLast {
          // createPage a local Maven repository in repositoryDirectory using the project coordinates
          String groupIdPath = project.group.replace('.', '/')
          String repositoryPath = "${repositoryDirectory}/${groupIdPath}/${project.name}/${project.version}"
          File repositoryDirectory = new File(repositoryPath)
          repositoryDirectory.mkdirs()
          // adding a Maven pom file
          String pomFileName = "${repositoryPath}/${project.name}-${project.version}.pom"
          // pom will automatically have the dependencies and project coordinates included, you can add more here
          pom {}.writeTo(pomFileName)
          // adding the build output
          copy {
            from "${buildDir}/distributions"
            into repositoryDirectory
          }
          logger.lifecycle("    Build artifacts deployed in repository in ${repositoryDirectory}")
        }
      }

      task('stageLocally', dependsOn: [configure, build, createRepository], description: 'Prepares a local staging folder') {
        inputs.dir repositoryDirectory
        outputs.dir stagingDirectory
        doLast {
          ant.'staging:stageLocally' {
            'staging:nexusStagingInfo'(refid: nexusStagingInfoId)
            fileset(dir: repositoryDirectory, includes: "**/*.*")
          }
          logger.lifecycle("    Staged build output into ${stagingDirectory}")
        }
      }

      task('stageRemotely', dependsOn: stageLocally, description: 'Upload the local staging folder') {
        doLast {
          ant.'staging:stageRemotely' {
            'staging:nexusStagingInfo'(refid: nexusStagingInfoId)
          }
          logger.lifecycle("    Created the remote staging repository.")
        }
      }

      task('releaseStagingRepository', dependsOn: stageRemotely, description: 'Release the repo from staging') {
        doLast {
          ant.'staging:releaseStagingRepository'(description: 'release staging repo') {
            'staging:nexusStagingInfo'(refid: nexusStagingInfoId)
          }
          logger.lifecycle("    Released the remote staging repository.")
        }
      }
    }
  }
}
