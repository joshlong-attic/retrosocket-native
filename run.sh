#!/usr/bin/env bash

mvn -DskipTests=true clean spring-boot:build-image
docker run docker.io/library/retrosocket-native:0.0.1-SNAPSHOT