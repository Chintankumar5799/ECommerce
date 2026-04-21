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

        stage('Prepare EC2 Directory') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    bat '''
                    ssh ec2-user@100.54.145.139 "mkdir -p /home/ec2-user/app"
                    '''
                }
            }
        }

        stage('Copy Artifacts to EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    bat '''
                    scp -o StrictHostKeyChecking=no ECommerce/target/*.jar ec2-user@100.54.145.139:/home/ec2-user/app/
                    scp -o StrictHostKeyChecking=no -r ecommerce-frontend/build ec2-user@100.54.145.139:/home/ec2-user/app/frontend/
                    scp -o StrictHostKeyChecking=no docker-compose.yml ec2-user@100.54.145.139:/home/ec2-user/app/
                    '''
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    bat '''
                    ssh ec2-user@100.54.145.139 "
                    cd /home/ec2-user/app &&
                    docker compose down --remove-orphans || true &&
                    docker compose up -d --build
                    "
                    '''
                }
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