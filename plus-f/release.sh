#!/bin/bash -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#${SCRIPT_DIR}/../gradlew clean debPackage rpmPackage
for i in $(ls build/distributions/plus-f-*); do
    if ! [[ $i =~ .+tar ]]; then
      name=$(echo $i | sed -E "s/(.+plus-f.+)(\....)/Plus-F\2/")
      echo "Uploading $i to S3 as $name ..."
      aws s3 cp $i s3://download.socialthingy.com/${name}
      echo "Done"
    fi
done
