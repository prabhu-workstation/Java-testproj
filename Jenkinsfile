pipeline {

    agent any

    environment {
        DOCKER_IMAGE = "prabhuworkstation/java-testproj"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    tools {
        maven 'Maven'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/prabhu-workstation/Java-testproj.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -Dmaven.test.skip=true'
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=Java-testproj \
                    -Dsonar.host.url=http://3.87.167.253:9000 \
                    -Dsonar.login=sqa_9be552ea35c713521d76b9396be94634d1e36923   
                    '''
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .'
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'd84391c1-752b-47bd-a3ed-34d34ec3742d',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {

                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh 'docker push $DOCKER_IMAGE:$DOCKER_TAG'
            }
        }

        stage('Deploy Container') {
            steps {

                sh '''
                docker stop java-app || true
                docker rm java-app || true

                docker run -d \
                --name java-app \
                -p 8080:8080 \
                $DOCKER_IMAGE:$DOCKER_TAG
                '''
            }
        }
    }

    post {

        success {
            echo 'Pipeline executed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
        }
    }
}