pipeline {
    agent any
    triggers {
        cron('0 0,12 * * *')
    }

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
    }

    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'domains.groovy'
                    vpsInfos = load 'vps.groovy'
                }
            }
        }

        stage('Check Domain Resolution') {
            steps {
                script {
                    def wrongDomains = []

                    repos.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        def domain = repo.MAIN_DOMAIN
                            .replaceAll('https://','')
                            .replaceAll('http://','')
                            .replaceAll('/','')
                            .replaceAll('^www\\.', '')

                        def url = "http://${domain}"

                        // 🔍 Try resolving IP
                        def domainIp = sh(
                            script: "dig +short ${domain} | tail -n1",
                            returnStdout: true
                        ).trim()

                        // 🌐 Do HTTP check (curl returns HTTP code only)
                        def httpCode = sh(
                            script: "curl -s -o /dev/null -w \"%{http_code}\" --max-time 10 ${url}",
                            returnStdout: true
                        ).trim()

                        if (!domainIp) {
                            echo "⚠️ Domain ${domain} could not be resolved"
                            wrongDomains << [domain: domain, reason: "Unresolvable", expected: vpsInfo.vpsHost]
                        } else if (httpCode != "200") {
                            echo "❌ Domain ${domain} resolved to ${domainIp} but HTTP returned ${httpCode}"
                            wrongDomains << [domain: domain, reason: "HTTP ${httpCode}", resolved: domainIp, expected: vpsInfo.vpsHost]
                        } else if (domainIp != vpsInfo.vpsHost) {
                            echo "❌ Domain ${domain} resolves to ${domainIp}, expected ${vpsInfo.vpsHost}"
                            wrongDomains << [domain: domain, reason: "IP mismatch", resolved: domainIp, expected: vpsInfo.vpsHost]
                        } else {
                            echo "✅ Domain ${domain} resolves correctly to ${domainIp} and HTTP ${httpCode}"
                        }
                    }

                    if (wrongDomains) {
                        echo "🚨 Wrong domains detected:"
                        wrongDomains.each { d ->
                            echo " - ${d.domain} (${d.reason}): resolved=${d.resolved ?: 'none'}, expected=${d.expected}"
                        }

                        writeFile file: "wrong_domains.log", text: wrongDomains.collect { d ->
                            "${d.domain}, reason=${d.reason}, resolved=${d.resolved ?: 'none'}, expected=${d.expected}"
                        }.join("\n")

                        archiveArtifacts artifacts: 'wrong_domains.log', onlyIfSuccessful: false
                    } else {
                        echo "✨ All domains resolve and respond correctly"
                    }
                }
            }
        }

    }
}
