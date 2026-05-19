pipeline {
    agent any

    
    environment {
        DB_HOST = credentials('DB_HOST')
        DB_PORT = credentials('DB_PORT')
        DB_NAME_CMS = credentials('DB_NAME_CMS')
        DB_NAME_AUTH = credentials('DB_NAME_AUTH')
        MYSQL_ROOT_PASSWORD = credentials('MYSQL_ROOT_PASSWORD')
        MYSQL_USER = credentials('MYSQL_USER')
        MYSQL_PASSWORD = credentials('MYSQL_PASSWORD')
        DB_HOST_PORT = credentials('DB_HOST_PORT')

        EUREKA_HOST = credentials('EUREKA_HOST')
        EUREKA_PORT = credentials('EUREKA_PORT')
        EUREKA_URL = credentials('EUREKA_URL')

        JWT_SECRET = credentials('JWT_SECRET')
        JWT_EXPIRY_MS = credentials('JWT_EXPIRY_MS')
        INTERNAL_SECRET = credentials('INTERNAL_SECRET')

        SPRING_PROFILES_ACTIVE = credentials('SPRING_PROFILES_ACTIVE')
    }


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

        // Already the Dockerfile of frontend has the npm install and npm run build
        // stage('Build Frontend') {
        //     steps {
        //         dir('Frontend') {
        //             bat 'npm install'
        //             bat 'npm run build'
        //         }
        //     }
        // }
