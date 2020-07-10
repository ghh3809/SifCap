#!/usr/bin/env bash

# Be sure your script exit whenever encounter errors
set -e

mvn -U clean install -Dmaven.test.skip=true

sh ./postBuild.sh
