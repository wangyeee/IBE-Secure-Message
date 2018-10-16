pipeline {
    agent any

    stages {
        stage('Build') { 
            steps {
                sh 'mvn clean'
                sh 'mvn compile'
                sh 'mvn -DskipTests package' 
            }
        }
    }
}
