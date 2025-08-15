pipeline {
    agent any
    stages {
        // stage('Debug SSH') {
        //     steps {
        //         sshagent(credentials: ['id_ed25519_stevehuytrannd92']) {
        //             sh '''
        //                 set +e  # don't exit on non-zero
        //                 echo "Testing SSH..."
        //                 ssh -vT git@github.com
        //                 env | grep SSH_AUTH_SOCK
        //                 echo "SSH authentication to GitHub succeeded."

        //             '''
        //         }
        //     }
        // }


        stage('Load Script') {
            steps {
                script {
                    // The 'load' step is placed inside a 'script' block within a 'steps' block,
                    // which is implicitly within the agent's context.
                    repos = load 'repos.groovy'
                }
            }
        }

        stage('Repos Pulls') {
            steps {
                script {
                    repos.each { repo ->
                        dir(repo.folder) {
                            if (!fileExists('.git')) {
                                // First time clone
                                checkout([
                                    $class: 'GitSCM',
                                    branches: [[name: "*/${repo.branch}"]],
                                    doGenerateSubmoduleConfigurations: false,
                                    userRemoteConfigs: [[
                                        url: repo.url,
                                        credentialsId: repo.credId
                                    ]]
                                ])
                            } else {
                                // Fetch and compare
                                checkout([
                                    $class: 'GitSCM',
                                    branches: [[name: "*/${repo.branch}"]],
                                    doGenerateSubmoduleConfigurations: false,
                                    userRemoteConfigs: [[
                                        url: repo.url,
                                        credentialsId: repo.credId
                                    ]],
                                    extensions: [
                                        [$class: 'WipeWorkspace'],        // optional: clean workspace if needed
                                        [$class: 'PruneStaleBranch'],     // remove stale branches
                                        [$class: 'CleanBeforeCheckout']   // optional: ensure clean state
                                    ]
                                ])
                            }
                        }
                    }
                }
            }
        }


        stage('Build Projects') {
            steps {
                script {
                    repos.each { repo ->
                        echo "=== Starting build for ${repo.folder} ==="

                        node {
                            docker.image('node:20-bullseye').inside('-u node') {
                                dir(repo.folder) {
                                    sh """
                                        if [ -f package.json ]; then
                                            export CI=true
                                            npm ci
                                            npm run nextbuild
                                        else
                                            echo "No package.json found, skipping build."
                                        fi
                                    """
                                }
                            }
                        }

                        echo "=== Finished build for ${repo.folder} ==="
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
