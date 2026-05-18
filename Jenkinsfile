pipeline {
    agent any

    stages {

        stage('Build Backend') {
            steps {
                script {
                    def services = [
                        'eureka-server',
                        'auth-service',
                        'patient',
                        'doctor',
                        'appointment',
                        'diagnostic',
                        'prescription',
                        'api-gateway'
                    ]

                    for (s in services) {
                        dir("Backend/${s}") {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Docker Build & Deploy') {
            steps {
                dir('Backend') {
                    bat 'docker-compose up --build -d'
                }
            }
        }
    }
}
