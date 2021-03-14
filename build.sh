#!/usr/bin/env bash

# mvn -Pnative-image -DskipTests=true clean spring-aot:generate   package
mvn -DskipTests=true clean spring-boot:build-image