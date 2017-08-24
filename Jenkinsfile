def pod_label = "${env.JOB_NAME}-${env.BUILD_NUMBER}".replace("/", "-")
def scala_version = "2.12.8"
podTemplate(
        label: pod_label,
        containers: [
                containerTemplate(name: 'jnlp', image: env.JNLP_SLAVE_IMAGE, args: '${computer.jnlpmac} ${computer.name}', alwaysPullImage: true),
                containerTemplate(name: 'sbt', image: "${env.PRIVATE_REGISTRY}/library/sbt:${scala_version}-fabric8", ttyEnabled: true, command: 'cat', alwaysPullImage: true),
                containerTemplate(name: 'dind', image: 'docker:stable-dind', privileged: true, ttyEnabled: true, command: 'dockerd', args: '--host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=vfs')
        ],
        volumes: [
                emptyDirVolume(mountPath: '/var/run', memory: false),
                hostPathVolume(mountPath: "/etc/docker/certs.d/${env.PRIVATE_REGISTRY}/ca.crt", hostPath: "/etc/docker/certs.d/${env.PRIVATE_REGISTRY}/ca.crt"),
                hostPathVolume(mountPath: '/home/jenkins/.kube/config', hostPath: '/etc/kubernetes/admin.conf'),
                persistentVolumeClaim(claimName: env.JENKINS_IVY2, mountPath: '/home/jenkins/.ivy2', readOnly: false),
        ]) {

    node(pod_label) {
        stage('build') {
            checkout scm
            container('sbt') {
                sh "sbt compile"
            }
        }
        stage('test') {
            container('sbt') {
                sh "sbt test"
            }
        }
    }
}