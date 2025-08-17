pipeline {
    agent any

    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                }
            }
        }

        stage('Renew Certificates') {
            steps {
                script {
                    repos.each { repo ->
                        repo.envs.each { site ->
                            sshagent (credentials: [repo.vpsCredId]) {
                                sh """
                                    ssh -o StrictHostKeyChecking=no ubuntu@your-vps-ip \\
                                    "sudo certbot certonly --webroot -w ${repo.webrootBase}/${site.name} \\
                                    -d ${site.MAIN_DOMAIN.replaceAll('https://','').replaceAll('/','')} \\
                                    -d www.${site.MAIN_DOMAIN.replaceAll('https://','').replaceAll('/','')}"
                                """
                            }
                        }
                    }
                }
            }
        }
    }
}
