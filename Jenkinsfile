pipeline {
    agent any
    stages {
        stage('Debug SSH') {
            steps {
                sshagent(credentials: ['id_ed25519_stevehuytrannd92']) {
                    sh '''
                        echo "Testing SSH..."
                        ssh -vT git@github.com
                        env | grep SSH_AUTH_SOCK
                    '''
                }
            }
        }


        stage('Load Script') {
            steps {
                script {
                    // The 'load' step is placed inside a 'script' block within a 'steps' block,
                    // which is implicitly within the agent's context.
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
                                extensions: [
                                    [$class: 'CleanBeforeCheckout'],
                                    [$class: 'RelativeTargetDirectory', relativeTargetDir: repo.folder]
                                ],
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
                            sh """
                                echo "=== Building ${repo.folder} ==="
                                npm ci
                                npm run build
                            """
                        }
                    }
                }
            }
        }

        // stage('Deploy to VPS') {
        //     // steps {
        //     //     script {
        //     //         repos.each { repo ->
        //     //             dir(repo.folder) {
        //     //                 sshagent (credentials: ['vps-ssh-key']) {
        //     //                     sh """
        //     //                         echo "Deploying ${repo.folder}..."
        //     //                         scp -r ./build user@vps:/var/www/${repo.folder}
        //     //                     """
        //     //                 }
        //     //             }
        //     //         }
        //     //     }
        //     // }
        // }
    }
}
