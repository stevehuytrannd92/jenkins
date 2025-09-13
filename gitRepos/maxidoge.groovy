// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'maxidoge',
        folder: 'maxidoge',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://maxidoge.io/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://maxidogecoin.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://maxidogtoken.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/maxidoge/logo.svg',
                MAIN_GTAG:'G-BS0FWQNNNZ',
                MAIN_SITENAME:'maxidoge',
                MAIN_TWITTER:'@maxidoge',
                OG_PATH:'/img/maxidoge/OG.jpeg'
            ],
        ]

    ]
]