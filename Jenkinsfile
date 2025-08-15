pipeline {
    agent any
    tools {
        nodejs 'NODE_20' // NodeJS installation configured in Jenkins global tools
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
                        dir(repo.folder) {
                            sh '''
                                if [ -f package.json ]; then
                                    export CI=true
                                    npm ci
                                    npm run nextbuild
                                else
                                    echo "No package.json found, skipping build."
                                fi
                            '''

                            // Verify the "out" folder has files
                            sh '''
                                if [ -d out ] && [ "$(ls -A out)" ]; then
                                    echo "✅ Build output exists in out/ for ${repo.folder}"
                                else
                                    echo "❌ ERROR: out/ folder is missing or empty in ${repo.folder}"
                                    exit 1
                                fi
                            '''
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
