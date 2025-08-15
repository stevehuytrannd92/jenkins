// repos.groovy
return [
    [
        url: 'git@github.com:stevehuytrannd92/presale.git',
        branch: 'maxidoge',
        folder: 'maxidoge',
        credId: 'id_ed25519_stevehuytrannd92',
        envs: [
            [ 
                name: 'maxidoge', 
                deployTarget: 'dev-server',
                extraParam: 'value1'
            ],
             [ 
                name: 'maxidogecoin', 
                deployTarget: 'dev-server',
                extraParam: 'value1'
            ]
        ]

    ],
    // [
    //     url: 'git@github.com:stevehuytrannd92/presale.git',
    //     branch: 'btcswift',
    //     folder: 'btcswift',
    //     credId: 'id_ed25519_stevehuytrannd92'
    // ],
    // [
    //     url: 'git@github.com:stevehuytrannd92/flockerz.git',
    //     branch: 'btchyper',
    //     folder: 'btchyper',
    //     credId: 'id_ed25519_stevehuytrannd92'
    // ],
    
]
