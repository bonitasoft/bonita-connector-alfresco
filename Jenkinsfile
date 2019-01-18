timestamps {
    ansiColor('xterm') {
        node {
            stage('Setup') {
                checkout scm
            }

            stage('Build') {
                try {
                    sh './mvnw clean verify'
                    archiveArtifacts 'target/bonita-connector-alfresco-*.zip -Djvm=${env.JAVA_HOME_11}/bin/java'
                } finally {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
    }
}