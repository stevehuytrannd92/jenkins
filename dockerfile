FROM jenkins/jenkins:lts
USER root
RUN apt-get update && apt-get install -y dnsutils
RUN apt-get update && apt-get install -y python3 build-essential


USER jenkins
