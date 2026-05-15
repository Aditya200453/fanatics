pipeline {
    agent any

    stages {

        stage('Build Backend') {
    steps {
        dir('Backend') {
            // ✅ CORE SERVICES
            bat 'cd auth-service && mvn clean package -DskipTests'
            bat 'cd patient-service && mvn clean package -DskipTests'
            bat 'cd doctor-service && mvn clean package -DskipTests'
            bat 'cd appointment-service && mvn clean package -DskipTests'
            bat 'cd diagnostic-service && mvn clean package -DskipTests'
            bat 'cd prescription-service && mvn clean package -DskipTests'
            bat 'cd eureka-server && mvn clean package -DskipTests'
            bat 'cd api-gateway && mvn clean package -DskipTests'
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
