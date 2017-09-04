def podLabel = "${env.JOB_NAME}-${env.BUILD_NUMBER}".replace('/', '-').replace('.', '')
def scalaVersion = "2.12.3"

podTemplate(
        label: podLabel,
        containers: [
                containerTemplate(name: 'jnlp', image: env.JNLP_SLAVE_IMAGE, args: '${computer.jnlpmac} ${computer.name}', alwaysPullImage: true),
                containerTemplate(name: 'sbt', image: "${env.PRIVATE_REGISTRY}/library/sbt:${scalaVersion}-fabric8", ttyEnabled: true, command: 'cat', alwaysPullImage: true),
                containerTemplate(name: 'helm', image: env.HELM_IMAGE, ttyEnabled: true, command: 'cat'),
                containerTemplate(name: 'dind', image: 'docker:stable-dind', privileged: true, ttyEnabled: true, command: 'dockerd', args: '--host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=vfs')
        ],
        volumes: [
                emptyDirVolume(mountPath: '/var/run', memory: false),
                hostPathVolume(mountPath: "/etc/docker/certs.d/${env.PRIVATE_REGISTRY}/ca.crt", hostPath: "/etc/docker/certs.d/${env.PRIVATE_REGISTRY}/ca.crt"),
                hostPathVolume(mountPath: '/home/jenkins/.kube/config', hostPath: '/etc/kubernetes/admin.conf'),
                persistentVolumeClaim(claimName: env.JENKINS_IVY2, mountPath: '/home/jenkins/.ivy2', readOnly: false),
        ]) {

    node(podLabel) {
        ansiColor('xterm') {
            stage('build') {
                checkout scm
                container('sbt') {
                    sh "sbt compile"
                }
            }

            stage('test') {
                container('sbt') {
                    sh "sbt cucumber"
                }
            }

            def HEAD = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            def imageTag = "${HEAD}-${env.BUILD_NUMBER}"
            def imageRepo = "${env.PRIVATE_REGISTRY}/inu/storedq-domain"
            def image

            stage('build image') {
                container('sbt') {
                    sh "sbt cpJarsForDocker"
                }
                dir('target/docker') {
                    def mainClass = sh(returnStdout: true, script: 'cat mainClass').trim()
                    image = docker.build(imageRepo, "--pull --build-arg JAVA_MAIN_CLASS=${mainClass} .")
                }
            }

            stage('push image') {
                docker.withRegistry(env.PRIVATE_REGISTRY_URL, 'docker-login') {
                    image.push(imageTag)
                }
            }

            stage('helm init') {
                container('helm') {
                    sh 'helm init --client-only'
                }
            }

            stage('install chart') {
                container('helm') {
                    dir('helm/storedq-domain') {
                        def release = "storedq-domain-${env.BUILD_ID}"

                        sh 'helm lint .'
                        sh "helm install -n ${release} --set=image.tag=${imageTag} ."

                        try {
                            sh "helm test ${release} --cleanup"
                        } catch (err) {
                            echo "${error}"
                            currentBuild.result = FAILURE
                        }
                        finally {
                            sh "helm delete --purge ${release}"
                        }
                    }
                }
            }
        }
    }
}