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