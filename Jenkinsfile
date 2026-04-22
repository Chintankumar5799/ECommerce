pipeline {

    agent any

    environment {
        DB_PASSWORD          = credentials('DB_PASSWORD')
        JWT_SECRET           = credentials('JWT_SECRET')
        STRIPE_API_KEY       = credentials('STRIPE_API_KEY')
        GOOGLE_CLIENT_ID     = credentials('GOOGLE_CLIENT_ID')
        GOOGLE_CLIENT_SECRET = credentials('GOOGLE_CLIENT_SECRET')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('ECommerce') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('ecommerce-frontend') {
                    bat 'npm install'
                    bat 'npm run build'
                }
            }
        }

        stage('Prepare EC2') {
            steps {
                bat '''
                ssh -i C:/Users/PLW_002/Downloads/Backend-Pair.pem -o StrictHostKeyChecking=no ec2-user@34.230.30.181 "mkdir -p /home/ec2-user/app"
                '''
            }
        }

     stage('Deploy to EC2') {
    steps {
        bat '''
       ssh -i C:\\Users\\PLW_002\\Downloads\\Backend-Pair.pem -o StrictHostKeyChecking=no ec2-user@34.230.30.181 "docker network create ecommerce-net; docker volume create mydata; docker stop ecommerce || true; docker rm ecommerce || true; docker pull chintankumar5799/ecommerce-backend; docker run -d --name ecommerce -p 8081:8081 -e JWT_SECRET=%JWT_SECRET% -e GOOGLE_CLIENT_ID=%GOOGLE_CLIENT_ID% -e GOOGLE_CLIENT_SECRET=%GOOGLE_CLIENT_SECRET% -e STRIPE_API_KEY=%STRIPE_API_KEY% -e SPRING_DATASOURCE_URL=jdbc:postgresql://ecommerce.cmzkwumcaf8z.us-east-1.rds.amazonaws.com:5432/postgres -e SPRING_DATASOURCE_USERNAME=postgres -e SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD% -e SPRING_JPA_HIBERNATE_DDL_AUTO=update chintankumar5799/ecommerce-backend"
       '''
    }
}
        
    }

    post {
        always {
            echo 'Pipeline finished'
        }
        success {
            echo 'Build successful'
        }
        failure {
            echo 'Build failed'
        }
    }
}