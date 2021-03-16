#!/usr/bin/env bash
## make sure FN == the so-called fat jar
FN=$1
mkdir -p src/main/resources/META-INF/native-image
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/$FN

# then just run mvn clean spring-boot:build-image