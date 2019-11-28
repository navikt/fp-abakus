@Library('vl-jenkins') _


import no.nav.jenkins.*


def mvn = new maven()
def fpgithub = new fpgithub()
//def artifactId
def latestTag
def latestTagCommitHash
def githubRepoName
def MILJO = "t4"
def version
def GIT_COMMIT_HASH
def GIT_COMMIT_HASH_FULL
boolean skipBuild = false

pipeline {
    tools {
        jdk '11'
        maven 'default-maven'
    }
    //agent any
    agent {
        node {
            label 'MASTER'
        }
    }
    parameters {
        choice(name: 'miljo', choices: ['t4', 'q0', 'q1', 'p'], description: 'Miljøet applikasjonen skal deployes i. Default: t4')
        string(defaultValue: '', description: 'Overstyrer hvilken versjon som deployes til gitt miljø. Default(blank) deployer siste commit.', name: 'deployVersjon')
    }
    options {
        disableConcurrentBuilds()
        timestamps()
    }
    environment {
        DOCKERREGISTRY = "repo.adeo.no:5443"
        ARTIFACTID = readMavenPom().getArtifactId()
    }
    stages {

        stage('Checkout scm') {
            steps {
                script {
                    MILJO = params.miljo
                    Date date = new Date()

                    checkout scm
                    GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%h'", returnStdout: true)
                    GIT_COMMIT_HASH_FULL = sh(script: "git log -n 1 --pretty=format:'%H'", returnStdout: true)
                    changelist = "_" + date.format("YYYYMMddHHmmss") + "_" + GIT_COMMIT_HASH

                    mRevision = mvn.revision()
                    if("master".equalsIgnoreCase(env.BRANCH_NAME)) {
                        version = mRevision + changelist
                    } else {
                        version = mRevision + changelist + "-SNAPSHOT"
                    }
                    githubRepoName = sh(script: "basename -s .git `git config --get remote.origin.url`", returnStdout: true).trim()

                    if (env.BRANCH_NAME == 'master') {
                        sh "git fetch"
                        latestTag = sh(script: "git describe --tags", returnStdout: true)?.trim()
                        latestTagCommitHash = sh(script: "git describe \$(git rev-list --tags --max-count=1) | sed 's/.*\\_//'", returnStdout: true)?.trim()

                        echo "GIT_COMMIT_HASH: $GIT_COMMIT_HASH, latestTagCommitHash: $latestTagCommitHash"
                        def deployVersjon = params.deployVersjon
                        def deployGivenVersion = (deployVersjon != null && !"".equals(deployVersjon))
                        skipBuild = deployGivenVersion || GIT_COMMIT_HASH.equals(latestTagCommitHash)
                        if (skipBuild && deployGivenVersion) {
                            version = deployVersjon
                            echo "Version supplied, skipping build and deploy declared version={$version}."
                        } else if (skipBuild) {
                            version = latestTag
                            echo "No change detected in sourcecode, skipping build and deploy existing tag={$latestTag}."
                        }
                        currentBuild.displayName = version
                    }
                }
            }
        }

        stage('Set version') {
            steps {
                sh "mvn --version"
                //sh "echo $version > VERSION"
            }
        }

        stage('Build') {
            when {
                expression { return !skipBuild }
            }
            steps {
                script {
                    echo "Building $version"
                    currentBuild.displayName = version + "*"
                    fpgithub.updateBuildStatus(githubRepoName, "pending", GIT_COMMIT_HASH_FULL)

                    withMaven(mavenSettingsConfig: 'navMavenSettingsPkg', maven: 'maven-3.6.2') {
                        buildEnvironment = new buildEnvironment()
                        buildEnvironment.setEnv()
                        if (mvn.javaVersion() != null) {
                            buildEnvironment.overrideJDK(mvn.javaVersion())
                        }

                        envs = sh(returnStdout: true, script: 'env | sort -h').trim()
                        echo("envs: " + envs)

                        sh "mvn -B -e -U -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$version clean deploy"
                        sh "docker build --pull -t $DOCKERREGISTRY/$ARTIFACTID:$version ."
                        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                                          credentialsId   : 'nexusUser',
                                          usernameVariable: 'NEXUS_USERNAME',
                                          passwordVariable: 'NEXUS_PASSWORD']]) {
                            sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} ${DOCKERREGISTRY} && docker push ${DOCKERREGISTRY}/${ARTIFACTID}:${version}"
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        fpgithub.updateBuildStatus(githubRepoName, "success", GIT_COMMIT_HASH_FULL)
                    }
                }
                failure {
                    script {
                        fpgithub.updateBuildStatus(githubRepoName, "failure", GIT_COMMIT_HASH_FULL)
                    }
                }
            }
        }

        stage('Tag master') {
            when {
                branch 'master'
                expression { return !skipBuild }
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
                    sh "sed \'$value\' .deploy/${MILJO}.yaml > nais.yaml"

                    def naisNamespace
                    if (MILJO == "p") {
                        naisNamespace = "default"
                        sh "k config use-context prod-fss"
                    } else {
                        naisNamespace = MILJO
                        sh "k config use-context preprod-fss"
                    }
                    sh "k apply -f nais.yaml"

                    def exitCode = sh returnStatus: true, script: "k rollout status -n${naisNamespace} deployment/${ARTIFACTID}"
                    echo "exit code is $exitCode"
                    /*
                    if(exitCode == 0) {
                        def veraPayload = "{\"environment\": \"${MILJO}\",\"application\": \"${ARTIFACTID}\",\"version\": \"${version}\",\"deployedBy\": \"Jenkins\"}"
                        def response = httpRequest([
                                url                   : "https://vera.adeo.no/api/v1/deploylog",
                                consoleLogResponseBody: true,
                                contentType           : "APPLICATION_JSON",
                                httpMode              : "POST",
                                requestBody           : veraPayload,
                                ignoreSslErrors       : true
                        ])
                    }
                    */
                }
            }
        }
    }
}
