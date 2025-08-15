// repos.groovy
return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'maxidoge',
        folder: 'maxidoge',
        credId: 'id_ed25519_stevehuytrannd92',
        envs: [
            [ 
                name: 'maxidoge', 
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_DOMAIN:'https://maxidoge.io',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
            [ 
                name: 'maxidogecoin', 
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_DOMAIN:'https://maxidogecoin.com',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ]
        ]

    ],
    // [
    //     url: 'git@github.com:stevehuytrannd92/presale.git',
    //     branch: 'btcswift',
    //     folder: 'btcswift',
    //     credId: 'id_ed25519_stevehuytrannd92'
    // ],
    // [
    //     url: 'git@github.com:stevehuytrannd92/flockerz.git',
    //     branch: 'btchyper',
    //     folder: 'btchyper',
    //     credId: 'id_ed25519_stevehuytrannd92'
    // ],
    
]
