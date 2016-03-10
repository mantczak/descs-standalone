#!/bin/sh
mvn clean compile test-compile antrun:run test assembly:single -P java-8
