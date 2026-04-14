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

        stage('Build & Deploy'){
            steps{
                echo 'Launching the entire application with Docker'
                bat 'docker-compose up --build -d'
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