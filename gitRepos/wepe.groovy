return [
    [
        url: 'git@github.com:trungplq/flockerz.git',
        branch: 'wepe',
        folder: 'wepe',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

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
]