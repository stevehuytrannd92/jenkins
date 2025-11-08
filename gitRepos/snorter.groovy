return [
    [
        url: 'git@github.com:stevehuytrannd92/flockerz.git',
        branch: 'snortertoken',
        folder: 'snortertoken',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            // [ 
            //     MAIN_DOMAIN:'https://snorter.io/',
            //     BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
            //     LOGO_PATH:'/img/snorter/logo.png',
            //     MAIN_GTAG:'G-6FC2N1KBNH',
            //     MAIN_SITENAME:'snorter',
            //     MAIN_TWITTER:'@snorter',
            //     OG_PATH:'/img/snorter/OG.jpg'
            // ],
            // [ 
            //     MAIN_DOMAIN:'https://snortercoin.com/',
            //     BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
            //     LOGO_PATH:'/img/snorter/logo.png',
            //     MAIN_GTAG:'G-6FC2N1KBNH',
            //     MAIN_SITENAME:'snorter',
            //     MAIN_TWITTER:'@snorter',
            //     OG_PATH:'/img/snorter/OG.jpg'
            // ],
        ]

    ],
]