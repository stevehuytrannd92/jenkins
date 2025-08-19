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

ssh -i keys/Test123.pem root@165.154.235.205



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



# handle link ngnix:
ls -l /etc/nginx/sites-enabled/
sudo rm -r /etc/nginx/sites-enabled/sites-available
sudo nginx -t
sudo systemctl reload nginx


# dig installation
docker exec -it -u root jenkins-sandbox bash

apt-get update && apt-get install -y dnsutils

