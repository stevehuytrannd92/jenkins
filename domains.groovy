def repos = []
repos += load "${env.WORKSPACE}/gitRepos/btclayer.groovy"
repos += load "${env.WORKSPACE}/gitRepos/megaeth.groovy"
repos += load "${env.WORKSPACE}/gitRepos/maxidoge.groovy"
repos += load "${env.WORKSPACE}/gitRepos/btchyper.groovy"
repos += load "${env.WORKSPACE}/gitRepos/btcbullcoin.groovy"
repos += load "${env.WORKSPACE}/gitRepos/snorter.groovy"
repos += load "${env.WORKSPACE}/gitRepos/dogeverse.groovy"
repos += load "${env.WORKSPACE}/gitRepos/pepenode.groovy"

// Extract only MAIN_DOMAIN + vpsRef
def repoDomains = repos.collectMany { repo ->
    (repo.envs ?: []).collect { env ->
        def formatedDomain = env.MAIN_DOMAIN
            .replaceAll(/^https?:\/\//, '')   // remove protocol
            .replaceAll(/\/$/, '')            // remove trailing slash
            .replaceAll(/[^a-zA-Z0-9]/, '_')  // replace special chars with underscore
            .replaceAll(/_+/, '_')            // collapse consecutive underscores
            .toLowerCase()                    // optional: normalize case
        return [
            "MAIN_DOMAIN" : env.MAIN_DOMAIN,
            "vpsRef" : repo.vpsRef,
            "name": formatedDomain
        ]
    }
}

return repoDomains
