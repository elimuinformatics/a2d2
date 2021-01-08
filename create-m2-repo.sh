#!/bin/sh

if [ "x$1" = "x" ];then
  echo "Missing environment parameter (i.e. usc)"
  exit 2
fi
mvn clean install -DskipTests -D$1 -Dmaven.repo.local=./$1-repo --settings=./a2d2-settings.xml -P dev

zip -r $1-repo.zip $1-repo/
