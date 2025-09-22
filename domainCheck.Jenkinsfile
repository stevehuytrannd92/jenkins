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

                        def url = "https://${domain}"

                        // 🌐 Do HTTP check (curl returns HTTP code only)
                        def httpCode = sh(
                            script: "curl -s -o /dev/null -w \"%{http_code}\" --max-time 10 ${url}",
                            returnStdout: true
                        ).trim()

                        if (httpCode != "200") {
                            echo "❌ Domain ${domain} HTTP returned ${httpCode}"
                            wrongDomains << [domain: domain, reason: "HTTP ${httpCode}"]
                        } else {
                            echo "✅ Domain ${domain} resolves HTTP ${httpCode}"
                        }
                    }

                    if (wrongDomains) {
                        echo "🚨 Wrong domains detected:"
                        wrongDomains.each { d ->
                            echo " - ${d.domain} (${d.reason}): resolved=${d.resolved ?: 'none'}, expected=${d.expected}"
                        }
                    } else {
                        echo "✨ All domains resolve and respond correctly"
                    }
                }
            }
        }

    }
}
