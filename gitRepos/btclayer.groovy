return [
    [
        url: 'git@github.com:stevehuytrannd92/flockerz.git',
        branch: 'btclayer',
        folder: 'btclayer',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://btclayer.io/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/btclayer/logo.png',
                MAIN_GTAG:'G-FLEHHN1B68',
                MAIN_SITENAME:'btclayer',
                MAIN_TWITTER:'@btclayer',
                OG_PATH:'/img/btclayer/OG.png'
            ],
        ]

    ],
]