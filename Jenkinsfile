pipeline {
    agent any

    stages {

        stage('Build Backend') {
            steps {

                dir('backend/auth-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/patient-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/doctor-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/appointment-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/diagnostic-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/prescription-service') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/eureka-server') {
                    bat 'mvn clean package -DskipTests'
                }

                dir('backend/api-gateway') {
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
