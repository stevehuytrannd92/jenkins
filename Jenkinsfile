pipeline {
    agent any

    stages {
        stage('Load Repo Config') {
            steps {
                script {
                    // Load repos from file
                    repos = load 'repos.groovy'
                }
            }
        }

        stage('Clone Repos') {
            steps {
                script {
                    repos.each { repo ->
                        dir(repo.folder) {
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: "*/${repo.branch}"]],
                                doGenerateSubmoduleConfigurations: false,
                                extensions: [[$class: 'CleanBeforeCheckout']],
                                userRemoteConfigs: [[
                                    url: repo.url,
                                    credentialsId: repo.credId
                                ]]
                            ])
                        }
                    }
                }
            }
        }

        stage('Build Projects') {
            steps {
                script {
                    repos.each { repo ->
                        dir(repo.folder) {
                            sh "echo Building ${repo.folder}..."
                            // Replace this with your actual build command
                            sh "npm install && npm run build"
                        }
                    }
                }
            }
        }

        stage('Deploy to VPS') {
            steps {
                // script {
                //     repos.each { repo ->
                //         dir(repo.folder) {
                //             // Example deployment via SSH
                //             sshagent (credentials: ['vps-ssh-key']) {
                //                 sh "scp -r ./build user@vps:/var/www/${repo.folder}"
                //             }
                //         }
                //     }
                // }
            }
        }
    }
}
