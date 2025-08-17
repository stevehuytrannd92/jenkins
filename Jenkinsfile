pipeline {
    agent any
    tools {
        nodejs 'NODE_20' // NodeJS installation configured in Jenkins global tools
    }

    environment {
        // store missing domains across stages
        MISSING_CERTS = ""
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


        stage('Check Certificates') {
            steps {
                script {
                    def missingCerts = []
                    
                    repos.each { repo ->
                        repo.envs.each { site ->
                            def domain = site.MAIN_DOMAIN
                                .replaceAll('https://','')
                                .replaceAll('http://','')
                                .replaceAll('/','')
                                .replaceAll('^www\\.', '')   // strip www

                            sshagent (credentials: [repo.vpsCredId]) {
                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \
                                        "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
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

                    if (missingCerts) {
                        echo "⚠️  Some certificates are missing: ${missingCerts.join(', ')}"
                        // Fire certbot handler in background, do not fail current pipeline
                        // save for later stages
                        env.MISSING_CERTS = missingCertDomains.join(',')


                        build job: 'cerbot-handler',
                            parameters: [],
                            propagate: false,   // don't fail if certbot-handler fails
                            wait: false
                    } else {
                        echo "✅ All certificates present"
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
                    def missing = env.MISSING_CERTS?.split(',') as List

                    repos.each { repo ->
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                
                                def domain = envConf.MAIN_DOMAIN
                                    .replaceAll(/^https?:\/\//, '')
                                    .replaceAll(/\/$/, '')
                                    .replaceAll(/^www\./, '')

                                if (missing.contains(domain)) {
                                    echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }


                                echo "=== Building ${repo.folder} branch >>${repo.branch}<< for environment: ${envConf.name} ==="

                                withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
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
                                    def envOut = "outs/${envConf.name}"
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
                                            echo "✅ Build output exists for ${repo.folder}/${envConf.name}"
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
                    def missing = env.MISSING_CERTS?.split(',') as List

                    repos.each { repo ->
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = envConf.MAIN_DOMAIN
                                    .replaceAll(/^https?:\/\//, '')
                                    .replaceAll(/\/$/, '')
                                    .replaceAll(/^www\./, '')

                                if (missing.contains(domain)) {
                                    echo "⏭️ Skipping deploy for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def envOut = "outs/${envConf.name}"
                                echo "🚀 Deploying ${envOut} to ${repo.vpsHost}:${repo.webrootBase}/${envConf.name}"

                                sshagent (credentials: [repo.vpsCredId]) {
                                    sh """
                                        # Copy build output to VPS
                                        tar -czf ${envConf.name}.tar.gz -C outs/${envConf.name} .
                                        scp -o StrictHostKeyChecking=no ${envConf.name}.tar.gz ${repo.vpsUser}@${repo.vpsHost}:/tmp/

                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mkdir -p ${repo.webrootBase}/${envConf.name} &&
                                            sudo tar -xzf /tmp/${envConf.name}.tar.gz -C ${repo.webrootBase}/${envConf.name} &&
                                            rm /tmp/${envConf.name}.tar.gz
                                        "

                                        # Restore ownership to root if needed (optional, usually keep as ubuntu:www-data)
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                        "sudo chown -R www-data:www-data ${repo.webrootBase}/${envConf.name}"
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
                    def missing = env.MISSING_CERTS?.split(',') as List

                    repos.each { repo ->
                        dir(repo.folder) {

                            repo.envs.each { envConf ->
                                def domain = envConf.MAIN_DOMAIN
                                    .replaceAll(/^https?:\/\//, '')
                                    .replaceAll(/\/$/, '')
                                    .replaceAll(/^www\./, '')

                                if (missing.contains(domain)) {
                                    echo "⏭️ Skipping nginx config for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def tmpConfigFile = "${envConf.name}.conf"

                                // Replace placeholders
                                def nginxConfig = ngnixTemplate
                                    .replace('{{DOMAIN}}', domain)
                                    .replace('{{ENV_NAME}}', envConf.name)

                                // Write locally
                                writeFile(file: tmpConfigFile, text: nginxConfig)
                                echo "✅ Generated Nginx config for ${envConf.name} locally: ${tmpConfigFile}"

                                // Print content locally
                                echo "📄 Local nginx config content for ${envConf.name}:\n${nginxConfig}"

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
