#!/usr/bin/env bash
set -eu

echo "Updating gh-pages..."

SUBDIR="gh-pages"
SOURCE_BRANCH="master"
TARGET_BRANCH="gh-pages"
SETUP_GIT=${DRONE:-false}

git checkout master
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
SHA=`git rev-parse --verify HEAD`
sbt "readme/run --validate-links"

rm -rf ${SUBDIR}
git clone ${REPO} ${SUBDIR}
cd ${SUBDIR}
git checkout ${TARGET_BRANCH} || git checkout --orphan ${TARGET_BRANCH}
cd ..
cp -r readme/target/scalatex/* ${SUBDIR}

cd ${SUBDIR}
# If there are no changes to the compiled out (e.g. this is a README update) then just bail.
if [[ -z `git diff --exit-code` ]]; then
    echo "No changes to the output on this push; exiting."
    exit 0
fi

if [[ ${SETUP_GIT} == "true" ]]; then
  mkdir -p $HOME/.ssh
  RSA_FILE="$HOME/.ssh/github_rsa"
  git config user.name "olafur pall"
  git config user.email "olafurpg@gmail.com"
  echo "$GITHUB_DEPLOY_KEY" > ${RSA_FILE}
  chmod 600 ${RSA_FILE}
  export GIT_SSH_COMMAND="ssh -i $RSA_FILE"
fi

git add .
git commit -m "Deploy to GitHub Pages: ${SHA}"

git push -f ${SSH_REPO} gh-pages
git checkout master
cd ..
rm -rf gh-pages

echo "Done!"
