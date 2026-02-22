FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# outils
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven \
    git \
    ca-certificates \
    jq \ 
    && rm -rf /var/lib/apt/lists/*

# ima 
COPY tools/ima /usr/local/bin/ima
RUN chmod +x /usr/local/bin/ima

WORKDIR /builds
