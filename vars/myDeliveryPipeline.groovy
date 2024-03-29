
def call(Map pipelineParams) {

    pipeline {
     agent {
        docker {
            image 'maven:3-alpine' 
            args '-v $HOME/.m2:/root/.m2' 
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
                    sh 'echo build'
                }
            }

            stage('Test'){
                steps{
                    sh 'mvn test'
                }
                post {
                    always {
                        junit 'target/surefire-reports/*.xml' 
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
