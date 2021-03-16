#!/usr/bin/env bash
mvn -X -DskipTests=true clean spring-boot:build-image | tee ~/Desktop/output.txt