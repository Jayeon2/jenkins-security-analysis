pipeline {
    agent any
    environment {
        SONARQUBE = 'MySonerQubeServer'
        GIT_REPO = 'https://github.com/Jayeon2/jenkins-security-analysis.git'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: "${GIT_REPO}"
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv(SONARQUBE) {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }
    }
    post {
        always {
            echo "Pipeline finished"
        }
        success {
            echo "Build and SonarQube analysis completed successfully."
        }
        failure {
            echo "Build or SonarQube analysis failed."
        }
    }
}
