#!/usr/bin/env bash

function docker-mvn() {
    local image_tag=$1
    shift
    docker run \
        --interactive \
        --rm \
        --tty \
        --volume $HOME/.m2:/root/.m2 \
        --volume $PWD:/workdir \
        --workdir /workdir \
        openjdk:$image_tag \
        ./mvnw $@
}

set -ex
docker-mvn ${1:-11-jdk-slim} clean package -DskipTests=true
docker-mvn ${2:-12-jdk-alpine} verify
