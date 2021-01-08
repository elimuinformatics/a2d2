#!/bin/sh

if [ "x$1" = "x" ];then
  echo "Missing environment parameter (i.e. usc)"
  exit 2
fi
LOCAL_REPO="$HOME/.m2/repository"
SOURCE_REPO="./$1-repo/"
_dir="`dirname $0`"
unzip $1-repo.zip

rsync -a $_dir/$1-repo/ $LOCAL_REPO

