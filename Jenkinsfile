@Library('vl-jenkins') _


import no.nav.jenkins.*


    def maven = new maven()
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
        //agent any
        agent {
            node {
                label 'MASTER'
            }
        }
        parameters {
          string(defaultValue: 't4', description: '', name: 'miljo')
        }
        options {
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
                        
                        mRevision = maven.revision()
                        version = mRevision + changelist
                        githubRepoName = sh(script: "basename -s .git `git config --get remote.origin.url`", returnStdout: true).trim()
                        currentBuild.displayName = version

                        if (env.BRANCH_NAME == 'master') {
                            sh "git fetch"
                            latestTag = sh(script: "git describe --tags", returnStdout: true)?.trim()
                            latestTagCommitHash = sh(script: "git describe --tags | sed 's/.*\\_//'", returnStdout: true)?.trim()

                            echo "GIT_COMMIT_HASH: $GIT_COMMIT_HASH, latestTagCommitHash: $latestTagCommitHash"

                            skipBuild = GIT_COMMIT_HASH.equals(latestTagCommitHash)
                            if (skipBuild) {
                                version = latestTag
                                echo "No change detected in sourcecode, skipping build and deploy existing tag={$latestTag}."
                            }
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
                        fpgithub.updateBuildStatus(githubRepoName, "pending", GIT_COMMIT_HASH_FULL)
                        
                        withMaven (mavenSettingsConfig: 'navMavenSettings') {
                            buildEnvironment = new buildEnvironment()
                            buildEnvironment.setEnv()
                            if (maven.javaVersion() != null) {
                                buildEnvironment.overrideJDK(maven.javaVersion())
                            }

                            envs = sh(returnStdout: true, script: 'env | sort -h').trim()
                            echo("envs: " + envs)

                            sh "mvn -B -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$version clean install"
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
                      sh "sed \'$value\' .deploy/${MILJO}.yaml > nais.yaml"
                      sh "k config use-context preprod-fss"
                      sh "k apply -f nais.yaml"

                      def naisNamespace
                      if (MILJO == "p") {
                          naisNamespace = "default"
                      } else {
                          naisNamespace = MILJO
                      }
                      def exitCode=sh returnStatus: true, script: "k rollout status -n${naisNamespace} deployment/${ARTIFACTID}"
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
