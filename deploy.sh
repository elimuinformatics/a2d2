#!/bin/bash

sub_projects=($(find "$(pwd)" -maxdepth 1 -type d -not -path "$(pwd)" -exec test -e '{}/pom.xml' ';' -printf '%f\n'))

mvn deploy --settings a2d2-settings.xml -Durl=https://elimu-435911253355.d.codeartifact.us-west-2.amazonaws.com/maven/a2d2-api/ -DpomFile=pom.xml -DrepositoryId=elimu-a2d2-api -DupdateReleaseInfo=true -DskipTests -U

for sub_project in "${sub_projects[@]}"; do
    cd "$sub_project" || exit 1
    mvn deploy --settings ../a2d2-settings.xml -Durl=https://elimu-435911253355.d.codeartifact.us-west-2.amazonaws.com/maven/a2d2-api/ -DpomFile=pom.xml -DrepositoryId=elimu-a2d2-api -DupdateReleaseInfo=true -DskipTests -U
    cd ..
done
