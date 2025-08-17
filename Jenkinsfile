// --- Helper functions ---
def extractDomain(String url) {
    return url
        .replaceAll(/^https?:\/\//, '')  // remove http(s)
        .replaceAll(/\/$/, '')           // remove trailing slash
        .replaceAll(/^www\./, '')        // strip leading www
}

// Check if domain exists in MISSING_CERTS string
def isMissingCert(String domain, String missingCertsStr) {
    if (!missingCertsStr?.trim()) {
        return false
    }
    def domains = missingCertsStr.split(',').collect { it.trim() }
    return domains.contains(domain)
}


pipeline {
    agent any
    tools {
        nodejs 'NODE_20'
    }

    environment {
        // store missing domains across stages
        MISSING_CERTS = ""
    }

    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    ngnixTemplate = readFile('ngnix/https.template.conf')
                }
            }
        }

        stage('Check Certificates') {
            steps {
                script {
                    def missing = []

                    repos.each { repo ->
                        repo.envs.each { site ->
                            def domain = extractDomain(site.MAIN_DOMAIN)


                            sshagent (credentials: [repo.vpsCredId]) {
                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \
                                        "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
                                    """,
                                    returnStdout: true
                                ).trim()

                                if (exists == "no") {
                                    echo "⚠️  Certificate missing for ${domain}"
                                    missing << domain
                                } else {
                                    echo "✅ Certificate exists for ${domain}"
                                }
                            }
                        }
                    }

                    if (missing) {
                        echo "⚠️  Some certificates are missing: ${missing.join(', ')}"
                        // save into env for later stages
                        env.MISSING_CERTS = missing.join(',')

                        // Trigger certbot handler in background
                        // build job: 'cerbot-handler',
                        //     parameters: [],
                        //     wait: false,
                        //     propagate: false
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

                    repos.each { repo ->
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain, env.MISSING_CERTS)) {
                                    echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                echo "=== Building ${repo.folder} branch >>${repo.branch}<< for environment: ${envConf.name} ==="

                                withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
                                    sh '''
                                        if [ -f package.json ]; then
                                            export CI=true
                                            npm ci
                                            npx next build && npx next-sitemap
                                        else
                                            echo "No package.json found, skipping build."
                                        fi
                                    '''

                                    def envOut = "outs/${envConf.name}"
                                    sh """
                                        mkdir -p outs
                                        rm -rf ${envOut} || true
                                        cp -r out ${envOut} || echo "⚠️ Warning: 'out' folder missing, copy skipped"
                                    """

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
                    repos.each { repo ->
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain, env.MISSING_CERTS)) {
                                    echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def envOut = "outs/${envConf.name}"
                                echo "🚀 Deploying ${envOut} to ${repo.vpsHost}:${repo.webrootBase}/${envConf.name}"

                                sshagent (credentials: [repo.vpsCredId]) {
                                    sh """
                                        tar -czf ${envConf.name}.tar.gz -C outs/${envConf.name} .
                                        scp -o StrictHostKeyChecking=no ${envConf.name}.tar.gz ${repo.vpsUser}@${repo.vpsHost}:/tmp/

                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mkdir -p ${repo.webrootBase}/${envConf.name} &&
                                            sudo tar -xzf /tmp/${envConf.name}.tar.gz -C ${repo.webrootBase}/${envConf.name} &&
                                            rm /tmp/${envConf.name}.tar.gz &&
                                            sudo chown -R www-data:www-data ${repo.webrootBase}/${envConf.name}
                                        "
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
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain, env.MISSING_CERTS)) {
                                    echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def tmpConfigFile = "${envConf.name}.conf"
                                def nginxConfig = ngnixTemplate
                                    .replace('{{DOMAIN}}', domain)
                                    .replace('{{ENV_NAME}}', envConf.name)

                                writeFile(file: tmpConfigFile, text: nginxConfig)
                                echo "✅ Generated Nginx config for ${envConf.name} locally: ${tmpConfigFile}"
                                echo "📄 Local nginx config content for ${envConf.name}:\n${nginxConfig}"

                                sshagent(credentials: [repo.vpsCredId]) {
                                    sh """
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${repo.vpsUser}@${repo.vpsHost}:/home/${repo.vpsUser}/${tmpConfigFile}
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mv /home/${repo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&
                                            sudo nginx -t &&
                                            sudo systemctl reload nginx
                                        "
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
