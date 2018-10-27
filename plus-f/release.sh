#!/usr/bin/env bash
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LAUNCH4J_DIR=${LAUNCH4J_DIR:-/opt/launch4j}
WINDOWS_JRE_DIR=${WINDOWS_JRE_DIR:-/opt/windows/jre-10.0.2}
WINDOWS_DIST_DIR=${SCRIPT_DIR}/build/distributions/windows
AWS_CMD=${AWS_CMD:-aws}
GRADLE=${SCRIPT_DIR}/../gradlew

export SCRIPT_DIR

function checkCommittedOnMaster {
  # Check we are on master
  if [ $(git rev-parse --abbrev-ref HEAD) = "master" ]; then
    echo "Current branch is master, proceeding with release"
  else
    echo "Current branch is not master, not releasing"
    exit 1
  fi

  # Check if there are any uncommitted files
  git diff-index --quiet HEAD -- || { echo 'uncommitted files found!'; exit 1; }
}

function buildShadowJar {
  # Download jsyn dependency
  wget http://www.softsynth.com/jsyn/developers/archives/jsyn-20171016.jar
  mv jsyn-20171016.jar lib/

  # Common JAR
  ${GRADLE} clean
  mkdir ${SCRIPT_DIR}/build
  ${GRADLE} check shadowJar
}

function checkVersionUpdated {
  VERSION=$(cat ${SCRIPT_DIR}/build/version)
  set +e
  found=$(git tag | grep -c release-${VERSION})
  set -e
  if [ "$found" != "0" ]; then
    echo "Version ${VERSION} already released"
    exit 1
  fi
}

function tagRelease {
  # Tag the release
  VERSION=$(cat ${SCRIPT_DIR}/build/version)
  echo "Tagging release as version ${VERSION}"
  git tag release-${VERSION} -m "Release version $VERSION"
  git push origin release-${VERSION}
}

function buildLinuxAndUniversal {
  # Linux and Universal builds
  ${GRADLE} distZip debPackage rpmPackage -x check
  for i in $(ls build/distributions/plus-f-*); do
    if ! [[ $i =~ .+tar ]]; then
      name=$(echo $i | sed -E "s/(.+plus-f.+)(\....)/Plus-F\2/")
      echo "Uploading $i to S3 as $name ..."
      ${AWS_CMD} s3 cp $i s3://download.socialthingy.com/${name}
      echo "Done"
    fi
  done
}

function buildWindows {
  VERSION=$(cat ${SCRIPT_DIR}/build/version)
  export VERSION  

  # Windows build
  ${GRADLE} distZip -x check
  mkdir -p ${WINDOWS_DIST_DIR}

  # Create launch4j package
  i{SCRIPT_DIR}/package/windows/template-launch4j.sh > ${WINDOWS_DIST_DIR}/launch4j.xml
  ${LAUNCH4J_DIR}/launch4j ${WINDOWS_DIST_DIR}/launch4j.xml
  cp -rf ${WINDOWS_JRE_DIR} ${WINDOWS_DIST_DIR}

  # Create installer
  ${SCRIPT_DIR}/package/windows/template-iss.sh > ${WINDOWS_DIST_DIR}/plus-f.iss
  wine "C:/Program Files/Inno Setup 5/ISCC.exe" Z:${WINDOWS_DIST_DIR}/plus-f.iss
  ${AWS_CMD} s3 cp ${WINDOWS_DIST_DIR}/Plus-F-Install.exe s3://download.socialthingy.com/Plus-F.exe
}

${GRADLE} writeVersionFile
checkCommittedOnMaster
checkVersionUpdated
buildShadowJar
tagRelease
buildLinuxAndUniversal
buildWindows

