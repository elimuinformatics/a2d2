# Running a2d2 on docker

1.  Use following docker run command in a2d2

        docker run -e "JAVA_OPTS=-Xms512m -Xmx4096m -Dfhir.client.instancecount=60 -Dmaven.repo.location=<your-maven-repo-location> -Dmaven.repo.pwd=<your-maven-repo-pwd> -Dmaven.repo.user=<your-Dmaven-repo-user> -Dspring.profiles.active=local,test,default -Dspring.datasource.url=<your-spring-datasource-url>  -Dspring.datasource.password=<your-spring-datasource-password> -Dspring.datasource.username=<your-spring-datasource-username> -Dorg.quartz.dataSource.nonManagedDS.URL=<your-org-quartz-dataSource-nonManagedDS-URL> -Dorg.quartz.dataSource.nonManagedDS.user=<your-org-quartz-dataSource-nonManagedDS-user>" --name a2d2 a2d2
```
