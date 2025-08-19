def loadRepos(String name) {
    return evaluate(new File("${WORKSPACE}/gitRepos/${name}.groovy"))
}

def repos = []
repos += load "${env.WORKSPACE}/gitRepos/maxidoge.groovy"
repos += load "${env.WORKSPACE}/gitRepos/btcswift.groovy"
repos += load "${env.WORKSPACE}/gitRepos/wepe.groovy"
repos += load "${env.WORKSPACE}/gitRepos/token6900.groovy"
repos += load "${env.WORKSPACE}/gitRepos/btchyper.groovy"
repos += load "${env.WORKSPACE}/gitRepos/btcbullcoin.groovy"


return repos.collect { repo ->
    repo.envs = repo.envs.collect { env ->
        def domain = env.MAIN_DOMAIN
            .replaceAll(/^https?:\/\//, '')   // remove protocol
            .replaceAll(/\/$/, '')            // remove trailing slash
            .replaceAll(/[^a-zA-Z0-9]/, '_')  // replace special chars with underscore
            .replaceAll(/_+/, '_')            // collapse consecutive underscores
            .toLowerCase()                    // optional: normalize case

        env.name = domain
        return env
    }
    return repo
}


