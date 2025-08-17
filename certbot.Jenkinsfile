pipeline {
    agent any

    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    certbotTemplate = readFile('ngnix/http.template.conf')
                }
            }
        }

        stage('Handle Certificates') {
            steps {
                script {
                    repos.each { repo ->
                        boolean needsRenew = false

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

                                if (exists == "yes") {
                                    echo "🔑 Certificate already exists for ${domain}"
                                    needsRenew = true
                                } else {
                                    echo "❌ No certificate for ${domain}, issuing new one"

                                    def tmpConfigFile = "${site.name}.conf"

                                    // Replace placeholders in nginx template
                                    def nginxConfig = certbotTemplate
                                        .replace('{{DOMAIN}}', domain)
                                        .replace('{{ENV_NAME}}', site.name)
                                        .replace('{{WEBROOT_BASE}}', repo.webrootBase)

                                    writeFile(file: tmpConfigFile, text: nginxConfig)
                                    echo "✅ Generated Nginx config for ${site.name}: ${tmpConfigFile}"

                                    sh """
                                        # Upload config
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${repo.vpsUser}@${repo.vpsHost}:/home/${repo.vpsUser}/${tmpConfigFile}

                                        # Move config into sites-available & enable
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "
                                            sudo mv /home/${repo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&
                                            sudo nginx -t &&
                                            sudo systemctl reload nginx
                                        "
                                        # Verify deployed config
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
                                    """

                                    // Ensure webroot folder and issue new cert
                                    sh """
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                        "sudo mkdir -p ${repo.webrootBase}/${site.name} && \\
                                         sudo certbot certonly --webroot -w ${repo.webrootBase}/${site.name} \\
                                         -d ${domain} -d www.${domain}"
                                    """
                                }
                            }
                        }

                        // Run renew ONCE per VPS
                        if (needsRenew) {
                            sshagent (credentials: [repo.vpsCredId]) {
                                sh """
                                    ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                    "sudo certbot renew --deploy-hook \\"systemctl reload nginx\\""
                                """
                            }
                        }
                    }
                }
            }
        }
    }
}
