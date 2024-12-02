stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('Jenkins') {  // Jenkins에 설정된 SonarQube 서버 이름
            sh """
            mvn sonar:sonar \
                -Dsonar.projectKey=Jenkins-323361 \
                -Dsonar.projectName=Jenkins
            """
        }
    }
}

post {
    always {
        script {
            def sonarServerUrl = "http://localhost:9000"
            def projectKey = "Jenkins-323361"
            def sonarApiUrl = "${sonarServerUrl}/api/issues/search?componentKeys=${projectKey}&types=VULNERABILITY"

            def sonarReport = sh(script: "curl -s ${sonarApiUrl} || true", returnStdout: true).trim()

            if (!sonarReport || !sonarReport.startsWith("{")) {
                echo "Invalid or empty response from SonarQube API."
                return
            }

            def vulnerabilities = readJSON(text: sonarReport).issues
            if (vulnerabilities.size() > 0) {
                emailext(
                    subject: "Jenkins Pipeline - Vulnerabilities Detected",
                    body: '''
                        <h2>Security Vulnerabilities Detected</h2>
                        <p>Project: Jenkins</p>
                        <p>${vulnerabilities.size()} vulnerabilities found in the project.</p>
                        <p><a href="http://localhost:9000/dashboard?id=Jenkins-323361">View project details in SonarQube</a></p>
                    ''',
                    to: 'qawdrs6416@gmail.com',
                    mimeType: 'text/html'
                )
            } else {
                echo "No vulnerabilities found in the project."
            }
        }
    }
}
