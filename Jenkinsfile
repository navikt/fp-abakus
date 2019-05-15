@Library('vl-jenkins') _

import no.nav.jenkins.*

def maven = new maven()
def fpgithub = new fpgithub()
def latestTag
def latestTagCommitHash
def version
def GIT_COMMIT_HASH
def GIT_COMMIT_HASH_FULL
boolean skipBuild = false
pipeline {
    agent any

    stages {

        stage('Checkout scm') { // checkout only tags.
            steps {
                script {
                    Date date = new Date()
                    DOCKERREGISTRY = "repo.adeo.no:5443"

                    checkout scm
                    GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%h'", returnStdout: true)
                    GIT_COMMIT_HASH_FULL = sh(script: "git log -n 1 --pretty=format:'%H'", returnStdout: true)
                    changelist = "_" + date.format("YYYYMMddHHmmss") + "_" + GIT_COMMIT_HASH
                    mRevision = maven.revision()
                    version = mRevision + changelist

                    currentBuild.displayName = version
                    if (env.BRANCH_NAME == 'master') {
                        latestTag = sh(script: "git describe --tags", returnStdout: true)?.trim()
                        latestTagCommitHash = sh(script: "git describe --tags | sed 's/.*\\_//'", returnStdout: true)?.trim()
                        skipBuild = GIT_COMMIT_HASH.equals(latestTagCommitHash)
                        if (skipBuild) {
                            version = latestTag
                            echo "No change detected in sourcecode, skipping build and deploy existing tag={$latestTag}."
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return !skipBuild }
            }
            steps {
                script {
                    echo "Building $version"
                    configFileProvider(
                            [configFile(fileId: 'navMavenSettings', variable: 'MAVEN_SETTINGS')]) {
                        artifactId = maven.artifactId()
                        buildEnvironment = new buildEnvironment()
                        if (maven.javaVersion() != null) {
                            buildEnvironment.overrideJDK(maven.javaVersion())
                        }

                        sh "mvn -U -B -s $MAVEN_SETTINGS -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$version clean install"
                        sh "docker build --pull -t ${DOCKERREGISTRY}/fpabakus:$version ."
                        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                          credentialsId   : 'nexusUser',
                                          usernameVariable: 'NEXUS_USERNAME',
                                          passwordVariable: 'NEXUS_PASSWORD']]) {
                            sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} ${DOCKERREGISTRY} && docker push ${DOCKERREGISTRY}/fpabakus:${version}"
                        }
                    }
                }
            }
        }

        stage('Tag master') {
            when {
                branch 'master'
                expression { return !(latestTagCommitHash == GIT_COMMIT_HASH) }
            }
            steps {
                sh "git tag $version -m $version"
                sh "git push origin --tag"
            }
        }

        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                script {
                    def value = "s/RELEASE_VERSION/${version}/g"
                    sh "sed \'$value\' .deploy/t4.yaml > nais.yaml"
                    sh "k config use-context preprod-fss"
                    sh "k apply -f nais.yaml"
                }
            }
        }
    }

    post {
        success {
            script {
                fpgithub.updateBuildStatus("fp-abakus", "success", GIT_COMMIT_HASH_FULL)
            }
        }
        failure {
            script {
                fpgithub.updateBuildStatus("fp-abakus", "failure", GIT_COMMIT_HASH_FULL)
            }
        }
    }
}
