pipeline {
    agent any
    environment {
        SONARQUBE = 'MySonarQubeServer'
        SONAR_PROJECT_KEY = '323361'
        SONAR_PROJECT_NAME = 'Jenkins'
        SONAR_PROJECT_VERSION = '1.0'
        SONAR_SOURCES = 'src/main/java'
        SONAR_BINARIES = 'target/classes'
        GIT_REPO = 'https://github.com/Jayeon2/jenkins-security-analysis.git'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: "${GIT_REPO}"
            }
        }
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv("${SONARQUBE}") {
                        sh """
                        mvn sonar:sonar 
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} 
                            -Dsonar.projectName=${SONAR_PROJECT_NAME} 
                            -Dsonar.projectVersion=${SONAR_PROJECT_VERSION} 
                            -Dsonar.sources=${SONAR_SOURCES} 
                            -Dsonar.java.binaries=${SONAR_BINARIES}
                        """
                    }
                }
            }
        }
    }
}
