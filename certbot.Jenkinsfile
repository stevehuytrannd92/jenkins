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
                    repos = load 'domains.groovy'
                    vpsInfos = load 'vps.groovy'

                    certbotTemplate = readFile('ngnix/http.template.conf')
                }
            }
        }

        stage('Cleanup Redundant Certificates') {
            steps {
                script {
                    // Build expected domains per VPS
                    def expectedDomainsPerVps = [:]

                    repos.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        def domain = repo.MAIN_DOMAIN
                            .replaceAll('https://','')
                            .replaceAll('http://','')
                            .replaceAll('/','')
                            .replaceAll('^www\\.', '') // normalize

                            if (!expectedDomainsPerVps.containsKey(repo.vpsRef)) {
                                expectedDomainsPerVps[repo.vpsRef] = []
                            }


                        expectedDomainsPerVps[repo.vpsRef] << domain
                        echo "📌 Collected domain for VPS ${repo.vpsRef}: ${domain} + www.${domain}"
                        
                    }

                    // After collection, dump the whole map
                    expectedDomainsPerVps.each { vpsKey, domains ->
                        echo "✅ VPS ${vpsKey} should have certs for: ${domains}"
                    }

                    // Loop VPSes and clean up
                    expectedDomainsPerVps.each { vpsKey, domains ->
                        def vpsInfo = vpsInfos[vpsKey]

                        sshagent (credentials: [vpsInfo.vpsCredId]) {
                            // list all certs in /etc/letsencrypt/live
                            def existingCerts = sh(
                                script: """
                                    ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \
                                    'sudo ls -1 /etc/letsencrypt/live 2>/dev/null || echo "No certs found"'
                                """,
                                returnStdout: true
                            ).trim().split("\\r?\\n") as List

                            echo "📜 VPS ${vpsInfo.vpsHost} has certs: ${existingCerts}"
                            echo "✅ Expected for repos: ${domains}"

                            def redundant = existingCerts.findAll { !domains.contains(it) }
                            if (redundant) {
                                echo "🗑️ Removing redundant certs on ${vpsInfo.vpsHost}: ${redundant}"
                                redundant.each { cert ->
                                    sh """
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \\
                                        "sudo certbot delete --cert-name ${cert} --non-interactive --quiet || true"
                                    """
                                }

                            } else {
                                echo "✨ No redundant certs to remove on ${vpsInfo.vpsHost}"
                            }
                        }
                    }
                }
            }
        }



        stage('Handle Certificates') {
            steps {
                script {
                    def vpsMap = [:]  // plain HashMap

                    repos.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        def domain = repo.MAIN_DOMAIN
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

                                def tmpConfigFile = "${repo.name}.conf"

                                // Replace placeholders in nginx template
                                def nginxConfig = certbotTemplate
                                    .replace('{{DOMAIN}}', domain)
                                    .replace('{{ENV_NAME}}', repo.name)
                                    .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)

                                writeFile(file: tmpConfigFile, text: nginxConfig)
                                echo "✅ Generated Nginx config for ${repo.name}: ${tmpConfigFile}"

                                sh """
                                    # Upload config
                                    scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}

                                    # Move config into sites-available & enable
                                    ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                                         # ❌ Clear all enabled sites
                                        sudo rm -f /etc/nginx/sites-enabled/* &&
                                        
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
                                    "sudo mkdir -p ${vpsInfo.webrootBase}/${repo.name}/.well-known/acme-challenge && \\
                                        sudo chown -R www-data:www-data ${vpsInfo.webrootBase}/${repo.name} && \\
                                        sudo nginx -t && \\
                                        sudo systemctl reload nginx && \\
                                        sudo certbot certonly --webroot -w ${vpsInfo.webrootBase}/${repo.name} \\
                                        -d ${domain} -d www.${domain} \\
                                        -v \\
                                        --agree-tos \\
                                        --email contact@${domain} \\
                                        --non-interactive"
                                """
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
