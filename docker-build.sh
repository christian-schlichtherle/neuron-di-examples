#!/usr/bin/env bash

function docker-mvn() {
    docker run \
        --interactive \
        --rm \
        --tty \
        --volume $HOME/.m2:/root/.m2 \
        --volume $PWD:/workspace \
        --workdir /workspace \
        christianschlichtherle/scala-sbt:$1 \
        ./mvnw $2
}

set -ex
docker-mvn ${1:-1.2.8-jdk11} compile
docker-mvn ${2:-1.2.8-jdk12} verify
