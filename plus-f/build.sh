#!/usr/bin/env bash
set -e
set -x

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
AWS_CMD=${AWS_CMD:-aws}
GRADLE=${SCRIPT_DIR}/../gradlew
${GRADLE} tasks --quiet
VERSION=$(${GRADLE} :plus-f:printVersion --quiet)

export SCRIPT_DIR
export VERSION

function writeVersionFile {
  if [[ ! -e "${SCRIPT_DIR}/src/main/resources" ]]; then
    mkdir -p src/main/resources
  fi

  echo "version=${VERSION}" > ${SCRIPT_DIR}/src/main/resources/version.properties
}

function checkCommittedOnMaster {
  # Check we are on master
  if [[ $(git rev-parse --abbrev-ref HEAD) = "master" ]]; then
    echo "Current branch is master, proceeding with release"
  else
    echo "Current branch is not master, not releasing"
    exit 1
  fi

  # Check if there are any uncommitted files
  git diff-index --quiet HEAD -- || { echo 'uncommitted files found!'; exit 1; }
}

function checkVersionUpdated {
  set +e
  found=$(git tag | grep -c release-${VERSION})
  set -e
  if [[ "$found" != "0" ]]; then
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

function buildDeb {
  DEB_DIR=${SCRIPT_DIR}/build/deb
  ${GRADLE} :plus-f:installDist -x check
  sed -i '$ i JAVACMD=$APP_HOME/runtime/bin/java' ${SCRIPT_DIR}/build/install/plus-f/bin/plus-f

  rm -rf ${DEB_DIR} || true
  jlink --verbose --no-header-files --no-man-pages --compress=2 --strip-debug \
        --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml,jdk.unsupported \
        --output ${DEB_DIR}/data/opt/Plus-F/runtime

  find ${DEB_DIR}/data/opt/Plus-F/runtime/ -name "*.so" | xargs strip -p --strip-unneeded

  mv ${SCRIPT_DIR}/build/install/plus-f/* ${DEB_DIR}/data/opt/Plus-F
  cp -r ${SCRIPT_DIR}/package/linux/deb/* ${DEB_DIR}
  echo "Version: ${VERSION}" >> ${DEB_DIR}/control/control

  tar czf ${DEB_DIR}/data.tar.gz -C ${DEB_DIR}/data .
  tar czf ${DEB_DIR}/control.tar.gz -C ${DEB_DIR}/control .

  ar r ${DEB_DIR}/Plus-F.deb ${DEB_DIR}/debian-binary ${DEB_DIR}/control.tar.gz ${DEB_DIR}/data.tar.gz

  ${AWS_CMD} s3 cp ${DEB_DIR}/Plus-F.deb s3://download.socialthingy.com/Plus-F.deb
}

function buildUniversal {
  ${GRADLE} :plus-f:distZip -x check
  ${AWS_CMD} s3 cp ${SCRIPT_DIR}/build/distributions/plus-f-${VERSION}.zip s3://download.socialthingy.com/Plus-F.zip
}

if [[ ! -e "build" ]]; then
  mkdir build
fi

${GRADLE} :plus-f:check

if [[ "${CIRCLE_BRANCH}" = "master" ]]; then
    writeVersionFile
    checkVersionUpdated
    tagRelease
    buildDeb
    buildUniversal
fi
