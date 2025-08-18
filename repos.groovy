// repos.groovy
return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'maxidoge',
        folder: 'maxidoge',
        credId: 'id_ed25519_stevehuytrannd92',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://maxidoge.io/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://maxidogecoin.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://maxidogtoken.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
        ]

    ],

    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'btcswift',
        folder: 'btcswift',
        credId: 'id_ed25519_stevehuytrannd92',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://btcswifts.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/btcswift/bitcoin-swift-logo-main.webp',
                MAIN_GTAG:'G-TB2PNE8KD4',
                MAIN_SITENAME:'btcswift',
                MAIN_TWITTER:'@btcswift',
                OG_PATH:'/img/btcswift/OG.jpeg'
            ],
        ]

    ],
    [
        url: 'git@github.com:trungplq/flockerz.git',
        branch: 'wepe',
        folder: 'wepe',
        credId: 'id_ed25519_stevehuytrannd92',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://wepetoken.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/wepe/token.svg',
                MAIN_GTAG:'G-LJLVSX3D6T',
                MAIN_SITENAME:'wepetoken',
                MAIN_TWITTER:'@wepetoken',
                OG_PATH:'/img/wepe/OG.jpeg'
            ],
        ]

    ],
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'token6900',
        folder: 'token6900',
        credId: 'id_ed25519_stevehuytrannd92',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://token6969.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/token6900/favicon.png',
                MAIN_GTAG:'G-15Q5N6TLHT',
                MAIN_SITENAME:'token6900',
                MAIN_TWITTER:'@token6900',
                OG_PATH:'/img/token6900/OG.jpeg'
            ],
        ]

    ],
    [
        url: 'git@github.com:stevehuytrannd92/flockerz.git',
        branch: 'btchyper',
        folder: 'btchyper',
        credId: 'id_ed25519_stevehuytrannd92',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://btchyper.io/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/btchyper/token.svg',
                MAIN_GTAG:'G-RKFMME3BTM',
                MAIN_SITENAME:'btchyper',
                MAIN_TWITTER:'@btchyper',
                OG_PATH:'/img/btchyper/OG.jpeg'
            ],
        ]

    ],
    
    [
        url: 'git@github.com:stevehuytrannd92/flockerz.git',
        branch: 'btcbull-btcbullcoin',
        folder: 'btcbullcoin',
        credId: 'id_ed25519_stevehuytrannd92',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://thebtcbull.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks3.json',
                LOGO_PATH:'/img/btcbull/logo.png',
                MAIN_GTAG:'G-GR6Q9PM18Q',
                MAIN_SITENAME:'btcbull',
                MAIN_TWITTER:'@btcbull',
                OG_PATH:'img/btcbull/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://btcbullcoin.io/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks3.json',
                LOGO_PATH:'/img/btcbull/logo.png',
                MAIN_GTAG:'G-GR6Q9PM18Q',
                MAIN_SITENAME:'btcbull',
                MAIN_TWITTER:'@btcbull',
                OG_PATH:'img/btcbull/OG.jpeg'
            ],
            [
                MAIN_DOMAIN:'https://btcxbull.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks3.json',
                LOGO_PATH:'/img/btcbull/logo.png',
                MAIN_GTAG:'G-GR6Q9PM18Q',
                MAIN_SITENAME:'btcbull',
                MAIN_TWITTER:'@btcbull',
                OG_PATH:'img/btcbull/OG.jpeg'
            ],
        ]

    ],

    

]
.collect { repo ->
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


