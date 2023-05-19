#!/usr/bin/env bash

ensure_tool(){
    cmd="$1"
    package="$2"
    if command -v "${cmd}" >/dev/null; then
        return
    fi
    if command -v apt-get; then
        sudo apt-get update; sudo apt-get -y install ${package}
    else
        echo "Unable to install ${cmd}, apt-get not detected if you're on a mac, use 'brew install ${package}'"
        return 1
    fi
}

ensure_mvn(){
    command -v mvn && return
    mvn_version="${1:-3.9.2}"
    curl "https://dlcdn.apache.org/maven/maven-3/${mvn_version}/binaries/apache-maven-${mvn_version}-bin.tar.gz" > maven.tgz
    tar xzf maven.tgz
    export PATH="${PATH}:${PWD}/apache-maven-${mvn_version}/bin"
}

ensure_xmlstarlet(){
    ensure_tool xmlstarlet xmlstarlet
}
