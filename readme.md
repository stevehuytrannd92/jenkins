docker volume create jenkins_home
docker run -d --name jenkins-sandbox -p 12000:8080 -p 12001:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts

docker exec jenkins-sandbox cat /var/jenkins_home/secrets/initialAdminPassword

jenkins-plugin-cli --plugins \
    git \
    github \
    github-branch-source \
    ssh-agent \
    publish-over-ssh



docker exec -it jenkins-sandbox bash
ssh -vT git@github.com


docker exec -it  -u  jenkins jenkins-sandbox bash
which npm
# or
echo $PATH


fix the node-gyp
docker exec -it -u root jenkins-sandbox bash

# Inside Jenkins container
apt-get update
apt-get install -y python3 build-essential
which python3

--reset container




check ssh key pass:
ssh-keygen -y -f /Users/steve/.ssh/id_ed25519_stevehuytrannd92


3. Remove the passphrase on the copy
ssh-keygen -p \
  -f keys/id_ed25519_stevehuytrannd92 \
  -N "" \
  -P "123456"



ssh ${vpsUser}@${vpsHost} 'ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile}'
ssh ${vpsUser}@${vpsHost} 'nginx -t && systemctl reload nginx'


ssh -i /Users/steve/Coding/Jenkins/keys/Test123.pem ubuntu@165.154.235.205
ssh -i /Users/steve/Coding/Jenkins/keys/hostinger_key.pem root@156.67.218.212




sudo certbot certonly --webroot -w /var/www/presale/btcswifts -d btcswifts.com -d www.btcswifts.com


docker exec -it jenkins-sandbox bash

# list workspaces
ls -lh /var/jenkins_home/workspace

# remove old ones (careful!)
rm -rf /var/jenkins_home/workspace/build-web
rm -rf /var/jenkins_home/workspace/build-web@script
rm -rf /var/jenkins_home/workspace/build-web@tmp
rm -rf /var/jenkins_home/workspace/build-web
rm -rf /var/jenkins_home/workspace/build-web



# handle link nginx:
ls -l /etc/nginx/sites-enabled/
sudo rm -r /etc/nginx/sites-enabled/sites-available
sudo nginx -t
sudo systemctl reload nginx


# dig installation
docker exec -it -u root jenkins-sandbox bash

apt-get update && apt-get install -y dnsutils



redundant.each { cert ->
    sh """
        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
            sudo rm -rf /etc/letsencrypt/live/${cert} /etc/letsencrypt/archive/${cert} /etc/letsencrypt/renewal/${cert}.conf || true
        "
    """
}


sudo certbot certificates
sudo cat /etc/letsencrypt/renewal/api.memepush.com.conf


grep -R "account =" /etc/letsencrypt/renewal/


ls /etc/letsencrypt/accounts/acme-v02.api.letsencrypt.org/directory/


cat /etc/letsencrypt/accounts/acme-v02.api.letsencrypt.org/directory/5b5057805855dfaabbd69a9752be6f2e/meta.json
## check account cerbot and replace ## 
sudo sed -i 's/^account = .*/account = 5b5057805855dfaabbd69a9752be6f2e/' /etc/letsencrypt/renewal/*.conf

grep -R "account =" /etc/lexstsencrypt/renewal/

sudo certbot renew --dry-run

sudo rm -rf /etc/letsencrypt/accounts/acme-v02.api.letsencrypt.org/directory/7d3a8b67d95da1c20959bc2b4a5f6f7b*


