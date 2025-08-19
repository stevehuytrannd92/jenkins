return [
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
]