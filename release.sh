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
    rm -rf target

    mvn -Dgpg.keyname=$KEYNAME -Dgpg.useAgent=true install

    cd target

    openssl dgst -md5 >qi4j-sdk-1.0-$VERSION-src.zip.md5 <qi4j-sdk-1.0-$VERSION-src.zip
    openssl dgst -md5 >qi4j-sdk-1.0-$VERSION-src.tar.bz2.md5 <qi4j-sdk-1.0-$VERSION-src.tar.bz2
    openssl dgst -md5 >qi4j-sdk-1.0-$VERSION-src.tar.gz.md5 <qi4j-sdk-1.0-$VERSION-src.tar.gz
    openssl dgst -md5 >qi4j-sdk-1.0-$VERSION-bin.zip.md5 <qi4j-sdk-1.0-$VERSION-bin.zip
    openssl dgst -md5 >qi4j-sdk-1.0-$VERSION-bin.tar.gz.md5 <qi4j-sdk-1.0-$VERSION-bin.tar.gz
    openssl dgst -md5 >qi4j-sdk-1.0-$VERSION-bin.tar.bz2.md5 <qi4j-sdk-1.0-$VERSION-bin.tar.bz2

    openssl dgst -sha1 >qi4j-sdk-1.0-$VERSION-src.zip.sha <qi4j-sdk-1.0-$VERSION-src.zip
    openssl dgst -sha1 >qi4j-sdk-1.0-$VERSION-src.tar.bz2.sha <qi4j-sdk-1.0-$VERSION-src.tar.bz2
    openssl dgst -sha1 >qi4j-sdk-1.0-$VERSION-src.tar.gz.sha <qi4j-sdk-1.0-$VERSION-src.tar.gz
    openssl dgst -sha1 >qi4j-sdk-1.0-$VERSION-bin.zip.sha <qi4j-sdk-1.0-$VERSION-bin.zip
    openssl dgst -sha1 >qi4j-sdk-1.0-$VERSION-bin.tar.gz.sha <qi4j-sdk-1.0-$VERSION-bin.tar.gz
    openssl dgst -sha1 >qi4j-sdk-1.0-$VERSION-bin.tar.bz2.sha <qi4j-sdk-1.0-$VERSION-bin.tar.bz2

    scp qi4j-sdk-1.0-$VERSION-* repository.ops4j.org:/home/www/dist.qi4j.org/releases/sdk/$VERSION/

    cd ..
  fi
fi

