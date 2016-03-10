#!/bin/bash
mvn clean compile test-compile antrun:run assembly:single -P java-8
