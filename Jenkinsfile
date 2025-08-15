pipeline {
    agent any
    tools {
        nodejs 'NODE_20'
    }
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
                                    extensions: [
                                        [$class: 'RelativeTargetDirectory', relativeTargetDir: repo.folder]
                                    ],
                                    userRemoteConfigs: [[
                                        url: repo.url,
                                        credentialsId: repo.credId
                                    ]]
                                ])
                            } else {
                                // Fetch and compare
                                sh """
                                    git fetch origin ${repo.branch}
                                    LOCAL_HASH=\$(git rev-parse HEAD)
                                    REMOTE_HASH=\$(git rev-parse FETCH_HEAD)
                                    if [ "\$LOCAL_HASH" != "\$REMOTE_HASH" ]; then
                                        echo "New changes found for ${repo.folder}, pulling..."
                                        git merge FETCH_HEAD
                                    else
                                        echo "No changes for ${repo.folder}, skipping pull."
                                    fi
                                """
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
                        dir(repo.folder) {
                            sh """
                                echo "=== Preparing to build ${repo.folder} ==="
                                
                                if [ -f package.json ]; then
                                    echo "Installing dependencies..."
                                    export CI=true
                                    npm ci
                                    echo "Building project..."
                                    npm run nextbuild
                                else
                                    echo "No package.json found in ${repo.folder}, skipping npm build."
                                fi
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
