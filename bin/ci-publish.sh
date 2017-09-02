#!/usr/bin/env bash
set -eu
PUBLISH=${CI_PUBLISH:-false}
SECURE_VAR=${TRAVIS_SECURE_ENV_VARS:-false}

if [[ "$TRAVIS_SECURE_ENV_VARS" == true && -n "$TRAVIS_TAG" && "$CI_PUBLISH" == true ]]; then
  set -eu
  mkdir -p $HOME/.ssh
  ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts
  git config --global user.email "olafurpg@gmail.com"
  git config --global user.name "Drone CI bot"
  git config --global push.default simple
  DEPLOY_KEY_FILE=$HOME/.ssh/id_rsa
  echo "$GITHUB_DEPLOY_KEY" > ${DEPLOY_KEY_FILE}
  chmod 600 ${DEPLOY_KEY_FILE}
  eval "$(ssh-agent -s)"
  ssh-add ${DEPLOY_KEY_FILE}
  sbt -Dsbt.ivy.home=/drone/.ivy2/ readme/publish
else
  echo "Skipping publish..."
fi
