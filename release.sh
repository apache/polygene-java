#!/bin/sh

if [ -z "$1" ] ; then
    echo "Need to provide the version."
    echo ""
    echo "./release.sh <version> <keyname>"
    echo ""
else

  if [ -z "$2" ] ; then
    echo "Need to provide GPG keyname."
    echo ""
    echo "./release.sh <version> <keyname>"
    echo ""
  else

    VERSION=$1
    KEYNAME=$2
#    gradle clean release
    cd build/distributions

    openssl dgst -md5 >qi4j-sdk-$VERSION-src.zip.md5 <qi4j-sdk-$VERSION-src.zip
    openssl dgst -md5 >qi4j-sdk-$VERSION-src.tgz.md5 <qi4j-sdk-$VERSION-src.tgz
    openssl dgst -md5 >qi4j-sdk-$VERSION-bin.zip.md5 <qi4j-sdk-$VERSION-bin.zip
    openssl dgst -md5 >qi4j-sdk-$VERSION-bin.tgz.md5 <qi4j-sdk-$VERSION-bin.tgz

    openssl dgst -sha1 >qi4j-sdk-$VERSION-src.zip.sha <qi4j-sdk-$VERSION-src.zip
    openssl dgst -sha1 >qi4j-sdk-$VERSION-src.tgz.sha <qi4j-sdk-$VERSION-src.tgz
    openssl dgst -sha1 >qi4j-sdk-$VERSION-bin.zip.sha <qi4j-sdk-$VERSION-bin.zip
    openssl dgst -sha1 >qi4j-sdk-$VERSION-bin.tgz.sha <qi4j-sdk-$VERSION-bin.tgz

    ssh dist.qi4j.org "mkdir -p /home/www/dist.qi4j.org/releases/sdk/$VERSION/"
    scp qi4j-sdk-$VERSION-* dist.qi4j.org:/home/www/dist.qi4j.org/releases/sdk/$VERSION/

    cd ..
  fi
fi

