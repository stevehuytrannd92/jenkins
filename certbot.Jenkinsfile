pipeline {
    agent any
    triggers {
        cron('30 2 * * *')
    }

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
        // buildDiscarder(logRotator(numToKeepStr: '10')) // optional cleanup
        // timeout(time: 60, unit: 'MINUTES')            // optional safety
    }


    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    vpsInfos = load 'vps.groovy'

                    certbotTemplate = readFile('ngnix/http.template.conf')
                }
            }
        }

        stage('Handle Certificates') {
            steps {
                script {
                    def vpsMap = [:]  // plain HashMap

                    repos.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]

                        repo.envs.each { site ->
                            def domain = site.MAIN_DOMAIN
                                .replaceAll('https://','')
                                .replaceAll('http://','')
                                .replaceAll('/','')
                                .replaceAll('^www\\.', '')   // strip www

                            sshagent (credentials: [vpsInfo.vpsCredId]) {

                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \
                                        "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
                                    """,
                                    returnStdout: true
                                ).trim()

                                if (exists == "yes") {
                                    echo "🔑 Certificate already exists for ${domain}"
                                    // mark VPS for renew later
                                    def key = "${vpsInfo.vpsHost}:${vpsInfo.vpsUser}:${vpsInfo.vpsCredId}"
                                    if (!vpsMap.containsKey(key)) {
                                        vpsMap[key] = [needsRenew: false]
                                    }
                                    vpsMap[key].needsRenew = true

                                } else {
                                    echo "❌ No certificate for ${domain}, issuing new one"

                                    // 🌐 Resolve domain to IP
                                    def domainIp = sh(
                                        script: "dig +short ${domain} | tail -n1",
                                        returnStdout: true
                                    ).trim()

                                    if (!domainIp) {
                                        echo "⚠️ Cannot resolve domain ${domain}, skipping cert issuance."
                                        return
                                    }

                                    

                                    echo "🔍 Domain ${domain} resolves to ${domainIp}, VPS expected IP is ${vpsInfo.vpsHost}"

                                    if (domainIp != vpsInfo.vpsHost) {
                                        echo "❌ Domain ${domain} does not point to expected VPS ${vpsInfo.vpsHost}, skipping cert issuance."
                                        return
                                    }

                                    def tmpConfigFile = "${site.name}.conf"

                                    // Replace placeholders in nginx template
                                    def nginxConfig = certbotTemplate
                                        .replace('{{DOMAIN}}', domain)
                                        .replace('{{ENV_NAME}}', site.name)
                                        .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)

                                    writeFile(file: tmpConfigFile, text: nginxConfig)
                                    echo "✅ Generated Nginx config for ${site.name}: ${tmpConfigFile}"

                                    sh """
                                        # Upload config
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}

                                        # Move config into sites-available & enable
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                                            sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&
                                            sudo nginx -t &&
                                            sudo systemctl reload nginx
                                        "
                                        # Verify deployed config
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
                                    """

                                    // Ensure webroot folder and issue new cert
                                    sh """
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \\
                                        "sudo mkdir -p ${vpsInfo.webrootBase}/${site.name}/.well-known/acme-challenge && \\
                                         sudo chown -R www-data:www-data ${vpsInfo.webrootBase}/${site.name} && \\
                                         sudo nginx -t && \\
                                         sudo systemctl reload nginx && \\
                                         sudo certbot certonly --webroot -w ${vpsInfo.webrootBase}/${site.name} \\
                                         -d ${domain} -d www.${domain}"
                                    """
                                }
                            }
                        }
                    }

                    // 🔁 Renew ONCE per VPS
                    vpsMap.each { key, info ->
                        if (info.needsRenew) {
                            def (host, user, credId) = key.split(':')
                            sshagent (credentials: [credId]) {
                                sh """
                                    ssh -o StrictHostKeyChecking=no ${user}@${host} \
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
