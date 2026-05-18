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
                        // ✅ DB
                        'DB_HOST=db',
                        'DB_PORT=3306',
                        'DB_NAME_CMS=cms',
                        'DB_NAME_AUTH=auth_db',
                        'MYSQL_ROOT_PASSWORD=root',
                        'MYSQL_USER=root',
                        'MYSQL_PASSWORD=root',
                        'DB_HOST_PORT=3307',

                        // ✅ EUREKA
                        'EUREKA_HOST=eureka-server',
                        'EUREKA_PORT=8761',
                        'EUREKA_URL=http://eureka-server:8761/eureka/',

                        // ✅ SECURITY
                        'JWT_SECRET=VGhpc0lzQVN1cGVyU2VjdXJlS2V5Rm9ySldUU2lnbmluZzEyMw==',
                        'JWT_EXPIRY_MS=3600000',
                        'INTERNAL_SECRET=CHANGE_ME_INTERNAL',

                        // ✅ SPRING
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
