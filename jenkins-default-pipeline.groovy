def verbose = params.VERBOSE ?: false
def cleanGradleRepository = params.CLEAN_GRADLE_REPOSITORY ?: false

def gitRefType = params.GIT_REF_TYPE ?: "branch"
def gitRef = params.GIT_REF ?: "master"

def publish = params.PUBLISH ?: false
def publishChannel = params.PUBLISH_CHANNEL ?: ""

def buildNumber = params.BUILD_NUMBER ?: "0"

if (publishChannel == "") {
    if (gitRef == "stable") {
        publishChannel = "default"
    } else if (gitRef.startsWith("snapshot") || gitRef.startsWith("support-")) {
        publishChannel = gitRef
    } else {
        publishChannel = "unknown"
    }
}

def gradleOptions = "-i -s " +
        "-Dplugin.verifier.home.dir=/root/.pluginVerifier " +
        "-Dorg.gradle.project.publishChannel=$publishChannel " +
        "-Dorg.gradle.project.buildNumber=$buildNumber"

podTemplate(
        activeDeadlineSeconds: 3600,
        idleMinutes: 1,
        // No need for secret volume, no mvn deploy done here.
        workspaceVolume: dynamicPVC(requestsSize: "10Gi"),
        volumes: [ persistentVolumeClaim(claimName: "midpoint-studio-gradle", mountPath: "/root/.gradle"),
                   persistentVolumeClaim(claimName: "midpoint-studio-plugin-verifier", mountPath: "/root/.pluginVerifier")],
        containers: [
                containerTemplate(name: 'jnlp',
                        image: 'jenkins/inbound-agent:4.13-2-alpine',
                        runAsUser: '0',
                        resourceRequestCpu: '1',
                        resourceLimitCpu: '1',
                        resourceRequestMemory: '1Gi',
                        resourceLimitMemory: '1Gi'),
                containerTemplate(name: 'jdk',
                        image: "${params.BUILDER_IMAGE ?: 'maven:3.8.5-openjdk-17'}",
                        runAsUser: '0',
                        ttyEnabled: true,
                        command: 'cat',
                        resourceRequestCpu: "${params.BUILDER_CPU ?: '4'}",
                        resourceLimitCpu: "${params.BUILDER_CPU ?: '4'}",
                        resourceRequestMemory: '4Gi',
                        resourceLimitMemory: '4Gi')
        ]
) {
    node(POD_LABEL) {
        try {
            lock("midpoint-studio-pvc-lock") {
                stage("clean-gradle-repository") {
                    if (cleanGradleRepository) {
                        sh """#!/bin/bash -ex       
                            rm -rf /root/.pluginVerifier/ides
                            ls -la /root/.gradle/
                            du -hs /root/.gradle/caches
                            rm -rf /root/.gradle/caches      
                        """
                    }
                }
                stage("checkout") {
                    def ref = gitRefType == "branch" ? gitRef : "refs/tags/${gitRef}"

                    checkout scmGit(
                            branches: [[name: "${ref}"]],
                            userRemoteConfigs: [[
                                                        url: 'https://github.com/Evolveum/midpoint-studio.git'
                                                ]]
                    )
                }
                stage("build") {
                    container('jdk') {
                        sh """#!/bin/bash -ex
                        if [ "${verbose}" = "true" ]
                        then
                            id
                            env | sort
                            ./gradlew --version
                        fi
    
                        ./gradlew --stop
                        ./gradlew clean buildPlugin verifyPlugin runPluginVerifier $gradleOptions
                    """
                    }
                }
                stage("publish") {
                    container('jdk') {
                        withCredentials([string(credentialsId: 'jetbrains-permanent-token', variable: 'PUBLISH_TOKEN')]) {
                            sh """#!/bin/bash -ex
                            if [ "${publish}" = "true" ]
                            then
                                ./gradlew publishPlugin $gradleOptions
                            fi
                        """
                        }
                    }
                }
                stage("post-build") {
                    archiveArtifacts artifacts: 'studio-idea-plugin/build/reports/pluginVerifier/**', followSymlinks: false
                }
                stage("cleanup") {
                    sh """#!/bin/bash -ex
                        git clean -f -d
                    """

                    sh """#!/bin/bash -ex
                        # Removes any jetbrains dep older than 30 days, mainly to remove old IDEA snapshots.
                        # Sometimes removes other stuff, but it should be refetched from remote repositories.
                        find /root/.gradle/caches/modules-2/files-2.1/com.jetbrains* -mindepth 2 -maxdepth 2 -mtime +30 -type d -exec rm -rf {} \\;
    
                        PLUGIN_VERIFIER_DIR=/root/.pluginVerifier/ides
    
                        if [ -d "\$PLUGIN_VERIFIER_DIR" ]; then
                          du -hs \$PLUGIN_VERIFIER_DIR\\..  
                        
                          find \$PLUGIN_VERIFIER_DIR -mindepth 1 -maxdepth 1 -mtime +30 -type d -exec rm -rf {} \\;
                        fi
                        
                        PLUGIN_CACHE_DIR=/root/.cache/pluginVerifier
                        if [ -d "\$PLUGIN_CACHE_DIR" ]; then
                          du -hs \$PLUGIN_CACHE_DIR\\  
                        
                          find \$PLUGIN_CACHE_DIR -mindepth 1 -maxdepth 2 -mtime +30 -type d -exec rm -rf {} \\\\;
                        fi
                    """
                }
            }
        } catch (Exception e) {
            currentBuild.result = 'FAILURE' // error below will not set result for mailer!
            error 'Marking build as FAILURE because of: ' + e
        } finally {
            try {
                step([$class: 'Mailer',
                      notifyEveryUnstableBuild: true,
                      recipients: env.DEFAULT_MAIL_RECIPIENT,
                      sendToIndividuals: false])

                sh """#!/bin/bash -ex
                    if [ "${verbose}" -ge 1 ]; then
                        df -h
                    fi
                """
            } catch (Exception e) {
                println 'Could not send email: ' + e
            }
        }
    }
}
