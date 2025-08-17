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
                        repo.envs.each { site ->
                            def domain = site.MAIN_DOMAIN.replaceAll('https://','').replaceAll('/','')
                            sshagent (credentials: [repo.vpsCredId]) {

                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \
                                        '[ -f /etc/letsencrypt/live/${domain}/fullchain.pem ] && echo yes || echo no'
                                    """,
                                    returnStdout: true
                                ).trim()
                                if (exists == "yes") {
                                        sh """
                                            ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                            "sudo certbot renew --deploy-hook \\"systemctl reload nginx\\""
                                        """
                                } else {                                
                                    def tmpConfigFile = "${site.name}.conf"


                                    // Replace placeholders
                                    def nginxConfig = certbotTemplate
                                        .replace('{{DOMAIN}}', domain)
                                        .replace('{{ENV_NAME}}', site.name)
                                        .replace('{{WEBROOT_BASE}}', repo.webrootBase)

                                    writeFile(file: tmpConfigFile, text: nginxConfig)
                                    echo "✅ Generated Nginx config for ${site.name} locally: ${tmpConfigFile}"

                                    // Print content locally
                                    echo "📄 Local nginx config content for ${site.name}:\n${nginxConfig}"
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

                                        sh """
                                            ssh -o StrictHostKeyChecking=no ${repo.vpsUser}@${repo.vpsHost} \\
                                            "sudo certbot certonly --webroot -w ${repo.webrootBase}/${site.name} \\
                                            -d ${domain} \\
                                            -d www.${domain}"
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
