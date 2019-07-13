#!/usr/bin/env bash

function docker-mvn() {
    local image_tag=$1
    shift
    docker run \
        --interactive \
        --rm \
        --tty \
        --volume $HOME/.m2:/root/.m2 \
        --volume $PWD:/workspace \
        --workdir /workspace \
        christianschlichtherle/scala-sbt:$image_tag \
        ./mvnw $@
}

set -ex
docker-mvn ${1:-1.2.8-jdk11} clean package -DskipTests=true
docker-mvn ${2:-1.2.8-jdk12} verify
