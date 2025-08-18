import groovy.transform.Field


@Field def changedRepos = []
@Field def missingCerts = []


// --- Helper functions ---
def extractDomain(String url) {
    return url
        .replaceAll(/^https?:\/\//, '')  // remove http(s)
        .replaceAll(/\/$/, '')           // remove trailing slash
        .replaceAll(/^www\./, '')        // strip leading www
}

// Check if domain exists in MISSING_CERTS string
def isMissingCert(String domain) {
    if (!missingCerts || missingCerts.isEmpty()) {
        return false
    }
    def domainList = missingCerts.collect { it?.trim()?.toLowerCase() }
                             .findAll { it } // filter out null/empty
    return domainList.contains(domain.toLowerCase())
}
// Check if new comit
// def isNewCommit(String repo, String newChangesRepo) {
//     if (!newChangesRepo?.trim()) {
//         return false
//     }
//     def repoList = newChangesRepo.split(',')
//         .collect { it.trim().toLowerCase() }
//         .findAll { it }  // drop empty strings
//     return repoList.contains(repo.toLowerCase())
// }



def isNewCommit(String repo) {
    if (!changedRepos || changedRepos.isEmpty()) {
        return false
    }
    def repoList = changedRepos.collect { it?.trim()?.toLowerCase() }
                             .findAll { it } // filter out null/empty
    return repoList.contains(repo.toLowerCase())
}

// Run a map of tasks with maxParallel at once
def runWithMaxParallel(tasks, maxParallel = 3) {
    def keys = tasks.keySet() as List
    def total = keys.size()

    for (int i = 0; i < total; i += maxParallel) {
        // 🔑 convert subList to real List so it's serializable
        def slice = new ArrayList(keys.subList(i, Math.min(i + maxParallel, total)))
        def batch = [:]
        slice.each { k -> batch[k] = tasks[k] }
        parallel batch
    }
}

pipeline {
    agent any
    tools {
        nodejs 'NODE_20'
    }

    parameters {
        booleanParam(
            name: 'FORCE_BUILD_ALL',
            defaultValue: false,
            description: 'Force build & deploy all repos, even if no new changes'
        )
        string(
            name: 'MAX_PARALLEL',
            defaultValue: '3',
            description: 'Maximum number of tasks to run in parallel'
        )

    }

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
        // buildDiscarder(logRotator(numToKeepStr: '10')) // optional cleanup
        // timeout(time: 60, unit: 'MINUTES')            // optional safety
    }


    triggers {
        cron('0,15,30,45 * * * *')
    }


    stages {
        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    vpsInfos = load 'vps.groovy'
                    ngnixTemplate = readFile('ngnix/https.template.conf')
                }
            }
        }

        stage('Check Certificates') {
            steps {
                script {

                    repos.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        repo.envs.each { site ->
                            def domain = extractDomain(site.MAIN_DOMAIN)

                            sshagent (credentials: [vpsInfo.vpsCredId]) {
                                def exists = sh(
                                    script: """
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} \
                                        "sudo test -f /etc/letsencrypt/live/${domain}/fullchain.pem && echo yes || echo no"
                                    """,
                                    returnStdout: true
                                ).trim()

                                if (exists == "no") {
                                    echo "⚠️  Certificate missing for ${domain}"
                                    missingCerts << domain
                                } else {
                                    echo "✅ Certificate exists for ${domain}"
                                }
                            }
                        }
                    }

                    if (missingCerts) {
                        echo "⚠️  Some certificates are missing: ${missingCerts}"
                    } else {
                        echo "✅ All certificates present"
                    }
                }
            }
        }

        stage('Repos Pulls') {
            steps {
                script {
                    def parallelTasks = [:]


                    repos.each { repo ->
                        parallelTasks["Pull-${repo.folder}"] = {
                            def changed = false
                            dir(repo.folder) {
                                def vpsInfo = vpsInfos[repo.vpsRef]

                                if (!fileExists('.git')) {
                                    // First time clone
                                    checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: "*/${repo.branch}"]],
                                        doGenerateSubmoduleConfigurations: false,
                                        userRemoteConfigs: [[
                                            url: repo.url,
                                            credentialsId: repo.credId
                                        ]],
                                        extensions: [
                                            // Optimize repo checkout
                                            [$class: 'CloneOption', depth: 1, noTags: true, shallow: true],
                                            [$class: 'PruneStaleBranch']
                                        ]
                                    ])
                                    changed = true  
                                    changedRepos << repo.folder
                                } else {
                                    def oldCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

                                    checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: "*/${repo.branch}"]],
                                        doGenerateSubmoduleConfigurations: false,
                                        userRemoteConfigs: [[
                                            url: repo.url,
                                            credentialsId: repo.credId
                                        ]],
                                        extensions: [
                                            [$class: 'PruneStaleBranch'],
                                            [$class: 'CloneOption', depth: 1, noTags: true, shallow: true]
                                        ]
                                    ])

                                    def newCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

                                    if (oldCommit != newCommit) {
                                        echo "🔄 Changes detected in ${repo.folder}: ${oldCommit} → ${newCommit}"
                                        changed = true
                                        changedRepos << repo.folder

                                    } else {
                                        echo "⏭️ No changes in ${repo.folder}"
                                        changed = false

                                    }
                                }

                            }
                            // 👇 write result to a file for later collection
                            writeFile file: "${repo.folder}.changed", text: changed.toString()
                        }
                    }

                    runWithMaxParallel(parallelTasks, params.MAX_PARALLEL.toInteger())  // 👈 cap parallelism

                    echo "Collected repos = ${changedRepos}"


                }
            }
        }

        stage('Build Projects') {
            steps {
                script {
                    def parallelBuilds = [:]

                    repos.each { repo ->
                        parallelBuilds["Repo-${repo.folder}"] = {
                            if (!params.FORCE_BUILD_ALL && !isNewCommit(repo.folder)) {
                                echo "⏭️ Skipping build for ${repo.folder}, no changes detected"
                                return
                            }

                            def vpsInfo = vpsInfos[repo.vpsRef]
                            dir(repo.folder) {
                                repo.envs.eachWithIndex { envConf, idx ->
                                    def domain = extractDomain(envConf.MAIN_DOMAIN)

                                    if (isMissingCert(domain)) {
                                        echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
                                        return
                                    }

                                    echo "=== Building ${repo.folder} branch >>${repo.branch}<< for environment: ${envConf.name} ==="

                                    withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
                                        if (idx == 0) {
                                            // 👉 First env: full CI build
                                            sh '''
                                                if [ -f package.json ]; then
                                                    export CI=true
                                                    npm ci
                                                    npx next build && npx next-sitemap

                                                    if [ -d .next ]; then
                                                        rm -rf .next/cache || true
                                                        rm -rf .next/server || true
                                                        rm -rf .next/**/*.nft.json || true
                                                    fi
                                                else
                                                    echo "No package.json found, skipping build."
                                                fi
                                            '''
                                        } else {
                                            sh '''
                                                if [ -f package.json ]; then
                                                    npx next build && npx next-sitemap

                                                    if [ -d .next ]; then
                                                        rm -rf .next/cache || true
                                                        rm -rf .next/server || true
                                                        rm -rf .next/**/*.nft.json || true
                                                    fi
                                                else
                                                    echo "No package.json found, skipping build."
                                                fi
                                            '''                                        
                                        }

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

                    runWithMaxParallel(parallelBuilds, params.MAX_PARALLEL.toInteger())  // 👈 cap parallelism
                }
            }
        }

        stage('Deploy Outs to VPS') {
            steps {
                script {
                    repos.each { repo ->
                        if (!params.FORCE_BUILD_ALL && !isNewCommit(repo.folder)) {
                            echo "⏭️ Skipping deploy for ${repo.folder}, no changes detected"
                            return
                        }

                        def vpsInfo = vpsInfos[repo.vpsRef]
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain)) {
                                    echo "⏭️ Skipping deploy for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def envOut = "outs/${envConf.name}"
                                echo "🚀 Deploying ${envOut} to ${vpsInfo.vpsHost}:${vpsInfo.webrootBase}/${envConf.name}"

                                sshagent (credentials: [vpsInfo.vpsCredId]) {
                                    sh """
                                        tar -czf ${envConf.name}.tar.gz -C outs/${envConf.name} .
                                        scp -o StrictHostKeyChecking=no ${envConf.name}.tar.gz ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/tmp/

                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                                            sudo mkdir -p ${vpsInfo.webrootBase}/${envConf.name} &&
                                            sudo tar -xzf /tmp/${envConf.name}.tar.gz -C ${vpsInfo.webrootBase}/${envConf.name} &&
                                            rm /tmp/${envConf.name}.tar.gz &&
                                            sudo chown -R www-data:www-data ${vpsInfo.webrootBase}/${envConf.name}
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
                        if (!params.FORCE_BUILD_ALL && !isNewCommit(repo.folder)) {
                            echo "⏭️ Skipping nginx config for ${repo.folder}, no changes detected"
                            return
                        }

                        def vpsInfo = vpsInfos[repo.vpsRef]
                        dir(repo.folder) {
                            repo.envs.each { envConf ->
                                def domain = extractDomain(envConf.MAIN_DOMAIN)

                                if (isMissingCert(domain)) {
                                    echo "⏭️ Skipping nginx config for ${envConf.name} (${domain}) due to missing cert"
                                    return
                                }

                                def tmpConfigFile = "${envConf.name}.conf"
                                def nginxConfig = ngnixTemplate
                                    .replace('{{DOMAIN}}', domain)
                                    .replace('{{ENV_NAME}}', envConf.name)
                                    .replace('{{WEBROOT_BASE}}', vpsInfo.webrootBase)


                                writeFile(file: tmpConfigFile, text: nginxConfig)
                                echo "✅ Generated Nginx config for ${envConf.name} locally: ${tmpConfigFile}"
                                echo "📄 Local nginx config content for ${envConf.name}:\n${nginxConfig}"

                                sshagent(credentials: [vpsInfo.vpsCredId]) {
                                    sh """
                                        scp -o StrictHostKeyChecking=no ${tmpConfigFile} ${vpsInfo.vpsUser}@${vpsInfo.vpsHost}:/home/${vpsInfo.vpsUser}/${tmpConfigFile}
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
                                            sudo mv /home/${vpsInfo.vpsUser}/${tmpConfigFile} /etc/nginx/sites-available/${tmpConfigFile} &&
                                            sudo chown root:root /etc/nginx/sites-available/${tmpConfigFile} &&

                                            # 👉 test only this config
                                            sudo nginx -t -c /etc/nginx/sites-available/${tmpConfigFile} &&

                                            # 👉 activate only this site
                                            sudo ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile} &&

                                            # 👉 reload nginx once (affects all, but only after this site passed syntax check)
                                            sudo systemctl reload nginx
                                        "
                                        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "cat /etc/nginx/sites-available/${tmpConfigFile}"
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
