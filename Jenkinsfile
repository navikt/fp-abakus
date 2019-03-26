@Library('vl-jenkins') _

import no.nav.jenkins.*

void setBuildStatus(String message, String state, String commitSha) {
    step([
            $class            : "GitHubCommitStatusSetter",
            reposSource       : [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/navikt/fp-abakus"],
            commitShaSource   : [$class: "ManuallyEnteredShaSource", sha: commitSha],
            contextSource     : [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
            errorHandlers     : [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
            statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
    ]);
}

def maven = new maven()
def version
def GIT_COMMIT_HASH
pipeline {
    agent any

    stages {

        stage('Checkout Tags') { // checkout only tags.
            steps {
                script {
                    Date date = new Date()
                    dockerRegistryIapp = "repo.adeo.no:5443"

                    checkout scm
                    GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%h'", returnStdout: true)
                    changelist = "_" + date.format("YYYYMMDDHHmmss") + "_" + GIT_COMMIT_HASH
                    mRevision = maven.revision()
                    version = mRevision + changelist
                    echo "Tag to be deployed $version"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    configFileProvider(
                            [configFile(fileId: 'navMavenSettings', variable: 'MAVEN_SETTINGS')]) {
                        artifactId = maven.artifactId()
                        buildEnvironment = new buildEnvironment()
                        if (maven.javaVersion() != null) {
                            buildEnvironment.overrideJDK(maven.javaVersion())
                        }

                        sh "mvn -U -B -s $MAVEN_SETTINGS -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$version clean install"
                        sh "docker build --pull -t $dockerRegistryIapp/$artifactId:$version ."
                        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                          credentialsId   : 'nexusUser',
                                          usernameVariable: 'NEXUS_USERNAME',
                                          passwordVariable: 'NEXUS_PASSWORD']]) {
                            sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} ${dockerRegistryIapp} && docker push ${dockerRegistryIapp}/${artifactId}:${version}"
                        }
                    }
                }
            }
        }


    }

    post {
        success {
            setBuildStatus("Build succeeded", "SUCCESS", GIT_COMMIT_HASH);
        }
        failure {
            setBuildStatus("Build failed", "FAILURE", GIT_COMMIT_HASH);
        }
    }

}
