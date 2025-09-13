FROM jenkins/jenkins:lts
USER root
RUN apt-get update && apt-get install -y dnsutils
RUN apt-get update && apt-get install -y python3 build-essential

RUN apt-get update && apt-get install -y redis-tools
RUN apt-get update && apt-get install -y rsync

USER jenkins
