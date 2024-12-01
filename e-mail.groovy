pipeline {
    agent any

    environment {
        SONARQUBE = 'MySonarQubeServer'
        GIT_REPO = 'https://github.com/Jayeon2/jenkins-security-analysis.git'
        RECIPIENTS = 'Evjenkins323361@gmail.com'
        SONAR_PROJECT_KEY = 'Jenkins-323361'
        SONAR_PROJECT_NAME = 'Jenkins'
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
                withSonarQubeEnv(SONARQUBE) {
                    sh """
                    mvn sonar:sonar 
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} 
                        -Dsonar.projectName=${SONAR_PROJECT_NAME}
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        def qualityGate = waitForQualityGate() // Quality Gate 결과 확인
                        if (qualityGate.status != 'OK') {
                            error "Quality Gate failed: ${qualityGate.status}" // Quality Gate 실패 시 빌드를 실패로 처리
                        }
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
            emailext( // Quality Gate 실패 시 이메일 알림 전송
                subject: "Jenkins Pipeline - SonarQube Quality Gate Failed",
                body: """
                    <h2>SonarQube Quality Gate Failed</h2>
                    <p>Project: ${env.SONAR_PROJECT_NAME}</p>
                    <p>Status: ${qualityGate.status}</p>
                """,
                to: "${RECIPIENTS}",
                mimeType: 'text/html'
            )
        }
    }
}
