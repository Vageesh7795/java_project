pipeline {

    agent any

    environment {

        PROJECT = 'cicd-demo'
        APP_NAME = 'java-app'

        OC_SERVER = 'https://api.YOUR-CLUSTER:6443'

        REGISTRY = 'image-registry.openshift-image-registry.svc:5000'

        IMAGE = "${REGISTRY}/${PROJECT}/${APP_NAME}:${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {

            steps {

                checkout scm

            }
        }


        stage('Verify Tools') {

            steps {

                sh '''
                    java -version
                    mvn -version
                    git --version
                    podman --version
                    oc version --client
                '''
            }
        }


        stage('Login to OpenShift') {

            steps {

                withCredentials([
                    string(
                        credentialsId: 'openshift-token',
                        variable: 'OC_TOKEN'
                    )
                ]) {

                    sh '''
                        oc login ${OC_SERVER} \
                          --token="$OC_TOKEN" \
                          --insecure-skip-tls-verify=true

                        oc project ${PROJECT}

                        oc whoami
                    '''
                }
            }
        }


        stage('Build Java Application') {

            steps {

                sh '''
                    mvn clean package -DskipTests=false
                '''
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


        stage('Login to OpenShift Registry') {

            steps {

                withCredentials([
                    string(
                        credentialsId: 'openshift-token',
                        variable: 'OC_TOKEN'
                    )
                ]) {

                    sh '''
                        podman login \
                          --tls-verify=false \
                          -u "$(oc whoami)" \
                          -p "$OC_TOKEN" \
                          ${REGISTRY}
                    '''
                }
            }
        }


        stage('Push Image') {

            steps {

                sh '''
                    podman push \
                      --tls-verify=false \
                      ${IMAGE}
                '''
            }
        }


        stage('Deploy') {

            steps {

                sh '''
                    oc apply -f k8s/deployment.yaml
                    oc apply -f k8s/service.yaml
                    oc apply -f k8s/route.yaml
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


        stage('Rollout') {

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
                    oc get pods
                    oc get svc
                    oc get route
                '''
            }
        }
    }


    post {

        success {

            echo 'CI/CD PIPELINE SUCCESSFUL'

        }

        failure {

            echo 'CI/CD PIPELINE FAILED'

        }

        always {

            sh '''
                podman logout ${REGISTRY} || true
            '''
        }
    }
}
