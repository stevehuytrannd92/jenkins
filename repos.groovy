// repos.groovy
return [
    // [
    //     url: 'git@github.com:stevehuytrannd92/presale.git',
    //     branch: 'maxidoge',
    //     folder: 'maxidoge',
    //     credId: 'id_ed25519_stevehuytrannd92',
    //     envs: [
    //         [ 
    //             name: 'maxidoge', 
    //             BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
    //             LOGO_PATH:'/img/maxidoge/logo.svg',
    //             MAIN_DOMAIN:'https://maxidoge.io',
    //             MAIN_GTAG:'G-BS0FWQNNNZ',
    //             MAIN_SITENAME:'maxidoge',
    //             MAIN_TWITTER:'@maxidoge',
    //             OG_PATH:'/img/maxidoge/OG.jpeg'
    //         ],
    //         [ 
    //             name: 'maxidogecoin', 
    //             BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
    //             LOGO_PATH:'/img/maxidoge/logo.svg',
    //             MAIN_DOMAIN:'https://maxidogecoin.com',
    //             MAIN_GTAG:'G-BS0FWQNNNZ',
    //             MAIN_SITENAME:'maxidoge',
    //             MAIN_TWITTER:'@maxidoge',
    //             OG_PATH:'/img/maxidoge/OG.jpeg'
    //         ]
    //     ]

    // ],
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'btcswift',
        folder: 'btcswift',
        credId: 'id_ed25519_stevehuytrannd92',
        vpsCredId: 'vps1',
        vpsUser: 'ubuntu',
        vpsHost: '165.154.235.205',
        webrootBase: '/var/www/presale',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                name: 'btcswifts', 
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
    

]



