pipeline {
    agent any

    stages {

        stage('Build Backend') {
            steps {

                dir('Backend/eureka-server') {
                    bat 'mvn clean package -DskipTests'
                }

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
                    withEnv([
                        'EUREKA_URL=http://eureka-server:8761/eureka/',
                        'MYSQL_ROOT_PASSWORD=root',
                        'DB_HOST=db',
                        'DB_PORT=3306',
                        'DATABASE=cms',
                        'DB_NAME=cms',
                        'DB_NAME_AUTH=auth_db',
                        'MYSQL_USER=root',
                        'MYSQL_PASSWORD=root',
                        'JWT_SECRET=VGhpc0lzQVN1cGVyU2VjdXJlS2V5Rm9ySldUU2lnbmluZzEyMw==',
                        'INTERNAL_SECRET=CHANGE_ME_INTERNAL',
                        'SPRING_PROFILES_ACTIVE=docker'
                    ]) {
                        bat 'docker-compose down'
                        bat 'docker-compose up -d'
                    }
                }
            }
        }
    }
}
