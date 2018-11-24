#!/usr/bin/env bash
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LAUNCH4J_DIR=${LAUNCH4J_DIR:-/opt/launch4j}
WINDOWS_JRE_DIR=${WINDOWS_JRE_DIR:-/opt/windows/jre-10.0.2}
WINDOWS_DIST_DIR=${SCRIPT_DIR}/build/distributions/windows
AWS_CMD=${AWS_CMD:-aws}
GRADLE=${SCRIPT_DIR}/../gradlew
JAVA_HOME=/opt/java/jdk-1.8.0_191
VERSION=$(${GRADLE} printVersion --quiet)

export SCRIPT_DIR
export JAVA_HOME
export VERSION

function writeVersionFile {
  if [[ ! -e "${SCRIPT_DIR}/src/main/resources" ]]; then
    mkdir -p src/main/resources
  fi

  echo "version=${VERSION}" > ${SCRIPT_DIR}/src/main/resources/version.properties
}

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

function buildDistributions {
  # Download jsyn dependency
  wget http://www.softsynth.com/jsyn/developers/archives/jsyn-20171016.jar
  mv jsyn-20171016.jar lib/

  # Common JAR
  ${GRADLE} clean
  mkdir ${SCRIPT_DIR}/build
  ${GRADLE} check shadowJar distZip
}

function nativePackage {
    local platform=$1

    ${JAVA_HOME}/bin/javapackager \
    -deploy \
    -v \
    -nosign \
    -native ${platform} \
    -outdir ${SCRIPT_DIR}/build/distributions \
    -outfile Plus-F \
    -name Plus-F \
    -description "ZX Spectrum Emulator with Network Play Capability" \
    -appclass com.socialthingy.plusf.spectrum.ui.PlusF \
    -srcfiles ${SCRIPT_DIR}/build/libs/plus-f-${VERSION}-all.jar \
    -BappVersion=${VERSION} \
    -BlicenseType=MIT \
    -BmainJar=plus-f-${VERSION}-all.jar
}

function checkVersionUpdated {
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
  echo "Tagging release as version ${VERSION}"
  git tag release-${VERSION} -m "Release version $VERSION"
  git push origin release-${VERSION}
}

function buildLinuxAndUniversal {
  # Linux and Universal builds
  ${GRADLE} distZip -x check
  nativePackage deb
  nativePackage rpm
  for i in $(find build/distributions -name "plus-f-*[zip|deb|rpm]"); do
    name=$(echo $i | sed -E "s/(.+plus-f.+)(\....)/Plus-F\2/")
    echo "Uploading $i to S3 as $name ..."
    ${AWS_CMD} s3 cp $i s3://download.socialthingy.com/${name}
    echo "Done"
  done
}

function buildWindows {
  # Windows build
  mkdir -p ${WINDOWS_DIST_DIR}

  # Create launch4j package
  ${SCRIPT_DIR}/package/windows/template-launch4j.sh > ${WINDOWS_DIST_DIR}/launch4j.xml
  ${LAUNCH4J_DIR}/launch4j ${WINDOWS_DIST_DIR}/launch4j.xml
  cp -rf ${WINDOWS_JRE_DIR} ${WINDOWS_DIST_DIR}

  # Create installer
  ${SCRIPT_DIR}/package/windows/template-iss.sh > ${WINDOWS_DIST_DIR}/plus-f.iss
  wine "C:/Program Files (x86)/Inno Setup 5/ISCC.exe" Z:${WINDOWS_DIST_DIR}/plus-f.iss
  ${AWS_CMD} s3 cp ${WINDOWS_DIST_DIR}/Plus-F-Install.exe s3://download.socialthingy.com/Plus-F.exe
}

if [[ ! -e "build" ]]; then
  mkdir build
fi

if [[ ! -e "lib" ]]; then
  mkdir lib
fi

writeVersionFile
checkCommittedOnMaster
checkVersionUpdated
buildDistributions
tagRelease
buildLinuxAndUniversal
buildWindows
