pipeline {
    agent any
    tools {
        nodejs 'NODE_20' // NodeJS installation configured in Jenkins global tools
    }

    stages {
        stage('Load Script') {
            steps {
                script {
                    // The 'load' step is placed inside a 'script' block within a 'steps' block,
                    // which is implicitly within the agent's context.
                    repos = load 'repos.groovy'
                    ngnixTemplate = readFile('ngnix/https.template.conf')
                }
            }
        }


        stage('Handle Certificates') {
            steps {
                script {
                    def missingCerts = []
                    
                    repos.each { repo ->
                        repo.envs.each { site ->
                            def domain = site.MAIN_DOMAIN.replaceAll('https://','').replaceAll('/','')
                            def certPath = "/etc/letsencrypt/live/${site.name}/fullchain.pem"

                            sshagent (credentials: [repo.vpsCredId]) {
                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \
                                        '[ -f ${certPath} ] && echo yes || echo no'
                                    """,
                                    returnStdout: true
                                ).trim()

                                if (exists == "no") {
                                    echo "❌ Certificate missing for ${domain}"
                                    missingCerts << domain
                                } else {
                                    echo "✅ Certificate exists for ${domain}"
                                }
                            }
                        }
                    }

                    // If missing, stop and trigger cert pipeline
                    if (missingCerts) {
                        echo "Some certificates are missing: ${missingCerts.join(', ')}"
                        currentBuild.result = 'ABORTED'
                        
                        // Trigger another pipeline for issuing certs
                        build job: 'cerbot-handler', 
                            parameters: [],
                            wait: false
                        
                        error("Stopping build because certificates are missing.")
                    }
                }
            }
        }


        stage('Repos Pulls') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
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
                                        # Copy build output to VPS
                                        tar -czf ${env.name}.tar.gz -C outs/${env.name} .
                                        scp -o StrictHostKeyChecking=no ${env.name}.tar.gz ${repo.vpsUser}@${repo.vpsHost}:/tmp/

                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mkdir -p ${repo.webrootBase}/${env.name} &&
                                            sudo tar -xzf /tmp/${env.name}.tar.gz -C ${repo.webrootBase}/${env.name} &&
                                            rm /tmp/${env.name}.tar.gz
                                        "

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
                                        # Upload config
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${repo.vpsUser}@${repo.vpsHost}:/home/${repo.vpsUser}/${tmpConfigFile}

                                        # Move config into sites-available
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mv /home/${repo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&

                                            # Enable site (symlink if not exists)
                                            sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&

                                            # Test nginx config
                                            sudo nginx -t &&

                                            # Reload nginx
                                            sudo systemctl reload nginx
                                        "

                                        # Verify deployed config
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
