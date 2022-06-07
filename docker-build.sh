#!/usr/bin/env bash

echo "PasteWrapper Docker for version ${1}"

ZIP_FILE=$(mktemp)
BUILD_DIR=$(mktemp -d)

echo "Downloading Image"
wget "-O" "${ZIP_FILE}" "https://maven.moddingx.org/org/moddingx/PasteWrapper/${1}/PasteWrapper-${1}-docker.zip"

echo "Extracting Image"
unzip "${ZIP_FILE}" "-d" "${BUILD_DIR}"

if [[ -f "config.json" ]]; then
  cp "config.json" "${BUILD_DIR}/config.json"
fi

echo "Building Image pastewrapper:${1//-/_}"
cd "${BUILD_DIR}" || exit
docker "build" "-t" "pastewrapper:${1//-/_}" "."

if docker "service" "inspect" "pastewrapper" 2> /dev/null > /dev/null; then
  echo "Updating Image in Docker Service"
  docker "service" "update" "--image" "pastewrapper:${1//-/_}" "pastewrapper"
else
  echo "Creating Docker Service"
  if [[ $2 == "no-ssl" ]]; then
    docker "service" "create" "--name" "pastewrapper" "--secret" "paste_token" "--secret" "paste_public_key" "--secret" "paste_private_key" "pastewrapper:${1//-/_}" 
  else
    docker "service" "create" "--name" "pastewrapper" "--secret" "paste_token" "--secret" "paste_public_key" "--secret" "paste_private_key" "--secret" "ssl_keystore" "--secret" "ssl_keystore_password" "pastewrapper:${1//-/_}" 
  fi
fi
