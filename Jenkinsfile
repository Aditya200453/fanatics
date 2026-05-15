pipeline {
    agent any

    stages {

        stage('Build Backend') {
            steps {
                dir('Backend') {   // ✅ MUST BE PRESENT
                    bat 'dir'      // ✅ TEMP DEBUG
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
    }
}
