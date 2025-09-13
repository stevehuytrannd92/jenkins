return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'presale_pepenode',
        folder: 'pepenode',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://pepenode.net/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/pepenode/token.svg',
                MAIN_GTAG:'G-V1Y47SRL6G',
                MAIN_SITENAME:'pepenode',
                MAIN_TWITTER:'@pepenode',
                OG_PATH:'/img/pepenode/OG.jpeg'
            ],
        ]

    ]
]