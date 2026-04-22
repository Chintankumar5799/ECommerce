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
                ssh -i C:\\keys\\ec2.pem -o StrictHostKeyChecking=no ec2-user@100.54.145.139 "mkdir -p /home/ec2-user/app"
                '''
            }
        }

        stage('Copy Backend Env File') {
            steps {
                bat '''
                scp -i C:\\keys\\ec2.pem -o StrictHostKeyChecking=no backend.env ec2-user@100.54.145.139:/home/ec2-user/app/
                '''
            }
        }

        stage('Deploy to EC2') {
            steps {
                bat '''
                ssh -i C:\\keys\\ec2.pem -o StrictHostKeyChecking=no ec2-user@100.54.145.139 "
                docker network create ecommerce-net || true &&
                docker volume create mydata || true &&
                docker stop ecommerce || true &&
                docker rm ecommerce || true &&
                docker pull chintankumar5799/ecommerce-backend &&
                docker run -d --name ecommerce \
                    --network ecommerce-net \
                    -p 8081:8081 \
                    -v mydata:/var/lib/ecommerce-backend-img/data \
                    --env-file /home/ec2-user/app/backend.env \
                    chintankumar5799/ecommerce-backend
                "
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