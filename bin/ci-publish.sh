#!/usr/bin/env bash
set -eu
DEPLOY_KEY_FILE=$HOME/github_rsa
echo "$GITHUB_DEPLOY_KEY" > ${DEPLOY_KEY_FILE}
chmod 600 ${DEPLOY_KEY_FILE}
ssh-agent bash -c "ssh-add $DEPLOY_KEY_FILE; sbt -Dsbt.ivy.home=/drone/.ivy2/ readme/publish"
