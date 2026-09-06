pipeline {

    agent any

    environment {

        PROJECT = "cicd-demo"

        APP_NAME = "springboot-demo"

        IMAGE =
          "image-registry.openshift-image-registry.svc:5000/${PROJECT}/${APP_NAME}:${BUILD_NUMBER}"

        OCP_SERVER = "https://api.ocp.example.com:6443"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Vageesh7795/java_project.git'
            }
        }

        stage('Build Application') {
            steps {
                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    mvn test
                '''
            }
        }

        stage('Login to OpenShift') {

            steps {

                withCredentials([
                    string(
                        credentialsId: 'openshift-token',
                        variable: 'OCP_TOKEN'
                    )
                ]) {

                    sh '''
                        oc login ${OCP_SERVER} \
                          --token=${OCP_TOKEN} \
                          --server=${OCP_SERVER}

                        oc project ${PROJECT}

                        oc whoami
                    '''
                }
            }
        }

        stage('Build Container Image') {

            steps {

                sh '''
                    podman build \
                      -t ${IMAGE} \
                      .
                '''
            }
        }

        stage('Login to Registry') {

            steps {

                withCredentials([
                    string(
                        credentialsId: 'openshift-token',
                        variable: 'OCP_TOKEN'
                    )
                ]) {

                    sh '''
                        podman login \
                          -u unused \
                          -p ${OCP_TOKEN} \
                          image-registry.openshift-image-registry.svc:5000 \
                          --tls-verify=false
                    '''
                }
            }
        }

        stage('Push Image') {

            steps {

                sh '''
                    podman push \
                      ${IMAGE} \
                      --tls-verify=false
                '''
            }
        }

        stage('Deploy to OpenShift') {

            steps {

                sh '''
                    oc apply -f deployment.yaml
                    oc apply -f service.yaml
                    oc apply -f route.yaml
                '''
            }
        }

        stage('Update Image') {

            steps {

                sh '''
                    oc set image deployment/${APP_NAME} \
                      ${APP_NAME}=${IMAGE}
                '''
            }
        }

        stage('Wait for Deployment') {

            steps {

                sh '''
                    oc rollout status \
                      deployment/${APP_NAME} \
                      --timeout=180s
                '''
            }
        }

        stage('Verify') {

            steps {

                sh '''
                    echo "===== Pods ====="

                    oc get pods \
                      -l app=${APP_NAME}

                    echo "===== Service ====="

                    oc get svc ${APP_NAME}

                    echo "===== Route ====="

                    oc get route ${APP_NAME}
                '''
            }
        }
    }

    post {

        success {
            echo "Deployment successful"
        }

        failure {
            echo "Deployment failed"
        }
    }
}
