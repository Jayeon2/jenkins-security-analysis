stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('Jenkins') {       //젠킨스에 설정된 소나큐브 서버 이름
            sh """
            mvn sonar:sonar    //소나큐브 분석 실행
                -Dsonar.projectKey=Jenkins-323361    //분석할 소나큐브 프로젝트 키
                -Dsonar.projectName=Jenkins        //분석할 소나큐브 프로젝트 이름
            """
        }
    }
}

post {
    always {
        script {
       def sonarServerUrl = "http://localhost:9000"
            def projectKey = "Jenkins-323361"
            def projectName = "Jenkins"
            def sonarApiUrl = "${sonarServerUrl}/api/issues/search?componentKeys=${projectKey}&types=VULNERABILITY"
       //소나큐브 API 호출(취약점 확인)
            def sonarReport = sh(script: 'curl -s "http://localhost:9000/api/issues/search?componentKeys=Jenkins-323361&types=VULNERABILITY" ', returnStdout: true).trim()  
            if (!sonarReport) {
      echo "No response from SonarQube API."
      return
       }
       //취약점 개수 확인
            def vulnerabilities = readJSON(text: sonarReport).issues
            if (vulnerabilities.size() > 0) {
                emailext( 
                    subject: "Jenkins Pipeline - Vulnerabilities Detected",
                    body: """
                        <h2>Security Vulnerabilities Detected</h2>
                        <p>Project: Jenkins</p>
                        <p>${vulnerabilities.size()} vulnerabilities found in the project.</p>
          <p><a href="http://localhost:9000/dashboard?id=Jenkins-323361">View project details in SonarQube</a></p>
                    """,
                    to: 'qawdrs@gmail.com',   
                    mimeType: 'text/html'
                )
            } else {
      echo "No vulnerabilities found in the project."
            }
    }
}
