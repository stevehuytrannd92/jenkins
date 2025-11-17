// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com-stevehuytrannd92/flockerz.git',
        branch: 'landing_liquidchain',
        folder: 'landing_liquidchain',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://liquidchaintoken.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/liquidchain/token.svg',
                MAIN_GTAG:'',
                MAIN_SITENAME:'liquidchain',
                MAIN_TWITTER:'@liquidchain',
                OG_PATH:'/img/liquidchain/OG.png'
            ],
            [ 
                MAIN_DOMAIN:'https://liquidchainlabs.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/liquidchain/token.svg',
                MAIN_GTAG:'',
                MAIN_SITENAME:'liquidchain',
                MAIN_TWITTER:'@liquidchain',
                OG_PATH:'/img/liquidchain/OG.png'
            ],
        ]

    ]
]