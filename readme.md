docker run -d \
    --name jenkins-sandbox \
    -p 12000:8080 \
    -p 12001:50000 \
    jenkins/jenkins:lts

docker exec jenkins-sandbox cat /var/jenkins_home/secrets/initialAdminPassword

jenkins-plugin-cli --plugins \
    git \
    github \
    github-branch-source \
    ssh-agent \
    publish-over-ssh



docker exec -it jenkins-sandbox bash
ssh -vT git@github.com


check ssh key pass:
ssh-keygen -y -f /Users/steve/.ssh/id_ed25519_stevehuytrannd92


3. Remove the passphrase on the copy
ssh-keygen -p \
  -f keys/id_ed25519_stevehuytrannd92 \
  -N "" \
  -P "123456"
