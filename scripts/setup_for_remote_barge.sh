#!/bin/bash

# wait for barge artifact files

ARTIFACT_FOLDER="artifacts"

BARGE_URL="${1:-http://localhost}"
REMOTE_BARGE="$BARGE_URL:8090"

if [ -d $ARTIFACT_FOLDER ]; then
    rm -rf $ARTIFACT_FOLDER
fi

mkdir $ARTIFACT_FOLDER
cd $ARTIFACT_FOLDER
wget -r -nd ${REMOTE_BARGE}/api/v1/ferry/barge/artifacts
