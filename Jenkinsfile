pipeline {
    agent any
    
    environment {
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        PROJECT_NAME = 'weekly-report'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Code checked out successfully'
            }
        }
        
        stage('Prepare Environment') {
            steps {
                script {
                    // Stop existing containers if they exist
                    sh '''
                        if docker ps -a | grep -q weekly-report; then
                            echo "Stopping existing containers..."
                            docker-compose -f ${DOCKER_COMPOSE_FILE} down || true
                        fi
                    '''
                }
            }
        }
        
        stage('Build and Deploy') {
            steps {
                script {
                    try {
                        // Build and start services
                        sh '''
                            echo "Building and starting services..."
                            docker-compose -f ${DOCKER_COMPOSE_FILE} up --build -d
                        '''
                        
                        // Wait for services to be healthy
                        sh '''
                            echo "Waiting for services to be healthy..."
                            for i in {1..30}; do
                                if docker-compose -f ${DOCKER_COMPOSE_FILE} ps | grep -q "healthy"; then
                                    echo "Services are healthy"
                                    break
                                fi
                                echo "Waiting for services... ($i/30)"
                                sleep 10
                            done
                        '''
                        
                    } catch (Exception e) {
                        echo "Build failed: ${e.getMessage()}"
                        // Get logs for debugging
                        sh '''
                            echo "=== Docker Compose Logs ==="
                            docker-compose -f ${DOCKER_COMPOSE_FILE} logs
                        '''
                        throw e
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    // Check backend health
                    sh '''
                        echo "Checking backend health..."
                        for i in {1..10}; do
                            if curl -f http://localhost:8081/api/health; then
                                echo "Backend health check passed"
                                break
                            fi
                            echo "Backend not ready yet... ($i/10)"
                            sleep 5
                        done
                    '''
                    
                    // Check MySQL connection
                    sh '''
                        echo "Checking MySQL connection..."
                        for i in {1..10}; do
                            if docker exec weekly-report-mysql mysqladmin ping -h localhost -u root -prootpass123; then
                                echo "MySQL health check passed"
                                break
                            fi
                            echo "MySQL not ready yet... ($i/10)"
                            sleep 5
                        done
                    '''
                }
            }
        }
        
        stage('Service Status') {
            steps {
                sh '''
                    echo "=== Final Service Status ==="
                    docker-compose -f ${DOCKER_COMPOSE_FILE} ps
                    echo "=== Running Containers ==="
                    docker ps | grep weekly-report
                    echo "=== Port Status ==="
                    netstat -tlnp | grep -E "(8081|3308)"
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Deployment completed successfully!'
            sh '''
                echo "=== Deployment Summary ==="
                echo "Backend: http://localhost:8081"
                echo "MySQL: localhost:3308"
                echo "Services Status:"
                docker-compose -f ${DOCKER_COMPOSE_FILE} ps
            '''
        }
        
        failure {
            echo 'Deployment failed!'
            sh '''
                echo "=== Debug Information ==="
                docker-compose -f ${DOCKER_COMPOSE_FILE} logs
                docker ps -a
            '''
        }
        
        always {
            echo 'Cleaning up workspace...'
        }
    }
}