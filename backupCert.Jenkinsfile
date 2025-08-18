pipeline {
    agent any

    parameters {
        booleanParam(name: 'DO_RESTORE', defaultValue: false, description: 'Enable manual restore step?')
    }

    environment {
        BACKUP_DIR = "certbot_backups"   // just folder name
        DATE_SUFFIX = "${new Date().format('yyyy-MM-dd')}"
    }

    triggers {
        cron('H 2 * * *')
    }

    options {
        disableConcurrentBuilds()   // 🚫 no concurrent runs
        // buildDiscarder(logRotator(numToKeepStr: '10')) // optional cleanup
        // timeout(time: 60, unit: 'MINUTES')            // optional safety
    }



    stages {

        stage('Load Script') {
            steps {
                script {
                    vpsInfos = load 'vps.groovy'
                }
            }
        }

        stage('Backup Certbot on all VPS') {
            when { expression { return !params.DO_RESTORE } }
            steps {
                script {
                    vpsInfos.each { vpsKey, vps ->
                        sshagent(credentials: [vps.vpsCredId]) {
                            // Create backup on remote VPS (store under user's $HOME)
                            sh """
                                ssh -o StrictHostKeyChecking=no ${vps.vpsUser}@${vps.vpsHost} '
                                    mkdir -p /home/${vps.vpsUser}/${BACKUP_DIR} &&
                                    sudo tar -czf /home/${vps.vpsUser}/${BACKUP_DIR}/certbot-backup-${DATE_SUFFIX}.tar.gz /etc/letsencrypt
                                '
                            """

                            // Download backup into Jenkins workspace
                            sh """
                                scp -o StrictHostKeyChecking=no ${vps.vpsUser}@${vps.vpsHost}:/home/${vps.vpsUser}/${BACKUP_DIR}/certbot-backup-${DATE_SUFFIX}.tar.gz \
                                certbot-backup-${vpsKey}-${DATE_SUFFIX}.tar.gz
                            """
                        }

                        echo "✅ Backup completed and downloaded for VPS: ${vpsKey}"
                    }
                }
            }
        }


        stage('Restore Backup to VPS') {
            when { expression { return params.DO_RESTORE } }
            steps {
                script {
                    // collect list of backups in workspace
                    def backups = sh(script: "ls -1 certbot-backup-*.tar.gz || true", returnStdout: true).trim().split("\n")
                    if (backups.size() == 0) {
                        error("⚠️ No backups available in workspace")
                    }

                    // ask user for selection
                    def userInput = input(
                        id: 'restoreInput',
                        message: 'Select backup and VPS to restore',
                        parameters: [
                            choice(name: 'BACKUP_FILE', choices: backups.join("\n"), description: 'Select backup file'),
                            choice(name: 'RESTORE_VPS', choices: vpsInfos.keySet().join("\n"), description: 'Select VPS')
                        ]
                    )

                    def backupFile = userInput['BACKUP_FILE']
                    def restoreVps = userInput['RESTORE_VPS']

                    echo "🔧 Restoring ${backupFile} to ${restoreVps}"

                    def vps = vpsInfos[restoreVps]
                    sshagent(credentials: [vps.vpsCredId]) {
                        sh """
                            scp -o StrictHostKeyChecking=no ${backupFile} ${vps.vpsUser}@${vps.vpsHost}:/tmp/certbot-restore.tar.gz
                            ssh -o StrictHostKeyChecking=no ${vps.vpsUser}@${vps.vpsHost} '
                                sudo tar -xzf /tmp/certbot-restore.tar.gz -C / &&
                                sudo chown -R root:root /etc/letsencrypt &&
                                sudo chmod -R 700 /etc/letsencrypt &&
                                rm /tmp/certbot-restore.tar.gz
                            '
                        """
                    }
                    echo "✅ Restore complete: ${backupFile} → ${restoreVps}"
                }
            }
        }

    }
}
