pipeline{

    agent any

     environment {
        // This tells Jenkins to grab your "Secret texts" and turn them into environment variables
        DB_PASSWORD          = credentials('DB_PASSWORD')
        JWT_SECRET           = credentials('JWT_SECRET')
        STRIPE_API_KEY       = credentials('STRIPE_API_KEY')
        GOOGLE_CLIENT_ID     = credentials('GOOGLE_CLIENT_ID')
        GOOGLE_CLIENT_SECRET = credentials('GOOGLE_CLIENT_SECRET')
    }

    stages{
        stage('Checkout'){
             steps{
                checkout scm  
             }
        }

        stage('Build Backend'){
            steps{
                dir('ECommerce'){
                    echo 'Compiling Java Code....'

                    //-Dskiptests saves time
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend'){
            steps{
            dir('ecommerce-frontend'){
                echo "Installing React Dependencies"
                bat "npm install" 

                bat "npm run build"
            }
            }
        }

      

stage('Deploy') {
    steps {
        sh """
        docker compose down --remove-orphans || true
        docker compose up -d --build
        """
    }
}
    

   stage('Deploy to EC2') {
    steps {
        sshagent(['ec2-ssh-key']) {
            sh """
            ssh -o StrictHostKeyChecking=no ec2-user@100.54.145.139 '
              docker pull ecommerce:latest &&
              docker stop app || true &&
              docker rm app || true &&
              docker run -d --name app -p 80:80 ecommerce:latest
            '
            """
        }
    }
}
    }

    post {
    always {
        echo 'This runs always'
    }
    success {
        echo 'This runs only if build succeeded'
    }
    failure {
        echo 'This runs only if build failed'
    }
    }
}