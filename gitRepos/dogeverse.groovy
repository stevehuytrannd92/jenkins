return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'dogeverse',
        folder: 'dogeverse',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://thedogverse.io/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/dogeverse/token.svg',
                MAIN_GTAG:'G-KYB4XZ3NGN',
                MAIN_SITENAME:'thedogverse',
                MAIN_TWITTER:'@thedogverse',
                OG_PATH:'/img/dogeverse/OG.jpeg'
            ],
          
        ]

    ],
]