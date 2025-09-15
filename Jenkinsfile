// This annotation tells Jenkins to load the library you configured
@Library('my-shared-library') _ 



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
            defaultValue: '4',
            description: 'Maximum number of tasks to run in parallel'
        )

    }

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
        buildDiscarder(logRotator(numToKeepStr: '100')) // optional cleanup
        // timeout(time: 60, unit: 'MINUTES')            // optional safety
    }


    triggers {
        cron('0,10,20,30,40,50 * * * *')
    }


    stages {
        stage('Clear redis') {
            steps {
                script {
                    try {
                        redisState.clearAll()
                    } catch (Exception e) {
                        echo "redisState not found: ${e}"
                    }
                }
            }
        }



        stage('Load Script') {
            steps {
                script {
                    repos = load 'repos.groovy'
                    vpsInfos = load 'vps.groovy'

                }
            }
        }


        stage('Verify repo envs') {
            steps {
                script {
                    repos.each { repo ->
                        if (repo.envs.size() > 16) {
                            error "❌ Repo '${repo.folder}' has ${repo.envs.size()} envs, exceeds limit of 16."
                        }
                    }
                    echo "✅ All repos have ≤ 16 envs."
                }
            }
        }

        stage('Verify Domains') {
            steps {
                script {
                    def domainMap = [:]
                    def duplicates = []

                    repos.each { repo ->
                        repo.envs.each { env ->
                            def domain = env.MAIN_DOMAIN
                                .replaceAll(/^https?:\/\//, '')   // remove protocol
                                .replaceAll(/\/$/, '')            // remove trailing slash
                                .toLowerCase()

                            if (domainMap.containsKey(domain)) {
                                duplicates << [
                                    domain: domain,
                                    repo1: domainMap[domain],
                                    repo2: "${repo.folder}:${env.name}"
                                ]
                            } else {
                                domainMap[domain] = "${repo.folder}:${env.name}"
                            }
                        }
                    }

                    if (duplicates) {
                        echo "❌ Found duplicate MAIN_DOMAIN(s):"
                        duplicates.each { d ->
                            echo " - ${d.domain} used in ${d.repo1} and ${d.repo2}"
                        }
                        error("Duplicate MAIN_DOMAIN detected, aborting build.")
                    } else {
                        echo "✅ All MAIN_DOMAIN values are unique."
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
                                    redisState.addChangedRepo(repo.folder)
                                    // changedRepos << repo.folder
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

                                        // changedRepos << repo.folder
                                        redisState.addChangedRepo(repo.folder)

                                    } else {
                                        echo "⏭️ No changes in ${repo.folder}"


                                    }
                                }

                            }

                        }
                    }

                    runWithMaxParallel(parallelTasks, params.MAX_PARALLEL.toInteger())  // 👈 cap parallelism
                    
                    def changedRepos = redisState.getChangedRepos() as List

                    echo "Collected repos = ${changedRepos}"
                    if (!params.FORCE_BUILD_ALL && changedRepos.isEmpty()) {
                        echo "⏭️ No changes and FORCE_BUILD_ALL not set, stopping pipeline early."
                        return  // exits this stage, and since no later stages run → SUCCESS
                    }
                }
            }
        }


        stage('Check Certificates') {
            when { expression { 
                def changedRepos = redisState.getChangedRepos() as List
                return params.FORCE_BUILD_ALL || !changedRepos.isEmpty() 
            } }

            steps {
                script {
                    def changedRepos = redisState.getChangedRepos() as List
                    def reposToCheck = params.FORCE_BUILD_ALL ? repos : repos.findAll { r -> changedRepos.contains(r.folder) }

                    reposToCheck.each { repo ->
                        def vpsInfo = vpsInfos[repo.vpsRef]
                        repo.envs.each { site ->
                            def domain = commonUtils.extractDomain(site.MAIN_DOMAIN)

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
                                    redisState.addMissingCert(domain)
                                } else {
                                    echo "✅ Certificate exists for ${domain}"
                                }
                            }
                        }
                    }

                    if (redisState.getMissingCerts()) {
                        echo "⚠️  Some certificates are missing: ${redisState.getMissingCerts()}"
                    } else {
                        echo "✅ All certificates present"
                    }
                }
            }
        }


        stage('Build Projects') {
            steps {
                script {

                    def parallelSetups = [:]

                    repos.each { repo ->
                        parallelSetups["setup-${repo.folder}"] = {
                            if (!params.FORCE_BUILD_ALL && !redisState.isNewCommit(repo.folder)) {
                                echo "⏭️ Skipping setup for ${repo.folder}, no changes detected"
                                return
                            }
                            buildUtils.setupBuild(repo);
                        }
                    }

                    runWithMaxParallel(parallelSetups, params.MAX_PARALLEL.toInteger())  // 👈 cap parallelism

                    def parallelBuilds = [:]

                    repos.each { repo ->
                        repo.envs.eachWithIndex { envConf, idx ->
                            parallelBuilds["build-${envConf.name}"] = {
                                if (!params.FORCE_BUILD_ALL && !redisState.isNewCommit(repo.folder)) {
                                    echo "⏭️ Skipping setup for ${repo.folder}, no changes detected"
                                    return
                                }
                                buildUtils.build(repo, envConf);
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
                    def parallelTasks = [:]

                    repos.each { repo ->
                        parallelTasks["Repo-${repo.folder}"] = {
                            if (!params.FORCE_BUILD_ALL && !redisState.isNewCommit(repo.folder)) {
                                echo "⏭️ Skipping build for ${repo.folder}, no changes detected"
                                return
                            }
                            repo.envs.eachWithIndex { envConf, idx ->
                                deployUtils.deploy(repo, envConf, vpsInfos)
                            }
                        }
                    }

                    runWithMaxParallel(parallelTasks, 3)  // 👈 cap parallelism


                }
            }
        }

        stage('Generate NGNIX config and deploy SSH') {
            steps {
                script {
                    def changedRepos = redisState.getChangedRepos()
                    if (!params.FORCE_BUILD_ALL && !changedRepos) {
                        echo "⏭️ Skipping nginx reload, no changes detected"
                        return
                    }

                    repos.each { repo ->
                        repo.envs.each { envConf ->
                            nginxUtils.generate(repo, envConf, vpsInfos )
                        }
                    
                    }


                    vpsInfos.values().each { vpsConf -> 
                        sshagent(credentials: [vpsConf.vpsCredId]) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${vpsConf.vpsUser}@${vpsConf.vpsHost} "

                                    sudo nginx -t &&

                                    sudo systemctl reload nginx
                                "
                            """
                        }
                    }
                }
            }
        }
    }
}
