def deploycontainer(imagename) {

    sh """
    docker stop java-app || true
    docker rm java-app || true

    docker run -d \
    --name java-app \
    -p 8080:8080 \
    ${imagename}
    """
}

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
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh """
                        mvn sonar:sonar -Dsonar.projectKey=Java-testproj -Dsonar.host.url=http://3.87.158.94:9000 -Dsonar.login=$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .'
            }
        }

        stage('Docker Image Scan') {
            steps {
                sh '''
                trivy image --skip-java-db-update --scanners vuln --severity CRITICAL,HIGH --exit-code 1 $DOCKER_IMAGE:$DOCKER_TAG || true
                '''
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'd84391c1-752b-47bd-a3ed-34d34ec3742d',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {

                    sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh 'docker push $DOCKER_IMAGE:$DOCKER_TAG'
            }
        }

        stage('Backup Current Version') {
            steps {
                sh '''
                docker inspect java-app \
                --format='{{.Config.Image}}' \
                > previous-image.txt || true
                '''
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    deploycontainer("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Health Check') {
            steps {
                script {

                    def status = sh(
                        script: '''
                        sleep 30
                        curl -f \
                        http://localhost:8080/actuator/health
                        ''',
                        returnStatus: true
                    )

                    if (status != 0) {

                        echo "Health Check Failed"
                        echo "Starting Rollback..."

                        def previousImage = sh(
                            script: "cat previous-image.txt",
                            returnStdout: true
                        ).trim()

                        echo "Rollback Image: ${previousImage}"

                        deploycontainer(previousImage)

                        error("Rollback completed")
                    }

                    echo "Health Check Passed"
                }
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