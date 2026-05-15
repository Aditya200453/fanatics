pipeline {
    agent any

    stages {

        stage('Build Backend') {
            steps {
                dir('Backend') {   // ✅ THIS IS THE MAIN FIX
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Docker Build') {
            steps {
                dir('Backend') {
                    bat 'docker-compose build'
                }
            }
        }

        stage('Deploy') {
            steps {
                dir('Backend') {
                    bat 'docker-compose down'
                    bat 'docker-compose up -d'
                }
            }
        }

        stage('Debug') {
            steps {
                bat 'dir'
            }
        }
    }
}
