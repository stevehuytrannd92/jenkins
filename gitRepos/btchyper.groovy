return [
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
            [ 
                MAIN_DOMAIN:'https://btchypercoin.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/btchyper/token.svg',
                MAIN_GTAG:'G-RKFMME3BTM',
                MAIN_SITENAME:'btchyper',
                MAIN_TWITTER:'@btchyper',
                OG_PATH:'/img/btchyper/OG.jpeg'
            ],
        ]

    ],
]