
def call(Map pipelineParams) {

    pipeline {
     agent {
        docker {
            image 'maven:3-alpine' 
            args '-v /root/.m2:/root/.m2' 
             }
           }
        stages {
            stage('checkout git') {
                steps {
                    git branch: pipelineParams.branch, credentialsId: 'bitbucket', url: pipelineParams.scmUrl
                }
            }

            stage('build') {
                steps {
                    sh 'mvn clean package -DskipTests=true'
                }
            }

            stage ('test') {
                steps {
                    parallel (
                            "unit tests": { sh 'mvn test' },
                            "integration tests": { sh 'mvn integration-test' }
                    )
                    
                    post {

                        always {
                            junit 'target/surefire-reports/*.xml' 
                        }
                    }
                }
            }

            stage('deploy developmentServer'){
                steps {
                    deploy(pipelineParams.developmentServer, pipelineParams.serverPort)
                }
            }

            stage('deploy staging'){
                steps {
                    deploy(pipelineParams.stagingServer, pipelineParams.serverPort)
                }
            }

            stage('deploy production'){
                steps {
                    deploy(pipelineParams.productionServer, pipelineParams.serverPort)
                }
            }
        }
        post {
            failure {
                sh "echo Pipeline failed body: ${env.BUILD_URL}"
            }
        }
    }
}
