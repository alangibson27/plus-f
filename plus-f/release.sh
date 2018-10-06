#!/bin/bash -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LAUNCH4J_DIR=${LAUNCH4J_DIR:-/opt/launch4j}
WINDOWS_JRE_DIR=${WINDOWS_JRE_DIR:-/opt/windows/jre-10.0.2}
AWS_CMD=${AWS_CMD:-aws}

wget http://www.softsynth.com/jsyn/developers/archives/jsyn-20171016.jar
mv jsyn-20171016.jar lib/

# Common JAR
${SCRIPT_DIR}/../gradlew clean shadowJar

# Linux and Universal builds
${SCRIPT_DIR}/../gradlew distZip debPackage rpmPackage
for i in $(ls build/distributions/plus-f-*); do
    if ! [[ $i =~ .+tar ]]; then
      name=$(echo $i | sed -E "s/(.+plus-f.+)(\....)/Plus-F\2/")
      echo "Uploading $i to S3 as $name ..."
      ${AWS_CMD} s3 cp $i s3://download.socialthingy.com/${name}
      echo "Done"
    fi
done

# Windows build
mkdir -p ${SCRIPT_DIR}/build/distributions/windows
${LAUNCH4J_DIR}/launch4j ${SCRIPT_DIR}/package/windows/launch4j.xml
cp -rf ${WINDOWS_JRE_DIR} ${SCRIPT_DIR}/build/distributions/windows
wine "C:/Program Files/Inno Setup 5/ISCC.exe" Z:${SCRIPT_DIR}/package/windows/plus-f.iss
${AWS_CMD} s3 cp ${SCRIPT_DIR}/build/distributions/windows/Plus-F-Install.exe s3://download.socialthingy.com/Plus-F.exe

