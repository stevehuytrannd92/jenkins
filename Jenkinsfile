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
                    ngnixTemplate = readFile('ngnix/nginx.template.conf')
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
                            repo.envs.each { env ->
                                echo "=== Building ${repo.folder} branch >>${repo.branch}<< for environment: ${env.name} ==="

                                withEnv(env.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
                                    // Build once (all envs use same build) or repeat if needed
                                    sh '''
                                        if [ -f package.json ]; then
                                            export CI=true
                                            npm ci
                                            npx next build && npx next-sitemap
                                        else
                                            echo "No package.json found, skipping build."
                                        fi
                                    '''

                                    // Copy out folder to environment-specific folder
                                    def envOut = "outs/${env.name}"
                                    sh """
                                        # Ensure parent folder exists
                                        mkdir -p outs
                                        
                                        # Remove previous output folder if exists (safe even if missing)
                                        if [ -d ${envOut} ]; then
                                            rm -rf ${envOut}
                                        fi

                                        # Copy build output
                                        cp -r out ${envOut} || echo "⚠️ Warning: 'out' folder missing, copy skipped"
                                    """

                                    // Verify
                                    sh """
                                        if [ -d ${envOut} ] && [ "\$(ls -A ${envOut})" ]; then
                                            echo "✅ Build output exists for ${repo.folder}/${env.name}"
                                        else
                                            echo "❌ ERROR: ${envOut} missing or empty for ${repo.folder}"
                                            exit 1
                                        fi
                                    """
                                }


                                
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy Outs to VPS') {
            steps {
                script {
                    repos.each { repo ->
                        dir(repo.folder) {
                            repo.envs.each { env ->
                                def envOut = "outs/${env.name}"
                                echo "🚀 Deploying ${envOut} to ${repo.vpsHost}:${repo.webrootBase}/${env.name}"

                                sshagent (credentials: [repo.vpsCredId]) {
                                    sh """
                                        # Make sure target folder exists
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                        "sudo mkdir -p ${repo.webrootBase}/${env.name} && sudo chown -R ${repo.vpsUser}:${repo.vpsUser} ${repo.webrootBase}/${env.name}"

                                        # Copy build output to VPS
                                        scp -o StrictHostKeyChecking=no -r ${envOut}/* \\
                                        ${repo.vpsUser}@${repo.vpsHost}:${repo.webrootBase}/${env.name}/

                                        # Restore ownership to root if needed (optional, usually keep as ubuntu:www-data)
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                        "sudo chown -R www-data:www-data ${repo.webrootBase}/${env.name}"
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }



        stage('Generate NGNIX config and deploy SSH') {
            steps {
                script {
                    repos.each { repo ->
                        dir(repo.folder) {

                            repo.envs.each { env ->
                                def domain = env.MAIN_DOMAIN.replaceAll(/^https?:\/\//, '').replaceAll(/\/$/, '')
                                def tmpConfigFile = "${env.name}.conf"

                                // Replace placeholders
                                def nginxConfig = ngnixTemplate
                                    .replace('{{DOMAIN}}', domain)
                                    .replace('{{ENV_NAME}}', env.name)

                                // Write locally
                                writeFile(file: tmpConfigFile, text: nginxConfig)
                                echo "✅ Generated Nginx config for ${env.name} locally: ${tmpConfigFile}"

                                // Print content locally
                                echo "📄 Local nginx config content for ${env.name}:\n${nginxConfig}"

                                sshagent(credentials: [repo.vpsCredId]) {
                                    sh """
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${repo.vpsUser}@${repo.vpsHost}:/home/${repo.vpsUser}/${tmpConfigFile}

                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                        "sudo mv /home/${repo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} && sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile}"

                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }





    }
}
