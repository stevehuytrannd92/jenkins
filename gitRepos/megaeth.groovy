// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'presale_megaeth',
        folder: 'presale_megaeth',
        credId: 'id_ed25519_stevehuytrannd92',
        buildType: 'nextjs',

        vpsRef : 'vps1',   // 👈 just reference which VPS to use

        envs: [
          
            [ 
                MAIN_DOMAIN:'https://megaethtoken.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/megaeth/token.svg',
                MAIN_GTAG:'G-KGQ352LLBS',
                MAIN_SITENAME:'megaeth',
                MAIN_TWITTER:'@megaeth',
                OG_PATH:'/img/megaeth/OG.png'
            ],
        ]

    ]
]