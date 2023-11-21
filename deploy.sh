#!/bin/bash
echo "inside shell"
sub_folders=( "cds-hook-model" "generic-model" "generic-helpers" "core-dependencies" "kie-based-services" "service-daos" "cds-hook-services" "core-wih" "cql-mods" "cql-wih" "ftl-transform-wih" "fhir-resource-wih" "cdshooks-wih" "web-util" "fhir-query-helper-base" "fhir-query-helper-dstu2" "fhir-query-helper-dstu3" "fhir-query-helper-r4" "oauth-helpers" "fhir-helpers" "task-utilities" "sms-wih" "a2d2-api")

mvn deploy --settings a2d2-settings.xml -Durl=https://elimu-435911253355.d.codeartifact.us-west-2.amazonaws.com/maven/a2d2-api/ -DpomFile=pom.xml -DrepositoryId=elimu-a2d2-api -DupdateReleaseInfo=true -DskipTests -U

for sub_folder in "${sub_folders[@]}"; do
    cd "$sub_folder" || exit 1
    
    mvn deploy --settings ../a2d2-settings.xml -Durl=https://elimu-435911253355.d.codeartifact.us-west-2.amazonaws.com/maven/a2d2-api/ -DpomFile=pom.xml -DrepositoryId=elimu-a2d2-api -DupdateReleaseInfo=true -DskipTests -U
    
    cd ..
done
