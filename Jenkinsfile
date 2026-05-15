pipeline {
    agent any

    stages {

        stage('Build Backend') {
            steps {

                dir('Backend/auth-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/patient') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/doctor') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/appointment') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/diagnostic') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/prescription') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/eureka-server') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('Backend/api-gateway') {
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
